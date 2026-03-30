package gh.marad.chi.core.types

import gh.marad.chi.core.Expression

fun replaceTypes(expr: Expression, solutions: List<Pair<Variable, Type>>) {
    if (solutions.isEmpty()) return
    val replacer = BulkVariableReplacer(solutions.toMap())
    fun walk(e: Expression) {
        if (e.type != null) {
            e.type = replacer.replace(e.type!!)
        }
        e.children().forEach { walk(it) }
    }
    walk(expr)
}

fun mapType(type: Type, solution: List<Pair<Variable, Type>>): Type {
    if (solution.isEmpty()) return type
    return BulkVariableReplacer(solution.toMap()).replace(type)
}
