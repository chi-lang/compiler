package gh.marad.chi.lua

import gh.marad.chi.core.*
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Type
import party.iroiro.luajava.lua54.Lua54

class LuaEmitter(val program: Program) {
    private var sb = StringBuilder()
    fun emit(): String {
        sb = StringBuilder()
        val mod = modName()
        val pkg = pkgName()
        if (!(mod == CompilationDefaults.defaultModule && pkg == CompilationDefaults.defaultPacakge)) {
            emitCode("if not chi.$mod then chi.$mod={} end;")
            emitCode("if not chi.$mod.$pkg then chi.$mod.$pkg={} end;")
            emitCode("if not chi.$mod.$pkg._package then chi.$mod.$pkg._package={} end;")
        }
        // todo
        //  - emit exports
        //  - emit type aliases
        //  - build compilation namespace from lua env

        emitPackageInfo()

        val iter = program.expressions.iterator()
        while(iter.hasNext()) {
            emitExpr(iter.next())
            if (iter.hasNext()) {
                emitCode(";")
            }
        }
        return sb.toString()
    }

    private fun emitPackageInfo() {
        val descPath = "${generateModuleName(program.packageDefinition.moduleName, program.packageDefinition.packageName)}._package"
        val iter = program.expressions.filterIsInstance<NameDeclaration>().iterator()
        while(iter.hasNext()) {
            val it = iter.next()
            emitCode("$descPath.${it.name}={")
            emitCode("public=${it.public},")
            emitCode("mutable=${it.mutable},")
            emitCode("type=\"${it.type}\"")
            emitCode("};")
        }
    }

    private fun emitCode(code: String) {
        sb.append(code)
    }

    private fun emitExpr(term: Expression) {
        when(term) {
            is Atom -> emitAtom(term)
            is NameDeclaration -> emitNameDeclaration(term)
            is Fn -> emitFn(term)
            is FnCall -> emitFnCall(term)
            is Block -> emitBlock(term)
            is VariableAccess -> emitVariableAccess(term)
            is CreateArray -> emitCreateArray(term)
            is CreateRecord -> emitCreateRecord(term)
            is Assignment -> emitAssignment(term)
            is IndexOperator -> emitIndexOperator(term)
            is IndexedAssignment -> emitIndexAssignment(term)
            is Cast -> emitExpr(term.expression)
            is FieldAccess -> emitFieldAccess(term)
            is FieldAssignment -> emitFieldAssignment(term)
            is IfElse -> emitIfElse(term)
            is InfixOp -> emitInfixOp(term)
            is PrefixOp -> emitPrefixOp(term)
            is InterpolatedString -> emitInterpolatedString(term)
            is Is -> TODO()
            is WhileLoop -> TODO()
            is Break -> emitCode("break")
            is Continue -> emitCode("continue")
            is Return -> emitCode("return")
            is Handle -> TODO()
            is EffectDefinition -> TODO()
//            else -> TODO("Term $term not supported yet!")
        }
    }


    private fun emitAtom(term: Atom) {
        if (term.type == Type.string) {
            emitCode("\"")
            emitCode(term.value)
            emitCode("\"")
        } else {
            emitCode(term.value)
        }
    }

    private fun emitNameDeclaration(term: NameDeclaration) {
        emitCode(qualifiedName(term.name))
        emitCode("=")
        emitExpr(term.value)
        emitCode(";")
    }

    private fun emitFn(term: Fn) {
        emitCode("function(")
        emitCode(term.parameters.joinToString(",") { it.name })
        emitCode(") ")
        emitBlock(term.body, includeReturn = true)
        emitCode(" end")
    }

    private fun emitFnCall(term: FnCall) {
        emitExpr(term.function)
        emitCode("(")
        val iter = term.parameters.iterator()
        while(iter.hasNext()) {
            emitExpr(iter.next())
            if (iter.hasNext()) {
                emitCode(",")
            }
        }
        emitCode(")")
    }

    private fun emitBlock(term: Block, includeDo: Boolean = false, includeReturn: Boolean = false) {
        if (includeDo) emitCode("do ")
        val iter = term.body.iterator()
        while(iter.hasNext()) {
            val expr = iter.next()
            if (includeReturn && !iter.hasNext()) {
                emitCode("return ")
            }
            emitExpr(expr)
            if (iter.hasNext()) {
                emitCode(";")
            }
        }
        if (includeDo) emitCode(" end")
        term.body
    }

