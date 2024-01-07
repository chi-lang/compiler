package gh.marad.chi.core.compiler

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.*
import gh.marad.chi.core.compiler.checks.FnCallCheckingVisitor
import gh.marad.chi.core.compiler.checks.VisibilityCheckingVisitor
import gh.marad.chi.core.expressionast.internal.convertPackageDefinition
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parseSource
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.TypeInferenceFailed
import java.lang.RuntimeException

object Compiler2 {

    fun compile(code: String, ns: GlobalCompilationNamespace): Pair<Program, List<Message>> = compile(ChiSource(code), ns)

    fun compile(source: ChiSource, ns: GlobalCompilationNamespace): Pair<Program, List<Message>> {
        // parsing
        val (parsedProgram, messages) = parseSource(source)
        val packageDefinition = parsedProgram.packageDefinition?.let { convertPackageDefinition(it) }
            ?: Package("user", "default")

        val resultMessages = messages.toMutableList()
        resultMessages.addAll(messages)

        // TODO verify package declaration and imports
        // ==========================
        // TODO check that imported names exist and are public


        // build global symbol table
        // ==========================
        val symbolTable = SymbolTable()
        // get already defined symbols
        symbolTable.add(ns.getOrCreatePackage(packageDefinition).symbols)
        // add imported symbols
        parsedProgram.imports.forEach { import ->
            val importPkg = ns.getOrCreatePackage(import.moduleName.name, import.packageName.name)
            import.entries.forEach { entry ->
                importPkg.symbols.get(entry.name)?.let { symbol ->
                    symbolTable.add(symbol.copy(name = entry.alias?.alias ?: entry.name))
                    // verify that imported type is public
                    if (!symbol.public && import.moduleName.name != packageDefinition.moduleName) {
                        resultMessages.add(CannotAccessInternalName(entry.name, entry.section.toCodePoint()))
                    }
                } ?: resultMessages.add(UnrecognizedName(entry.name, entry.section.toCodePoint()))
            }
        }

        // build type table
        // ================
        val typeTable = TypeTable()
        // add imported types
        parsedProgram.imports.forEach { import ->
            val importPkg = ns.getOrCreatePackage(import.moduleName.name, import.packageName.name)
            import.entries.forEach { entry ->
                importPkg.types.get(entry.name)?.let {
                    typeTable.add(it)
                }
            }
        }
        // add locally defined types
        parsedProgram.typeDefinitions.forEach { typeDef ->
            val typeSchemeVariables = typeDef.typeParameters.map { TypeVariable(it.name) }

            val base = SumType(
                moduleName = packageDefinition.moduleName,
                packageName = packageDefinition.packageName,
                name = typeDef.typeName,
                typeParams = typeSchemeVariables,
                subtypes = typeDef.variantConstructors.map { it.name },
                typeSchemeVariables = typeSchemeVariables,
            )

            val baseTypeInfo = TypeInfo(
                name = typeDef.typeName,
                type = base,
                isPublic = true,
                isVariantConstructor = false,
                fields = emptyList()
            )
            typeTable.add(baseTypeInfo)

            typeDef.variantConstructors.map { ctor ->
                val paramTypeNames = ctor.formalFields.flatMap { it.typeRef.findTypeNames() }
                val ctorTypeSchemeVariables = typeSchemeVariables.filter { it.name in paramTypeNames }
                val fields = ctor.formalFields.map { formalField ->
                    VariantField(formalField.name, resolveType(typeTable, typeSchemeVariables.map { it.name }, formalField.typeRef), formalField.public)
                }

                val type = if (ctor.formalFields.isEmpty()) {
                    SimpleType(packageDefinition.moduleName, packageDefinition.packageName, ctor.name)
                } else {
                    ProductType(
                        moduleName = packageDefinition.moduleName,
                        packageName = packageDefinition.packageName,
                        name = ctor.name,
                        types = fields.map { it.type },
                        typeParams = ctorTypeSchemeVariables,
                        typeSchemeVariables = ctorTypeSchemeVariables)
                }


                val constructorOrSymbolType = if (ctor.formalFields.isNotEmpty()) {
                    FunctionType(fields.map { it.type } + type, ctorTypeSchemeVariables)
                } else {
                    type
                }

                symbolTable.add(
                    Symbol(
                        moduleName = packageDefinition.moduleName,
                        packageName = packageDefinition.packageName,
                        name = ctor.name,
                        SymbolKind.Local,
                        slot = 0,
                        type = constructorOrSymbolType,
                        public = ctor.public,
                        mutable = false
                    )
                )

                typeTable.add(TypeInfo(
                    name = ctor.name,
                    type = type,
                    isPublic = ctor.public,
                    isVariantConstructor = true,
                    fields = fields,
                ))

                return@map type
            }
        }

        // analyze parse ast
        CheckNamesVisitor(parsedProgram, symbolTable).check(resultMessages)

        if (resultMessages.isNotEmpty()) {
            return Pair(
                Program(packageDefinition, emptyList(), emptyList(), parsedProgram.section),
                resultMessages
            )
        }

        // convert to ast
        // ==============
        val converter = ExprConversionVisitor(packageDefinition, symbolTable, typeTable)
        val functions = parsedProgram.functions.map { converter.visit(it) }
        val code = parsedProgram.topLevelCode.map { converter.visit(it) }
        var expressions = functions + code

        // autocast & unit insert
        // ======================

        // infer types
        // ===========
        val typeGraph = TypeGraph() // add defined and imported types
        // TODO do we even need the type graph?
        val inferenceContext = InferenceContext(typeGraph, typeTable)
        val env = mutableMapOf<String, Type>() // use global symbol table
        symbolTable.forEach {
            if (it.type != null) {
                env[it.name] = it.type
            }
        }

        try {
            inferAndFillTypes(inferenceContext, env, Block(expressions, parsedProgram.section))
        } catch (ex: TypeInferenceFailed) {
            resultMessages.add(gh.marad.chi.core.analyzer.TypeInferenceFailed(ex))
        } catch (ex: CompilerMessageException) {
            resultMessages.add(ex.msg)
        }

        // perform post construction checks
        // ================================
        VisibilityCheckingVisitor(packageDefinition.moduleName, typeTable)
            .check(expressions, resultMessages)
        FnCallCheckingVisitor()
            .check(expressions, resultMessages)

        // make messages more informative
        // ==============================

        return Pair(
            Program(packageDefinition, emptyList(), expressions, parsedProgram.section),
            refineMessages(resultMessages),
        )
    }

