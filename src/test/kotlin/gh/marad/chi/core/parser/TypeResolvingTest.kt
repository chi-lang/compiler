package gh.marad.chi.core.parser

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import org.junit.jupiter.api.Test

class TypeResolvingTest {
    @Test
    fun `foo bar`() {
        val ns = GlobalCompilationNamespace()
        val ctx = ConversionContext(ns)
        val ast = testParse("""
            { x: int ->
              id(5)
              id(true)
            }
        """.trimIndent())[0]


        val expr = generateExpressionAst(ctx, ast)

        val tvA = TypeVariable("A")
        val env = mapOf<String, Type>(
            "id" to TypeScheme(FunctionType(listOf(tvA, tvA)), listOf(tvA))
        )

        val result = inferTypes(env, expr)
        println("Inferred: $result")
        val unified = unify(result.constraints)
        println("Unified: $unified")
        val solved = solve(result.type, unified)
        println(solved)
    }


    sealed interface Type {
        fun contains(v: TypeVariable): Boolean
        fun substitute(v: TypeVariable, t: Type): Type
    }

    data class SimpleType(val name: String) : Type {
        override fun contains(v: TypeVariable): Boolean = false
        override fun substitute(v: TypeVariable, t: Type) = this
        override fun toString(): String = name
    }

    data class TypeVariable(val name: String) : Type {
        override fun contains(v: TypeVariable): Boolean = v == this
        override fun substitute(v: TypeVariable, t: Type): Type =
            if (v == this) { t } else { this }
        override fun toString(): String = name
    }

    data class FunctionType(val types: List<Type>) : Type {
        override fun contains(v: TypeVariable): Boolean = types.any { it.contains(v) }
        override fun substitute(v: TypeVariable, t: Type): Type =
            FunctionType(types.map { it.substitute(v, t) })

        override fun toString(): String = "(" + types.joinToString(" -> ") + ")"
    }

    data class TypeScheme(val base: Type, val variables: List<TypeVariable>) : Type {
        override fun contains(v: TypeVariable): Boolean = variables.contains(v)
        override fun substitute(v: TypeVariable, t: Type): Type =
            TypeScheme(base.substitute(v, t), variables - v)

        override fun toString(): String = "$base[${variables.joinToString(", ")}]"
    }

    data class Constraint(var a: Type, var b: Type) {
        fun substitute(v: TypeVariable, t: Type) {
            a = a.substitute(v,t)
            b = b.substitute(v,t)
        }
        override fun toString(): String = "$a = $b"
    }
    data class InferenceResult(val type: Type, val constraints: Set<Constraint>)

    val intType = SimpleType("int")
    val boolType = SimpleType("bool")
    val unitType = SimpleType("unit")

    private var typeNum = 0
    fun nextTypeVariable() = TypeVariable("t${typeNum++}")