    private fun emitVariableAccess(term: VariableAccess) {
        when(val target = term.target) {
            is LocalSymbol -> {
                emitCode(qualifiedName(target.name))
            }
            is PackageSymbol -> {
                emitCode(
                    qualifiedName(
                        target.moduleName, target.packageName, target.name
                    )
                )
            }
        }

    }

    private fun emitCreateArray(term: CreateArray) {
        emitCode("{")
        val iter = term.values.iterator()
        while(iter.hasNext()) {
            emitExpr(iter.next())
            if (iter.hasNext()) {
                emitCode(",")
            }
        }
        emitCode("}")
    }

    private fun emitCreateRecord(term: CreateRecord) {
        emitCode("{")
        val iter = term.fields.iterator()
        while (iter.hasNext()) {
            val field = iter.next()
            emitCode(field.name)
            emitCode("=")
            emitExpr(field.value)
            if (iter.hasNext()) {
                emitCode(",")
            }
        }
        emitCode("}")
    }

    private fun emitAssignment(term: Assignment) {
//        term.
        when(term.target) {
            is LocalSymbol -> {
                emitCode(term.target.name)
            }
            is PackageSymbol -> {
                if (term.target.moduleName == program.packageDefinition.moduleName &&
                    term.target.packageName == program.packageDefinition.packageName) {
                    emitCode(term.target.name)
                } else {
                    val module = generateModuleName(term.target.moduleName, term.target.packageName)
                    emitCode(module)
                    emitCode(".")
                    emitCode(term.target.name)
                }
            }
        }
        emitCode("=")
        emitExpr(term.value)
    }

    private fun emitIndexAssignment(term: IndexedAssignment) {
        emitExpr(term.variable)
        emitCode("[")
        emitExpr(term.index)
        emitCode("+1")
        emitCode("]")
        emitCode("=")
        emitExpr(term.value)
    }
    private fun emitIndexOperator(term: IndexOperator) {
        emitExpr(term.variable)
        emitCode("[")
        emitExpr(term.index)
        emitCode("+1")
        emitCode("]")
    }

    private fun emitFieldAssignment(term: FieldAssignment) {
        emitExpr(term.receiver)
        emitCode(".")
        emitCode(term.fieldName)
        emitCode("=")
        emitExpr(term.value)
    }

    private fun emitFieldAccess(term: FieldAccess) {
        val target = term.target!!
        when(target) {
            DotTarget.Field -> {
                emitExpr(term.receiver)
                emitCode(".")
                emitCode(term.fieldName)
            }
            DotTarget.LocalFunction -> TODO()
            is DotTarget.PackageFunction -> TODO()
        }
    }

    private fun emitIfElse(term: IfElse) {
        emitCode("(function() ")
        emitCode("if ")
        emitExpr(term.condition)
        emitCode(" then ")
        emitBlock(term.thenBranch as Block, includeDo = true, includeReturn = true)
        if (term.elseBranch != null) {
            emitCode(" else ")
            if (term.elseBranch is Block) {
                emitBlock(term.elseBranch, includeDo = true, includeReturn = true)
            } else {
                emitExpr(term.elseBranch)
            }
        }
        emitCode(" end")
        emitCode(" end)()")
    }

    private fun emitInfixOp(term: InfixOp) {
        emitExpr(term.left)
        emitCode(" ")
        emitCode(term.op)
        emitCode(" ")
        emitExpr(term.right)
    }

    private fun emitPrefixOp(term: PrefixOp) {
        emitCode(term.op)
        emitExpr(term.expr)
    }

    private fun emitInterpolatedString(term: InterpolatedString) {
        val parts = term.parts.map {
        }

        val iter = term.parts.iterator()
        while (iter.hasNext()) {
            val it = iter.next()
            if (it is Atom && it.type == Type.string) {
                emitExpr(it)
            } else {
                emitCode("tostring(")
                emitExpr(it)
                emitCode(")")
            }
            if (iter.hasNext()) {
                emitCode(" .. ")
            }
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