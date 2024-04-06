package gh.marad.chi.lua

import gh.marad.chi.core.*
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Record
import gh.marad.chi.core.types.Type
import gh.marad.chi.runtime.TypeWriter.encodeType
import party.iroiro.luajava.Lua
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
        val effects = program.expressions.filterIsInstance<EffectDefinition>().iterator()
        while (effects.hasNext()) {
            val it = effects.next()
            emitCode("$descPath.${it.name}={")
            emitCode("public=${it.public},")
            emitCode("mutable=false,")
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
            is Is -> emitIs(term, needResult)
            is WhileLoop -> emitWhile(term, needResult)
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
            is Handle -> {
                val tmpName = nextTmpName()

                emitCode("local ${tmpName}_handlers={")
                val iter = term.cases.iterator()
                while (iter.hasNext()) {
                    val case = iter.next()
                    val name = normaliseEffectName(qualifiedName(case.moduleName, case.packageName, case.effectName))
                    emitCode(name)
                    emitCode("=function(args) ")
                    emitCode("local function resume(x) return false, x end;")
                    case.argumentNames.forEachIndexed { index, name ->
                        emitCode("local $name=args[${index+1}];")
                    }

                    insideFunction {
                        val result = emitExpr(case.body, needResult = true)
                        emitCode("return $result")
                    }
                    emitCode(" end")
                    if (iter.hasNext()) {
                        emitCode(",")
                    }
                }
                emitCode("};")

                emitCode("local ${tmpName}_body=coroutine.create(function() ")
                val result = emitExpr(term.body, needResult = true)
                emitCode("return $result;")
                emitCode(" end);")
                emitCode("local ${tmpName}=chi_handle_effect(${tmpName}_body,{},${tmpName}_handlers);")
                tmpName
            }
            is EffectDefinition -> {
                val name = qualifiedName(term.moduleName, term.packageName, term.name)
                emitCode("function ")
                emitCode(name)
                emitCode("(...) return coroutine.yield(\"")
                emitCode(normaliseEffectName(name))
                emitCode("\", {...})")
                emitCode(" end;")
                "nil"
            }
            else -> TODO("Term $term not supported yet!")
        }
    }


    private fun normaliseEffectName(name: String): String =
        name.replace(".", "_")


    private fun emitAtom(term: Atom, needResult: Boolean): String {
        val value = if (term.type == Type.string) {
//            emitCode("\"")
//            emitCode(term.value)
//            emitCode("\"")
            "\"${term.value}\""
        } else {
//            emitCode(term.value)
            term.value
        }
        return "($value)"
    }

    private fun emitNameDeclaration(term: NameDeclaration, needResult: Boolean): String {
        val value = emitExpr(term.value, true)
        val name = if (inFunction) "local ${term.name}" else qualifiedName(term.name)
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
            }
            is PackageSymbol -> {
                qualifiedName(
                    target.moduleName, target.packageName, target.name
                )
            }
        }
    }

    private fun emitCreateArray(term: CreateArray, needResult: Boolean): String {
        val contents = term.values.map {
            emitExpr(it, true)
        }.joinToString(",")
        return "({$contents})"
    }

    private fun emitCreateRecord(term: CreateRecord, needResult: Boolean): String {
        val contents = term.fields.map {
            val value = emitExpr(it.value, true)
            "${it.name}=$value"
        }.joinToString(",")

        return "({$contents})"
    }

    private fun emitAssignment(term: Assignment, needResult: Boolean): String {
        val varName = when(term.target) {
            is LocalSymbol -> {
                if (inFunction) {
                    term.target.name
                } else {
                    qualifiedName(term.target.name)
                }
            }
            is PackageSymbol -> {
                qualifiedName(term.target.moduleName, term.target.packageName, term.target.name)
            }
        }
        val valueName = emitExpr(term.value, true)

        emitCode(varName)
        emitCode("=")
        emitCode(valueName)
        emitCode(";")
        return varName
    }

    private fun emitIndexOperator(term: IndexOperator, needResult: Boolean): String {
        val variable = emitExpr(term.variable, true)
        val index = emitExpr(term.index, true)
        val name = nextTmpName()
        emitCode("local $name=")
        emitCode(variable)
        emitCode("[")
        emitCode(index)
        emitCode("+1")
        emitCode("];")
        return name
    }

    private fun emitIndexAssignment(term: IndexedAssignment, needResult: Boolean): String {
        val variable = emitExpr(term.variable, true)
        val index = emitExpr(term.index, true)
        val value = emitExpr(term.value, true)

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
        val condition = emitExpr(term.condition, needResult = true)
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
        val leftVar = emitExpr(term.left, true)
        val rightVar = emitExpr(term.right, true)

        val op = if (term.left.type == Type.string) {
            ".."
        } else term.op

        return "($leftVar $op $rightVar)"
    }

    private fun emitPrefixOp(term: PrefixOp, needResult: Boolean): String {
        val valueVar = emitExpr(term.expr, true)
        val resultName = nextTmpName()
        emitCode("local $resultName=")
        val op = when (term.op) {
            "!" -> "not "
            else -> TODO("Unsupported prefix operator: ${term.op}")
        }
        emitCode(op)
        emitCode(valueVar)
        emitCode(";")
        return resultName
    }

    private fun emitInterpolatedString(term: InterpolatedString, needResult: Boolean): String {
        val partVars = term.parts.map { emitExpr(it, needResult = true) }
        val resultName = nextTmpName()
        emitCode("local $resultName=")
        emitCode(partVars.joinToString(" .. "))
        emitCode(";")
        return resultName
    }

    private fun emitIs(term: Is, needResult: Boolean): String {
        return if (term.type == term.checkedType) {
            "true"
        } else {
            val value = emitExpr(term.value, true)
            when (term.checkedType) {
                Type.float, Type.int -> "type($value) == \"number\""
                Type.bool -> "type($value) == \"boolean\""
                Type.string -> "type($value) == \"string\""
                is Array -> {
                    TODO()
                }
                is Record -> {
                    TODO()
                }
                is Function -> {
                    TODO()
                }
                else -> TODO()
            }
        }
    }


    private fun emitWhile(term: WhileLoop, needResult: Boolean): String {
        val condFunName = nextTmpName()
        emitCode("local $condFunName = function() ")
        val result = emitExpr(term.condition, true)
        emitCode("return $result end;")

        emitCode("while ($condFunName()) do ")
        emitExpr(term.loop)
        emitCode("end;")
        return "nil"
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

// TODO:
//  - add tests for each of the expressions (compile and run)
//  - figure out how to redirect IO from/to Lua
//  - implement effects with coroutines
//  - remove the 'main' below
//  - cleanup REPL
//  - create launcher to launch either the REPL or script
//  - compile the launcher to native image

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
        var i = 0
        while i < 5 {
            print(i)
            i += 1
        }
    """.trimIndent()
    val result = Compiler.compile(code, ns)
    val emitter = LuaEmitter(result.program)
    val luaCode = emitter.emit()
    println(luaCode)

    val lua = Lua54()
    lua.openLibraries()
    lua.register("chi_println") {
        val arg = it.get().toJavaObject()
        println(arg)
        0
    }

    lua.run("""
        chi = { 
            std = { 
                lang = { 
                    _package = {
                        println = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' }
                    },
                    println = chi_println,
                } 
            },
            user = { default = { _package = {  }, print = chi_println } }
        }
    """.trimIndent())

    val status = lua.run(luaCode)
    if (status != Lua.LuaError.OK) {
        println(lua.get().toJavaObject())
    }
}