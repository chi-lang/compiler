package gh.marad.chi.core.types

import gh.marad.chi.core.analyzer.*

fun occursIn(variable: Variable, type: Type): Boolean = when (type) {
    is Variable -> type == variable
    else -> type.children().any { occursIn(variable, it) }
}

fun occursInExcludingSumBranches(variable: Variable, type: Type): Boolean = when (type) {
    is Variable -> type == variable
    is Sum -> {
        // Don't count the variable as "occurring" if it's just a direct branch.
        // Only flag if it occurs INSIDE a non-sum child.
        val branches = Sum.listTypes(type)
        val otherBranches = branches - variable
        otherBranches.any { occursIn(variable, it) }
    }
    else -> type.children().any { occursIn(variable, it) }
}

/**
 * Attempt to unify without throwing on failure. Returns null if unification fails.
 * Used for method resolution probing in InferenceContext.
 */
fun tryUnify(constraints: List<Constraint>): List<Pair<Variable, Type>>? {
    return try {
        unify(constraints)
    } catch (ex: CompilerMessage) {
        null
    }
}

fun unify(constraints: List<Constraint>): List<Pair<Variable, Type>> {
    val uf = UnionFind()
    val queue = ArrayDeque(constraints.sortedBy { it.expected !is Variable })

    while (queue.isNotEmpty()) {
        val rawConstraint = queue.removeFirst()
        // Resolve both sides through the union-find to get current bindings
        val constraint = uf.resolveConstraint(rawConstraint)
        val (expected, actual, section) = constraint
        when {
            expected == actual -> {}
            expected == Type.any -> {}
            expected is Primitive && actual is Primitive -> {
                if (expected.ids.intersect(actual.ids.toSet()).isEmpty()) {
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
                if (occursInExcludingSumBranches(expected, actual)) {
                    throw CompilerMessage(InfiniteType(expected, actual, section.toCodePoint()))
                }
                uf.bind(expected, actual)
            }

            actual is Variable -> {
                if (occursInExcludingSumBranches(actual, expected)) {
                    throw CompilerMessage(InfiniteType(actual, expected, section.toCodePoint()))
                }
                uf.bind(actual, expected)
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
                    // Merge partial solutions into our union-find
                    for ((v, t) in partialSolution) {
                        uf.bind(v, t)
                    }
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

            else ->
                throw CompilerMessage(TypeMismatch(expected, actual, section.toCodePoint()))
        }
    }

    return uf.allBindings()
}
