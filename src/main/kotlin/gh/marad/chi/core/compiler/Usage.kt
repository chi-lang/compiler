package gh.marad.chi.core.compiler

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ExpressionVisitor

fun markUsed(terms: List<Expression>) {
    val marker = UsageMarker()
    terms.forEach { marker.visit(it) }
}

fun markUsed(term: Expression) {
    UsageMarker().visit(term)
}

class UsageMarker : ExpressionVisitor {
    override fun visitCreateRecord(createRecord: CreateRecord) {
        if (createRecord.used) {
            createRecord.fields.forEach { it.value.used = true }
        }
        visitChildren(createRecord)
    }

    override fun visitCreateArray(createArray: CreateArray) {
        if (createArray.used) {
            createArray.values.forEach { it.used = true }
        }
        visitChildren(createArray)
    }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) {
        nameDeclaration.value.used = true
        visitChildren(nameDeclaration)
    }

    override fun visitFn(fn: Fn) {
        visitChildren(fn)
    }

    override fun visitAssignment(assignment: Assignment) {
        assignment.value.used = true
        visitChildren(assignment)
    }

    override fun visit(expr: Expression) {
        expr.accept(this)
    }

    fun visitChildren(expr: Expression) {
        expr.children().forEach(this::visit)
    }

    override fun visitAtom(atom: Atom) {}

    override fun visitInterpolatedString(interpolatedString: InterpolatedString) {
        visitChildren(interpolatedString)
    }

    override fun visitVariableAccess(variableAccess: VariableAccess) {
        visitChildren(variableAccess)
    }

    override fun visitFieldAccess(fieldAccess: FieldAccess) {
        visitChildren(fieldAccess)
    }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) {
        fieldAssignment.value.used = true
        visitChildren(fieldAssignment)
    }

    override fun visitBlock(block: Block) {
        block.body.lastOrNull()?.let {
            it.used = true
        }
        visitChildren(block)
    }

    override fun visitFnCall(fnCall: FnCall) {
        fnCall.parameters.forEach {
            it.used = true
        }
        visitChildren(fnCall)
    }

    override fun visitIfElse(ifElse: IfElse) {
        ifElse.thenBranch.used = ifElse.used
        ifElse.elseBranch?.used = ifElse.used
        visitChildren(ifElse)
    }

    override fun visitInfixOp(infixOp: InfixOp) {
        infixOp.left.used = infixOp.used
        infixOp.right.used = infixOp.used
        visitChildren(infixOp)
    }

    override fun visitPrefixOp(prefixOp: PrefixOp) {
        prefixOp.expr.used = prefixOp.used
        visitChildren(prefixOp)
    }

    override fun visitCast(cast: Cast) {
        cast.expression.used = cast.used
        visitChildren(cast)
    }

    override fun visitWhileLoop(whileLoop: WhileLoop) {
        visitChildren(whileLoop)
    }

    override fun visitBreak(arg: Break) {}

    override fun visitContinue(arg: Continue) {}

    override fun visitIndexOperator(indexOperator: IndexOperator) {
        indexOperator.index.used = true
        visitChildren(indexOperator)
    }

    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment) {
        indexedAssignment.index.used = true
        indexedAssignment.value.used = true
        visitChildren(indexedAssignment)
    }

    override fun visitIs(arg: Is) {
        arg.value.used = arg.used
        visitChildren(arg)
    }

    override fun visitEffectDefinition(effectDefinition: EffectDefinition) {
        visitChildren(effectDefinition)
    }

    override fun visitHandle(handle: Handle) {
        handle.body.used = handle.used
        visitChildren(handle)
    }

    override fun visitReturn(arg: Return) {
        arg.value?.used = true
        visitChildren(arg)
    }
}