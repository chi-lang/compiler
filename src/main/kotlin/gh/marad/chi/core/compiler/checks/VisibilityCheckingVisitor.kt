package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Expression
import gh.marad.chi.core.LocalSymbol
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.VariableAccess
import gh.marad.chi.core.analyzer.CannotAccessInternalName
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.namespace.GlobalCompilationNamespace

class VisibilityCheckingVisitor(
    private val currentModule: String,
    private val ns: GlobalCompilationNamespace,
) : DefaultExpressionVisitor {
    private var messages = mutableListOf<Message>()

    fun check(exprs: List<Expression>, messages: MutableList<Message>) {
        this.messages = messages
        exprs.forEach(this::visit)
    }

    override fun visitVariableAccess(variableAccess: VariableAccess) {
        when(variableAccess.target) {
            is LocalSymbol -> {} // local symbols can be accessed normally
            is PackageSymbol -> {
                val symbol = ns.getSymbol(
                    variableAccess.target.moduleName,
                    variableAccess.target.packageName,
                    variableAccess.target.name)
                if (variableAccess.target.moduleName != currentModule && symbol?.public == false) {
                    messages.add(CannotAccessInternalName(
                        variableAccess.target.toString(),
                        variableAccess.sourceSection.toCodePoint()))
                }
            }
        }
    }
}