package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CompilerMessage
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
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(expr.type!!, emptySet())
        }
        is VariableAccess -> {
            val t = env.getType(expr.target, expr.sourceSection)
            val finalType = instantiate(ctx, t)
            expr.type = finalType
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(finalType, emptySet())
        }

        is NameDeclaration -> {
            val valueType = inferTypes(ctx, env, expr.value)
            val generalizedType = generalize(valueType.constraints, env, expr.name, valueType.type)
            val constraints = if (expr.expectedType != null) {
                expr.type = expr.expectedType
                valueType.constraints + (Constraint(generalizedType, expr.expectedType, expr.sourceSection))
            } else {
                expr.type = generalizedType
                valueType.constraints
            }
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(generalizedType, constraints)
        }

        is EffectDefinition -> {
            if (expr.type != null) {
                val type = expr.type!!
                type.sourceSection = expr.sourceSection
                env.setType(expr.name, type)
                InferenceResult(type, emptySet())
            } else {
                val t = ctx.nextTypeVariable()
                val generalizedType = generalize(emptySet(), env, expr.name, t)
                generalizedType.sourceSection = expr.sourceSection
                expr.type = generalizedType
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
            expr.type = result.type
            expr.type?.sourceSection = expr.value.sourceSection
            result
        }

        is Block -> {
            val constraints = mutableSetOf<Constraint>()
            val last = expr.body.map {
                val result = inferTypes(ctx, env, it)
                constraints.addAll(result.constraints)
                result
            }.lastOrNull() ?: InferenceResult(Types.unit, setOf())
            expr.type = last.type
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(last.type, constraints)
        }

        is Fn -> {
            val paramNamesAndTypes: List<Pair<String, Type>> =
                expr.parameters.map {
                    // if param types were optional we would have to generate
                    // new types for them and normally solve with constraints
                    val type = it.type
                    if (type != null) {
                        it.name to type
                    } else {
                        val type = ctx.nextTypeVariable()
                        it.type = type
                        it.name to type
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
            expr.type = inferredType
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(inferredType, bodyType.constraints)
        }

        is FnCall -> {
            val t = ctx.nextTypeVariable()

            val funcType = inferTypes(ctx, env, expr.function)

            // Rewrite the AST when field is actually a function that we want to call on a receiver
            if (expr.function is FieldAccess) {
                val dotOp = expr.function as FieldAccess
                val target = dotOp.target!!
                expr.function = when (target) {
                    DotTarget.Field -> dotOp
                    DotTarget.LocalFunction -> {
                        expr.parameters.add(0, dotOp.receiver)
                        VariableAccess(LocalSymbol(dotOp.fieldName), dotOp.memberSection).also {
                            it.type = dotOp.type
                        }
                    }
                    is DotTarget.PackageFunction -> {
                        expr.parameters.add(0, dotOp.receiver)
                        VariableAccess(PackageSymbol(
                            target.moduleName, target.packageName, target.name
                        ), dotOp.memberSection).also {
                            it.type = dotOp.type
                        }
                    }
                }
            }


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

            expr.type = t
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }

        is IfElse -> {
            val condType = inferTypes(ctx, env, expr.condition)
            val thenBranchType = inferTypes(ctx, env, expr.thenBranch)
            val elseBranchType = expr.elseBranch?.let { inferTypes(ctx, env, it) }
                ?: InferenceResult(Types.unit, setOf())

            val condConstraint = Constraint(actual = condType.type, expected = Types.bool, section = expr.condition.sourceSection)
            val allConstraints = condType.constraints + thenBranchType.constraints + elseBranchType.constraints + condConstraint
            val solution = unify(allConstraints)
            val thenType = applySubstitution(thenBranchType.type, solution)
            val elseType = applySubstitution(elseBranchType.type, solution)

            var finalType: Type? = null

            if (expr.elseBranch == null) {
                finalType = Types.unit // if without else branch has unit type
            } else if (thenType == elseType) {
                finalType = thenType // when types are the same - no problem
            } else if (Types.isSubtype(thenType, elseType)) {
                finalType = thenType // then type is broader
            } else if (Types.isSubtype(elseType, thenType)) {
                finalType = elseType // else type is broader
            } else if (thenType != elseType) {
                finalType = Types.any // when types are not related - if returns any
            }

            if (finalType == null) {
                finalType = ctx.nextTypeVariable() // safety measure, probably not crucial
            }

            expr.type = finalType
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(expr.type!!, allConstraints)
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
            expr.type = t
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }

        is Break -> {
            expr.type = Types.unit
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, emptySet())
        }
        is Continue -> {
            expr.type = Types.unit
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, emptySet())
        }
        is Cast -> {
            val inferred = inferTypes(ctx, env, expr.expression)
            expr.type = expr.targetType
            expr.type?.sourceSection = expr.sourceSection
            if (expr.expression is VariableAccess) {
                env.setType(expr.expression.target.name, expr.targetType)
            }
            InferenceResult(expr.type!!, inferred.constraints)
        }
        is Handle -> {
            val t = ctx.nextTypeVariable()
            val constraints = mutableSetOf<Constraint>()

            val body = inferTypes(ctx, env, expr.body)
            constraints.add(Constraint(t, body.type, expr.sourceSection))
            constraints.addAll(body.constraints)

            expr.cases.forEach { handleCase ->
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

            expr.type = t
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(t, constraints)
        }
        is PrefixOp -> {
            if (expr.op != "!") {
                throw TypeInferenceFailed("Unknown prefix operator '${expr.op}.", expr.sourceSection)
            }

            val value = inferTypes(ctx, env, expr.expr)
            val type = value.type
            expr.type = type
            expr.type?.sourceSection = expr.sourceSection
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

            expr.type = element
            expr.type?.sourceSection = expr.sourceSection
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

            expr.type = element
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(element, constraints)
        }
        is InterpolatedString -> {
            val constraints = mutableSetOf<Constraint>()

            expr.parts.forEach {
                val inferred = inferTypes(ctx, env, it)
                constraints.addAll(inferred.constraints)
            }

            expr.type = Types.string
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(Types.string, constraints)
        }
        is Is -> {
            val valueType = inferTypes(ctx, env, expr.value)
            expr.type = Types.bool
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(Types.bool, valueType.constraints)
        }
        is Return -> {
            if (expr.value != null) {
                val inferredValue = inferTypes(ctx, env, expr.value)
                expr.type = inferredValue.type
                expr.type?.sourceSection = expr.sourceSection
                inferredValue
            } else {
                expr.type = Types.unit
                expr.type?.sourceSection = expr.sourceSection
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

            expr.type = Types.unit
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(Types.unit, constraints)
        }
        is FieldAccess -> {
            val receiverInferred = inferTypes(ctx, env, expr.receiver)
            val solution = unify(receiverInferred.constraints)
            val receiverType = applySubstitution(receiverInferred.type, solution)

            // try to find field
            val typeInfo = ctx.typeTable.find(receiverType)
            val field = typeInfo?.fields?.firstOrNull { it.name == expr.fieldName }
            if (field != null) {
                expr.target = DotTarget.Field
                expr.type = field.type
            } else {
                // then try to find a function in current local scope
                val funcType = env.getTypeOrNull(expr.fieldName)
                if (funcType != null && funcType is FunctionType && funcType.types.size >= 2 && funcType.types.first() == receiverType) {
                    expr.target = DotTarget.LocalFunction
                    expr.type = funcType
                } else {
                    // finally try to find a function in receiver types package
                    val typePkg = ctx.getTypePackageOrNull(receiverType)
                    val symbol = typePkg?.symbols?.get(expr.fieldName)
                    val symbolType = symbol?.type
                    if (symbolType is FunctionType && symbolType.types.size >= 2 && symbolType.types.first() == receiverType) {
                        expr.target = DotTarget.PackageFunction(symbol.moduleName, symbol.packageName, symbol.name)
                        expr.type = symbolType
                    } else {
                        throw CompilerMessage(
                            MemberDoesNotExist(
                                receiverType, expr.fieldName, expr.memberSection.toCodePoint()
                            )
                        )
                    }
                }
            }

            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(expr.type!!, receiverInferred.constraints)
        }
        is FieldAssignment -> {
            val t = ctx.nextTypeVariable()
            val receiverInferred = inferTypes(ctx, env, expr.receiver)
            val valueInferred = inferTypes(ctx, env, expr.value)
            expr.type = t
            expr.type?.sourceSection = expr.sourceSection
            InferenceResult(t, receiverInferred.constraints + valueInferred.constraints)
        }
    }
}

fun unify(constraints: Set<Constraint>): List<Pair<TypeVariable, Type>> {
    val q = ArrayDeque(constraints)
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
            typeMismatch(expected = e, actual = a, section = section)
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
                typeMismatch(expected = e, actual = a, section)
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
                typeMismatch(expected = e, actual = a, section)
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
                typeMismatch(expected = e, actual = a, section)
            }
        } else if (e is SumType && a is SumType) {
            CompilerMessage.from(
                "Comparing SumTypes! This is compiler error, please report it along with the code!",
                section)
            // if module, package or name different - fail
            // paramTypes should be equal
            // zip param types together and create new constraints
        } else {
            typeMismatch(expected = e, actual = a, section = section)
        }
    }

    return substitutions
}

fun typeMismatch(expected: Type, actual: Type, section: ChiSource.Section?) {
    val selectedSection = actual.sourceSection ?: section
    throw CompilerMessage(TypeMismatch(
        expected = expected,
        actual = actual,
        codePoint = selectedSection.toCodePoint()))
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
