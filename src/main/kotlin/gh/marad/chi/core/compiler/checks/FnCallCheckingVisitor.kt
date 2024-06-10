package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Expression
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.NotAFunction
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.types.Function

class FnCallCheckingVisitor : DefaultExpressionVisitor {
    private var messages = mutableListOf<Message>()
    fun check(exprs: List<Expression>, messages: MutableList<Message>) {
        this.messages = messages
        exprs.forEach(this::visit)
    }

    override fun visitFnCall(fnCall: FnCall) {
        val t = fnCall.function.type!!
        if (t !is Function) {
            messages.add(NotAFunction(fnCall.function.sourceSection.toCodePoint()))
        }
        super.visitFnCall(fnCall)
    }

}