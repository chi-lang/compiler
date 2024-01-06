package gh.marad.chi.core.types

import gh.marad.chi.core.Expression
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor

class TypeFiller(private val solution: List<Pair<TypeVariable, Type>>) : DefaultExpressionVisitor {
    override fun visit(expr: Expression) {
        expr.accept(this)
        assert(expr.newType != null) {
            "Expression did not have type set: $expr"
        }
        expr.newType = applySubstitution(expr.newType!!, solution)
        val startLetter = 'a'.code
        expr.newType!!.typeSchemeVariables().distinctBy { it.name }.sortedBy { it.name }
            .forEachIndexed { i, v ->
                val newName = TypeVariable(Char(startLetter + i).toString())
                SubstituteTypeVariable(v, newName).visit(expr)
            }
    }
}