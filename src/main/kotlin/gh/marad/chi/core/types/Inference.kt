package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.parser.ChiSource
import java.lang.RuntimeException


fun inferAndFillTypes(ctx: InferenceContext, env: Map<String, Type>, expr: Expression) {
    val inferred = inferTypes(ctx, env, expr)
    val solution = unify(ctx.typeGraph, inferred.constraints)
    expr.accept(TypeFiller(solution))
}

data class Constraint(
    var actual: Type,
    var expected: Type,
    val section: ChiSource.Section?,
    /// This parameter has very specific use case. It's used
    /// for FnCall type inference to convey the parameter
    /// sections, to produce meaningful errors.
    /// It's also used for GenericType inference for
    /// generic type parameters
    val paramSections: List<ChiSource.Section?>? = null
) {
    fun substitute(v: TypeVariable, t: Type) {
        actual = actual.substitute(v,t)
        expected = expected.substitute(v,t)
    }
    override fun toString(): String = "$actual = $expected"
}

data class InferenceResult(val type: Type, val constraints: Set<Constraint>, val env: Map<String, Type>)

class InferenceContext(val typeGraph: TypeGraph) {
    private var nextTypeVariableNum = 0
    fun nextTypeVariable() = TypeVariable("t${nextTypeVariableNum++}")
}

class TypeFiller(private val solution: List<Pair<TypeVariable, Type>>) : DefaultExpressionVisitor {
    override fun visit(expr: Expression) {
        assert(expr.newType != null) {
            "Expression did not have type set: $expr"
        }
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
) : RuntimeException(message + if (section != null) " at $section" else "")

internal fun inferTypes(env: Map<String, Type>, expr: Expression): InferenceResult =
    inferTypes(InferenceContext(TypeGraph()), env, expr)

internal fun inferTypes(ctx: InferenceContext, env: Map<String, Type>, expr: Expression): InferenceResult {
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
            val (updatedEnv, generalizedType) = generalize(ctx.typeGraph, valueType.constraints, env, expr.name, valueType.type)
            val constraints = if (expr.expectedType != null) {
                valueType.constraints + (Constraint(generalizedType, expr.expectedType, expr.sourceSection))
            } else {
                valueType.constraints
            }
            expr.newType = generalizedType
            InferenceResult(generalizedType, constraints, updatedEnv)
        }

