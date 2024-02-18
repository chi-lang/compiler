package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.Program
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.analyzer.*
import gh.marad.chi.core.compiler.checks.CheckNamesVisitor
import gh.marad.chi.core.compiler.checks.ImmutabilityCheckVisitor
import gh.marad.chi.core.compiler.checks.VisibilityCheckingVisitor
import gh.marad.chi.core.namespace.*
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.parseSource
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types3.*
import gh.marad.chi.core.types3.Constraint
import gh.marad.chi.core.types3.Function

object Compiler {

    @JvmStatic
    fun compile(code: String, ns: GlobalCompilationNamespace): CompilationResult = compile(ChiSource(code), ns)

    @JvmStatic
    fun compile(source: ChiSource, ns: GlobalCompilationNamespace): CompilationResult {
        // parsing

        val (parsedProgram, messages) =
            try { parseSource(source) }
            catch (ex: CompilerMessage) {
                Pair(
                    ParseProgram(null, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), null),
                    listOf(ex.msg)
                )
            }
        val packageDefinition = parsedProgram.packageDefinition?.let {
            Package(it.moduleName, it.packageName)
        } ?: Package("user", "default")

        val resultMessages = mutableListOf<Message>()
        resultMessages.addAll(messages)


        // verify package declaration and imports
        // ======================================

        if (packageDefinition.moduleName.isEmpty()) {
            resultMessages.add(InvalidModuleName(packageDefinition.moduleName, CodePoint(1, 0)))
        }
        if (packageDefinition.packageName.isEmpty()) {
            resultMessages.add(InvalidPackageName(packageDefinition.packageName, CodePoint(1, 0)))
        }

        // check that imported names exist and are public
        parsedProgram.imports.forEach { import ->
            val importPkg = ns.getOrCreatePackage(import.moduleName, import.packageName)
            import.entries.forEach { entry ->
                val symbol = importPkg.symbols.get(entry.name)
                val type = importPkg.types.get(entry.name)
                if (symbol != null && !symbol.public && import.moduleName != packageDefinition.moduleName) {
                    resultMessages.add(CannotAccessInternalName(entry.name, entry.section.toCodePoint()))
                }

                if (symbol == null && type == null) {
                    resultMessages.add(UnrecognizedName(entry.name, entry.section.toCodePoint()))
                }
            }
        }

        // Build symbol and type tables
        // ============================

        val tables = CompileTables(packageDefinition, ns)
        tables.addImports(ns.prelude.map {
            Import(it.moduleName, it.packageName, null, listOf(Import.Entry(it.name, it.alias, null)), null)
        })
        tables.addImports(parsedProgram.imports)

        val definedTypeAliases = parsedProgram.typeAliases.map { typeAliasDef ->
            val typeSchemeVariables = typeAliasDef.typeParameters.map { it.name }
            val id = TypeId(packageDefinition.moduleName, packageDefinition.packageName, typeAliasDef.name)
            TypeAlias(
                id,
                resolveNewType(tables.localTypeTable, typeSchemeVariables, typeAliasDef.type).let {
                    when(it) {
                        is Record -> it.copy(id = id)
                        is Sum -> it.copy(id = id)
                        else -> it
                    }
                }
            ).also {
                tables.defineTypeAlias(it)
            }
        }

        val definedTypes = mutableListOf<TypeInfo>()

