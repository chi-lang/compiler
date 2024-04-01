package gh.marad.chi.lua

import gh.marad.chi.core.*
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Type
import gh.marad.chi.encodeType
import party.iroiro.luajava.lua54.Lua54

class LuaEmitter(val program: Program) {
    private var sb = StringBuilder()
    fun emit(returnLastValue: Boolean = false): String {
        sb = StringBuilder()
        val mod = modName()
        val pkg = pkgName()
        if (!(mod == CompilationDefaults.defaultModule && pkg == CompilationDefaults.defaultPacakge)) {
            emitCode("if not chi.$mod then chi.$mod={} end;")
            emitCode("if not chi.$mod.$pkg then chi.$mod.$pkg={} end;")
            emitCode("if not chi.$mod.$pkg._package then chi.$mod.$pkg._package={} end;")
        }

        emitPackageInfo()

        val iter = program.expressions.iterator()
        while(iter.hasNext()) {
            val result = emitExpr(iter.next(), returnLastValue && !iter.hasNext())
//            emitCode(";")
            if (returnLastValue && !iter.hasNext()) {
                // after emitting the last last expression we need to return
                emitCode("return $result")
            }
        }
        return sb.toString()
    }

    private var inFunction = false
    private fun <T> insideFunction(f: () -> T): T {
        val previous = inFunction
        inFunction = true
        val result = f()
        inFunction = previous
        return result
    }

    private fun emitPackageInfo() {
        val descPath = "${generateModuleName(program.packageDefinition.moduleName, program.packageDefinition.packageName)}._package"
        val iter = program.expressions.filterIsInstance<NameDeclaration>().iterator()
        while(iter.hasNext()) {
            val it = iter.next()
            emitCode("$descPath.${it.name}={")
            emitCode("public=${it.public},")
            emitCode("mutable=${it.mutable},")
            val type = it.type
            if (type != null) {
                emitCode("type=\"${encodeType(type)}\"")
            }
            emitCode("};")
        }
    }

    private fun emitCode(code: String) {
        sb.append(code)
    }

    /// Returns the value or name of local variable with the result
    private fun emitExpr(term: Expression, needResult: Boolean = false): String {
        return when(term) {
            is Atom -> emitAtom(term, needResult)
            is NameDeclaration -> emitNameDeclaration(term, needResult)
            is Fn -> emitFn(term, needResult)
            is FnCall -> emitFnCall(term, needResult)
            is Block -> emitBlock(term, needResult)
            is VariableAccess -> emitVariableAccess(term, needResult)
            is CreateArray -> emitCreateArray(term, needResult)
            is CreateRecord -> emitCreateRecord(term, needResult)
            is Assignment -> emitAssignment(term, needResult)
            is IndexOperator -> emitIndexOperator(term, needResult)
            is IndexedAssignment -> emitIndexAssignment(term, needResult)
            is Cast -> emitCast(term, needResult)
            is FieldAccess -> emitFieldAccess(term, needResult)
            is FieldAssignment -> emitFieldAssignment(term, needResult)
            is IfElse -> emitIfElse(term, needResult)
            is InfixOp -> emitInfixOp(term, needResult)
            is PrefixOp -> emitPrefixOp(term, needResult)
            is InterpolatedString -> emitInterpolatedString(term, needResult)
//            is Is -> TODO()
//            is WhileLoop -> TODO()
            is Break -> {
                emitCode("break")
                "nil"
            }
            is Continue -> {
                emitCode("continue")
                "nil"
            }
            is Return -> {
                emitCode("return")
                "nil"
            }
//            is Handle -> TODO()
//            is EffectDefinition -> TODO()
            else -> TODO("Term $term not supported yet!")
        }
    }


    private fun emitAtom(term: Atom, needResult: Boolean): String {
        return if (term.type == Type.string) {
//            emitCode("\"")
//            emitCode(term.value)
//            emitCode("\"")
            "\"${term.value}\""
        } else {
//            emitCode(term.value)
            term.value
        }
    }

    private fun emitNameDeclaration(term: NameDeclaration, needResult: Boolean): String {
        val value = emitExpr(term.value, needResult)
        val name = qualifiedName(term.name)
        emitCode(name)
        emitCode("=")
        emitCode(value)
        emitCode(";")
        return name
    }

    private var nextTmpId = 0
    private fun nextTmpName() = "tmp${nextTmpId++}"

