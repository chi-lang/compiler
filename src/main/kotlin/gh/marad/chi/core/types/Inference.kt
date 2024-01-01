package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.parser.ChiSource
import java.lang.RuntimeException


fun inferAndFillTypes(env: Map<String, Type>, expr: Expression) {
    val inferred = inferTypes(env, expr)
    val solution = unify(inferred.constraints)
    expr.accept(TypeFiller(solution))
}

data class Constraint(var a: Type, var b: Type) {
    fun substitute(v: TypeVariable, t: Type) {
        a = a.substitute(v,t)
        b = b.substitute(v,t)
    }
    override fun toString(): String = "$a = $b"
}

data class InferenceResult(val type: Type, val constraints: Set<Constraint>, val env: Map<String, Type>)

internal class InferenceContext {
    val typeGraph = TypeGraph().apply {
        addType("any")
        addSubtype("any", "@number")
        addSubtype("@number", "int")
        addSubtype("@number", "float")
        addSubtype("any", "string")
        addSubtype("any", "bool")
        addSubtype("any", "unit")
    }

    private var nextTypeVariableNum = 0
    fun nextTypeVariable() = TypeVariable("t${nextTypeVariableNum++}")
}

class TypeFiller(private val solution: List<Pair<TypeVariable, Type>>) : DefaultExpressionVisitor {
    override fun visit(expr: Expression) {
        expr.newType = applySubstitution(expr.newType!!, solution)
        val startLetter = 'a'.code
        expr.newType!!.typeSchemeVariables().distinctBy { it.name }.sortedBy { it.name }
            .forEachIndexed { i, v ->
                val newName = TypeVariable(Char(startLetter + i).toString())
                expr.accept(SubstituteTypeVariable(v, newName))
            }
    }
}

class SubstituteTypeVariable(private val v: TypeVariable, private val t: Type) : DefaultExpressionVisitor {
    override fun visit(expr: Expression) {
        expr.newType = expr.newType?.substitute(v, t)
    }
}


class TypeInferenceFailed(
    message: String,
    val section: ChiSource.Section?
) : RuntimeException(message)

internal fun inferTypes(env: Map<String, Type>, expr: Expression): InferenceResult =
    inferTypes(InferenceContext(), env, expr)

