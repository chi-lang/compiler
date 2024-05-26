package gh.marad.chi.lua

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type.Companion.float
import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import gh.marad.chi.runtime.TypeWriter.encodeType

class LuaEmitter(val program: Program) {
    private var sb = StringBuilder()

    /**
     * @param emitModule Tells the emitter to create LUA module. False means it should return the last value
     *                   and should be used whenever you need to evaluate some code and get it's result.
     */
    fun emit(emitModule: Boolean = true): String {
        sb = StringBuilder()

        val packageQualifier = "${program.packageDefinition.moduleName}/${program.packageDefinition.packageName}"
        emitCode("local __P_ = package.loaded['$packageQualifier'] or {_package={},_types={}};")
        emitCode("package.loaded['$packageQualifier'] = __P_;")
        emitCode("local __S_ = __P_._package;")
        emitCode("local __T_ = __P_._types;")

        emitPackageInfo()

        program.typeAliases.forEach {
            emitCode("__T_.${it.typeId.name}=\"")
            emitCode(encodeType(it.type))
            emitCode("\";")
        }

        // Requires must be after _package and _types declarations to avoid circular dependencies
        val requires = mutableSetOf<Pair<String,String>>()
        val visitor = object : DefaultExpressionVisitor {
            override fun visitVariableAccess(va: VariableAccess) {
                if (va.target is PackageSymbol &&
                    (va.target.moduleName != program.packageDefinition.moduleName ||
                    va.target.packageName != program.packageDefinition.packageName)) {
                    requires.add(va.target.moduleName to va.target.packageName)
                }
                super.visitVariableAccess(va)
            }
        }

        program.expressions.forEach { it.accept(visitor) }

        requires.forEach { (mod, pkg) ->
            val localName = localPackagePath(mod, pkg)
            emitCode("local $localName = require(\"$mod/$pkg\");")
        }

        val iter = program.expressions.iterator()
        while(iter.hasNext()) {
            val result = emitExpr(iter.next())
            if (!iter.hasNext()) {
                if (emitModule) {
                    emitCode("return __P_")
                } else {
                    emitCode("return $result")
                }
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
        val descPath = "__S_"
        val symbols = program.symbolTable.iterator()

        while (symbols.hasNext()) {
            val it = symbols.next()
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
    private fun emitExpr(term: Expression): String {
        return when(term) {
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
            is Cast -> emitCast(term)
            is FieldAccess -> emitFieldAccess(term)
            is FieldAssignment -> emitFieldAssignment(term)
            is IfElse -> emitIfElse(term)
            is InfixOp -> emitInfixOp(term)
            is PrefixOp -> emitPrefixOp(term)
            is InterpolatedString -> emitInterpolatedString(term)
            is Is -> emitIs(term)
            is WhileLoop -> emitWhile(term)
            is ForLoop -> emitForLoop(term)
            is Break -> {
                emitCode("break;")
                "nil"
            }
            is Continue -> {
                emitCode("continue;")
                "nil"
            }
            is Return -> {
                if (term.value != null) {
                    val result = emitExpr(term.value)
                    emitCode("return $result;")
                } else {
                    emitCode("return;")
                }
                "nil"
            }
//            is Handle -> {
//                val tmpName = nextTmpName()
//
//                emitCode("local ${tmpName}_handlers={")
//                val iter = term.cases.iterator()
//                while (iter.hasNext()) {
//                    val case = iter.next()
//                    val name = normaliseEffectName(qualifiedName(case.moduleName, case.packageName, case.effectName))
//                    emitCode(name)
//                    emitCode("=function(args) ")
//                    emitCode("local function resume(x) return false, x end;")
//                    case.argumentNames.forEachIndexed { index, name ->
//                        emitCode("local $name=args[${index}];")
//                    }
//
//                    insideFunction {
//                        val result = emitExpr(case.body, needResult = true)
//                        emitCode("return $result")
//                    }
//                    emitCode(" end")
//                    if (iter.hasNext()) {
//                        emitCode(",")
//                    }
//                }
//                emitCode("};")
//
//                emitCode("local ${tmpName}_body=coroutine.create(function() ")
//                val result = emitExpr(term.body, needResult = true)
//                emitCode("return $result;")
//                emitCode(" end);")
//                emitCode("local ${tmpName}=chi_handle_effect(${tmpName}_body,{},${tmpName}_handlers);")
//                tmpName
//            }
//            is EffectDefinition -> {
//                val name = qualifiedName(term.moduleName, term.packageName, term.name)
//                emitCode("function ")
//                emitCode(name)
//                emitCode("(...) return coroutine.yield(\"")
//                emitCode(normaliseEffectName(name))
//                emitCode("\", {...})")
//                emitCode(" end;")
//                "nil"
//            }
            else -> TODO("Term $term not supported yet!")
        }
    }


    private fun normaliseEffectName(name: String): String =
        name.replace(".", "_")


    private fun emitAtom(term: Atom): String {
        val value = when (term.type) {
            string -> {
                // TODO: this should escape all the escaped codes like \n, ...
                //"\"${term.value}\""
                val tmp = nextTmpName()
                emitCode("local $tmp = java.new(String,'${term.value}');")
                tmp
            }
            Type.unit -> {
                "nil"
            }
            else -> {
                term.value
            }
        }
        return "($value)"
    }

    private fun emitNameDeclaration(term: NameDeclaration): String {
        if (!inFunction && term.value is Fn) {
            return emitFn(term.value, name = term.name, topLevel = true)
        } else {
            val value = emitExpr(term.value)
            val name = if (inFunction) "local ${term.name}" else topLevelName(term.name)
            emitCode(name)
            emitCode("=")
            emitCode(value)
            emitCode(";")
            return if (inFunction) term.name else topLevelName(term.name)
        }
    }

    private var nextTmpId = 0
    private fun nextTmpName() = "tmp${nextTmpId++}"

    private fun emitFn(term: Fn, name: String? = null, topLevel: Boolean = false): String {
        val tmpName = name ?: nextTmpName()
        if (topLevel) {
            emitCode("function __P_.$tmpName(")
        } else {
            emitCode("function $tmpName(")
        }
        emitCode(term.parameters.joinToString(",") { it.name })
        emitCode(") ")
        insideFunction {
            term.defaultValues.forEach { (name, value) ->
                emitCode("if $name == nil then ")
                val default = emitExpr(value)
                emitCode("$name=$default;")
                emitCode("end;")
            }
        }
        val result = emitFnBody(term.body)

        if (term.body.body.lastOrNull() !is Return) {
            // if the last expr is Return then it will emit this itself
            emitCode("return $result")
        }
        emitCode(" end;")
        return tmpName
    }

    private fun emitFnBody(block: Block): String = insideFunction { emitBlock(block) }

    private val embedLuaTarget = PackageSymbol("std", "lang", "embedLua")
    private val luaExprTarget = PackageSymbol("std", "lang", "luaExpr")
    private fun emitFnCall(term: FnCall): String {
        val function = term.function
        if (function is VariableAccess && function.target == embedLuaTarget) {
            val codeParam = term.parameters.first()
            if (codeParam is Atom && codeParam.type == string) {
                val luaCode = codeParam.value.replace("\n", ";")
                emitCode("$luaCode;")
                return "nil"
            } else {
                TODO("embedLua function requires string parameter verbatim (not a variable)")
            }
        } else if (function is VariableAccess && function.target == luaExprTarget) {
            val codeParam = term.parameters.first()
            if (codeParam is Atom && codeParam.type == string) {
                val luaCode = codeParam.value.replace("\n", ";")
                return luaCode
            } else {
                TODO("luaExpr function requires string parameter verbatim (not a variable)")
            }
        } else {
            val fnName = emitExpr(term.function)
            val iter = term.parameters.iterator()
            val params = ArrayList<String>(term.parameters.size)
            while (iter.hasNext()) {
                val param = iter.next()
                if (param is Atom && param.value == "@") {
                    // this is a default parameter - skip it
                    continue
                }
                params.add(emitExpr(param))
            }


            val tmpName = nextTmpName()
            emitCode("local $tmpName=")
            emitCode(fnName)
            emitCode("(")
            emitCode(params.joinToString(","))
            emitCode(");")
            return tmpName
        }
    }

    private fun emitBlock(term: Block): String {
        val iter = term.body.iterator()
        var lastExprResult = "nil"
        while(iter.hasNext()) {
            val expr = iter.next()
            lastExprResult = emitExpr(expr)
            if (expr is Return) {
                break
            }
        }
        return lastExprResult
    }

    private fun emitVariableAccess(term: VariableAccess): String {
        return when(val target = term.target) {
            is LocalSymbol -> {
                if (inFunction) {
                    target.name
                } else {
                    topLevelName(target.name)
                }
            }
            is PackageSymbol -> {
                if (target.moduleName == program.packageDefinition.moduleName &&
                    target.packageName == program.packageDefinition.packageName) {
                    topLevelName(target.name)
                } else {
                    localQualifiedName(
                        target.moduleName, target.packageName, target.name
                    )
                }
            }
        }
    }

    private fun emitCreateArray(term: CreateArray): String {
        val contents = term.values.map {
            emitExpr(it)
        }.joinToString(",")
        val name = nextTmpName()
        emitCode("local $name={$contents};")
        emitCode("setmetatable($name, array_meta_table);")
        return name
    }

    private fun emitCreateRecord(term: CreateRecord): String {
        val contents = term.fields.map {
            val value = emitExpr(it.value)
            "${it.name}=$value"
        }.joinToString(",")

        val name = nextTmpName()
        emitCode("local $name={$contents};")
        emitCode("setmetatable($name, record_meta_table);")
        return name
    }

    private fun emitAssignment(term: Assignment): String {
        val varName = when(term.target) {
            is LocalSymbol -> {
                if (inFunction) {
                    term.target.name
                } else {
                    topLevelName(term.target.name)
                }
            }
            is PackageSymbol -> {
                if (term.target.moduleName == program.packageDefinition.moduleName &&
                    term.target.packageName == program.packageDefinition.packageName) {
                    topLevelName(term.target.name)
                } else {
                    localQualifiedName(
                        term.target.moduleName, term.target.packageName, term.target.name
                    )
                }
            }
        }
        val valueName = emitExpr(term.value)

        emitCode(varName)
        emitCode("=")
        emitCode(valueName)
        emitCode(";")
        return varName
    }

    private fun emitIndexOperator(term: IndexOperator): String {
        val variable = emitExpr(term.variable)
        val index = emitExpr(term.index)
        val name = nextTmpName()
        emitCode("local $name=")
        emitCode(variable)
        emitCode("[")
        emitCode(index)
        emitCode("];")
        return name
    }

    private fun emitIndexAssignment(term: IndexedAssignment): String {
        val variable = emitExpr(term.variable)
        val index = emitExpr(term.index)
        val value = emitExpr(term.value)

        emitCode(variable)
        emitCode("[")
        emitCode(index)
        emitCode("]")
        emitCode("=")
        emitCode(value)
        emitCode(";")
        return value
    }

    private fun emitCast(term: Cast): String {
        var result = emitExpr(term.expression)
        if (term.targetType == term.expression.type) {
            // no casting necessary
            return result
        }
        if (term.expression.type == string && term.targetType != string) {
           result = "java.luaify($result)"
        }
        val name = nextTmpName()
        return when (term.targetType) {
            string -> {
                emitCode("local $name=chi_tostring($result);")
                name
            }
            int, float -> {
                emitCode("local $name=tonumber($result);")
                name
            }
            else -> result
        }
    }

    private fun emitFieldAccess(term: FieldAccess): String {
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

    private fun emitFieldAssignment(term: FieldAssignment): String {
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

    private fun emitIfElse(term: IfElse): String {
        val tmpName = nextTmpName()
        emitCode("local $tmpName;")
        val condition = emitExpr(term.condition)
        emitCode("if ")
        emitCode(condition)
        emitCode(" then ")
        val thenResultName = emitBlock(term.thenBranch as Block)
        if (term.thenBranch.body.lastOrNull() !is Return) {
            emitCode("$tmpName = $thenResultName")
        }
        if (term.elseBranch != null) {
            emitCode(" else ")
            if (term.elseBranch is Block) {
                val elseResultName = emitBlock(term.elseBranch)
                if (term.elseBranch.body.lastOrNull() !is Return) {
                    emitCode("$tmpName = $elseResultName")
                }
            } else {
                val elseResultName = emitExpr(term.elseBranch)
                if (term.elseBranch !is Return) {
                    emitCode("$tmpName = $elseResultName")
                }
            }
        }
        emitCode(" end;")
        return tmpName
    }

    private fun emitInfixOp(term: InfixOp): String {
        val leftVar = emitExpr(term.left)
        val rightVar = emitExpr(term.right)
        return mapInfixOperation(leftVar, rightVar, term.op, term.left.type)
    }

    private fun mapInfixOperation(leftVar: String, rightVar: String, op: String, leftType: Type?): String {
        val mappedOp = when (op) {
            "!=" -> "~="
            "&&" -> "and"
            "||" -> "or"
            "+" -> if (leftType == string) {
                return "chistr.concat($leftVar, $rightVar)"
            } else op

            else -> op
        }
        return "($leftVar $mappedOp $rightVar)"
    }

    private fun emitPrefixOp(term: PrefixOp): String {
        val valueVar = emitExpr(term.expr)
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

    private fun emitInterpolatedString(term: InterpolatedString): String {
        val partVars = term.parts.map { emitExpr(it) }
        val resultName = nextTmpName()
        emitCode("local $resultName=")
        emitCode("chistr.concat(${partVars.joinToString(",")})")
        emitCode(";")
        return resultName
    }

    private fun emitIs(term: Is): String {
        return if (term.type == term.checkedType) {
            "true"
        } else {
            val value = emitExpr(term.value)
            when (term.checkedType) {
                Type.unit -> "type($value) == \"nil\""
                float, int -> "type($value) == \"number\""
                Type.bool -> "type($value) == \"boolean\""
                string -> "type($value) == \"string\""
//                is Array -> {
//                    TODO()
//                }
//                is Record -> {
//                    TODO()
//                }
//                is Function -> {
//                    TODO()
//                }
                is Variable -> {
                    "false"
                }
                else -> TODO(term.toString())
            }
        }
    }

    private fun foo(term: Expression, bar: MutableList<NameDeclaration>): String {
        return when (term) {
            is InfixOp -> {
                val left = foo(term.left, bar)
                val right = foo(term.right, bar)
                //InfixOp(term.op, left, right, term.sourceSection)
                mapInfixOperation(left, right, term.op, term.left.type)
            }
            else -> {
                val tmpName = nextTmpName()
                // create val $tmpName = { term }
                // and add it to the list of declarations
                bar.add(
                    NameDeclaration(false, tmpName, mutable = false, expectedType = null, sourceSection = null,
                        value = Fn(emptyList(), emptyMap(), sourceSection = null, body = Block(sourceSection = null, body = listOf(term))))
                )
                // return a call to the declared function as an expression
//                FnCall(
//                    function = VariableAccess(LocalSymbol(tmpName), null),
//                    mutableListOf(),
//                    null
//                )
                "$tmpName()"
            }
        }
    }

    private fun emitWhile(term: WhileLoop): String {
//        val visitor = object : DefaultMappingVisitor() {
//            override fun visitInfixOp(infixOp: InfixOp): Expression {
//                return super.visitInfixOp(infixOp)
//            }
//        }

        // TODO: each condition should be separate function
        //   otherwise we loose the special treatment of 'or' and 'and'
        //   operators. This is more related to the infix operators
        //   than while or if expressions

//        val condFunName = nextTmpName()
//        emitCode("local $condFunName = function() ")
//        val result = emitExpr(term.condition, true)
//        emitCode(" return $result end;")

//        emitCode("while ($condFunName()) do ")


        val declarations = mutableListOf<NameDeclaration>()
        val condition = foo(term.condition, declarations)

        insideFunction {
            declarations.forEach {
                    emitExpr(it)
            }
        }
        emitCode("while $condition do ")
        emitExpr(term.loop)
        emitCode("end;")
        return "nil"
    }

    private fun emitForLoop(term: ForLoop): String {
        val iterable = emitExpr(term.iterable)
        val (vars, elements) = if (term.iterable.type is Array) {
            val vars = if (term.vars.size == 1) {
                "_,${term.vars.first()}"
            } else {
                term.vars.joinToString(",")
            }
            vars to "ipairs($iterable)"
        } else if (term.iterable.type is Record) {
            term.vars.joinToString(",") to "pairs($iterable)"
        } else if (term.iterable.type is Function) {
            if (term.state != null && term.init != null) {
                val state = emitExpr(term.state)
                val init = emitExpr(term.init)
                term.vars.joinToString(",") to "$iterable,$state,$init"
            } else {
                // basic generator function
                term.vars.joinToString(",") to iterable
            }
        } else {
            throw RuntimeException("Not implemented for type ${term.iterable.type}")
        }

        emitCode("for $vars in $elements do ")
        insideFunction {
            emitExpr(term.body)
        }
        emitCode(" end;")
        return "nil"
    }


    private fun topLevelName(name: String) =
        "__P_.$name"

    companion object {
        fun modName(name: String): String =
            name.replace(".", "_")

        fun pkgName(pkg: String): String =
            pkg.replace(".", "_")

        fun packagePath(module: String, pkg: String): String =
            "${modName(module)}__${pkgName(pkg)}"

        fun localPackagePath(module: String, pkg: String): String =
            "__${packagePath(module, pkg)}"

        fun localQualifiedName(module: String, pkg: String, name: String): String =
            "${localPackagePath(module, pkg)}.$name"

    }

}
