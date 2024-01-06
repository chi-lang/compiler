package gh.marad.chi.core.compiler

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CannotAccessInternalName
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.UnrecognizedName
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.internal.convertPackageDefinition
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parseSource
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.FunctionTypeRef
import gh.marad.chi.core.parser.readers.TypeConstructorRef
import gh.marad.chi.core.parser.readers.TypeNameRef
import gh.marad.chi.core.parser.readers.TypeRef
import gh.marad.chi.core.types.*
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
            val base = if(typeSchemeVariables.isEmpty()) {
                SimpleType(packageDefinition.moduleName, packageDefinition.packageName, typeDef.typeName)
            } else {
                GenericType(
                    listOf(SimpleType(packageDefinition.moduleName, packageDefinition.packageName, typeDef.typeName), *typeSchemeVariables.toTypedArray()),
                    typeSchemeVariables)
            }

            val baseTypeInfo = TypeInfo(
                name = typeDef.typeName,
                type = base,
                isPublic = true,
                isVariantConstructor = false,
                parent = null,
                fields = emptyList()
            )
            typeTable.add(baseTypeInfo)

            typeDef.variantConstructors.forEach { ctor ->
                val basicVariantType = SimpleType(packageDefinition.moduleName, packageDefinition.packageName, ctor.name)
                val type = if (typeSchemeVariables.isEmpty()) {
                    basicVariantType
                } else {
                    val paramTypeNames = ctor.formalFields.flatMap { it.typeRef.findTypeNames() }
                    val ctorTypeSchemeVariables = typeSchemeVariables.filter { it.name in paramTypeNames }
                    GenericType(
                        listOf(basicVariantType, *ctorTypeSchemeVariables.toTypedArray()),
                        ctorTypeSchemeVariables
                    )
                }

                val fields = ctor.formalFields.map { formalField ->
                    VariantField(formalField.name, resolveType(typeTable, typeSchemeVariables.map { it.name }, formalField.typeRef), formalField.public)
                }

                typeTable.add(TypeInfo(
                    name = ctor.name,
                    type = type,
                    isPublic = ctor.public,
                    isVariantConstructor = true,
                    parent = baseTypeInfo,
                    fields = fields,
                ))
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
        typeTable.forEach {
            if (!typeGraph.contains(it.type.toString())) {
                val parent = if (it.parent != null) {
                    it.parent.type
                } else {
                    Types.any
                }
                typeGraph.addSubtype(parent.toString(), it.type.toString())
            }
        }
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
        }

        // perform post construction checks
        // ================================
        CheckAccessToToPublicFieldsOfTypesVisitor(packageDefinition.moduleName, typeTable)
            .check(expressions, resultMessages)

        return Pair(
            Program(packageDefinition, emptyList(), expressions, parsedProgram.section),
            resultMessages,
        )
    }

    fun resolveType(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef): Type {
        return when(ref) {
            is TypeNameRef ->
                if (ref.typeName in typeSchemeVariables) {
                    TypeVariable(ref.typeName)
                } else {
                    typeTable.get(ref.typeName)?.type ?: TODO("Type $ref not found.")
                }

            is TypeConstructorRef -> {
                val base = resolveType(typeTable, typeSchemeVariables, ref.baseType)
                val params = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it) }
                val types = listOf(base, *params.toTypedArray())
                GenericType(
                    types,
                    types.flatMap { it.typeSchemeVariables() }
                )
            }

            is FunctionTypeRef -> {
                val returnType = resolveType(typeTable, typeSchemeVariables, ref.returnType)
                val params = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it) }
                val types = listOf(*params.toTypedArray(), returnType)
                GenericType(
                    types,
                    types.flatMap { it.typeSchemeVariables() }
                )
            }
            else -> throw RuntimeException("This should not happen!")
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