    fun inferTypes(env: Map<String, Type>, expr: Expression): InferenceResult {
        return when(expr) {
            is Atom -> InferenceResult(SimpleType(expr.type.name), emptySet())
            is VariableAccess -> {
                val t = env[expr.name] ?: TODO("Type inference failed. Name ${expr.name} is not defined in.")
                InferenceResult(instantiate(t), emptySet())
            }
            is IfElse -> {
                val t = nextTypeVariable()
                val condType = inferTypes(env, expr.condition)
                val thenBranchType = inferTypes(env, expr.thenBranch)
                val elseBranchType = expr.elseBranch?.let { inferTypes(env, it) }
                    ?: InferenceResult(unitType, setOf())

                val constraints = mutableSetOf<Constraint>()
                constraints.add(Constraint(condType.type, boolType))
                constraints.add(Constraint(t, thenBranchType.type))
                constraints.add(Constraint(t, elseBranchType.type))
                constraints.addAll(condType.constraints)
                constraints.addAll(thenBranchType.constraints)
                constraints.addAll(elseBranchType.constraints)
                InferenceResult(t, constraints)
            }
            is Block -> {
                val constraints = mutableSetOf<Constraint>()
                val last = expr.body.map {
                    var result = inferTypes(env, it)
                    constraints.addAll(result.constraints)
                    result
                }.lastOrNull() ?: InferenceResult(unitType, setOf())
                InferenceResult(last.type, constraints)
            }
            is Fn -> {
                val paramNamesAndTypes: List<Pair<String, Type>> =
                    expr.parameters.map {
                        // if param types were optional we would have to generate
                        // new types for them and normally solve with constraints
//                        if (it.type != null) {
//                            it.name to SimpleType(it.type.name)
//                        } else {
                            it.name to nextTypeVariable()
//                        }
                    }

                val extEnv = mutableMapOf<String, Type>()
                extEnv.putAll(env)
                extEnv.putAll(paramNamesAndTypes)

                val bodyType = inferTypes(extEnv, expr.body)

                val funcTypes = paramNamesAndTypes.map { it.second }.toMutableList()
                funcTypes.add(bodyType.type)

                InferenceResult(FunctionType(funcTypes), bodyType.constraints)
            }
            is FnCall -> {
                val t = nextTypeVariable()

                val funcType = inferTypes(env, expr.function)
                val paramTypes = expr.parameters.map { inferTypes(env, it) }

                val constraints = mutableSetOf<Constraint>()
                constraints.add(Constraint(
                    funcType.type,
                    FunctionType(paramTypes.map { it.type } + t)))
                constraints.addAll(funcType.constraints)
                paramTypes.forEach { constraints.addAll(it.constraints) }

                InferenceResult(t, constraints)
            }
            is InfixOp -> {
                val t = nextTypeVariable()
                val left = inferTypes(env, expr.left)
                val right = inferTypes(env, expr.right)
                val constraints = mutableSetOf<Constraint>()
                constraints.add(Constraint(t, left.type))
                constraints.add(Constraint(t, right.type))
                constraints.addAll(left.constraints)
                constraints.addAll(right.constraints)
                InferenceResult(t, constraints)
            }
            else -> TODO("Type inference is not supported for ${expr.javaClass.name}")
        }
    }

    private fun instantiate(t: Type): Type =
        if (t is TypeScheme) {
            val mappings = t.variables.map { it to nextTypeVariable() }
            var current = t.base
            for ((v, t) in mappings) {
                current = current.substitute(v, t)
            }
            current
        } else {
            t
        }

    fun unify(constraints: Set<Constraint>): List<Pair<TypeVariable, Type>> {
        var q = ArrayDeque(constraints)
        val substitutions = mutableListOf<Pair<TypeVariable, Type>>()

        while (q.isNotEmpty()) {
            val (a, b) = q.removeFirst()
//            println("$a = $b")
//            println(q)
            if (a == b) {
                // this is nothing interesting
                continue
            } else if (a is FunctionType && b is FunctionType) {
                val aHead = a.types.first()
                val bHead = b.types.first()
                q.add(Constraint(aHead, bHead))

                val aTail = a.types.drop(1).let { if (it.size == 1) it[0] else FunctionType(it) }
                val bTail = b.types.drop(1).let { if (it.size == 1) it[0] else FunctionType(it) }
                q.add(Constraint(aTail, bTail))
            } else if (a is TypeVariable) {
                if (b.contains(a)) {
                    TODO("Inference failed - $constraints are unsolvable")
                }
                q.forEach { it.substitute(a, b) }
                substitutions.add(a to b)
            } else if (b is TypeVariable) {
                if (a.contains(b)) {
                    TODO("Inference failed - $constraints are unsolvable")
                }
                q.forEach { it.substitute(b, a) }
                substitutions.add(b to a)
            } else {
                TODO("FAIL - cannot solve")
            }
        }

        return substitutions
    }

    fun solve(type: Type, solutions: List<Pair<TypeVariable, Type>>): Type {
        var currentType = type
        solutions.forEach {
            currentType = currentType.substitute(it.first, it.second)
        }
        return currentType
    }
}