        is EffectDefinition -> {
            val signatureTypes = mutableListOf<Type>()
            expr.parameters.forEach {
                signatureTypes.add(it.type!!)
            }
            signatureTypes.add(expr.returnType.toNewType())
            val type = FunctionType(signatureTypes)
            val (updatedEnv, generalizedType) = generalize(ctx.typeGraph, emptySet(), env, expr.name, type)
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
                val result = inferTypes(ctx, currentEnv, it)
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
                        it.name to it.type
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
                FunctionType(paramTypes.map { it.type } + t),
                paramSections = expr.parameters.map { it.sourceSection },
                section = expr.function.sourceSection
            ))
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
            constraints.add(Constraint(
                actual = condType.type,
                expected = Types.bool,
                section = expr.condition.sourceSection))
            if (expr.elseBranch != null) {
                constraints.add(Constraint(
                    actual = t,
                    expected = thenBranchType.type,
                    section = expr.sourceSection))
                constraints.add(
                    Constraint(t, elseBranchType.type, expr.elseBranch.sourceSection ?: expr.sourceSection)
                )
            } else {
                constraints.add(
                    Constraint(t, Types.unit, expr.sourceSection)
                )
            }
            constraints.addAll(condType.constraints)
            constraints.addAll(thenBranchType.constraints)
            constraints.addAll(elseBranchType.constraints)
            expr.newType = t
            InferenceResult(t, constraints, env)
        }

        is InfixOp -> {
            val t = ctx.nextTypeVariable()
            val left = inferTypes(ctx, env, expr.left)
            val right = inferTypes(ctx, env, expr.right)
            val constraints = mutableSetOf<Constraint>()
            when (expr.op) {
                in listOf("==", "!=", "<", ">", "<=", ">=") -> {
                    constraints.add(Constraint(t, Types.bool, expr.sourceSection))
                    constraints.add(Constraint(left.type, right.type, expr.sourceSection))
                }
                in listOf("&&", "||") -> {
                    constraints.add(Constraint(t, Types.bool, expr.sourceSection))
                    constraints.add(Constraint(left.type, Types.bool, expr.left.sourceSection))
                    constraints.add(Constraint(right.type, Types.bool, expr.right.sourceSection))
                }
                in listOf("+", "-", "*", "/") -> {
                    constraints.add(Constraint(t, left.type, expr.left.sourceSection))
                    constraints.add(Constraint(t, right.type, expr.right.sourceSection))
                }
                in listOf("%", ">>", "<<", "&", "|") -> {
                    constraints.add(Constraint(t, Types.int, expr.sourceSection))
                    constraints.add(Constraint(left.type, Types.int, expr.left.sourceSection))
                    constraints.add(Constraint(right.type, Types.int, expr.right.sourceSection))
                }
                else -> {
                    throw TypeInferenceFailed("Unknown infix operator '${expr.op}.", expr.sourceSection)
                }
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
            val inferred = inferTypes(ctx, env, expr.expression)
            expr.newType = targetType
            InferenceResult(targetType, inferred.constraints, env)
        }
        is Group -> {
            val value = inferTypes(ctx, env, expr.value)
            expr.newType = value.type
            value
        }
        is Handle -> {
            val t = ctx.nextTypeVariable()
            val constraints = mutableSetOf<Constraint>()

            val body = inferTypes(ctx, env, expr.body)
            constraints.add(Constraint(t, body.type, expr.sourceSection))
            constraints.addAll(body.constraints)

            expr.cases.forEach { handleCase ->
                val effectType = env[handleCase.effectName] ?: throw TypeInferenceFailed("Symbol ${handleCase.effectName} not found in scope.", handleCase.sourceSection)
                if (effectType is FunctionType) {
                    val effectReturnType = effectType.types.last()
                    val caseEnv = env + ("resume" to FunctionType(listOf(effectReturnType, t)))
                    val inferred = inferTypes(ctx, caseEnv, handleCase.body)
                    constraints.add(Constraint(inferred.type, t, handleCase.sourceSection))
                    constraints.addAll(inferred.constraints)
                } else {
                    throw TypeInferenceFailed("Symbol ${handleCase.effectName} has type $effectType but a function type was expected!", handleCase.sourceSection)
                }
            }

            expr.newType = t
            InferenceResult(t, constraints, env)
        }
        is PrefixOp -> {
            if (expr.op != "!") {
                throw TypeInferenceFailed("Unknown prefix operator '${expr.op}.", expr.sourceSection)
            }

            val value = inferTypes(ctx, env, expr.expr)
            val type = value.type
            expr.newType = type
            value.copy(constraints =
                value.constraints + Constraint(type, Types.bool, expr.expr.sourceSection)
            )
        }
        is IndexOperator -> {
            val base = ctx.nextTypeVariable()
            val element = ctx.nextTypeVariable()
            val t = Types.generic(base, element)

            val variableType = inferTypes(ctx, env, expr.variable)
            val indexType = inferTypes(ctx, env, expr.index)

            val constraints = mutableSetOf<Constraint>()
            constraints.add(Constraint(variableType.type, t, expr.variable.sourceSection))
            constraints.add(Constraint(indexType.type, Types.int, expr.index.sourceSection))
            constraints.addAll(variableType.constraints)
            constraints.addAll(indexType.constraints)

            expr.newType = element
            InferenceResult(element, constraints, env)
        }
        is IndexedAssignment -> {
            val base = ctx.nextTypeVariable()
            val element = ctx.nextTypeVariable()
            val t = Types.generic(base, element)

            val variableType = inferTypes(ctx, env, expr.variable)
            val indexType = inferTypes(ctx, env, expr.index)
            val valueType = inferTypes(ctx, env, expr.value)

            val constraints = mutableSetOf<Constraint>()

            constraints.add(Constraint(variableType.type, t, expr.variable.sourceSection))
            constraints.add(Constraint(indexType.type, Types.int, expr.index.sourceSection))
            constraints.add(Constraint(valueType.type, element, expr.value.sourceSection))
            constraints.addAll(variableType.constraints)
            constraints.addAll(indexType.constraints)
            constraints.addAll(valueType.constraints)

            expr.newType = element
            InferenceResult(element, constraints, env)
        }
        is InterpolatedString -> {
            val constraints = mutableSetOf<Constraint>()

            expr.parts.forEach {
                val inferred = inferTypes(ctx, env, it)
                constraints.addAll(inferred.constraints)
            }

            expr.newType = Types.string
            InferenceResult(Types.string, constraints, env)
        }
        is Is -> {
            val valueType = inferTypes(ctx, env, expr.value)
            expr.newType = Types.bool
            InferenceResult(Types.bool, valueType.constraints, env)
        }
        is Return -> {
            if (expr.value != null) {
                val inferredValue = inferTypes(ctx, env, expr.value)
                expr.newType = inferredValue.type
                inferredValue
            } else {
                expr.newType = Types.unit
                InferenceResult(Types.unit, setOf(), env)
            }
        }
        is WhileLoop -> {
            val condition = inferTypes(ctx, env, expr.condition)
            val loop = inferTypes(ctx, env, expr.loop)

            val constraints = mutableSetOf<Constraint>()
            constraints.add(Constraint(condition.type, Types.bool, expr.condition.sourceSection))
            constraints.addAll(condition.constraints)
            constraints.addAll(loop.constraints)

            expr.newType = Types.unit
            InferenceResult(Types.unit, constraints, env)
        }
        is DefineVariantType -> {
            val newEnv = env.toMutableMap()
            val base = expr.baseVariantType
            val typeSchemeVariables = base.genericTypeParameters.map { it.toNewType() as TypeVariable }
            val baseType = SimpleType(base.moduleName, base.packageName, expr.name)
            newEnv[expr.name] = baseType

            expr.constructors.forEach { constructor ->
                val type = SimpleType(base.moduleName, base.packageName, constructor.name)
                if (constructor.fields.isEmpty()) {
                    newEnv[constructor.name] = type
                } else {
                    val paramVariables = constructor.fields.map { it.type.toNewType() }
                    newEnv[constructor.name] = FunctionType(
                        paramVariables + type,
                        typeSchemeVariables.filter { paramVariables.contains(it) }
                    )
                }
            }

            expr.newType = Types.unit
            InferenceResult(Types.unit, setOf(), newEnv)
        }
        is FieldAccess -> {
            val t = ctx.nextTypeVariable()
            val receiverInferred = inferTypes(ctx, env, expr.receiver)
            expr.newType = t
            InferenceResult(t, receiverInferred.constraints, env)
        }
        is FieldAssignment -> {
            val t = ctx.nextTypeVariable()
            val receiverInferred = inferTypes(ctx, env, expr.receiver)
            val valueInferred = inferTypes(ctx, env, expr.value)
            expr.newType = t
            InferenceResult(t, receiverInferred.constraints + valueInferred.constraints, env)
        }
    }
}