    private fun emitFn(term: Fn, needResult: Boolean): String {
        val tmpName = nextTmpName()
        if (needResult) {
            emitCode("local $tmpName=")
        }
        emitCode("function(")
        emitCode(term.parameters.joinToString(",") { it.name })
        emitCode(") ")
        val result = insideFunction { emitBlock(term.body, needResult = true) }
        emitCode("return $result")
        emitCode(" end;")
        return tmpName
    }

    private fun emitFnCall(term: FnCall, needResult: Boolean): String {
        val fnName = emitExpr(term.function, true)

        val iter = term.parameters.iterator()
        val params = ArrayList<String>(term.parameters.size)
        while(iter.hasNext()) {
            params.add(emitExpr(iter.next(), true))
        }


        val tmpName = nextTmpName()
        if (needResult) {
            emitCode("local $tmpName=")
        }
        emitCode(fnName)
        emitCode("(")
        emitCode(params.joinToString(","))
        emitCode(");")
        return tmpName
    }

    private fun emitBlock(term: Block, needResult: Boolean): String {
        val iter = term.body.iterator()
        var lastExprResult = "nil"
        while(iter.hasNext()) {
            val expr = iter.next()
            lastExprResult = emitExpr(expr, needResult && !iter.hasNext())
        }
        return lastExprResult
    }

    private fun emitVariableAccess(term: VariableAccess, needResult: Boolean): String {
        return when(val target = term.target) {
            is LocalSymbol -> {
                if (inFunction) {
                    target.name
                } else {
                    qualifiedName(target.name)
                }
//                emitCode(name)
            }
            is PackageSymbol -> {
                qualifiedName(
                    target.moduleName, target.packageName, target.name
                )
//                emitCode(
//                    qualifiedName(
//                        target.moduleName, target.packageName, target.name
//                    )
//                )
            }
        }

    }

    private fun emitCreateArray(term: CreateArray, needResult: Boolean): String {
        return if (needResult) {
            val name = nextTmpName()

            val iter = term.values.iterator()
            val values = ArrayList<String>(term.values.size)
            while(iter.hasNext()) {
                values.add(emitExpr(iter.next(), true))
            }

            emitCode("local $name = {")
            emitCode(values.joinToString(","))
            emitCode("};")
            name
        } else {
            "nil"
        }
    }

    private fun emitCreateRecord(term: CreateRecord, needResult: Boolean): String {
        return if (needResult) {
            val name = nextTmpName()

            val iter = term.fields.iterator()
            val items = ArrayList<String>(term.fields.size)
            while(iter.hasNext()) {
                val field = iter.next()
                val resultName = emitExpr(field.value, true)
                items.add("${field.name}=$resultName")
            }

            emitCode("local $name={")
            emitCode(items.joinToString(","))
            emitCode("};")
            name
        } else {
            "nil"
        }
    }

    private fun emitAssignment(term: Assignment, needResult: Boolean): String {
        val varName = when(term.target) {
            is LocalSymbol -> {
                qualifiedName(term.target.name)
            }
            is PackageSymbol -> {
                qualifiedName(term.target.moduleName, term.target.packageName, term.target.name)
            }
        }
        val valueName = emitExpr(term.value)

        emitCode(varName)
        emitCode("=")
        emitCode(valueName)
        emitCode(";")
        return varName
    }

    private fun emitIndexOperator(term: IndexOperator, needResult: Boolean): String {
        return if (needResult) {
            val variable = emitExpr(term.variable)
            val index = emitExpr(term.index)
            val name = nextTmpName()
            emitCode("local $name=")
            emitCode(variable)
            emitCode("[")
            emitCode(index)
            emitCode("+1")
            emitCode("];")
            name
        } else {
            "nil"
        }
    }

    private fun emitIndexAssignment(term: IndexedAssignment, needResult: Boolean): String {
        val variable = emitExpr(term.variable)
        val index = emitExpr(term.index)
        val value = emitExpr(term.value)

        emitCode(variable)
        emitCode("[")
        emitCode(index)
        emitCode("+1")
        emitCode("]")
        emitCode("=")
        emitCode(value)
        emitCode(";")
        return value
    }

    private fun emitCast(term: Cast, needResult: Boolean): String {
        return if (needResult) {
            val result = emitExpr(term.expression, needResult)
            val name = nextTmpName()
            when (term.targetType) {
                Type.string -> {
                    emitCode("local $name=tostring($result);")
                    name
                }
                Type.int, Type.float -> {
                    emitCode("local $name=tonumber($result);")
                    name
                }
                else -> result
            }
        } else {
            emitExpr(term.expression, needResult)
        }
    }

