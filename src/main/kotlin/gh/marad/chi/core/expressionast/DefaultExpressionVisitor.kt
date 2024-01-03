package gh.marad.chi.core.expressionast

import gh.marad.chi.core.*

interface DefaultExpressionVisitor : ExpressionVisitor {
    override fun visit(expr: Expression) = expr.accept(this)

    override fun visitDefineVariantType(defineVariantType: DefineVariantType) { visit(defineVariantType) }

    override fun visitAtom(atom: Atom) { visit(atom) }

    override fun visitInterpolatedString(interpolatedString: InterpolatedString) {
        visit(interpolatedString)
        for (part in interpolatedString.parts) {
            part.accept(this)
        }
    }

    override fun visitVariableAccess(variableAccess: VariableAccess) { visit(variableAccess) }

    override fun visitFieldAccess(fieldAccess: FieldAccess) {
        fieldAccess.receiver.accept(this)
        visit(fieldAccess)
    }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) {
        fieldAssignment.receiver.accept(this)
        fieldAssignment.value.accept(this)
        visit(fieldAssignment)
    }

    override fun visitAssignment(assignment: Assignment) {
        assignment.value.accept(this)
        visit(assignment)
    }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) {
        nameDeclaration.value.accept(this)
        visit(nameDeclaration)
    }

    override fun visitGroup(group: Group) {
        group.value.accept(this)
        visit(group)
    }

    override fun visitFn(fn: Fn) {
        fn.body.accept(this)
        visit(fn)
    }

    override fun visitBlock(block: Block) {
        for (expression in block.body) {
            expression.accept(this)
        }
        visit(block)
    }

    override fun visitFnCall(fnCall: FnCall) {
        fnCall.function.accept(this)
        for (parameter in fnCall.parameters) {
            parameter.accept(this)
        }
        visit(fnCall)
    }

    override fun visitIfElse(ifElse: IfElse) {
        ifElse.condition.accept(this)
        ifElse.thenBranch.accept(this)
        ifElse.elseBranch?.accept(this)
        visit(ifElse)
    }

    override fun visitInfixOp(infixOp: InfixOp) {
        infixOp.left.accept(this)
        infixOp.right.accept(this)
        visit(infixOp)
    }

    override fun visitPrefixOp(prefixOp: PrefixOp) {
        prefixOp.expr.accept(this)
        visit(prefixOp)
    }

    override fun visitCast(cast: Cast) {
        cast.expression.accept(this)
        visit(cast)
    }

    override fun visitWhileLoop(whileLoop: WhileLoop) {
        whileLoop.condition.accept(this)
        whileLoop.loop.accept(this)
        visit(whileLoop)
    }

    override fun visitBreak(arg: Break) {
        visit(arg)
    }

    override fun visitContinue(arg: Continue) {
        visit(arg)
    }

    override fun visitIndexOperator(indexOperator: IndexOperator) {
        indexOperator.variable.accept(this)
        indexOperator.index.accept(this)
        visit(indexOperator)
    }

    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment) {
        indexedAssignment.variable.accept(this)
        indexedAssignment.index.accept(this)
        indexedAssignment.value.accept(this)
        visit(indexedAssignment)
    }

    override fun visitIs(arg: Is) {
        arg.value.accept(this)
        visit(arg)
    }

    override fun visitEffectDefinition(effectDefinition: EffectDefinition) {
        visit(effectDefinition)
    }

    override fun visitHandle(handle: Handle) {
        handle.body.accept(this)
        for (case in handle.cases) {
            case.body.accept(this)
        }
        visit(handle)
    }

    override fun visitReturn(arg: Return) {
        visit(arg)
    }
}