package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Assignment
import gh.marad.chi.core.Expression
import gh.marad.chi.core.analyzer.CannotChangeImmutableVariable
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor

class ImmutabilityCheckVisitor(val messages: MutableList<Message>) : DefaultExpressionVisitor {

    fun check(exprs: List<Expression>) {
        exprs.forEach(this::visit)
    }

    override fun visitAssignment(assignment: Assignment) {
        if (!assignment.symbol.mutable) {
            messages.add(CannotChangeImmutableVariable(assignment.sourceSection.toCodePoint()))
        }
        super.visitAssignment(assignment)
    }
}