private fun inferTypes(ctx: InferenceContext, env: Map<String, Type>, expr: Expression): InferenceResult {
    return when (expr) {
        is Atom -> {
            expr.newType = expr.type.toNewType()
            InferenceResult(expr.newType!!, emptySet(), env)
        }
        is VariableAccess -> {
            val t = env[expr.name] ?: throw TypeInferenceFailed("Symbol ${expr.name} not found in scope.", expr.sourceSection)
            expr.newType = t
            InferenceResult(instantiate(ctx, t), emptySet(), env)
        }

        is NameDeclaration -> {
            val valueType = inferTypes(ctx, env, expr.value)
            val (updatedEnv, generalizedType) = generalize(valueType.constraints, env, expr.name, valueType.type)
            expr.newType = generalizedType
            InferenceResult(generalizedType, valueType.constraints, updatedEnv)
        }

        is EffectDefinition -> {
            val signatureTypes = mutableListOf<Type>()
            expr.parameters.forEach {
                signatureTypes.add(it.type.toNewType())
            }
            signatureTypes.add(expr.returnType.toNewType())
            val type = FunctionType(signatureTypes)
            val (updatedEnv, generalizedType) = generalize(emptySet(), env, expr.name, type)
            expr.newType = generalizedType
            InferenceResult(generalizedType, emptySet(), updatedEnv)
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
            val result = inferTypes(ctx, env, expr.value)
            expr.newType = result.type
            result
        }

        is Block -> {
            var currentEnv = env
            val constraints = mutableSetOf<Constraint>()
            val last = expr.body.map {
                var result = inferTypes(ctx, currentEnv, it)
                currentEnv = result.env
                constraints.addAll(result.constraints)
                result
            }.lastOrNull() ?: InferenceResult(Types.unit, setOf(), env)
            expr.newType = last.type
            InferenceResult(last.type, constraints, env)
        }

        is Fn -> {
            val paramNamesAndTypes: List<Pair<String, Type>> =
                expr.parameters.map {
                    // if param types were optional we would have to generate
                    // new types for them and normally solve with constraints
                    if (it.type != null) {
                        it.name to it   .type.toNewType()
                    } else {
                        it.name to ctx.nextTypeVariable()
                    }
                }

            val extEnv = mutableMapOf<String, Type>()
            extEnv.putAll(env)
            extEnv.putAll(paramNamesAndTypes)

            val bodyType = inferTypes(ctx, extEnv, expr.body)

            val funcTypes = paramNamesAndTypes.map { it.second }.toMutableList()
            funcTypes.add(bodyType.type)

            val inferredType = FunctionType(funcTypes)
            expr.newType = inferredType
            InferenceResult(inferredType, bodyType.constraints, env)
        }

        is FnCall -> {
            val t = ctx.nextTypeVariable()

            val funcType = inferTypes(ctx, env, expr.function)
            val paramTypes = expr.parameters.map { inferTypes(ctx, env, it) }

            val constraints = mutableSetOf<Constraint>()
            constraints.add(Constraint(
                funcType.type,
                FunctionType(paramTypes.map { it.type } + t)))
            constraints.addAll(funcType.constraints)
            paramTypes.forEach { constraints.addAll(it.constraints) }

            expr.newType = t
            InferenceResult(t, constraints, env)
        }

        is IfElse -> {
            // TODO: wymagania powinny zależeć od tego czy ten expr
            //       jest używany jako wyrażenie czy nie
            //       jeśli nie to jego typem powinien być po prostu unit

            val t = ctx.nextTypeVariable()
            val condType = inferTypes(ctx, env, expr.condition)
            val thenBranchType = inferTypes(ctx, env, expr.thenBranch)
            val elseBranchType = expr.elseBranch?.let { inferTypes(ctx, env, it) }
                ?: InferenceResult(Types.unit, setOf(), env)

            val constraints = mutableSetOf<Constraint>()
            constraints.add(Constraint(condType.type, Types.bool))
            constraints.add(Constraint(t, thenBranchType.type))
//            constraints.add(Constraint(t, elseBranchType.type))
            constraints.addAll(condType.constraints)
            constraints.addAll(thenBranchType.constraints)
            constraints.addAll(elseBranchType.constraints)
            // FIXME: nie wiem czy to nie zepsuje inferencji
            expr.newType = t
            InferenceResult(t, constraints, env)
        }

        is InfixOp -> {
            val t = ctx.nextTypeVariable()
            val left = inferTypes(ctx, env, expr.left)
            val right = inferTypes(ctx, env, expr.right)
            val constraints = mutableSetOf<Constraint>()
            if (expr.op in listOf("==", "!=", "<", ">", "<=", ">=", "&&", "||") ) {
                constraints.add(Constraint(t, Types.bool))
            } else {
                constraints.add(Constraint(t, left.type))
                constraints.add(Constraint(t, right.type))
            }
            constraints.addAll(left.constraints)
            constraints.addAll(right.constraints)
            expr.newType = t
            InferenceResult(t, constraints, env)
        }

        is Break -> {
            expr.newType = Types.unit
            InferenceResult(Types.unit, emptySet(), env)
        }
        is Continue -> {
            expr.newType = Types.unit
            InferenceResult(Types.unit, emptySet(), env)
        }
        is Cast -> {
            val targetType = expr.targetType.toNewType()
            expr.newType = targetType
            InferenceResult(targetType, emptySet(), env)
        }
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
                TODO("Inference failed - $a is contained in $b")
            }
            q.forEach { it.substitute(a, b) }
            substitutions.add(a to b)
        } else if (b is TypeVariable) {
            if (a.contains(b)) {
                TODO("Inference failed - $b is contained in $a")
            }
            q.forEach { it.substitute(b, a) }
            substitutions.add(b to a)
        } else {
            TODO("FAIL - cannot solve: $a = $b")
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

private fun instantiate(ctx: InferenceContext, inputType: Type): Type =
    if (inputType.isTypeScheme()) {
        val mappings = inputType.typeSchemeVariables().map { it to ctx.nextTypeVariable() }
        inputType.instantiate(mappings)
    } else {
        inputType
    }

private fun generalize(
    constraints: Set<Constraint>,
    env: Map<String, Type>,
    name: String,
    type: Type
): Pair<Map<String, Type>, Type> {
    val unified = unify(constraints)
    val typeVariablesNotToGeneralize = mutableSetOf<TypeVariable>()
    val newEnv = env.mapValues {
        val result = applySubstitution(it.value, unified)
        typeVariablesNotToGeneralize.addAll(result.findTypeVariables())
        result
    }
    val newType = applySubstitution(type, unified)
    val generalizedTypeVariables = newType.findTypeVariables().toSet() - typeVariablesNotToGeneralize
    val generalizedType = newType.generalize(generalizedTypeVariables.toList())
    return Pair(newEnv + (name to generalizedType), generalizedType)
}

private fun OldType.toNewType(): Type {
    return when (this) {
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