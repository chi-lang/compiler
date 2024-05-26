package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.utils.DefaultArguments

class TypingError(message: String) : RuntimeException(message)

fun err(message: String, sourceSection: ChiSource.Section?): Nothing = throw CompilerMessage.from(message, sourceSection)

class Typer(
    private val ctx: InferenceContext
) {

    fun typeTerms(terms: List<Expression>, constraints: MutableList<Constraint>, level: Int = 0): List<Type> {
        return terms.map { typeTerm(it, level, constraints) }
    }

    fun typeTerm(term: Expression, level: Int = 0, constraints: MutableList<Constraint>): Type =
        when (term) {
            is Atom ->
                term.type!!

            is VariableAccess ->
                ctx.getTargetType(term.target, level, term.sourceSection)

            is CreateRecord ->
                Record(null, term.fields.map { Record.Field(it.name, typeTerm(it.value, level, constraints)) })

            is CreateArray -> {
                val elementType = ctx.freshVariable(level)
                typeTerms(term.values, constraints, level).forEach {
                    constraints.add(Constraint(elementType, it, term.sourceSection)) // FIXME - this should point at element type if possible
                }
                Array(elementType)
            }

            is Fn -> {
                ctx.withNewLocalScope {
                    val returnType = ctx.freshVariable(level)
                    val params = term.parameters.map { fnParam ->
                        val typeAnnotation = fnParam.type
                        if (typeAnnotation != null) {
                            fnParam.name to typeAnnotation
                        } else {
                            fnParam.name to ctx.freshVariable(level)
                        }.also {
                            val defaultValue = term.defaultValues[it.first]
                            if (defaultValue != null) {
                                val valueType = typeTerm(defaultValue, level, constraints)
                                constraints.add(Constraint(it.second, valueType, defaultValue.sourceSection))
                            }
                            ctx.defineLocalSymbol(it.first, it.second)
                        }
                    }
                    val bodyType = typeTerm(term.body, level, constraints)

                    val fnType = Function(
                        params.map { it.second } + returnType
                    )
                    constraints.add(Constraint(returnType, bodyType, term.body.sourceSection))
                    fnType
                }
            }

            is Block -> {
                val types = term.body.map { typeTerm(it, level + 1, constraints) }
                types.lastOrNull() ?: Type.unit
            }

            is FnCall -> {
                val definedFunctionType = typeTerm(term.function, level, constraints)
                val result = ctx.freshVariable(level)

                if (term.function is FieldAccess) {
                    val dotOp = term.function as FieldAccess
                    val target = dotOp.target!!
                    term.function = when(target) {
                        DotTarget.Field -> dotOp
                        DotTarget.LocalFunction -> {
                            term.parameters.add(0, dotOp.receiver)
                            val symbol = ctx.compileTables.getLocalSymbol(dotOp.fieldName)
                            DefaultArguments.fill(term.parameters, symbol)
                            VariableAccess(LocalSymbol(dotOp.fieldName), dotOp.memberSection).also {
                                it.type = dotOp.type
                            }
                        }
                        is DotTarget.PackageFunction -> {
                            term.parameters.add(0, dotOp.receiver)
                            val target = PackageSymbol(
                                target.moduleName, target.packageName, target.name
                            )
                            val symbol = ctx.ns.getSymbol(target)
                            DefaultArguments.fill(term.parameters, symbol)
                            VariableAccess(target, dotOp.memberSection).also {
                                it.type = dotOp.type
                            }
                        }
                    }
                }

                val callType = Function(
                    term.parameters.map { typeTerm(it, level, constraints) } + result
                )

                constraints.add(Constraint(definedFunctionType, callType, term.sourceSection))
                result
            }

            is FieldAccess -> {
                val receiverType = typeTerm(term.receiver, level, constraints)
                val finalReceiverType = mapType(receiverType, unify(constraints))
                if (finalReceiverType is Record && finalReceiverType.fields.any { it.name == term.fieldName }) {
                    val result = ctx.freshVariable(level)
                    term.target = DotTarget.Field
                    constraints.add(Constraint(Type.record(term.fieldName to result), receiverType, term.memberSection))
                    result
                } else {
                    val function = ctx.listLocalFunctionsForType(term.fieldName, finalReceiverType).singleOrNull()
                        ?: ctx.listCurrentPackageFunctionsForType(term.fieldName, finalReceiverType).singleOrNull()
                        ?: ctx.listTypesPackageFunctionsForType(term.fieldName, finalReceiverType).singleOrNull()
                    if (function != null) {
                        val (dotTarget, fnType) = function
                        term.target = dotTarget
                        fnType.instantiate(level, ctx::freshVariable)
                    } else {
                        throw CompilerMessage(MemberDoesNotExist(finalReceiverType, term.fieldName, term.memberSection.toCodePoint()))
                    }
                }
            }

            is NameDeclaration -> {
                val expectedType = term.expectedType ?: ctx.freshVariable(level+1)
                val valueType = typeTerm(term.value, level + 1, constraints)
                constraints.add(Constraint(expectedType, valueType, term.value.sourceSection))

                // generalization of the type
                // for example val id = { a -> a } which by default gets type 'a1 -> 'a2
                // this step converts it to type 'a1 -> 'a1 which is important because
                // we later instantiate and loose all the information about the original variables
                val solution = unify(constraints)
                val polymorphicType = PolyType(level, mapType(expectedType, solution))
                ctx.defineLocalSymbol(term.name, polymorphicType)

                expectedType
            }

            is Assignment -> {
                val variableType = ctx.getTargetType(term.target, level, term.sourceSection)
                val valueType = typeTerm(term.value, level, constraints)
                constraints.add(Constraint(variableType, valueType, term.value.sourceSection))
                variableType
            }

            is IfElse -> {
                val conditionType = typeTerm(term.condition, level, constraints)
                val thenBranchType = typeTerm(term.thenBranch, level, constraints)
                constraints.add(Constraint(Type.bool, conditionType, term.condition.sourceSection))
                if (term.elseBranch != null) {
                    val elseBranchType = typeTerm(term.elseBranch, level, constraints)
                    Sum.create(thenBranchType, elseBranchType)
                } else {
                    Sum.create(thenBranchType, Type.unit)
                }
            }

            is InterpolatedString -> {
                typeTerms(term.parts, constraints, level)
                Type.string
            }

            is Cast -> {
                typeTerm(term.expression, level, constraints)
                // TODO: usage marking doesn't work correctly so this had to be turned off
//                if (!term.used && term.expression is VariableAccess) {
//                    ctx.updateSymbolType(term.expression.target, term.targetType)
//                }
                term.targetType
            }

            is Break -> Type.unit
            is Continue -> Type.unit
            is WhileLoop -> {
                val conditionType = typeTerm(term.condition, level, constraints)
                typeTerm(term.loop, level, constraints)
                constraints.add(Constraint(Type.bool, conditionType, term.condition.sourceSection))
                Type.unit
            }

            is ForLoop -> {
                val iterableType = typeTerm(term.iterable, level, constraints)
                val stateType = term.state?.let { typeTerm(it, level, constraints) }
                val initType = term.init?.let { typeTerm(it, level, constraints) }

                val solution = unify(constraints)
                val finalIterableType = mapType(iterableType, solution)

                val varTypes = term.vars.map { it to ctx.freshVariable(level) }

                // basic form for array
                if (finalIterableType is Array) {
                    when (term.vars.size) {
                        1 -> {
                            // type of the variable is the type of element array
                            constraints.add(
                                Constraint(
                                    finalIterableType.elementType,
                                    varTypes.first().second,
                                    term.varSections.first()
                                )
                            )
                        }

                        2 -> {
                            // type of the index variable is int, the second variable's type is the same as array element type
                            constraints.add(Constraint(Type.int, varTypes[0].second, term.varSections[0]))
                            constraints.add(
                                Constraint(
                                    finalIterableType.elementType,
                                    varTypes[1].second,
                                    term.varSections[1]
                                )
                            )
                        }

                        else -> {
                            err("Too many variable names!", term.varSections.first())
                        }
                    }
                } else if (finalIterableType is Record) {
                    if (term.vars.size != 2) {
                        err("Iteration through record should have two variables", term.varSections[0])
                    }
                    constraints.add(Constraint(Type.string, varTypes[0].second, term.varSections[0]))
                    constraints.add(Constraint(Type.any, varTypes[1].second, term.varSections[1]))
                } else if (finalIterableType is Function) {
                    if (term.vars.size != 1) {
                        err("Generator function requires only one variable", term.varSections[0])
                    }

                    if (stateType != null && initType != null) {
                        // stateful generator
                        if (finalIterableType.types.size != 3) {
                            err("Stateful generator function should have two arguments", term.iterableSection)
                        }
                        val stateArgType = finalIterableType.types[0]
                        val lastArgType = finalIterableType.types[1]

                        constraints.add(Constraint(lastArgType, varTypes[0].second, term.varSections[0]))
                        constraints.add(Constraint(initType, lastArgType, term.initSection))
                        constraints.add(Constraint(stateType, stateArgType, term.stateSection))
                    } else {
                        // basic generator should have no arguments and return optional value
                        if (finalIterableType.types.size != 1) {
                            err("Generator function should have no arguments", term.iterableSection)
                        }
                    }

                    // verify that both generator functions return an option
                    val returnValue = finalIterableType.types.last()
                    if (returnValue is HasTypeId && returnValue.getTypeId() == Type.optionTypeId) {
                        val optionalType = (returnValue as Sum).removeType(Type.unit)
                        constraints.add(Constraint(optionalType, varTypes[0].second, term.varSections[0]))
                    } else {
                        err("Generator function should return optional value (value or unit)", term.iterableSection)
                    }
                } else {
                    err("Unsupported iterable type in for loop: $finalIterableType", term.iterableSection)
                }


                ctx.withNewLocalScope {
                    varTypes.forEach { (name, type) ->
                        ctx.defineLocalSymbol(name, type)
                    }
                    typeTerm(term.body, level, constraints)
                }
                Type.unit
            }

            is FieldAssignment -> {
                val receiverType = typeTerm(term.receiver, level, constraints)
                val valueType = typeTerm(term.value, level, constraints)
                val expectedType = Type.record(term.fieldName to valueType)

                val result = ctx.freshVariable(level)
                constraints.add(Constraint(valueType, result, term.value.sourceSection))
                constraints.add(Constraint(expectedType, receiverType, term.receiver.sourceSection))
                result
            }

            is EffectDefinition -> {
                term.type!!
            }

            is Handle -> {
                val result = ctx.freshVariable(level)
                val bodyType = typeTerm(term.body, level, constraints)
                constraints.add(Constraint(result, bodyType, term.body.sourceSection))
                term.cases.forEach { case ->
                    val effectType = ctx.getTargetType(PackageSymbol(case.moduleName, case.packageName, case.effectName), level, term.sourceSection)
                    if (effectType is Function) {
                        ctx.withNewLocalScope {
                            case.argumentNames.zip(effectType.types.dropLast(1)).forEach { (name, type) ->
                                ctx.defineLocalSymbol(name, type)
                            }
                            val effectReturnType = effectType.types.last()
                            ctx.defineLocalSymbol("resume", Function(listOf(effectReturnType, ctx.freshVariable(level))))
                            val caseBodyType = typeTerm(case.body, level, constraints)
                            constraints.add(Constraint(result, caseBodyType, case.body.sourceSection))
                        }
                    } else {
                        err("Symbol ${case.effectName} has type $effectType but a function type was expected!", case.sourceSection)
                    }
                }
                result
            }

            is IndexOperator -> {
                val elementType = ctx.freshVariable(level)
                val variableType = typeTerm(term.variable, level, constraints)
                val indexType = typeTerm(term.index, level, constraints)
                constraints.add(Constraint(variableType, Type.array(elementType), term.variable.sourceSection))
                constraints.add(Constraint(Type.int, indexType, term.index.sourceSection))
                elementType
            }

            is IndexedAssignment -> {
                val elementType = ctx.freshVariable(level)
                val variableType = typeTerm(term.variable, level, constraints)
                val valueType = typeTerm(term.value, level, constraints)
                val indexType = typeTerm(term.index, level, constraints)
                constraints.add(Constraint(elementType, valueType, term.value.sourceSection))
                constraints.add(Constraint(variableType, Type.array(elementType), term.variable.sourceSection))
                constraints.add(Constraint(Type.int, indexType, term.index.sourceSection))
                elementType
            }

            is InfixOp -> {
                val result = ctx.freshVariable(level)
                val lhsType = typeTerm(term.left, level, constraints)
                val rhsType = typeTerm(term.right, level, constraints)
                if (term.op in listOf("<", "<=", ">", ">=", "==", "!=", "&&", "||")) {
                    constraints.add(Constraint(lhsType, rhsType, term.right.sourceSection))
                    Type.bool
                } else {
                    constraints.add(Constraint(result, lhsType, term.left.sourceSection))
                    constraints.add(Constraint(result, rhsType, term.right.sourceSection))
                    result
                }
            }

            is Is -> {
                typeTerm(term.value, level, constraints)
                Type.bool
            }

            is PrefixOp -> {
                val valueType = typeTerm(term.expr, level, constraints)
                constraints.add(Constraint(Type.bool, valueType, term.expr.sourceSection))
                Type.bool
            }

            is Return -> {
                if (term.value != null) {
                    typeTerm(term.value, level, constraints)
                } else {
                    Type.unit
                }
            }

        }.also {
            term.type = it
        }

}