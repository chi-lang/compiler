package gh.marad.chi.core.types3

import gh.marad.chi.core.Expression

fun replaceTypes(expr: Expression, solutions: List<Pair<Variable, Type3>>) {
    if (expr.newType != null) {
        expr.newType = mapType(expr.newType!!, solutions)
    }
    expr.children().forEach { replaceTypes(it, solutions) }
}

fun mapType(type: Type3, solution: List<Pair<Variable, Type3>>): Type3 {
    return solution.fold(type) { t, subs ->
        VariableReplacer(subs.first, subs.second).replace(t)
    }
}

