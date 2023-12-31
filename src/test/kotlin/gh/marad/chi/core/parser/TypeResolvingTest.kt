package gh.marad.chi.core.parser

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.readers.ParseBlock
import gh.marad.chi.core.parser.readers.ParseLambda
import org.junit.jupiter.api.Test

class TypeResolvingTest {
    @Test
    fun `foo bar`() {
        val ns = GlobalCompilationNamespace()
        val ctx = ConversionContext(ns)
        val ast = testParse("""
            {
              effect hello(a: int): int
              hello
            }
        """.trimIndent())[0]

        val body = (ast as ParseLambda).body

        val expr = generateExpressionAst(ctx, ParseBlock(body, ast.section))

        val env = mapOf<String, Type>()

        val result = inferTypes(env, expr)
        println("Inferred: $result")
        val unified = unify(result.constraints)
        println("Unified: $unified")
        val solved = applySubstitution(result.type, unified)
        println(solved)
    }


    sealed interface Type {
        fun contains(v: TypeVariable): Boolean
        fun substitute(v: TypeVariable, t: Type): Type
        fun isTypeScheme(): Boolean

        /// If type is a type scheme, typeVariables returns it's generalized type variables
        fun typeSchemeVariables(): List<TypeVariable>

        /// Lists all the type variables within this type (not only generalized ones)
        fun findTypeVariables(): List<TypeVariable>
        fun generalize(variables: List<TypeVariable>): Type
    }

    data class SimpleType(val name: String,
                          val parameters: List<Type> = emptyList(),
                          val typeSchemeVariables: List<TypeVariable> = emptyList()
    ) : Type {
        override fun contains(v: TypeVariable): Boolean = false
        override fun substitute(v: TypeVariable, t: Type) = copy(
            parameters = parameters.map { it.substitute(v,t) },
            typeSchemeVariables = typeSchemeVariables - v
        )
        override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
        override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
        override fun findTypeVariables(): List<TypeVariable> = parameters.flatMap { it.findTypeVariables() }
        override fun generalize(variables: List<TypeVariable>): Type = copy(typeSchemeVariables = variables)
        override fun toString(): String {
            val sb = StringBuilder()
            sb.append(name)
            if (parameters.isNotEmpty()) {
                sb.append('[')
                sb.append(parameters.joinToString(", "))
                sb.append(']')
            }
            return sb.toString()
        }
    }

    data class TypeVariable(val name: String, val typeScheme: Boolean = false) : Type {
        override fun contains(v: TypeVariable): Boolean = v == this
        override fun substitute(v: TypeVariable, t: Type): Type =
            if (v == this) { t } else { this }
        override fun isTypeScheme(): Boolean = typeScheme
        override fun typeSchemeVariables(): List<TypeVariable> = if (isTypeScheme()) listOf(this) else emptyList()
        override fun findTypeVariables(): List<TypeVariable> = listOf(this)
        override fun generalize(variables: List<TypeVariable>): Type = if (variables.contains(this)) {
            copy(typeScheme = true)
        } else {
            this
        }
        override fun toString(): String = "'$name"
    }

    data class FunctionType(val types: List<Type>, val typeSchemeVariables: List<TypeVariable> = emptyList()) : Type {
        override fun contains(v: TypeVariable): Boolean = types.any { it.contains(v) }
        override fun substitute(v: TypeVariable, t: Type): Type =
            FunctionType(types.map { it.substitute(v, t) }, typeSchemeVariables - v)
        override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
        override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
        override fun findTypeVariables(): List<TypeVariable> = types.flatMap { it.findTypeVariables() }
        override fun generalize(variables: List<TypeVariable>): Type = copy(typeSchemeVariables = variables)
        override fun toString(): String {
            val sb = StringBuilder()
            if (isTypeScheme()) {
                sb.append('[')
                sb.append(typeSchemeVariables.joinToString(", "))
                sb.append(']')
            }
            sb.append('(')
            sb.append(types.joinToString(" -> "))
            sb.append(')')
            return sb.toString()
        }
    }

    data class Constraint(var a: Type, var b: Type) {
        fun substitute(v: TypeVariable, t: Type) {
            a = a.substitute(v,t)
            b = b.substitute(v,t)
        }
        override fun toString(): String = "$a = $b"
    }
    data class InferenceResult(val type: Type, val constraints: Set<Constraint>, val env: Map<String, Type>)

    val intType = SimpleType("int")
    val boolType = SimpleType("bool")
    val unitType = SimpleType("unit")

    private var typeNum = 0
    private fun nextTypeVariable() = TypeVariable("t${typeNum++}")

    private fun gh.marad.chi.core.Type.toNewType(): Type {
        return when(this) {
            is AnyType -> TODO()
            is ArrayType -> TODO()
            is VariantType -> TODO()
            is FnType -> TODO()
            is GenericTypeParameter -> TypeVariable(name)
            is OverloadedFnType -> TODO()
            is PrimitiveType -> SimpleType(name)
            is StringType -> SimpleType(name)
            is UndefinedType -> TODO()
        }
    }

