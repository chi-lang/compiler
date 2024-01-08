package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CompilerMessageException
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.ChiSource


fun inferAndFillTypes(ctx: InferenceContext, env: InferenceEnv, expr: Expression) {
    val inferred = inferTypes(ctx, env, expr)
    val solution = unify(inferred.constraints)
    TypeFiller(solution).visit(expr)
}


internal fun inferTypes(env: InferenceEnv, expr: Expression): InferenceResult {
    val ns = GlobalCompilationNamespace()
    return inferTypes(InferenceContext(ns, TypeLookupTable(ns)), env, expr)
}

internal fun inferTypes(ctx: InferenceContext, env: InferenceEnv, expr: Expression): InferenceResult {
    return when (expr) {
        is Atom -> {
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(expr.newType!!, emptySet())
        }
        is VariableAccess -> {
//            val t = env[expr.target.name] ?: throw TypeInferenceFailed("Symbol ${expr.target.name} not found in scope.", expr.sourceSection)
            val t = env.getType(expr.target, expr.sourceSection)
            val finalType = instantiate(ctx, t)
            expr.newType = finalType
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(finalType, emptySet())
        }

        is NameDeclaration -> {
            val valueType = inferTypes(ctx, env, expr.value)
            val generalizedType = generalize(valueType.constraints, env, expr.name, valueType.type)
            val constraints = if (expr.expectedType != null) {
                expr.newType = expr.expectedType
                valueType.constraints + (Constraint(generalizedType, expr.expectedType, expr.sourceSection))
            } else {
                expr.newType = generalizedType
                valueType.constraints
            }
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(generalizedType, constraints)
        }

        is EffectDefinition -> {
            if (expr.newType != null) {
                val type = expr.newType!!
                type.sourceSection = expr.sourceSection
                env.setType(expr.name, type)
                InferenceResult(type, emptySet())
            } else {
                val t = ctx.nextTypeVariable()
                val generalizedType = generalize(emptySet(), env, expr.name, t)
                generalizedType.sourceSection = expr.sourceSection
                expr.newType = generalizedType
                InferenceResult(generalizedType, emptySet())
            }
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
            expr.newType?.sourceSection = expr.value.sourceSection
            result
        }

        is Block -> {
            val constraints = mutableSetOf<Constraint>()
            val last = expr.body.map {
                val result = inferTypes(ctx, env, it)
                constraints.addAll(result.constraints)
                result
            }.lastOrNull() ?: InferenceResult(Types.unit, setOf())
            expr.newType = last.type
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(last.type, constraints)
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

            val bodyType = env.withNewLocalEnv {
                paramNamesAndTypes.forEach { (name, type) ->
                    env.setType(name, type)
                }
                inferTypes(ctx, env, expr.body)
            }

            val funcTypes = paramNamesAndTypes.map { it.second }.toMutableList()
            funcTypes.add(bodyType.type)

            val inferredType = FunctionType(funcTypes)
            expr.newType = inferredType
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(inferredType, bodyType.constraints)
        }

        is FnCall -> {
            val t = ctx.nextTypeVariable()

            val funcType = inferTypes(ctx, env, expr.function)
            val paramTypes = expr.parameters.map { inferTypes(ctx, env, it) }

            val constraints = mutableSetOf<Constraint>()
            constraints.add(Constraint(
                actual = FunctionType(paramTypes.map { it.type } + t),
                expected = funcType.type,
                paramSections = expr.parameters.map { it.sourceSection },
                section = expr.function.sourceSection
            ))
            constraints.addAll(funcType.constraints)
            paramTypes.forEach { constraints.addAll(it.constraints) }

            expr.newType = t
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }

        is IfElse -> {
            // TODO: wymagania powinny zależeć od tego czy ten expr
            //       jest używany jako wyrażenie czy nie
            //       jeśli nie to jego typem powinien być po prostu unit

            val t = ctx.nextTypeVariable()
            val condType = inferTypes(ctx, env, expr.condition)
            val thenBranchType = inferTypes(ctx, env, expr.thenBranch)
            val elseBranchType = expr.elseBranch?.let { inferTypes(ctx, env, it) }
                ?: InferenceResult(Types.unit, setOf())

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
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }

        is InfixOp -> {
            val t = ctx.nextTypeVariable()
            val left = inferTypes(ctx, env, expr.left)
            val right = inferTypes(ctx, env, expr.right)
            val constraints = mutableSetOf<Constraint>()
            when (expr.op) {
                in listOf("==", "!=", "<", ">", "<=", ">=") -> {
                    constraints.add(Constraint(t, Types.bool, expr.sourceSection))
                    constraints.add(Constraint(right.type, left.type, expr.sourceSection))
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
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }

        is Break -> {
            expr.newType = Types.unit
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, emptySet())
        }
        is Continue -> {
            expr.newType = Types.unit
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, emptySet())
        }
        is Cast -> {
            val inferred = inferTypes(ctx, env, expr.expression)
            expr.newType = expr.targetType
            expr.newType?.sourceSection = expr.sourceSection
            if (expr.expression is VariableAccess) {
                env.setType(expr.expression.target.name, expr.targetType)
            }
            InferenceResult(expr.newType!!, inferred.constraints)
        }
        is Group -> {
            val value = inferTypes(ctx, env, expr.value)
            expr.newType = value.type
            expr.newType?.sourceSection = expr.sourceSection
            value
        }
        is Handle -> {
            val t = ctx.nextTypeVariable()
            val constraints = mutableSetOf<Constraint>()

            val body = inferTypes(ctx, env, expr.body)
            constraints.add(Constraint(t, body.type, expr.sourceSection))
            constraints.addAll(body.constraints)

            expr.cases.forEach { handleCase ->
//                val effectType = env[handleCase.effectName] ?: throw TypeInferenceFailed("Symbol ${handleCase.effectName} not found in scope.", handleCase.sourceSection)
                val effectType = env.getType(handleCase.effectName, handleCase.sourceSection)
                if (effectType is FunctionType) {
                    env.withNewLocalEnv {
                        handleCase.argumentNames.zip(effectType.types.dropLast(1)).forEach { (name, type) ->
                            env.setType(name, type)
                        }
                        val effectReturnType = effectType.types.last()
                        env.setType("resume", FunctionType(listOf(effectReturnType, t)))
                        val inferred = inferTypes(ctx, env, handleCase.body)
                        constraints.add(Constraint(inferred.type, t, handleCase.sourceSection))
                        constraints.addAll(inferred.constraints)
                    }
                } else {
                    throw TypeInferenceFailed("Symbol ${handleCase.effectName} has type $effectType but a function type was expected!", handleCase.sourceSection)
                }
            }

            expr.newType = t
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }
        is PrefixOp -> {
            if (expr.op != "!") {
                throw TypeInferenceFailed("Unknown prefix operator '${expr.op}.", expr.sourceSection)
            }

            val value = inferTypes(ctx, env, expr.expr)
            val type = value.type
            expr.newType = type
            expr.newType?.sourceSection = expr.sourceSection
            value.copy(constraints =
                value.constraints + Constraint(type, Types.bool, expr.expr.sourceSection)
            )
        }
        is IndexOperator -> {
            val element = ctx.nextTypeVariable()
            val t = Types.array(element)
            t.sourceSection = expr.sourceSection

            val variableType = inferTypes(ctx, env, expr.variable)
            val indexType = inferTypes(ctx, env, expr.index)

            val constraints = mutableSetOf<Constraint>()
            constraints.add(Constraint(variableType.type, t, expr.variable.sourceSection))
            constraints.add(Constraint(indexType.type, Types.int, expr.index.sourceSection))
            constraints.addAll(variableType.constraints)
            constraints.addAll(indexType.constraints)

            expr.newType = element
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(element, constraints)
        }
        is IndexedAssignment -> {
            val element = ctx.nextTypeVariable()
            val t = Types.array(element)

            val variableType = inferTypes(ctx, env, expr.variable)
            val indexType = inferTypes(ctx, env, expr.index)
            val valueType = inferTypes(ctx, env, expr.value)

            val constraints = mutableSetOf<Constraint>()

            constraints.add(Constraint(indexType.type, Types.int, expr.index.sourceSection))
            constraints.add(Constraint(valueType.type, element, expr.value.sourceSection))
            constraints.add(Constraint(variableType.type, t, expr.variable.sourceSection))
            constraints.addAll(variableType.constraints)
            constraints.addAll(indexType.constraints)
            constraints.addAll(valueType.constraints)

            expr.newType = element
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(element, constraints)
        }
        is InterpolatedString -> {
            val constraints = mutableSetOf<Constraint>()

            expr.parts.forEach {
                val inferred = inferTypes(ctx, env, it)
                constraints.addAll(inferred.constraints)
            }

            expr.newType = Types.string
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(Types.string, constraints)
        }
        is Is -> {
            val valueType = inferTypes(ctx, env, expr.value)
            expr.newType = Types.bool
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(Types.bool, valueType.constraints)
        }
        is Return -> {
            if (expr.value != null) {
                val inferredValue = inferTypes(ctx, env, expr.value)
                expr.newType = inferredValue.type
                expr.newType?.sourceSection = expr.sourceSection
                inferredValue
            } else {
                expr.newType = Types.unit
                expr.newType?.sourceSection = expr.sourceSection
                InferenceResult(Types.unit, setOf())
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
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, constraints)
        }
        is DefineVariantType -> {
//            val newEnv = env.toMutableMap()
//            val base = expr.baseVariantType
//            val typeSchemeVariables = base.genericTypeParameters.map { it.toNewType() as TypeVariable }
//            val baseType = SimpleType(base.moduleName, base.packageName, expr.name)
//            newEnv[expr.name] = baseType
//
//            expr.constructors.forEach { constructor ->
//                val type = SimpleType(base.moduleName, base.packageName, constructor.name)
//                if (constructor.fields.isEmpty()) {
//                    newEnv[constructor.name] = type
//                } else {
//                    val paramVariables = constructor.fields.map { it.type.toNewType() }
//                    newEnv[constructor.name] = FunctionType(
//                        paramVariables + type,
//                        typeSchemeVariables.filter { paramVariables.contains(it) }
//                    )
//                }
//            }
            expr.newType = Types.unit
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, setOf(),)
        }
        is FieldAccess -> {
            val receiverInferred = inferTypes(ctx, env, expr.receiver)
            val solution = unify(receiverInferred.constraints)
            val receiverType = applySubstitution(receiverInferred.type, solution)

            val typeInfo = ctx.typeTable.find(receiverType)
                ?: throw TypeInferenceFailed("Unknown type $receiverType", expr.receiver.sourceSection)

            val field = typeInfo.fields.firstOrNull { it.name == expr.fieldName }
                ?: throw CompilerMessageException(MemberDoesNotExist(
                    receiverType, expr.fieldName, expr.memberSection.toCodePoint()))

            expr.newType = field.type
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(field.type, receiverInferred.constraints)
        }
        is FieldAssignment -> {
            val t = ctx.nextTypeVariable()
            val receiverInferred = inferTypes(ctx, env, expr.receiver)
            val valueInferred = inferTypes(ctx, env, expr.value)
            expr.newType = t
            expr.newType?.sourceSection = expr.sourceSection
            InferenceResult(t, receiverInferred.constraints + valueInferred.constraints)
        }
    }
}

