package gh.marad.chi.core.expressionast

import gh.marad.chi.core.*

interface DefaultExpressionVisitor : ExpressionVisitor {
    fun visitAll(exprs: List<Expression>) = exprs.forEach(this::visit)
    override fun visit(expr: Expression) = expr.accept(this)

    override fun visitAtom(atom: Atom) =
        atom.children().forEach { visit(it) }

    override fun visitInterpolatedString(interpolatedString: InterpolatedString) =
        interpolatedString.children().forEach { visit(it) }

    override fun visitVariableAccess(variableAccess: VariableAccess) =
        variableAccess.children().forEach { visit(it) }

    override fun visitFieldAccess(fieldAccess: FieldAccess) =
        fieldAccess.children().forEach { visit(it) }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) =
        fieldAssignment.children().forEach { visit(it) }

    override fun visitAssignment(assignment: Assignment) =
        assignment.children().forEach { visit(it) }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration) =
        nameDeclaration.children().forEach { visit(it) }

    override fun visitFn(fn: Fn) =
        fn.children().forEach { visit(it) }

    override fun visitBlock(block: Block) =
        block.children().forEach { visit(it) }

    override fun visitFnCall(fnCall: FnCall) =
        fnCall.children().forEach { visit(it) }

    override fun visitIfElse(ifElse: IfElse) =
        ifElse.children().forEach { visit(it) }

    override fun visitInfixOp(infixOp: InfixOp) =
        infixOp.children().forEach { visit(it) }

    override fun visitPrefixOp(prefixOp: PrefixOp) =
        prefixOp.children().forEach { visit(it) }

    override fun visitCast(cast: Cast) =
        cast.children().forEach { visit(it) }

    override fun visitWhileLoop(whileLoop: WhileLoop) =
        whileLoop.children().forEach { visit(it) }

    override fun visitBreak(arg: Break) =
        arg.children().forEach { visit(it) }

    override fun visitContinue(arg: Continue) =
        arg.children().forEach { visit(it) }

    override fun visitIndexOperator(indexOperator: IndexOperator) =
        indexOperator.children().forEach { visit(it) }

    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment) =
        indexedAssignment.children().forEach { visit(it) }

    override fun visitIs(arg: Is) =
        arg.children().forEach { visit(it) }

    override fun visitEffectDefinition(effectDefinition: EffectDefinition) =
        effectDefinition.children().forEach { visit(it) }

    override fun visitHandle(handle: Handle) =
        handle.children().forEach { visit(it) }

    override fun visitReturn(arg: Return) =
        arg.children().forEach { visit(it) }
}