    fun resolveType(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef): Type {
        return when(ref) {
            is TypeParameterRef -> TypeVariable(ref.name).also { it.sourceSection = ref.section }
            is TypeNameRef ->
                if (ref.typeName in typeSchemeVariables) {
                    TypeVariable(ref.typeName).also { it.sourceSection = ref.section }
                } else {
                    typeTable.get(ref.typeName)?.type?.also { it.sourceSection = ref.section }
                        ?: TODO("Type $ref not found.")
                }

            is TypeConstructorRef -> {
                val base = resolveType(typeTable, typeSchemeVariables, ref.baseType)
                val typeParameters = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it) }
                return when (base) {
                    is SumType -> base.copy(typeParams = typeParameters)
                    is ProductType -> base.copy(types = typeParameters)
                    else -> TODO("INVALID TYPE CONSTRUCTOR - change this to compiler message")
                }.also { it.sourceSection = ref.section }
            }

            is FunctionTypeRef -> {
                val returnType = resolveType(typeTable, typeSchemeVariables, ref.returnType)
                val params = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it) }
                val types = listOf(*params.toTypedArray(), returnType)
                FunctionType(
                    types,
                    types.flatMap { it.typeSchemeVariables() },
                ).also { it.sourceSection = ref.section }
            }
            else -> throw RuntimeException("This should not happen!")
        }
    }

    fun refineMessages(messages: List<Message>): List<Message> =
        messages.map {
            if (it is TypeMismatch && it.expected is FunctionType && it.actual !is FunctionType) {
                NotAFunction(it.codePoint)
            } else {
                it
            }
        }

    @JvmStatic
    fun formatCompilationMessage(source: String, message: Message): String {
        val sourceSection = message.codePoint
        val sb = StringBuilder()
        if (sourceSection != null) {
            val sourceLine = source.lines()[sourceSection.line - 1]
            sb.appendLine(sourceLine)
            repeat(sourceSection.column) { sb.append(' ') }
            sb.append("^ ")
        }
        sb.append(message.message)
        return sb.toString()
    }

}