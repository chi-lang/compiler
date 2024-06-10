package gh.marad.chi.core.expressionast

import gh.marad.chi.core.*

interface ExpressionVisitor<T> {
    fun visit(expr: Expression): T
    fun visitAtom(atom: Atom): T
    fun visitInterpolatedString(interpolatedString: InterpolatedString): T
    fun visitVariableAccess(variableAccess: VariableAccess): T
    fun visitFieldAccess(fieldAccess: FieldAccess): T
    fun visitFieldAssignment(fieldAssignment: FieldAssignment): T
    fun visitAssignment(assignment: Assignment): T
    fun visitNameDeclaration(nameDeclaration: NameDeclaration): T
    fun visitFn(fn: Fn): T
    fun visitBlock(block: Block): T
    fun visitFnCall(fnCall: FnCall): T
    fun visitIfElse(ifElse: IfElse): T
    fun visitInfixOp(infixOp: InfixOp): T
    fun visitPrefixOp(prefixOp: PrefixOp): T
    fun visitCast(cast: Cast): T
    fun visitWhileLoop(whileLoop: WhileLoop): T
    fun visitBreak(arg: Break): T
    fun visitContinue(arg: Continue): T
    fun visitIndexOperator(indexOperator: IndexOperator): T
    fun visitIndexedAssignment(indexedAssignment: IndexedAssignment): T
    fun visitIs(arg: Is): T
    fun visitEffectDefinition(effectDefinition: EffectDefinition): T
    fun visitHandle(handle: Handle): T
    fun visitReturn(arg: Return): T
    fun visitCreateRecord(createRecord: CreateRecord): T
    fun visitCreateArray(createArray: CreateArray): T
    fun visitForLoop(forLoop: ForLoop): T
}