    fun inferTypes(env: Map<String, Type>, expr: Expression): InferenceResult {
        return when(expr) {
            is Atom -> InferenceResult(expr.type.toNewType(), emptySet(), env)
            is VariableAccess -> {
                val t = env[expr.name] ?: TODO("Type inference failed. Name ${expr.name} is not defined in.")
                InferenceResult(instantiate(t), emptySet(), env)
            }
            is NameDeclaration -> {
                val valueType = inferTypes(env, expr.value)
                val updatedEnv = generalize(valueType.constraints, env, expr.name, valueType.type)
                InferenceResult(valueType.type, valueType.constraints, updatedEnv)
            }
            is EffectDefinition -> {
                val signatureTypes = mutableListOf<Type>()
                expr.parameters.forEach {
                    signatureTypes.add(it.type.toNewType())
                }
                signatureTypes.add(expr.returnType.toNewType())
                val type = FunctionType(signatureTypes)
                val updatedEnv = generalize(emptySet(), env, expr.name, type)
                InferenceResult(type, emptySet(), updatedEnv)
            }
            is Assignment -> {
                // TODO implement value restriction
                //      i think it would be enough to update the env with more specific type
                //      that was the result of inferring the value to be assigned
                //      and then report error if we are trying to assign another type
                //      Example code:
                //        var ref : Option['a -> 'a] = None
                //        ref = Just({ i: int -> i + 1 })
                //        ref = Just({ b: bool -> !b })
                //        ref.value(5)
                //      More info: https://www.youtube.com/watch?v=6tj9WrRqPeU
                inferTypes(env, expr.value)
            }
            is Block -> {
                var currentEnv = env
                val constraints = mutableSetOf<Constraint>()
                val last = expr.body.map {
                    var result = inferTypes(currentEnv, it)
                    currentEnv = result.env
                    constraints.addAll(result.constraints)
                    result
                }.lastOrNull() ?: InferenceResult(unitType, setOf(), env)
                InferenceResult(last.type, constraints, env)
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

                InferenceResult(FunctionType(funcTypes), bodyType.constraints, env)
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

                InferenceResult(t, constraints, env)
            }
            is IfElse -> {
                val t = nextTypeVariable()
                val condType = inferTypes(env, expr.condition)
                val thenBranchType = inferTypes(env, expr.thenBranch)
                val elseBranchType = expr.elseBranch?.let { inferTypes(env, it) }
                    ?: InferenceResult(unitType, setOf(), env)

                val constraints = mutableSetOf<Constraint>()
                constraints.add(Constraint(condType.type, boolType))
                constraints.add(Constraint(t, thenBranchType.type))
                constraints.add(Constraint(t, elseBranchType.type))
                constraints.addAll(condType.constraints)
                constraints.addAll(thenBranchType.constraints)
                constraints.addAll(elseBranchType.constraints)
                InferenceResult(t, constraints, env)
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
                InferenceResult(t, constraints, env)
            }
            is Break -> InferenceResult(unitType, emptySet(), env)
            is Continue -> InferenceResult(unitType, emptySet(), env)
            is Cast -> TODO("For this to work I need the parser support - target type must be supplied in the expr")
            is DefineVariantType -> TODO("This should not be evaluated here")
            is FieldAccess -> TODO()
            is FieldAssignment -> TODO()
            is Group -> TODO()
            is Handle -> TODO()
            is Import -> TODO()
            is IndexOperator -> TODO()
            is IndexedAssignment -> TODO()
            is InterpolatedString -> TODO()
            is Is -> TODO()
            is Package -> TODO()
            is PrefixOp -> TODO()
            is Program -> TODO()
            is Return -> TODO()
            is WhileLoop -> TODO()
        }
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

    fun applySubstitution(type: Type, solutions: List<Pair<TypeVariable, Type>>): Type {
        var currentType = type
        solutions.forEach {
            currentType = currentType.substitute(it.first, it.second)
        }
        return currentType
    }

    private fun instantiate(inputType: Type): Type =
        if (inputType.isTypeScheme()) {
            val mappings = inputType.typeSchemeVariables().map { it to nextTypeVariable() }
            var current = inputType
            for ((v, t) in mappings) {
                current = current.substitute(v, t)
            }
            current
        } else {
            inputType
        }

    private fun generalize(constraints: Set<Constraint>, env: Map<String, Type>, name: String, type: Type): Map<String, Type> {
        val unified = unify(constraints)
        val typeVariablesNotToGeneralize = mutableSetOf<TypeVariable>()
        val newEnv = env.mapValues {
            val result = applySubstitution(it.value, unified)
            typeVariablesNotToGeneralize.addAll(result.findTypeVariables())
            result
        }
        val newType = applySubstitution(type, unified)
        val generalizedTypeVariables = newType.findTypeVariables().toSet() - typeVariablesNotToGeneralize
        return newEnv + (name to newType.generalize(generalizedTypeVariables.toList()))
    }

}
