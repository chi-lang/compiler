package gh.marad.chi.core.compiler

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.*
import gh.marad.chi.core.compiler.checks.*
import gh.marad.chi.core.namespace.CompilationEnv
import gh.marad.chi.core.namespace.TypeTable
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function

object Compiler {

    @JvmStatic
    fun compile(code: String, ns: CompilationEnv): CompilationResult = compile(ChiSource(code), ns)

    @JvmStatic
    fun compile(source: ChiSource, ns: CompilationEnv): CompilationResult {
        // parsing

        val (parsedProgram, messages) =
            try { parseSource(source) }
            catch (ex: CompilerMessage) {
                Pair(
                    ParseProgram(null, emptyList(), emptyList(), emptyList(), emptyList(), null),
                    listOf(ex.msg)
                )
            }
        val packageDefinition = parsedProgram.packageDefinition?.let {
            Package(it.moduleName, it.packageName)
        } ?: Package(CompilationDefaults.defaultModule, CompilationDefaults.defaultPacakge)

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
            import.entries.forEach { entry ->
                val symbol = ns.getSymbol(import.moduleName, import.packageName, entry.name)
                val type = ns.getTypeAlias(import.moduleName, import.packageName, entry.name)
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

        val imports = ns.getPreludeImports() + parsedProgram.imports


        val tables = CompileTables(packageDefinition, ns, imports)

        val definedTypeAliases = parsedProgram.typeAliases.map { typeAliasDef ->
            val typeSchemeVariables = typeAliasDef.typeParameters.map { it.name }
            val id = TypeId(packageDefinition.moduleName, packageDefinition.packageName, typeAliasDef.name)
            TypeAlias(
                id,
                resolveTypeAndWrapRecursive(tables.localTypeTable, typeSchemeVariables, typeAliasDef.type, id, 1)
            ).also {
                tables.defineTypeAlias(it)
            }
        }

        // analyze parse ast
        CheckNamesVisitor(ParseBlock(parsedProgram.code, parsedProgram.section), tables)
            .check(resultMessages)

        if (resultMessages.isNotEmpty()) {
            return CompilationResult(
                refineMessages(resultMessages),
                Program(packageDefinition, imports, definedTypeAliases, emptyList(), tables.getSymbolTable(), parsedProgram.section),
            )
        }

        // convert to ast
        // ==============
        val expressions = try {
            val converter = ExprConversionVisitor(packageDefinition, tables)
            parsedProgram.code.map { converter.visit(it) }
        } catch (ex: CompilerMessage) {
            resultMessages.add(ex.msg)
            emptyList()
        }

        markUsed(expressions)

        // autocast & unit insert
        // ======================

        // infer types
        // ===========
        val (functions, code) = run {
            val groups = expressions.groupBy {
                (it is NameDeclaration && it.value is Fn && it.expectedType != null)
                        || it is EffectDefinition
            }
            Pair(groups[true] ?: emptyList(), groups[false] ?: emptyList())
        }

        val ctx = InferenceContext(packageDefinition, ns, tables)
        val typer = Typer(ctx)
        functions.forEach {
            if (it is NameDeclaration && it.expectedType != null) {
                ctx.defineLocalSymbol(it.name, PolyType(0, it.expectedType))
            }
            if (it is EffectDefinition) {
                ctx.defineLocalSymbol(it.name, PolyType(0, it.type!!))
            }
        }

        val constraints = mutableListOf<Constraint>()
        try {
            for (expression in code) {
                typer.typeTerm(expression, 0, constraints)
            }
            val solutions = unify(constraints)
            for (expression in code) {
                replaceTypes(expression, solutions)
            }
        } catch (ex: CompilerMessage) {
            resultMessages.add(ex.msg)
        }

        functions.forEach {
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
                Program(packageDefinition, imports, definedTypeAliases, expressions, tables.getSymbolTable(), parsedProgram.section),
            )
        }

        // perform post construction updates and checks
        // ============================================
        VisibilityCheckingVisitor(packageDefinition.moduleName, ns)
            .check(expressions, resultMessages)
        FnCallCheckingVisitor()
            .check(expressions, resultMessages)
        ImmutabilityCheckVisitor(packageDefinition.moduleName, packageDefinition.packageName, resultMessages, tables, ns)
            .check(expressions)
        ReturnTypeCheckVisitor(resultMessages)
            .check(expressions)

        // make messages more informative
        // ==============================

        return CompilationResult(
            refineMessages(resultMessages),
            Program(packageDefinition, imports, definedTypeAliases, expressions, tables.getSymbolTable(), parsedProgram.section),
        )
    }

