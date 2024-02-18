package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.Program
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.analyzer.*
import gh.marad.chi.core.compiler.checks.CheckNamesVisitor
import gh.marad.chi.core.compiler.checks.ImmutabilityCheckVisitor
import gh.marad.chi.core.compiler.checks.ReturnTypeCheckVisitor
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.TypeTable
import gh.marad.chi.core.parseSource
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types3.*
import gh.marad.chi.core.types3.Array
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
        val constraints = mutableListOf<Constraint>()
        expressions.forEach {
            try {
                typer.typeTerm(it, 0, constraints)
            } catch (ex: CompilerMessage) {
                resultMessages.add(ex.msg)
            }
        }
        try {
            val solutions = unify(constraints)
            expressions.forEach { replaceTypes(it, solutions) }
        } catch (ex: CompilerMessage) { resultMessages.add(ex.msg) }


        if (resultMessages.isNotEmpty()) {
            return CompilationResult(
                refineMessages(resultMessages),
                Program(packageDefinition, parsedProgram.imports, definedTypeAliases, expressions, parsedProgram.section),
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
            Program(packageDefinition, parsedProgram.imports, definedTypeAliases, expressions, parsedProgram.section),
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
                        "any" -> Type3.any
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
            is TypeConstructorRef -> {
                resolveNewType(typeTable, typeSchemeVariables, ref.baseType)
            }
            is FunctionTypeRef -> {
                val returnType = resolveNewType(typeTable, typeSchemeVariables, ref.returnType)
                val params = ref.argumentTypeRefs.map { resolveNewType(typeTable, typeSchemeVariables, it) }
                Function(params + listOf(returnType))
            }
            is SumTypeRef ->
                Sum.create(
                    id = null,
                    lhs = resolveNewType(typeTable, typeSchemeVariables, ref.lhs),
                    rhs = resolveNewType(typeTable, typeSchemeVariables, ref.rhs),
                )
            is RecordTypeRef ->
                Record(
                    id = null,
                    fields = ref.fields.map { Record.Field(it.name, resolveNewType(typeTable, typeSchemeVariables, it.typeRef)) }
                )
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