package gh.marad.chi.lua

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
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
            val result = emitExpr(iter.next(), !iter.hasNext())
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


    private fun emitAtom(term: Atom, needResult: Boolean): String {
        val value = if (term.type == Type.string) {
            // TODO: this should escape all the escaped codes like \n, ...
            "\"${term.value}\""
        } else if (term.type == Type.unit) {
            "nil"
        } else {
            term.value
        }
        return "($value)"
    }

    private fun emitNameDeclaration(term: NameDeclaration, needResult: Boolean): String {
        if (!inFunction && term.value is Fn) {
            emitCode("function __P_.${term.name}(")
            emitCode(term.value.parameters.joinToString(",") { it.name })
            emitCode(") ")
            val result = emitFnBody(term.value.body)
            emitCode("return $result")
            emitCode(" end;")
            return "nil"
        } else {
            val value = emitExpr(term.value, true)
            val name = if (inFunction) "local ${term.name}" else topLevelName(term.name)
            emitCode(name)
            emitCode("=")
            emitCode(value)
            emitCode(";")
            return name
        }
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
        val result = emitFnBody(term.body)
        emitCode("return $result")
        emitCode(" end;")
        return tmpName
    }

    private fun emitFnBody(block: Block): String = insideFunction { emitBlock(block,needResult = true) }

    private val embedLuaTarget = PackageSymbol("std", "lang", "embedLua")
    private val luaExprTarget = PackageSymbol("std", "lang", "luaExpr")
    private fun emitFnCall(term: FnCall, needResult: Boolean): String {
        val function = term.function
        if (function is VariableAccess && function.target == embedLuaTarget) {
            val codeParam = term.parameters.first()
            if (codeParam is Atom && codeParam.type == Type.string) {
                val luaCode = codeParam.value.replace("\n", ";")
                emitCode("$luaCode;")
                return "nil"
            } else {
                TODO("embedLua function requires string parameter verbatim (not a variable)")
            }
        } else if (function is VariableAccess && function.target == luaExprTarget) {
            val codeParam = term.parameters.first()
            if (codeParam is Atom && codeParam.type == Type.string) {
                val luaCode = codeParam.value.replace("\n", ";")
                return luaCode
            } else {
                TODO("luaExpr function requires string parameter verbatim (not a variable)")
            }
        } else {
            val fnName = emitExpr(term.function, true)
            val iter = term.parameters.iterator()
            val params = ArrayList<String>(term.parameters.size)
            while (iter.hasNext()) {
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

    private fun emitCreateArray(term: CreateArray, needResult: Boolean): String {
        val contents = term.values.map {
            emitExpr(it, true)
        }.joinToString(",")
        val name = nextTmpName()
        emitCode("local $name={$contents};")
        emitCode("setmetatable($name, array_meta_table);")
        return name
    }

    private fun emitCreateRecord(term: CreateRecord, needResult: Boolean): String {
        val contents = term.fields.map {
            val value = emitExpr(it.value, true)
            "${it.name}=$value"
        }.joinToString(",")

        val name = nextTmpName()
        emitCode("local $name={$contents};")
        emitCode("setmetatable($name, record_meta_table);")
        return name
    }

    private fun emitAssignment(term: Assignment, needResult: Boolean): String {
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
        emitCode("]")
        emitCode("=")
        emitCode(value)
        emitCode(";")
        return value
    }

    private fun emitCast(term: Cast, needResult: Boolean): String {
        val result = emitExpr(term.expression, true)
        val name = nextTmpName()
        return when (term.targetType) {
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
        val op = mapInfixOperation(term.op, term.left.type)
        return "($leftVar $op $rightVar)"
    }

    private fun mapInfixOperation(op: String, leftType: Type?): String =
        when (op) {
            "!=" -> "~="
            "&&" -> "and"
            "||" -> "or"
            "+" -> if (leftType == Type.string) {
                ".."
            } else op
            else -> op
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
                Type.unit -> "type($value) == \"nil\""
                Type.float, Type.int -> "type($value) == \"number\""
                Type.bool -> "type($value) == \"boolean\""
                Type.string -> "type($value) == \"string\""
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
                val op = mapInfixOperation(term.op, term.left.type)
                "($left $op $right)"
            }
            else -> {
                val tmpName = nextTmpName()
                // create val $tmpName = { term }
                // and add it to the list of declarations
                bar.add(
                    NameDeclaration(false, tmpName, mutable = false, expectedType = null, sourceSection = null,
                        value = Fn(emptyList(), sourceSection = null, body = Block(sourceSection = null, body = listOf(term))))
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

    private fun emitWhile(term: WhileLoop, needResult: Boolean): String {
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
//        emitCode("return $result end;")

//        emitCode("while ($condFunName()) do ")


        val declarations = mutableListOf<NameDeclaration>()
        val condition = foo(term.condition, declarations)

        insideFunction {
            declarations.forEach {
                    emitExpr(it, false)
            }
        }
        emitCode("while $condition do ")
        emitExpr(term.loop)
        emitCode("end;")
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