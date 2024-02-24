package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.Program
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.analyzer.*
import gh.marad.chi.core.compiler.checks.*
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.TypeTable
import gh.marad.chi.core.parseSource
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function

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
                val type = importPkg.types.getAlias(entry.name)
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
                resolveTypeAndWrapRecursive(tables.localTypeTable, typeSchemeVariables, typeAliasDef.type, id)
            ).also {
                tables.defineTypeAlias(it)
            }
        }

        // analyze parse ast
        CheckNamesVisitor(ParseBlock(parsedProgram.functions + parsedProgram.topLevelCode, parsedProgram.section), tables)
            .check(resultMessages)

        if (resultMessages.isNotEmpty()) {
            return CompilationResult(
                refineMessages(resultMessages),
                Program(packageDefinition, parsedProgram.imports, definedTypeAliases, emptyList(), parsedProgram.section),
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
        val typer = Typer(InferenceContext(packageDefinition, ns))
        expressions.forEach {
            try {
                val constraints = mutableListOf<Constraint>()
                typer.typeTerm(it, 0, constraints)
                val solutions = unify(constraints)
                replaceTypes(it, solutions)
            } catch (ex: CompilerMessage) {
                resultMessages.add(ex.msg)
            }
        }


        if (resultMessages.isNotEmpty()) {
            return CompilationResult(
                refineMessages(resultMessages),
                Program(packageDefinition, parsedProgram.imports, definedTypeAliases, expressions, parsedProgram.section),
            )
        }

        // perform post construction updates and checks
        // ============================================
        VisibilityCheckingVisitor(packageDefinition.moduleName, ns)
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
            Program(packageDefinition, parsedProgram.imports, definedTypeAliases, expressions, parsedProgram.section),
        )
    }

    private fun createRecursiveVariable(id: TypeId) = Variable(id.toString(), -1)
    private fun resolveTypeAndWrapRecursive(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef, id: TypeId): Type {
        val variable = createRecursiveVariable(id)
        val type = resolveType(typeTable, typeSchemeVariables, ref, id).let {
            when(it) {
                is Record -> it.copy(id = id)
                is Sum -> it.copy(id = id)
                else -> it
            }
        }

        val typeContainsVariable = object : TypeVisitor<Boolean> {
            override fun visitVariable(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") v: Variable): Boolean = v == variable
            override fun visitPrimitive(primitive: Primitive): Boolean = false
            override fun visitFunction(function: Function): Boolean = function.children().any { it.accept(this) }
            override fun visitRecord(record: Record): Boolean = record.children().any { it.accept(this) }
            override fun visitSum(sum: Sum): Boolean = sum.children().any { it.accept(this) }
            override fun visitArray(array: Array): Boolean = array.children().any { it.accept(this) }
            override fun visitRecursive(recursive: Recursive): Boolean = recursive.type.accept(this)
        }

        return if (type.accept(typeContainsVariable)) {
            Recursive(variable, type)
        } else {
            type
        }
    }

    @Suppress("NAME_SHADOWING")
    fun resolveType(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef, currentlyReadTypeId: TypeId? = null, createdVars: MutableList<String> = mutableListOf()): Type {
        val level = 1 // this should probably come from the environment
        return when(ref) {
            is TypeParameterRef -> { // FIXME: here level should probably be passed from above
                createdVars.add(ref.name)
                Variable(ref.name, level)
            }
            is TypeNameRef ->
                if (ref.typeName in typeSchemeVariables) {
                    createdVars.add(ref.typeName)
                    Variable(ref.typeName, level)
                } else {
                    when (ref.typeName) {
                        "any" -> Type.any
                        "string" -> Type.string
                        "bool" -> Type.bool
                        "int" -> Type.int
                        "float" -> Type.float
                        "unit" -> Type.unit
                        else -> {
                            if (currentlyReadTypeId != null && ref.typeName == currentlyReadTypeId.name) {
                                createRecursiveVariable(currentlyReadTypeId).also {
                                    createdVars.add(it.name)
                                }
                            } else {
                                typeTable.getAlias(ref.typeName)?.type
                                    ?: throw CompilerMessage.from("Type $ref not found", ref.section)
                            }
                        }
                    }
                }
            is TypeConstructorRef -> {
                if (ref.baseType is TypeNameRef && ref.baseType.typeName == "array") {
                    if (ref.typeParameters.size != 1) {
                        throw CompilerMessage.from("Array type must have exactly one type parameter!", ref.section)
                    }
                    val elementType = resolveType(typeTable, typeSchemeVariables, ref.typeParameters.first(), currentlyReadTypeId)
                    return Type.array(elementType)
                }
                val base = resolveType(typeTable, typeSchemeVariables, ref.baseType, currentlyReadTypeId)
                if (base is Variable && currentlyReadTypeId != null && base.name == createRecursiveVariable(currentlyReadTypeId).name) {
                    return base
                }
                val params = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it) }
                val typeParamNames = base.typeParams()
                if (params.isNotEmpty() && params.size != typeParamNames.size) {
                    throw CompilerMessage.from("Provided type parameters count (${params.size}) is different then expected (${typeParamNames.size})", ref.section)
                }
                val replacements = typeParamNames.map { Variable(it, level) }.zip(params)
                mapType(base, replacements)
            }
            is FunctionTypeRef -> {
                val createdVars = mutableListOf<String>()
                val returnType = resolveType(typeTable, typeSchemeVariables, ref.returnType, currentlyReadTypeId, createdVars)
                val params = ref.argumentTypeRefs.map { resolveType(typeTable, typeSchemeVariables, it, currentlyReadTypeId, createdVars) }
                Function(params + listOf(returnType), createdVars.filter { it in typeSchemeVariables })
            }
            is SumTypeRef -> {
                val createdVars = mutableListOf<String>()
                val lhs = resolveType(typeTable, typeSchemeVariables, ref.lhs, currentlyReadTypeId, createdVars)
                val rhs = resolveType(typeTable, typeSchemeVariables, ref.rhs, currentlyReadTypeId, createdVars)
                Sum.create(id = null, lhs, rhs, createdVars.filter { it in typeSchemeVariables })
            }
            is RecordTypeRef -> {
                val createdVars = mutableListOf<String>()
                val fields = ref.fields.map {
                    Record.Field(
                        it.name,
                        resolveType(typeTable, typeSchemeVariables, it.typeRef, currentlyReadTypeId)
                    )
                }
                Record(id = null, fields, createdVars.filter { it in typeSchemeVariables })
            }
        }
    }

    fun refineMessages(messages: List<Message>): List<Message> =
        messages.map {
            if (it is TypeMismatch && it.expected is Array && it.actual !is Array) {
                TypeIsNotIndexable(it.actual, it.codePoint)
            } else if (it is TypeMismatch && it.actual is Array && it.expected !is Array) {
                TypeIsNotIndexable(it.expected, it.codePoint)
            } else if (it is TypeMismatch && it.expected is Function && it.actual !is Function) {
                NotAFunction(it.codePoint)
            }  else {
                it
            }
        }.toSet().toList()

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