    private fun createRecursiveVariable(id: TypeId) = Variable(id.toString(), -1)
    private fun resolveTypeAndWrapRecursive(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef, id: TypeId, level: Int): Type {
        val variable = createRecursiveVariable(id)
        val type = resolveType(typeTable, typeSchemeVariables, ref, level, id).let {
            when(it) {
                is Record -> it.copy(ids = listOf(id))
                is Sum -> it.copy(ids = listOf(id))
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
    fun resolveType(typeTable: TypeTable, typeSchemeVariables: Collection<String>, ref: TypeRef, level: Int, currentlyReadTypeId: TypeId? = null): Type {
        return when(ref) {
            is TypeParameterRef -> { // FIXME: here level should probably be passed from above
                Variable(ref.name, level)
            }
            is TypeNameRef ->
                if (ref.typeName in typeSchemeVariables) {
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
                                createRecursiveVariable(currentlyReadTypeId)
                            } else {
                                val alias = typeTable.getAlias(ref.typeName)
                                    ?: throw CompilerMessage.from("Type $ref not found", ref.section)
                                val type = alias.type
                                if (type is HasTypeId) {
                                    type.withAddedTypeId(alias.typeId)
                                } else {
                                    type
                                }
                            }
                        }
                    }
                }
            is TypeConstructorRef -> {
                if (ref.baseType is TypeNameRef && ref.baseType.typeName == "array") {
                    if (ref.typeParameters.size != 1) {
                        throw CompilerMessage.from("Array type must have exactly one type parameter!", ref.section)
                    }
                    val elementType = resolveType(typeTable, typeSchemeVariables, ref.typeParameters.first(), level, currentlyReadTypeId)
                    return Type.array(elementType)
                }
                val base = resolveType(typeTable, typeSchemeVariables, ref.baseType, level, currentlyReadTypeId)
                if (base is Variable && currentlyReadTypeId != null && base.name == createRecursiveVariable(currentlyReadTypeId).name) {
                    return base
                }
                val params = ref.typeParameters.map { resolveType(typeTable, typeSchemeVariables, it, level, currentlyReadTypeId) }
                val typeParamNames = base.typeParams()
                if (params.isNotEmpty() && params.size != typeParamNames.size) {
                    throw CompilerMessage.from("Provided type parameters count (${params.size}) is different then expected (${typeParamNames.size})", ref.section)
                }
                val replacements = typeParamNames.map { Variable(it, level) }.zip(params)
                mapType(base, replacements)
            }
            is FunctionTypeRef -> {
                val returnType = resolveType(typeTable, typeSchemeVariables, ref.returnType, level, currentlyReadTypeId)
                val params = ref.argumentTypeRefs.map { resolveType(typeTable, typeSchemeVariables, it, level, currentlyReadTypeId) }
                Function(params + listOf(returnType),  typeSchemeVariables.toList())
            }
            is SumTypeRef -> {
                val lhs = resolveType(typeTable, typeSchemeVariables, ref.lhs, level, currentlyReadTypeId)
                val rhs = resolveType(typeTable, typeSchemeVariables, ref.rhs, level, currentlyReadTypeId)
                Sum.create(ids = emptyList(), lhs, rhs, typeSchemeVariables.toList())
            }
            is RecordTypeRef -> {
                val fields = ref.fields.map {
                    Record.Field(
                        it.name,
                        resolveType(typeTable, typeSchemeVariables, it.typeRef, level, currentlyReadTypeId)
                    )
                }
                Record(ids = emptyList(), fields, typeSchemeVariables.toList())
            }
        }
    }

    fun refineMessages(messages: List<Message>): List<Message> =
        messages.map {
//            if (it is TypeMismatch && it.expected is Array && it.actual !is Array) {
//                TypeIsNotIndexable(it.actual, it.codePoint)
//            } else if (it is TypeMismatch && it.actual is Array && it.expected !is Array) {
//                TypeIsNotIndexable(it.expected, it.codePoint)
//            } else
                if (it is TypeMismatch && it.expected is Function && it.actual !is Function) {
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