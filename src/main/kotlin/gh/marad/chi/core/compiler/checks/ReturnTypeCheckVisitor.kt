package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Fn
import gh.marad.chi.core.NameDeclaration
import gh.marad.chi.core.Return
import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.analyzer.ErrorMessage
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.types.Constraint
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.unify

class ReturnTypeCheckVisitor(val messages: MutableList<Message>) : DefaultExpressionVisitor {
    private var expectedReturnType: Type? = null

    fun check(exprs: List<Expression>) {
        exprs.forEach(this::visit)
    }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) {
        // Use the value's (Fn's) inferred type rather than the NameDeclaration's type.
        // When a named function omits its return type annotation, the NameDeclaration
        // defaults to `unit` but the Fn's type is updated by type inference to the
        // actual inferred return type.
        val t = nameDeclaration.value.type ?: nameDeclaration.type!!
        val prevExpectedReturnType = expectedReturnType
        if (t is Function) {
            expectedReturnType = t.types.last()
        }

        visitAll(nameDeclaration.value.children())
        expectedReturnType = prevExpectedReturnType
    }

    override fun visitFn(fn: Fn) {
        val t = fn.type!!
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
            return
        }

        val actualType = arg.type!!
        if (actualType != expRet) {
            // Types are not structurally equal, but the actual type might still be
            // compatible (e.g., returning `string` when expected type is `string | unit`).
            // Use unification to check compatibility.
            try {
                unify(listOf(Constraint(expRet, actualType, arg.sourceSection, emptyList())))
            } catch (ex: CompilerMessage) {
                messages.add(TypeMismatch(expRet, actualType, arg.sourceSection.toCodePoint()))
            }
        }
    }

}