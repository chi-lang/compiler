package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Fn
import gh.marad.chi.core.NameDeclaration
import gh.marad.chi.core.Return
import gh.marad.chi.core.analyzer.ErrorMessage
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.types3.Function
import gh.marad.chi.core.types3.Type3

class ReturnTypeCheckVisitor(val messages: MutableList<Message>) : DefaultExpressionVisitor {
    private var expectedReturnType: Type3? = null

    fun check(exprs: List<Expression>) {
        exprs.forEach(this::visit)
    }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) {
        val t = nameDeclaration.newType!!
        val prevExpectedReturnType = expectedReturnType
        if (t is Function) {
            expectedReturnType = t.types.last()
        }

        visitAll(nameDeclaration.value.children())
        expectedReturnType = prevExpectedReturnType
    }

    override fun visitFn(fn: Fn) {
        val t = fn.newType!!
        val prevReturnType = expectedReturnType
        t as Function
        expectedReturnType = t.types.last()
        super.visitFn(fn)
        expectedReturnType = prevReturnType
    }

    override fun visitReturn(arg: Return) {
        val expRet = expectedReturnType
        if (expRet == null) {
            messages.add(ErrorMessage("Return used outside of function body.", arg.sourceSection.toCodePoint()))
        }

        if (expRet != null && arg.newType!! != expRet) {
            messages.add(TypeMismatch(expRet, arg.newType!!, arg.sourceSection.toCodePoint()))
        }
    }

}