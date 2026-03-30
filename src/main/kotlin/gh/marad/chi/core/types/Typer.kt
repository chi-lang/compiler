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

    fun typeTerms(terms: List<Expression>, constraints: MutableList<Constraint>, level: Int = 0, uf: UnionFind? = null): List<Type> {
        return terms.map { typeTerm(it, level, constraints, uf) }
    }

    fun typeTerm(term: Expression, level: Int = 0, constraints: MutableList<Constraint>, uf: UnionFind? = null): Type =
        when (term) {
            is Atom ->
                term.type!!

            is VariableAccess -> {
                val result = ctx.getTargetType(term.target, level, term.sourceSection)
                result.instantiate(level, ctx::freshVariable)
            }

            is CreateRecord ->
                Record(emptyList(), term.fields.map { Record.Field(it.name, typeTerm(it.value, level, constraints, uf)) })

            is CreateArray -> {
                val elementType = ctx.freshVariable(level)
                typeTerms(term.values, constraints, level, uf).forEach {
                    constraints.add(Constraint(elementType, it, term.sourceSection, emptyList())) // FIXME - this should point at element type if possible
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
                                val valueType = typeTerm(defaultValue, level, constraints, uf)
                                constraints.add(Constraint(it.second, valueType, defaultValue.sourceSection, emptyList()))
                            }
                            ctx.defineLocalSymbol(it.first, it.second)
                        }
                    }
                    val bodyType = typeTerm(term.body, level, constraints, uf)

                    val fnType = Function(
                        params.map { it.second } + returnType
                    )
                    constraints.add(Constraint(returnType, bodyType, term.body.sourceSection, emptyList()))
                    fnType
                }
            }

            is Block -> {
                val types = term.body.map { typeTerm(it, level + 1, constraints, uf) }
                types.lastOrNull() ?: Type.unit
            }

            is FnCall -> {
                val definedFunctionType = typeTerm(term.function, level, constraints, uf)
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
                    term.parameters.map { typeTerm(it, level, constraints, uf) } + result
                )

                constraints.add(Constraint(definedFunctionType, callType, term.sourceSection, emptyList()))
                result
            }

            is FieldAccess -> {
                val receiverType = typeTerm(term.receiver, level, constraints, uf)
                val finalReceiverType = if (uf != null) {
                    unify(constraints, uf)
                    constraints.clear()
                    uf.resolve(receiverType)
                } else {
                    mapType(receiverType, unify(constraints))
                }
                if (finalReceiverType is Record && finalReceiverType.fields.any { it.name == term.fieldName }) {
                    val result = ctx.freshVariable(level)
                    term.target = DotTarget.Field
                    constraints.add(Constraint(Type.record(term.fieldName to result), receiverType, term.memberSection, emptyList()))
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
                val valueType = typeTerm(term.value, level + 1, constraints, uf)
                constraints.add(Constraint(expectedType, valueType, term.value.sourceSection, emptyList()))

                // generalization of the type
                // for example val id = { a -> a } which by default gets type 'a1 -> 'a2
                // this step converts it to type 'a1 -> 'a1 which is important because
                // we later instantiate and loose all the information about the original variables
                val polymorphicType = if (uf != null) {
                    unify(constraints, uf)
                    constraints.clear()
                    PolyType(level, uf.resolve(expectedType))
                } else {
                    val solution = unify(constraints)
                    PolyType(level, mapType(expectedType, solution))
                }
                ctx.defineLocalSymbol(term.name, polymorphicType)

                expectedType
            }

            is Assignment -> {
                val variableType = ctx.getTargetType(term.target, level, term.sourceSection)
                val valueType = typeTerm(term.value, level, constraints, uf)
                constraints.add(Constraint(variableType, valueType, term.value.sourceSection, emptyList()))
                variableType
            }

            is IfElse -> {
                val conditionType = typeTerm(term.condition, level, constraints, uf)
                val thenBranchType = typeTerm(term.thenBranch, level, constraints, uf)
                constraints.add(Constraint(Type.bool, conditionType, term.condition.sourceSection, emptyList()))
                if (term.elseBranch != null) {
                    val elseBranchType = typeTerm(term.elseBranch, level, constraints, uf)
                    Sum.create(thenBranchType, elseBranchType)
                } else {
                    Sum.create(thenBranchType, Type.unit)
                }
            }

            is InterpolatedString -> {
                typeTerms(term.parts, constraints, level, uf)
                Type.string
            }

            is Cast -> {
                typeTerm(term.expression, level, constraints, uf)
                // TODO: usage marking doesn't work correctly so this had to be turned off
//                if (!term.used && term.expression is VariableAccess) {
//                    ctx.updateSymbolType(term.expression.target, term.targetType)
//                }
                term.targetType
            }

            is Break -> Type.unit
            is Continue -> Type.unit
            is WhileLoop -> {
                val conditionType = typeTerm(term.condition, level, constraints, uf)
                typeTerm(term.loop, level, constraints, uf)
                constraints.add(Constraint(Type.bool, conditionType, term.condition.sourceSection, emptyList()))
                Type.unit
            }

            is ForLoop -> {
                val iterableType = typeTerm(term.iterable, level, constraints, uf)
                val stateType = term.state?.let { typeTerm(it, level, constraints, uf) }
                val initType = term.init?.let { typeTerm(it, level, constraints, uf) }

                val finalIterableType = if (uf != null) {
                    unify(constraints, uf)
                    constraints.clear()
                    uf.resolve(iterableType)
                } else {
                    val solution = unify(constraints)
                    mapType(iterableType, solution)
                }

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
                                    term.varSections.first(),
                                    emptyList()
                                )
                            )
                        }

                        2 -> {
                            // type of the index variable is int, the second variable's type is the same as array element type
                            constraints.add(Constraint(Type.int, varTypes[0].second, term.varSections[0], emptyList()))
                            constraints.add(
                                Constraint(
                                    finalIterableType.elementType,
                                    varTypes[1].second,
                                    term.varSections[1],
                                    emptyList()
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
                    constraints.add(Constraint(Type.string, varTypes[0].second, term.varSections[0], emptyList()))
                    constraints.add(Constraint(Type.any, varTypes[1].second, term.varSections[1], emptyList()))
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

                        constraints.add(Constraint(lastArgType, varTypes[0].second, term.varSections[0], emptyList()))
                        constraints.add(Constraint(initType, lastArgType, term.initSection, emptyList()))
                        constraints.add(Constraint(stateType, stateArgType, term.stateSection, emptyList()))
                    } else {
                        // basic generator should have no arguments and return optional value
                        if (finalIterableType.types.size != 1) {
                            err("Generator function should have no arguments", term.iterableSection)
                        }
                    }

                    // verify that both generator functions return an option
                    val returnValue = finalIterableType.types.last()
                    if (returnValue is HasTypeId && Type.optionTypeId in returnValue.getTypeIds()) {
                        val optionalType = (returnValue as Sum).removeType(Type.unit)
                        constraints.add(Constraint(optionalType, varTypes[0].second, term.varSections[0], emptyList()))
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
                    typeTerm(term.body, level, constraints, uf)
                }
                Type.unit
            }

            is FieldAssignment -> {
                val receiverType = typeTerm(term.receiver, level, constraints, uf)
                val valueType = typeTerm(term.value, level, constraints, uf)
                val expectedType = Type.record(term.fieldName to valueType)

                val result = ctx.freshVariable(level)
                constraints.add(Constraint(valueType, result, term.value.sourceSection, emptyList()))
                constraints.add(Constraint(expectedType, receiverType, term.receiver.sourceSection, emptyList()))
                result
            }

            is EffectDefinition -> {
                term.type!!
            }

            is Handle -> {
                val result = ctx.freshVariable(level)
                val bodyType = typeTerm(term.body, level, constraints, uf)
                constraints.add(Constraint(result, bodyType, term.body.sourceSection, emptyList()))
                term.cases.forEach { case ->
                    val effectType = ctx.getTargetType(PackageSymbol(case.moduleName, case.packageName, case.effectName), level, term.sourceSection)
                    if (effectType is Function) {
                        ctx.withNewLocalScope {
                            case.argumentNames.zip(effectType.types.dropLast(1)).forEach { (name, type) ->
                                ctx.defineLocalSymbol(name, type)
                            }
                            val effectReturnType = effectType.types.last()
                            ctx.defineLocalSymbol("resume", Function(listOf(effectReturnType, ctx.freshVariable(level))))
                            val caseBodyType = typeTerm(case.body, level, constraints, uf)
                            constraints.add(Constraint(result, caseBodyType, case.body.sourceSection, emptyList()))
                        }
                    } else {
                        err("Symbol ${case.effectName} has type $effectType but a function type was expected!", case.sourceSection)
                    }
                }
                result
            }

            is IndexOperator -> {
                val elementType = ctx.freshVariable(level)
                val variableType = typeTerm(term.variable, level, constraints, uf)
                val indexType = typeTerm(term.index, level, constraints, uf)
                constraints.add(Constraint(variableType, Type.array(elementType), term.variable.sourceSection, emptyList()))
                constraints.add(Constraint(Type.int, indexType, term.index.sourceSection, emptyList()))
                elementType
            }

            is IndexedAssignment -> {
                val elementType = ctx.freshVariable(level)
                val variableType = typeTerm(term.variable, level, constraints, uf)
                val valueType = typeTerm(term.value, level, constraints, uf)
                val indexType = typeTerm(term.index, level, constraints, uf)
                constraints.add(Constraint(elementType, valueType, term.value.sourceSection, emptyList()))
                constraints.add(Constraint(variableType, Type.array(elementType), term.variable.sourceSection, emptyList()))
                constraints.add(Constraint(Type.int, indexType, term.index.sourceSection, emptyList()))
                elementType
            }

            is InfixOp -> {
                val result = ctx.freshVariable(level)
                val lhsType = typeTerm(term.left, level, constraints, uf)
                val rhsType = typeTerm(term.right, level, constraints, uf)
                if (term.op in listOf("&&", "||")) {
                    constraints.add(Constraint(Type.bool, lhsType, term.left.sourceSection, emptyList()))
                    constraints.add(Constraint(Type.bool, rhsType, term.right.sourceSection, emptyList()))
                    Type.bool
                } else if (term.op in listOf("<", "<=", ">", ">=", "==", "!=")) {
                    constraints.add(Constraint(lhsType, rhsType, term.right.sourceSection, emptyList()))
                    Type.bool
                } else {
                    constraints.add(Constraint(result, lhsType, term.left.sourceSection, emptyList()))
                    constraints.add(Constraint(result, rhsType, term.right.sourceSection, emptyList()))
                    result
                }
            }

            is Is -> {
                typeTerm(term.value, level, constraints, uf)
                Type.bool
            }

            is PrefixOp -> {
                val valueType = typeTerm(term.expr, level, constraints, uf)
                constraints.add(Constraint(Type.bool, valueType, term.expr.sourceSection, emptyList()))
                Type.bool
            }

            is Return -> {
                if (term.value != null) {
                    typeTerm(term.value, level, constraints, uf)
                } else {
                    Type.unit
                }
            }

        }.also {
            term.type = it
        }

}