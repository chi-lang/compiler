package gh.marad.chi.core.utils

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ExpressionVisitor

fun printAst(expr: Expression) {
    val visitor = PrintAstVisitor()
    expr.accept(visitor)
    println(visitor.toString().trim())
}

class PrintAstVisitor : ExpressionVisitor {
    var indent = ""
    val sb = StringBuilder()

    override fun toString(): String {
        return sb.toString()
    }

    override fun visit(expr: Expression) = expr.accept(this)

    override fun visitProgram(program: Program) {
        for (expression in program.expressions) {
            expression.accept(this)
        }
    }

    override fun visitPackage(arg: Package) {
        TODO("Not yet implemented")
    }

    override fun visitImport(import: Import) {
        TODO("Not yet implemented")
    }

    override fun visitDefineVariantType(defineVariantType: DefineVariantType) {
        TODO("Not yet implemented")
    }

    override fun visitAtom(atom: Atom) {
        sb.appendLine()
        sb.append(indent)
        sb.append("(Atom ${atom.value} : ${atom.newType})")
    }

    override fun visitInterpolatedString(interpolatedString: InterpolatedString) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) {
        TODO("Not yet implemented")
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
        sb.append(')')
    }

    override fun visitGroup(group: Group) {
        TODO("Not yet implemented")
    }

    override fun visitFn(fn: Fn) {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun visitWhileLoop(whileLoop: WhileLoop) {
        TODO("Not yet implemented")
    }

    override fun visitBreak(arg: Break) {
        TODO("Not yet implemented")
    }

    override fun visitContinue(arg: Continue) {
        TODO("Not yet implemented")
    }

    override fun visitIndexOperator(indexOperator: IndexOperator) {
        TODO("Not yet implemented")
    }

    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment) {
        TODO("Not yet implemented")
    }

    override fun visitIs(arg: Is) {
        TODO("Not yet implemented")
    }

    override fun visitEffectDefinition(effectDefinition: EffectDefinition) {
        TODO("Not yet implemented")
    }

    override fun visitHandle(handle: Handle) {
        TODO("Not yet implemented")
    }

    override fun visitReturn(arg: Return) {
        TODO("Not yet implemented")
    }
}