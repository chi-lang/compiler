package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Assignment
import gh.marad.chi.core.Expression
import gh.marad.chi.core.LocalSymbol
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.analyzer.CannotChangeImmutableVariable
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.compiler.CompileTables
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.namespace.CompilationEnv

class ImmutabilityCheckVisitor(
    val currentModule: String,
    val currentPackage: String,
    val messages: MutableList<Message>,
    val tables: CompileTables,
    val ns: CompilationEnv) : DefaultExpressionVisitor {

    fun check(exprs: List<Expression>) {
        exprs.forEach(this::visit)
    }

    override fun visitAssignment(assignment: Assignment) {
        val mutable = when(assignment.target) {
            is LocalSymbol -> tables.getLocalSymbol(assignment.target.name)?.mutable
            is PackageSymbol -> {
                val target = assignment.target
                if (target.moduleName == currentModule && target.packageName == currentPackage) {
                    tables.getLocalSymbol(assignment.target.name)?.mutable
                } else {
                    ns.getSymbol(assignment.target)?.mutable
                }
            }
        }

        if (mutable != null && !mutable) {
            messages.add(CannotChangeImmutableVariable(assignment.sourceSection.toCodePoint()))
        }
        super.visitAssignment(assignment)
    }
}