fun unify(constraints: Set<Constraint>): List<Pair<TypeVariable, Type>> {
    var q = ArrayDeque(constraints)
    val substitutions = mutableListOf<Pair<TypeVariable, Type>>()

    while (q.isNotEmpty()) {
        val constraint = q.removeLast()

        val (a, e, section, paramSections, history) = constraint
        if (a == e) {
            // this is nothing interesting
            continue
        } else if (a is SimpleType && e is SimpleType) {
            if (e == Types.any) {
                continue
            }
            typeMismatch(expected = e, actual = a, section = section, history)
        } else if (a is FunctionType && e is FunctionType) {
            val aHead = a.types.first()
            val eHead = e.types.first()

            if (e.types.size == 1 && eHead == Types.unit && aHead !is TypeVariable) {
                // if e.types.size == 1 then we are checking the return values
                // if expected type (eHead) is unit we don't need to check further
                // we accept any type returned - the value will be ignored
                // We also check if the actual type (aHead) is not a type variable
                // If it is, then we would want to resolve it to 'unit' so we don't continue
                continue
            }

            val headSection = if (paramSections != null && paramSections.firstOrNull() != null) {
                paramSections.first()
            } else {
                section
            }
            q.add(Constraint(aHead, eHead, headSection, history = history + constraint))

            val aTail = a.copy(types = a.types.drop(1))
            val eTail = a.copy(types = e.types.drop(1))
            if (aTail.types.isEmpty() || eTail.types.isEmpty()) {
                continue
            }

            if (paramSections != null && paramSections.size == 2) {
                // after taking one for head there is only single type left
                // so aTail and bTail are going to be simple types (not FunctionType)
                // so we can simply take the last param section as source section
                q.add(Constraint(aTail, eTail, paramSections.last(), history = history + constraint))
            } else {
                q.add(Constraint(aTail, eTail, section, paramSections?.drop(1), history = history + constraint))
            }
        } else if (a is TypeVariable) {
            if (e.contains(a)) {
                throw TypeInferenceFailed("$a is contained in $e", section)
            }
            q.forEach { it.substitute(a, e) }
            substitutions.add(a to e)
        } else if (e is TypeVariable) {
            if (a.contains(e)) {
                throw TypeInferenceFailed("$a is contained in $e", section)
            }
            q.forEach { it.substitute(e, a) }
            substitutions.add(e to a)
        } else if (e is SumType && a is SimpleType) {
            if (e.moduleName == a.moduleName &&
                e.packageName == a.packageName &&
                e.subtypes.contains(a.name)
            ) {
                continue
            } else {
                typeMismatch(expected = e, actual = a, section, history)
            }
        } else if (e is SumType && a is ProductType) {
            if (e.moduleName == a.moduleName &&
                e.packageName == a.packageName &&
                e.subtypes.contains(a.name)
            ) {
                e.typeParams.zip(a.typeParams).forEach { (expected, actual) ->
                    q.add(
                        Constraint(
                            expected = expected,
                            actual = actual,
                            section = section,
                            history = history + constraint
                        )
                    )
                }
            } else {
                typeMismatch(expected = e, actual = a, section, history)
            }
        } else if (e is ProductType && a is ProductType) {
            if (e.moduleName == a.moduleName &&
                e.packageName == a.packageName &&
                e.name == a.name &&
                e.typeParams.size == a.typeParams.size
            ) {

                e.typeParams.zip(a.typeParams).forEach { (expected, actual) ->
                    q.add(Constraint(actual, expected, section, paramSections, history = history + constraint))
                }
            } else {
                typeMismatch(expected = e, actual = a, section, history)
            }
        } else if (e is SumType && a is SumType) {
            TODO("Not sure if this ever happens!")
            // if module, package or name different - fail
            // paramTypes should be equal
            // zip param types together and create new constraints
        } else {
            typeMismatch(expected = e, actual = a, section = section, history)
        }
    }

    return substitutions
}

fun typeMismatch(expected: Type, actual: Type, section: ChiSource.Section?, history: List<Constraint>) {
    val section = actual.sourceSection ?: section
    throw CompilerMessageException(TypeMismatch(
        expected = expected,
        actual = actual,
        codePoint = section.toCodePoint()))
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
    env: InferenceEnv,
    name: String,
    type: Type
): Type {
    val unified = unify(constraints)
    env.applySubstitutionToAllTypes(unified)
    val typeVariablesNotToGeneralize = env.findAllTypeVariables()
    val newType = applySubstitution(type, unified)
    val generalizedTypeVariables = newType.findTypeVariables().toSet() - typeVariablesNotToGeneralize.toSet()
    val generalizedType = newType.generalize(generalizedTypeVariables.toList())
    env.setType(name, generalizedType)
    return generalizedType
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