        parsedProgram.typeDefinitions.forEach { typeDef ->
            val typeSchemeVariables = typeDef.typeParameters.map { TypeVariable(it.name) }

            val isSingleConstructorType = typeDef.variantConstructors.size == 1
                    && typeDef.typeName == typeDef.variantConstructors[0].name

            val supertype = if (!isSingleConstructorType) {
                val base = SumType(
                    moduleName = packageDefinition.moduleName,
                    packageName = packageDefinition.packageName,
                    name = typeDef.typeName,
                    typeParams = typeSchemeVariables,
                    subtypes = typeDef.variantConstructors.map { it.name },
                    typeSchemeVariables = typeSchemeVariables,
                )

                val baseTypeInfo = TypeInfo(
                    moduleName = packageDefinition.moduleName,
                    packageName = packageDefinition.packageName,
                    name = typeDef.typeName,
                    type = base,
                    supertype = Types.any,
                    isPublic = true,
                    fields = emptyList()
                )

                tables.defineType(baseTypeInfo)
                definedTypes.add(baseTypeInfo)
                base
            } else {
                Types.any
            }

            typeDef.variantConstructors.map { ctor ->
                val paramTypeNames = ctor.formalFields.flatMap { it.typeRef.findTypeNames() }
                val ctorTypeSchemeVariables = typeSchemeVariables.filter { it.name in paramTypeNames }
                val fields = ctor.formalFields.map { formalField ->
                    VariantField(formalField.name, resolveType(tables.localTypeTable, typeSchemeVariables.map { it.name }, formalField.typeRef), formalField.public)
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

                val ctorSymbol = Symbol(
                    moduleName = packageDefinition.moduleName,
                    packageName = packageDefinition.packageName,
                    name = ctor.name,
                    type = constructorOrSymbolType,
                    newType = Primitive(TypeId("", "", "TODO!")),
                    public = ctor.public,
                    mutable = false
                )

                tables.defineSymbol(ctorSymbol)

                val variantTypeInfo = TypeInfo(
                    moduleName = packageDefinition.moduleName,
                    packageName = packageDefinition.packageName,
                    name = ctor.name,
                    type = type,
                    supertype = supertype,
                    isPublic = ctor.public,
                    fields = fields,
                )

                tables.defineType(variantTypeInfo)
                definedTypes.add(variantTypeInfo)

                return@map type
            }
        }

        // analyze parse ast
        CheckNamesVisitor(ParseBlock(parsedProgram.functions + parsedProgram.topLevelCode, parsedProgram.section), tables)
            .check(resultMessages)

        if (resultMessages.isNotEmpty()) {
            return CompilationResult(
                refineMessages(resultMessages),
                Program(packageDefinition, parsedProgram.imports, definedTypeAliases, definedTypes, emptyList(), parsedProgram.section),
            )
        }

        // convert to ast
        // ==============
        val (functions, code) = try {
            val converter = ExprConversionVisitor(packageDefinition, tables)
            val functions = parsedProgram.functions.map { converter.visit(it) }
            val code = parsedProgram.topLevelCode.map { converter.visit(it) }
            Pair(functions, code)
        } catch (ex: CompilerMessage) {
            resultMessages.add(ex.msg)
            Pair(emptyList(), emptyList())
        }
        val expressions = functions + code

        markUsed(expressions)

        // autocast & unit insert
        // ======================

        // infer types
        // ===========
        val typeLookupTable = TypeLookupTable(ns)
//        val inferenceContext = InferenceContext(ns, typeLookupTable)
//        val env = InferenceEnv(packageDefinition, tables, ns)

        val typer = Typer(gh.marad.chi.core.types3.InferenceContext(packageDefinition, ns))
        val constraints = mutableListOf<Constraint>()
        expressions.forEach {
            typer.typeTerm(it, 0, constraints)
        }
        val solutions = unify(constraints)
        expressions.forEach { replaceTypes(it, solutions) }


        if (resultMessages.isNotEmpty()) {
            return CompilationResult(
                refineMessages(resultMessages),
                Program(packageDefinition, parsedProgram.imports, definedTypeAliases, definedTypes, expressions, parsedProgram.section),
            )
        }

        // perform post construction updates and checks
        // ============================================
        VisibilityCheckingVisitor(packageDefinition.moduleName, typeLookupTable, ns)
            .check(expressions, resultMessages)
        FnCallCheckingVisitor()
            .check(expressions, resultMessages)
        ImmutabilityCheckVisitor(resultMessages, tables, ns)
            .check(expressions)
        ReturnTypeCheckVisitor(resultMessages)
            .check(expressions)

        // make messages more informative
        // ==============================

        return CompilationResult(
            refineMessages(resultMessages),
            Program(packageDefinition, parsedProgram.imports, definedTypeAliases, definedTypes, expressions, parsedProgram.section),
        )
    }

    fun resolveNewType(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef): Type3 {
        return when(ref) {
            is TypeParameterRef -> Variable(ref.name, 0) // FIXME: here level should probably be passed from above
            is TypeNameRef ->
                if (ref.typeName in typeSchemeVariables) {
                    Variable(ref.typeName, 0)
                } else {
                    when (ref.typeName) {
                        "string" -> Type3.string
                        "bool" -> Type3.bool
                        "int" -> Type3.int
                        "float" -> Type3.float
                        "unit" -> Type3.unit
                        else ->
                            typeTable.getAlias(ref.typeName)?.newType
                                ?: throw CompilerMessage.from("Type $ref not found", ref.section)
                    }
                }
            is TypeConstructorRef -> TODO("I'm not sure if this is going to work the old way")
            is FunctionTypeRef -> {
                val returnType = resolveNewType(typeTable, typeSchemeVariables, ref.returnType)
                val params = ref.argumentTypeRefs.map { resolveNewType(typeTable, typeSchemeVariables, it) }
                Function(params + listOf(returnType))
            }
            is SumTypeRef ->
                Sum(
                    id = null,
                    lhs = resolveNewType(typeTable, typeSchemeVariables, ref.lhs),
                    rhs = resolveNewType(typeTable, typeSchemeVariables, ref.rhs),
                    level = 0
                )
            is RecordTypeRef ->
                Record(
                    id = null,
                    fields = ref.fields.map { Record.Field(it.name, resolveNewType(typeTable, typeSchemeVariables, it.typeRef)) }
                )
        }
    }

    fun resolveType(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef): Type {
        return when(ref) {
            is TypeParameterRef -> TypeVariable(ref.name).also { it.sourceSection = ref.section }
            is TypeNameRef ->
                if (ref.typeName in typeSchemeVariables) {
                    TypeVariable(ref.typeName).also { it.sourceSection = ref.section }
                } else {
                    typeTable.get(ref.typeName)?.type?.also { it.sourceSection = ref.section }
                        ?: throw CompilerMessage.from("Type $ref not found", ref.section)
                }

            is TypeConstructorRef -> {
                val base = resolveType(typeTable, typeSchemeVariables, ref.baseType)
                val typeParameters = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it) }
                return when (base) {
                    is SumType -> base.copy(typeParams = typeParameters)
                    is ProductType -> base.copy(types = typeParameters)
                    else -> throw CompilerMessage.from(
                        "Invalid type constructor. Only product and sum types are supported",
                        ref.section)
                }.also { it.sourceSection = ref.section }
            }

            is FunctionTypeRef -> {
                val returnType = resolveType(typeTable, typeSchemeVariables, ref.returnType)
                val params = ref.argumentTypeRefs.map { resolveType(typeTable, typeSchemeVariables, it) }
                val types = listOf(*params.toTypedArray(), returnType)
                FunctionType(
                    types,
                    types.flatMap { it.findTypeVariables() },
                ).also { it.sourceSection = ref.section }
            }

            is SumTypeRef -> Types.unit
            is RecordTypeRef -> Types.unit
        }
    }

    fun refineMessages(messages: List<Message>): List<Message> =
        messages.map {
            if (it is TypeMismatch && it.expected is Function && it.actual !is Function) {
                NotAFunction(it.codePoint)
            }  else {
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