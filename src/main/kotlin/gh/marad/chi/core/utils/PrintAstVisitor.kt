package gh.marad.chi.core.utils

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ExpressionVisitor
import gh.marad.chi.core.types.Type

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

class PrintAstVisitor : ExpressionVisitor {
    var indent = ""
    val sb = StringBuilder()

    override fun toString(): String {
        return sb.toString()
    }

    override fun visit(expr: Expression) = expr.accept(this)

    override fun visitAtom(atom: Atom) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Atom ${atom.value} : ${atom.type})")
    }

    override fun visitInterpolatedString(interpolatedString: InterpolatedString) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(InterpolatedString : ${interpolatedString.type}")
        withIndent {
            for (part in interpolatedString.parts) {
                part.accept(this)
            }
        }
        sb.append(")")
    }

    override fun visitVariableAccess(variableAccess: VariableAccess) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(VariableAccess ")
        sb.append(variableAccess.target)
        sb.append(" : ")
        sb.append(variableAccess.type)
        sb.append(")")
    }

    override fun visitFieldAccess(fieldAccess: FieldAccess) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(FieldAccess ")
        sb.append(fieldAccess.fieldName)
        sb.append(" : ")
        sb.append(fieldAccess.type)
        withIndent {
            fieldAccess.receiver.accept(this)
        }
        sb.append(")")
    }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(FieldAssignment ")
        sb.append(fieldAssignment.fieldName)
        sb.append(" : ")
        sb.append(fieldAssignment.type)
        withIndent {
            fieldAssignment.receiver.accept(this)
            fieldAssignment.value.accept(this)
        }
        sb.append(")")
    }

    override fun visitAssignment(assignment: Assignment) {
        TODO("Not yet implemented")
    }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(NameDeclaration ")
        sb.append(nameDeclaration.name)
        sb.append(" : ")
        sb.append(nameDeclaration.type)
        withIndent {
            nameDeclaration.value.accept(this)
        }
        sb.append(')')
    }

    override fun visitFn(fn: Fn) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Fn : ${fn.type}")
        withIndent {
            fn.body.accept(this)
        }
        sb.append(")")
    }

    override fun visitBlock(block: Block) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Block : ")
        sb.append(block.type)
        val prev = indent
        indent = "$indent\t"
        for (expression in block.body) {
            expression.accept(this)
        }
        sb.append(')')
        indent = prev
    }

    override fun visitFnCall(fnCall: FnCall) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(FnCall : ${fnCall.type}")
        val prev = indent
        indent = "$indent\t"
        fnCall.function.accept(this)
        for (parameter in fnCall.parameters) {
            parameter.accept(this)
        }
        sb.append(')')
        indent = prev
    }

    override fun visitIfElse(ifElse: IfElse) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(If : ${ifElse.type}")
        val prev = indent
        indent = "$indent\t"
        ifElse.thenBranch.accept(this)
        ifElse.elseBranch?.accept(this)
        sb.append(')')
        indent = prev
    }

    override fun visitInfixOp(infixOp: InfixOp) {
        TODO("Not yet implemented")
    }

    override fun visitPrefixOp(prefixOp: PrefixOp) {
        TODO("Not yet implemented")
    }

    override fun visitCast(cast: Cast) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Cast : ${cast.type}")
        withIndent {
            cast.expression.accept(this)
        }
        sb.append(")")
    }

    override fun visitWhileLoop(whileLoop: WhileLoop) {
        node("WhileLoop", whileLoop.type) {
            whileLoop.condition.accept(this)
            whileLoop.loop.accept(this)
        }
    }

    private fun node(name: String, type: Type?, f: () -> Unit = {}) {
        sb.appendLine()
        sb.append(indent)
        sb.append("($name : $type")
        withIndent(f)
        sb.append(")")
    }

    override fun visitBreak(arg: Break) {
        node("Break", arg.type)
    }

    override fun visitContinue(arg: Continue) {
        node("Continue", arg.type)
    }

    override fun visitIndexOperator(indexOperator: IndexOperator) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(IndexOperator : ${indexOperator.type}")
        val prev = indent
        indent = "$indent\t"
        indexOperator.variable.accept(this)
        indexOperator.index.accept(this)
        sb.append(')')
        indent = prev
    }

    private fun withIndent(f: () -> Unit) {
        val prev = indent
        indent = "$indent\t"
        f()
        indent = prev
    }

    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(IndexedAssignment : ${indexedAssignment.type}")
        val prev = indent
        indent = "$indent\t"
        indexedAssignment.variable.accept(this)
        indexedAssignment.index.accept(this)
        indexedAssignment.value.accept(this)
        sb.append(')')
        indent = prev
    }

    override fun visitIs(arg: Is) {
        TODO("Not yet implemented")
    }

    override fun visitEffectDefinition(effectDefinition: EffectDefinition) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(EffectDefinition ${effectDefinition.name} : ${effectDefinition.type}")
        withIndent {
            effectDefinition.parameters.forEach {
                sb.appendLine()
                sb.append(indent)
                sb.append(it.name)
                sb.append(" : ")
                sb.append(it.type)
            }
        }
        sb.append(')')
    }

    override fun visitHandle(handle: Handle) {
        TODO("Not yet implemented")
    }

    override fun visitReturn(arg: Return) {
        TODO("Not yet implemented")
    }
}