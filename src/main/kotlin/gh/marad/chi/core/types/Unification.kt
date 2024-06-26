package gh.marad.chi.core.types

import gh.marad.chi.core.analyzer.*

fun unify(constraints: List<Constraint>): List<Pair<Variable, Type>> {
    var queue = ArrayDeque(constraints.sortedBy { it.expected !is Variable })
    val solutions = mutableListOf<Pair<Variable, Type>>()

    while(queue.isNotEmpty()) {
        val constraint = queue.removeFirst()
        val (expected, actual, section) = constraint
        when {
            expected == actual -> {}
            expected == Type.any -> {}
            expected is Primitive && actual is Primitive -> {
                if(expected.ids.intersect(actual.ids.toSet()).isEmpty()) {
                    throw CompilerMessage(TypeMismatch(expected, actual, section.toCodePoint()))
                }
            }
            expected is Recursive -> {
                queue.addFirst(Constraint(expected.unfold(), actual, section, history = constraint.toHistory()))
            }
            actual is Recursive -> {
                queue.addFirst(Constraint(expected, actual.unfold(), section, constraint.toHistory()))
            }
            expected is Variable -> {
                solutions.add(expected to actual)
                val replacer = VariableReplacer(expected, actual)
                queue = ArrayDeque(queue.map { it.withReplacedVariable(replacer) })
            }

            actual is Variable -> {
                solutions.add(actual to expected)
                val replacer = VariableReplacer(actual, expected)
                queue = ArrayDeque(queue.map { it.withReplacedVariable(replacer) })
            }

            expected is Function && actual is Function -> {
                if (expected.types.size != actual.types.size) {
                    throw CompilerMessage(FunctionArityError(expected.types.size - 1, actual.types.size - 1, section.toCodePoint()))
                }

                val lastIndex = expected.types.size - 1
                expected.types.zip(actual.types).forEachIndexed { index, (expectedParam, actualParam) ->
                    if (index == lastIndex && expectedParam == Type.unit && actualParam !is Variable) {
                        // we can accept any function returning something if we expect 'unit'
                        // because we are not going to use the result anyway
                        return@forEachIndexed
                    }
                    queue.addFirst(Constraint(expectedParam, actualParam, section, constraint.toHistory()))
                }
            }

            expected is Record && actual is Record -> {
                expected.fields.forEach { expField ->
                    val actualField = actual.fields.firstOrNull { it.name == expField.name }
                        ?: err("Record $actual is missing expected field ${expField.name}", section)
                    queue.addFirst(Constraint(expField.type, actualField.type, section, constraint.toHistory()))
                }
            }

            expected is Array && actual is Array -> {
                queue.addFirst(Constraint(expected.elementType, actual.elementType, section, constraint.toHistory()))
            }

            expected is Sum -> {
                try {
                    // try to unify the *right* side because sum type associates left
                    val partialSolution = unify(listOf(Constraint(expected.rhs, actual, section, constraint.toHistory())))
                    val replacers = partialSolution.map { VariableReplacer(it.first, it.second) }
                    val updatedQueue = replacers.fold(queue.toList()) { q, replacer ->
                        q.map { it.withReplacedVariable(replacer) }
                    }
                    queue = ArrayDeque(updatedQueue)
                } catch (ex: CompilerMessage) {
                    // FIXME: this causes weird errors when it finishes because it
                    //        says the first type of the sum type does not match the actual
                    //        I think that Constraint should somehow accumulate the knowledge
                    //        that it comes from sum type comparison for better error message
                    queue.addFirst(Constraint(expected.lhs, actual, section, constraint.toHistory()))
                }
            }

            expected is Function && actual !is Function -> {
                throw CompilerMessage(NotAFunction(section.toCodePoint()))
            }

            else -> //err("Type mismatch. Expected: $expected, actual: $actual")
                throw CompilerMessage(TypeMismatch(expected, actual, section.toCodePoint()))
        }
    }

    return solutions
}
