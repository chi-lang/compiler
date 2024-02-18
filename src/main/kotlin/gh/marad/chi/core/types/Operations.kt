package gh.marad.chi.core.types

import gh.marad.chi.core.Expression

fun replaceTypes(expr: Expression, solutions: List<Pair<Variable, Type>>) {
    if (expr.newType != null) {
        expr.newType = mapType(expr.newType!!, solutions)
    }
    expr.children().forEach { replaceTypes(it, solutions) }
}

fun mapType(type: Type, solution: List<Pair<Variable, Type>>): Type {
    return solution.fold(type) { t, subs ->
        VariableReplacer(subs.first, subs.second).replace(t)
    }
}