    private fun emitFieldAccess(term: FieldAccess, needResult: Boolean): String {
        val target = term.target!!
        when(target) {
            DotTarget.Field -> {
                val receiverVar = emitExpr(term.receiver)
                return "$receiverVar.${term.fieldName}"
            }
            DotTarget.LocalFunction -> TODO("This should be resolved inside typing algorithm and not be present here!")
            is DotTarget.PackageFunction -> TODO()
        }
    }

    private fun emitFieldAssignment(term: FieldAssignment, needResult: Boolean): String {
        val receiverVar = emitExpr(term.receiver)
        val valueVar = emitExpr(term.value)

        emitCode(receiverVar)
        emitCode(".")
        emitCode(term.fieldName)
        emitCode("=")
        emitCode(valueVar)
        emitCode(";")

        return valueVar
    }

    private fun emitIfElse(term: IfElse, needResult: Boolean): String {
        val tmpName = nextTmpName()
        if (needResult) {
            emitCode("local $tmpName;")
        }
        val condition = emitExpr(term.condition)
        emitCode("if ")
        emitCode(condition)
        emitCode(" then ")
        val thenResultName = emitBlock(term.thenBranch as Block, needResult = true)
        if (needResult) {
            emitCode("$tmpName = $thenResultName")
        }
        if (term.elseBranch != null) {
            emitCode(" else ")
            val elseResultName = if (term.elseBranch is Block) {
                emitBlock(term.elseBranch, needResult = true)
            } else {
                emitExpr(term.elseBranch)
            }
            if (needResult) {
                emitCode("$tmpName = $elseResultName")
            }
        }
        emitCode(" end;")
        return tmpName
    }

    private fun emitInfixOp(term: InfixOp, needResult: Boolean): String {
        return if (needResult) {
            val leftVar = emitExpr(term.left)
            val rightVar = emitExpr(term.right)

            val resultName = nextTmpName()
            emitCode("local $resultName=")
            emitCode(leftVar)
            emitCode(" ")
            emitCode(term.op)
            emitCode(" ")
            emitCode(rightVar)
            emitCode(";")
            resultName
        } else {
            "nil"
        }
    }

    private fun emitPrefixOp(term: PrefixOp, needResult: Boolean): String {
        return if (needResult) {
            val valueVar = emitExpr(term.expr)
            val resultName = nextTmpName()
            emitCode("local $resultName=")
            emitCode(term.op)
            emitCode(valueVar)
            emitCode(";")
            resultName
        } else {
            "nil"
        }
    }

    private fun emitInterpolatedString(term: InterpolatedString, needResult: Boolean): String {
        return if (needResult) {
            val partVars = term.parts.map { emitExpr(it, needResult = true) }
            val resultName = nextTmpName()
            emitCode("local $resultName=")
            emitCode(partVars.joinToString(" .. "))
            emitCode(";")
            resultName
        } else {
            "nil"
        }
    }

    private fun modName(): String =
        program.packageDefinition.moduleName.replace(".", "_")

    private fun modName(name: String): String =
        name.replace(".", "_")
    private fun pkgName(): String =
        program.packageDefinition.packageName.replace(".", "_")

    private fun pkgName(pkg: String): String =
        pkg.replace(".", "_")

    private fun generateModuleName(module: String, pkg: String): String =
        "chi.${modName(module)}.${pkgName(pkg)}"

    private fun qualifiedName(module: String, pkg: String, name: String): String =
        "${generateModuleName(module, pkg)}.$name"

    private fun qualifiedName(name: String) =
        qualifiedName(
            program.packageDefinition.moduleName,
            program.packageDefinition.packageName,
            name)

}

fun main() {
    val ns = GlobalCompilationNamespace()
    ns.getDefaultPackage().symbols.add(Symbol(
        "user", "default",
        "print", Type.fn(Type.any, Type.unit),
        true, false
    ))
    val code = """
        val a = [1, 2, 3]
        fn hello() {
            val x = 6
            x = 2
            print(x)
        }
        hello()
        a[2] = 10
        print(a[2])
        val t = {a: 10, b: 12}
        t.b = 88
        print(t.b)
        if true { print(1) } else { print(2) }
        if true { 1 } else { 2 }
        print(123)
        val s = 4 + t.b
        print(s)
        print(-10)
        val id = { i -> i }
        print(id(5))
        print("hello ${'$'}s")
    """.trimIndent()
    val result = Compiler.compile(code, ns)
    val emitter = LuaEmitter(result.program)
    val luaCode = emitter.emit()
    println(luaCode)

    val lua = Lua54()
    lua.openLibraries()
    lua.execute(luaCode)
}