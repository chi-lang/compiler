package gh.marad.chi.core.types

import gh.marad.chi.core.Expression
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor

class SubstituteTypeVariable(private val v: TypeVariable, private val t: Type) : DefaultExpressionVisitor {
    override fun visit(expr: Expression) {
        expr.type = expr.type?.substitute(v, t)
        super.visit(expr)
    }
}