package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.analyzer.toCodePoint

class TypingError(message: String) : RuntimeException(message)

fun err(message: String): Nothing = throw TypingError(message)

class Typer(
    private val ctx: InferenceContext
) {

    fun typeTerms(terms: List<Expression>, constraints: MutableList<Constraint>, level: Int = 0): List<Type> {
        return terms.map { typeTerm(it, level, constraints) }
    }

    fun typeTerm(term: Expression, level: Int = 0, constraints: MutableList<Constraint>): Type =
        when (term) {
            is Atom ->
                term.newType!!

            is VariableAccess ->
                ctx.getTargetType(term.target, level)

            is CreateRecord ->
                Record(null, term.fields.map { Record.Field(it.name, typeTerm(it.value, level, constraints)) })

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
                            ctx.defineLocalSymbol(it.first, it.second)
                        }
                    }
                    val bodyType = typeTerm(term.body, level, constraints)

                    val fnType = Function(
                        params.map { it.second } + returnType
                    )
                    constraints.add(Constraint(returnType, bodyType))
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
                            VariableAccess(LocalSymbol(dotOp.fieldName), dotOp.memberSection).also {
                                it.newType = dotOp.newType
                            }
                        }
                        is DotTarget.PackageFunction -> {
                            term.parameters.add(0, dotOp.receiver)
                            VariableAccess(PackageSymbol(
                                target.moduleName, target.packageName, target.name
                            ), dotOp.memberSection).also {
                                it.newType = dotOp.newType
                            }
                        }
                    }
                }

                val callType = Function(
                    term.parameters.map { typeTerm(it, level, constraints) } + result
                )

                constraints.add(Constraint(definedFunctionType, callType))
                result
            }

            is FieldAccess -> {
                val receiverType = typeTerm(term.receiver, level, constraints)
                val finalReceiverType = mapType(receiverType, unify(constraints))
                if (finalReceiverType is Record && finalReceiverType.fields.any { it.name == term.fieldName }) {
                    val result = ctx.freshVariable(level)
                    term.target = DotTarget.Field
                    constraints.add(Constraint(Type.record(term.fieldName to result), receiverType))
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
                constraints.add(Constraint(expectedType, valueType))

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
                val variableType = ctx.getTargetType(term.target, level)
                val valueType = typeTerm(term.value, level, constraints)
                constraints.add(Constraint(variableType, valueType))
                variableType
            }

            is IfElse -> {
                val conditionType = typeTerm(term.condition, level, constraints)
                val thenBranchType = typeTerm(term.thenBranch, level, constraints)
                constraints.add(Constraint(Type.bool, conditionType))
                if (term.elseBranch != null) {
                    val elseBranchType = typeTerm(term.elseBranch, level, constraints)
                    Sum.create(thenBranchType, elseBranchType)
                } else {
                    Type.unit
                }
            }

            is InterpolatedString -> {
                typeTerms(term.parts, constraints, level)
                Type.string
            }

            is Cast -> {
                typeTerm(term.expression, level, constraints)
                if (term.expression is VariableAccess) {
                    ctx.updateSymbolType(term.expression.target, term.targetType)
                }
                term.targetType
            }

            is Break -> Type.unit
            is Continue -> Type.unit
            is WhileLoop -> {
                val conditionType = typeTerm(term.condition, level, constraints)
                typeTerm(term.loop, level, constraints)
                constraints.add(Constraint(Type.bool, conditionType))
                Type.unit
            }

            is FieldAssignment -> {
                val receiverType = typeTerm(term.receiver, level, constraints)
                val valueType = typeTerm(term.value, level, constraints)
                val expectedType = Type.record(term.fieldName to valueType)

                val result = ctx.freshVariable(level)
                constraints.add(Constraint(valueType, result))
                constraints.add(Constraint(expectedType, receiverType))
                result
            }

            is EffectDefinition -> Type.unit
            is Handle -> {
                val result = ctx.freshVariable(level)
                val bodyType = typeTerm(term.body, level, constraints)
                constraints.add(Constraint(result, bodyType))
                term.cases.forEach { case ->
                    val effectType = ctx.getTargetType(PackageSymbol(case.moduleName, case.packageName, case.effectName), level)
                    if (effectType is Function) {
                        ctx.withNewLocalScope {
                            case.argumentNames.zip(effectType.types.dropLast(1)).forEach { (name, type) ->
                                ctx.defineLocalSymbol(name, type)
                            }
                            val effectReturnType = effectType.types.last()
                            ctx.defineLocalSymbol("resume", Function(listOf(effectReturnType, ctx.freshVariable(level))))
                            val caseBodyType = typeTerm(case.body, level, constraints)
                            constraints.add(Constraint(result, caseBodyType))
                        }
                    } else {
                        err("Symbol ${case.effectName} has type $effectType but a function type was expected!")
                    }
                }
                result
            }

            is IndexOperator -> {
                val elementType = ctx.freshVariable(level)
                val variableType = typeTerm(term.variable, level, constraints)
                val indexType = typeTerm(term.index, level, constraints)
                constraints.add(Constraint(variableType, Type.array(elementType)))
                constraints.add(Constraint(Type.int, indexType))
                elementType
            }

            is IndexedAssignment -> {
                val elementType = ctx.freshVariable(level)
                val variableType = typeTerm(term.variable, level, constraints)
                val valueType = typeTerm(term.value, level, constraints)
                val indexType = typeTerm(term.index, level, constraints)
                constraints.add(Constraint(elementType, valueType))
                constraints.add(Constraint(variableType, Type.array(elementType)))
                constraints.add(Constraint(Type.int, indexType))
                elementType
            }

            is InfixOp -> {
                val result = ctx.freshVariable(level)
                val lhsType = typeTerm(term.left, level, constraints)
                val rhsType = typeTerm(term.right, level, constraints)
                constraints.add(Constraint(result, lhsType))
                constraints.add(Constraint(result, rhsType))
                result
            }

            is Is -> {
                typeTerm(term.value, level, constraints)
                Type.bool
            }

            is PrefixOp -> {
                val valueType = typeTerm(term.expr, level, constraints)
                constraints.add(Constraint(Type.bool, valueType))
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
            term.newType = it
        }

}