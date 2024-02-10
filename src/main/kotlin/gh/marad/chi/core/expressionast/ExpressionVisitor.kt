package gh.marad.chi.core.expressionast

import gh.marad.chi.core.*

interface ExpressionVisitor {
    fun visit(expr: Expression)
    fun visitAtom(atom: Atom)
    fun visitInterpolatedString(interpolatedString: InterpolatedString)
    fun visitVariableAccess(variableAccess: VariableAccess)
    fun visitFieldAccess(fieldAccess: FieldAccess)
    fun visitFieldAssignment(fieldAssignment: FieldAssignment)
    fun visitAssignment(assignment: Assignment)
    fun visitNameDeclaration(nameDeclaration: NameDeclaration)
    fun visitFn(fn: Fn)
    fun visitBlock(block: Block)
    fun visitFnCall(fnCall: FnCall)
    fun visitIfElse(ifElse: IfElse)
    fun visitInfixOp(infixOp: InfixOp)
    fun visitPrefixOp(prefixOp: PrefixOp)
    fun visitCast(cast: Cast)
    fun visitWhileLoop(whileLoop: WhileLoop)
    fun visitBreak(arg: Break)
    fun visitContinue(arg: Continue)
    fun visitIndexOperator(indexOperator: IndexOperator)
    fun visitIndexedAssignment(indexedAssignment: IndexedAssignment)
    fun visitIs(arg: Is)
    fun visitEffectDefinition(effectDefinition: EffectDefinition)
    fun visitHandle(handle: Handle)
    fun visitReturn(arg: Return)
    fun visitCreateRecord(createRecord: CreateRecord)
}