fun unify(typeGraph: TypeGraph, constraints: Set<Constraint>): List<Pair<TypeVariable, Type>> {
    var q = ArrayDeque(constraints)
    val substitutions = mutableListOf<Pair<TypeVariable, Type>>()

    while (q.isNotEmpty()) {
        val (a, b, section, paramSections) = q.removeFirst()
//            println("$a = $b")
//            println(q)
        if (a == b) {
            // this is nothing interesting
            continue
        } else if (a is SimpleType && b is SimpleType) {
            val typesAreRelated = typeGraph.isSubtype(a.toString(), b.toString())
                    || typeGraph.isSubtype(b.toString(), a.toString())
            if (!typesAreRelated) {
                throw TypeInferenceFailed(
                    "Expected type was '$a' but got '$b'",
                    section
                )
            }
        } else if (a is FunctionType && b is FunctionType) {
            val aHead = a.types.first()
            val bHead = b.types.first()
            val headSection = if (paramSections != null && paramSections.firstOrNull() != null) {
                paramSections.first()
            } else {
                section
            }
            q.add(Constraint(aHead, bHead, headSection))

            val aTail = a.types.drop(1).let { if (it.size == 1) it[0] else FunctionType(it) }
            val bTail = b.types.drop(1).let { if (it.size == 1) it[0] else FunctionType(it) }
            if (paramSections != null && paramSections.size == 2) {
                // after taking one for head there is only single type left
                // so aTail and bTail are going to be simple types (not FunctionType)
                // so we can simply take the last section as
                q.add(Constraint(aTail, bTail, paramSections.last()))
            } else {
                q.add(Constraint(aTail, bTail, section, paramSections?.drop(1)))
            }
        } else if (a is GenericType && b is GenericType) {
            val aHead = a.types.first()
            val bHead = b.types.first()
            val headSection = if (paramSections != null && paramSections.firstOrNull() != null) {
                paramSections.first()
            } else {
                section
            }
            q.add(Constraint(aHead, bHead, headSection))

            val aTail = a.types.drop(1).let { if (it.size == 1) it[0] else GenericType(it) }
            val bTail = b.types.drop(1).let { if (it.size == 1) it[0] else GenericType(it) }
            if (paramSections != null && paramSections.size == 2) {
                // after taking one for head there is only single type left
                // so aTail and bTail are going to be simple types (not FunctionType)
                // so we can simply take the last section as
                q.add(Constraint(aTail, bTail, paramSections.last()))
            } else {
                q.add(Constraint(aTail, bTail, section, paramSections?.drop(1)))
            }
        } else if (a is TypeVariable) {
            if (b.contains(a)) {
                throw TypeInferenceFailed("$a is contained in $b", section)
            }
            q.forEach { it.substitute(a, b) }
            substitutions.add(a to b)
        } else if (b is TypeVariable) {
            if (a.contains(b)) {
                throw TypeInferenceFailed("$a is contained in $b", section)
            }
            q.forEach { it.substitute(b, a) }
            substitutions.add(b to a)
        } else {
            throw TypeInferenceFailed(
                "Expected type was $b but got $a",
                section
            )
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
    typeGraph: TypeGraph,
    constraints: Set<Constraint>,
    env: Map<String, Type>,
    name: String,
    type: Type
): Pair<Map<String, Type>, Type> {
    val unified = unify(typeGraph, constraints)
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

internal fun OldType.toNewType(): Type {
    return when (this) {
        is AnyType -> Types.any
        is ArrayType -> Types.array(this.elementType.toNewType())
        is VariantType -> {
            val variant = this.variant
            // FIXME - handle generics
            val type = if (variant != null) {
                SimpleType(moduleName, packageName, variant.variantName)
            } else {
                SimpleType(moduleName, packageName, simpleName)
            }
            type
        }
        is FnType -> {
            val types = mutableListOf<Type>()
            types.addAll(this.paramTypes.map { it.toNewType() })
            types.add(this.returnType.toNewType())
            Types.fn(*types.toTypedArray())
        }
        is GenericTypeParameter -> TypeVariable(name)
        is OverloadedFnType -> TODO()
        is PrimitiveType -> SimpleType(moduleName, packageName, name)
        is StringType -> Types.string
        is UndefinedType -> TODO()
    }
}