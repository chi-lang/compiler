package gh.marad.chi.core.types3

import gh.marad.chi.core.*
import gh.marad.chi.core.Target
import gh.marad.chi.core.namespace.GlobalCompilationNamespace

class TypingError(message: String) : RuntimeException(message)

fun err(message: String): Nothing = throw TypingError(message)

class Typer(
    private val ns: GlobalCompilationNamespace,
    private val ctx: InferenceContext = InferenceContext()
) {
    private var localSymbols: LocalSymbols? = LocalSymbols()
    class LocalSymbols(val parent: LocalSymbols? = null) {
        val symbols = mutableMapOf<String, TypeScheme>()
        fun get(name: String): TypeScheme? = symbols[name] ?: parent?.get(name)
        fun define(name: String, typeScheme: TypeScheme) {
            symbols[name] = typeScheme
        }
    }

    fun typeTerms(terms: List<Expression>, constraints: MutableList<Constraint>, level: Int = 0): List<Type3> {
        return terms.map { typeTerm(it, level, constraints) }
    }

    fun typeTerm(term: Expression, level: Int = 0, constraints: MutableList<Constraint>): Type3 =
        when (term) {
            is Atom ->
                term.newType!!

            is VariableAccess ->
                getTargetType(term.target, level)

            is CreateRecord ->
                Record(null, term.fields.map { Record.Field(it.name, typeTerm(it.value, level, constraints)) })

            is Fn -> {
                withNewLocalScope {
                    val returnType = ctx.freshVariable(level)
                    val params = term.parameters.map { fnParam ->
                        val typeAnnotation = fnParam.newType
                        if (typeAnnotation != null) {
                            fnParam.name to typeAnnotation
                        } else {
                            fnParam.name to ctx.freshVariable(level)
                        }.also {
                            localSymbols?.define(it.first, it.second)
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
                types.lastOrNull() ?: Type3.unit
            }

            is FnCall -> {
                val fnType = typeTerm(term.function, level, constraints)
                val result = ctx.freshVariable(level)
                val expectedType = Function(
                    term.parameters.map { typeTerm(it, level, constraints) } + result
                )
                constraints.add(Constraint(expectedType, fnType))
                result
            }

            is FieldAccess -> {
                val result = ctx.freshVariable(level)
                val receiverType = typeTerm(term.receiver, level, constraints)
                val field = Record.Field(term.fieldName, result)
                constraints.add(Constraint(Record(null, listOf(field)), receiverType))
                result
            }

            is NameDeclaration -> {
                val expectedType = term.newType ?: ctx.freshVariable(level+1)
                val valueType = typeTerm(term.value, level + 1, constraints)
                constraints.add(Constraint(expectedType, valueType))

                // generalization of the type
                // for example val id = { a -> a } which by default gets type 'a1 -> 'a2
                // this step converts it to type 'a1 -> 'a1 which is important because
                // we later instantiate and loose all the information about the original variables
                val solution = unify(constraints)
                val polymorphicType = PolyType(level, mapType(expectedType, solution))
                localSymbols?.define(term.name, polymorphicType)

                expectedType
            }

            is Assignment -> {
                val variableType = getTargetType(term.target, level)
                val valueType = typeTerm(term.value, level, constraints)
                constraints.add(Constraint(variableType, valueType))
                variableType
            }

            is IfElse -> {
                val conditionType = typeTerm(term.condition, level, constraints)
                val thenBranchType = typeTerm(term.thenBranch, level, constraints)
                constraints.add(Constraint(Type3.bool, conditionType))
                if (term.elseBranch != null) {
                    val elseBranchType = typeTerm(term.elseBranch, level, constraints)
                    Sum.create(thenBranchType, elseBranchType, level)
                } else {
                    Type3.unit
                }
            }

            else -> TODO("Unsupported expression: $term")
        }.also {
            term.newType = it
        }

    private fun <T> withNewLocalScope(f: () -> T): T {
        val prev = localSymbols
        localSymbols = LocalSymbols(prev)
        return f().also { localSymbols = prev }
    }

    private fun getTargetType(target: Target, level: Int): Type3 =
        when(target) {
            is LocalSymbol ->
                localSymbols?.get(target.name)
                    ?.instantiate(level, ctx::freshVariable)
                    ?: err("Identifier ${target.name} not found!")

            is PackageSymbol -> {
                // FIXME replace 'localSymbols' with reading from a package
                //       once a package supports new types
//                ns.getSymbol(target)
                localSymbols?.get(target.name)
                    ?.instantiate(level, ctx::freshVariable)
                    ?: err("Identifier ${target.name} not found!")
            }
        }
}