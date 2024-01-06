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

    override fun visitDefineVariantType(defineVariantType: DefineVariantType) {
        TODO("Not yet implemented")
    }

    override fun visitAtom(atom: Atom) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Atom ${atom.value} : ${atom.newType})")
    }

    override fun visitInterpolatedString(interpolatedString: InterpolatedString) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(InterpolatedString : ${interpolatedString.newType}")
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
        sb.append(variableAccess.name)
        sb.append(" : ")
        sb.append(variableAccess.newType)
        sb.append(")")
    }

    override fun visitFieldAccess(fieldAccess: FieldAccess) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(FieldAccess ")
        sb.append(fieldAccess.fieldName)
        sb.append(" : ")
        sb.append(fieldAccess.newType)
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
        sb.append(fieldAssignment.newType)
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
        sb.append(nameDeclaration.newType)
        withIndent {
            nameDeclaration.value.accept(this)
        }
        sb.append(')')
    }

    override fun visitGroup(group: Group) {
        TODO("Not yet implemented")
    }

    override fun visitFn(fn: Fn) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Fn : ${fn.newType}")
        withIndent {
            fn.body.accept(this)
        }
        sb.append(")")
    }

    override fun visitBlock(block: Block) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Block : ")
        sb.append(block.newType)
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
        sb.append("(FnCall : ${fnCall.newType}")
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
        sb.append("(If : ${ifElse.newType}")
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
        sb.append("(Cast : ${cast.newType}")
        withIndent {
            cast.expression.accept(this)
        }
        sb.append(")")
    }

    override fun visitWhileLoop(whileLoop: WhileLoop) {
        node("WhileLoop", whileLoop.newType) {
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
        node("Break", arg.newType)
    }

    override fun visitContinue(arg: Continue) {
        node("Continue", arg.newType)
    }

    override fun visitIndexOperator(indexOperator: IndexOperator) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(IndexOperator : ${indexOperator.newType}")
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
        sb.append("(IndexedAssignment : ${indexedAssignment.newType}")
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
        sb.append("(EffectDefinition ${effectDefinition.name} : ${effectDefinition.newType}")
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