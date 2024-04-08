package gh.marad.chi.core.expressionast

import gh.marad.chi.core.*

abstract class DefaultMappingVisitor : ExpressionVisitor<Expression> {
    override fun visit(expr: Expression): Expression = expr

    override fun visitAtom(atom: Atom): Expression {
        return atom
    }

    override fun visitInterpolatedString(interpolatedString: InterpolatedString): Expression {
        interpolatedString.children().forEach { it.accept(this) }
        return interpolatedString
    }

    override fun visitVariableAccess(variableAccess: VariableAccess): Expression {
        variableAccess.children().forEach { it.accept(this) }
        return variableAccess
    }

    override fun visitFieldAccess(fieldAccess: FieldAccess): Expression {
        fieldAccess.children().forEach { it.accept(this) }
        return fieldAccess
    }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment): Expression {
        fieldAssignment.children().forEach { it.accept(this) }
        return fieldAssignment
    }

    override fun visitAssignment(assignment: Assignment): Expression {
        assignment.children().forEach { it.accept(this) }
        return assignment
    }

    override fun visitNameDeclaration(nameDeclaration: NameDeclaration): Expression {
        nameDeclaration.children().forEach { it.accept(this) }
        return nameDeclaration
    }

    override fun visitFn(fn: Fn): Expression {
        fn.children().forEach { it.accept(this) }
        return fn
    }

    override fun visitBlock(block: Block): Expression {
        block.children().forEach { it.accept(this) }
        return block
    }

    override fun visitFnCall(fnCall: FnCall): Expression {
        fnCall.children().forEach { it.accept(this) }
        return fnCall
    }

    override fun visitIfElse(ifElse: IfElse): Expression {
        ifElse.children().forEach { it.accept(this) }
        return ifElse
    }

    override fun visitInfixOp(infixOp: InfixOp): Expression {
        infixOp.children().forEach { it.accept(this) }
        return infixOp
    }

    override fun visitPrefixOp(prefixOp: PrefixOp): Expression {
        prefixOp.children().forEach { it.accept(this) }
        return prefixOp
    }

    override fun visitCast(cast: Cast): Expression {
        cast.children().forEach { it.accept(this) }
        return cast
    }

    override fun visitWhileLoop(whileLoop: WhileLoop): Expression {
        whileLoop.children().forEach { it.accept(this) }
        return whileLoop
    }

    override fun visitBreak(arg: Break): Expression {
        arg.children().forEach { it.accept(this) }
        return arg
    }

    override fun visitContinue(arg: Continue): Expression {
        arg.children().forEach { it.accept(this) }
        return arg
    }

    override fun visitIndexOperator(indexOperator: IndexOperator): Expression {
        indexOperator.children().forEach { it.accept(this) }
        return indexOperator
    }

    override fun visitIndexedAssignment(indexedAssignment: IndexedAssignment): Expression {
        indexedAssignment.children().forEach { it.accept(this) }
        return indexedAssignment
    }

    override fun visitIs(arg: Is): Expression {
        arg.children().forEach { it.accept(this) }
        return arg
    }

    override fun visitEffectDefinition(effectDefinition: EffectDefinition): Expression {
        effectDefinition.children().forEach { it.accept(this) }
        return effectDefinition
    }

    override fun visitHandle(handle: Handle): Expression {
        handle.children().forEach { it.accept(this) }
        return handle
    }

    override fun visitReturn(arg: Return): Expression {
        arg.children().forEach { it.accept(this) }
        return arg
    }

    override fun visitCreateRecord(createRecord: CreateRecord): Expression {
        createRecord.children().forEach { it.accept(this) }
        return createRecord
    }

    override fun visitCreateArray(createArray: CreateArray): Expression {
        createArray.children().forEach { it.accept(this) }
        return createArray
    }
}