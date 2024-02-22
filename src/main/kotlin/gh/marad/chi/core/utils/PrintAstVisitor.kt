@file:Suppress("unused")

package gh.marad.chi.core.utils

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ExpressionVisitor

fun printAst(expr: Expression) {
    val visitor = PrintAstVisitor()
    expr.accept(visitor)
    println(visitor.toString().trim())
}

fun printAst(exprs: List<Expression>) {
    val visitor = PrintAstVisitor()
    for (expr in exprs) {
        expr.accept(visitor)
    }
    println(visitor.toString().trim())
}

fun showAst(exprs: List<Expression>): String {
    val visitor = PrintAstVisitor()
    for (expr in exprs) {
        expr.accept(visitor)
    }
    return visitor.toString().trim()
}

class PrintAstVisitor : ExpressionVisitor {
    var indent = ""
    val sb = StringBuilder()

    override fun toString(): String {
        return sb.toString()
    }

    override fun visit(expr: Expression) = expr.accept(this)
    override fun visitAtom(atom: Atom) = node(atom)
    override fun visitInterpolatedString(interpolatedString: InterpolatedString) = node(interpolatedString)
    override fun visitVariableAccess(variableAccess: VariableAccess) = node(variableAccess, variableAccess.target.toString())
    override fun visitFieldAccess(fieldAccess: FieldAccess) = node(fieldAccess, fieldAccess.fieldName)
    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) = node(fieldAssignment)
    override fun visitAssignment(assignment: Assignment) = node(assignment)
    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) = node(nameDeclaration, nameDeclaration.name)
    override fun visitFn(fn: Fn) = node(fn)
    override fun visitBlock(block: Block) = node(block)
    override fun visitFnCall(fnCall: FnCall) = node(fnCall)
    override fun visitIfElse(ifElse: IfElse) = node(ifElse)
    override fun visitInfixOp(infixOp: InfixOp) = node(infixOp)
    override fun visitPrefixOp(prefixOp: PrefixOp) = node(prefixOp)
    override fun visitCast(cast: Cast) = node(cast)
    override fun visitWhileLoop(whileLoop: WhileLoop) = node(whileLoop)
    override fun visitBreak(arg: Break) = node(arg)
    override fun visitContinue(arg: Continue) = node(arg)
    override fun visitIndexOperator(indexOperator: IndexOperator) = node(indexOperator)
    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment) = node(indexedAssignment)
    override fun visitIs(arg: Is) = node(arg)
    override fun visitHandle(handle: Handle) = node(handle)
    override fun visitReturn(arg: Return) = node(arg)
    override fun visitCreateRecord(createRecord: CreateRecord) = node(createRecord)
    override fun visitCreateArray(createArray: CreateArray) = node(createArray)

    override fun visitEffectDefinition(effectDefinition: EffectDefinition) {
        node(effectDefinition) {
            effectDefinition.parameters.forEach {
                sb.appendLine()
                sb.append(indent)
                sb.append(it.name)
                sb.append(" : ")
                sb.append(it.type)
            }
        }
    }

    private fun node(expr: Expression, name: String? = null) {
        node(expr, name) {
            expr.children().forEach { it.accept(this) }
        }
    }

    private fun node(expr: Expression, name: String? = null, f: () -> Unit = {}) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(${expr.javaClass.simpleName} ")
        if (name != null) {
            sb.append(name)
            sb.append(' ')
        }
        sb.append(": ${expr.type}")
        withIndent(f)
        sb.append(")")
    }


    private fun withIndent(f: () -> Unit) {
        val prev = indent
        indent = "$indent\t"
        f()
        indent = prev
    }

}