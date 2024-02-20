package gh.marad.chi.core.parser.visitor

import gh.marad.chi.core.parser.readers.*

open class DefaultParseAstVisitor : ParseAstVisitor<Unit> {
    override fun visit(node: ParseAst) {
        node.accept(this)
    }

    override fun visitUnit(unitValue: UnitValue) =
        unitValue.children().forEach { it.accept(this) }

    override fun visitLong(longValue: LongValue) =
        longValue.children().forEach { it.accept(this) }

    override fun visitFloat(floatValue: FloatValue) =
        floatValue.children().forEach { it.accept(this) }

    override fun visitBool(boolValue: BoolValue) =
        boolValue.children().forEach { it.accept(this) }

    override fun visitString(stringValue: StringValue) =
        stringValue.children().forEach { it.accept(this) }

    override fun visitInterpolatedString(parseInterpolatedString: ParseInterpolatedString) =
        parseInterpolatedString.children().forEach { it.accept(this) }

    override fun visitStringInterpolation(parseInterpolation: ParseInterpolation) =
        parseInterpolation.children().forEach { it.accept(this) }

    override fun visitInterpolatedStringText(stringText: StringText) =
        stringText.children().forEach { it.accept(this) }

    override fun visitLambda(parseLambda: ParseLambda) =
        parseLambda.children().forEach { it.accept(this) }

    override fun visitFuncWithName(parseFuncWithName: ParseFuncWithName) =
        parseFuncWithName.children().forEach { it.accept(this) }

    override fun visitFnCall(parseFnCall: ParseFnCall) =
        parseFnCall.children().forEach { it.accept(this) }

    override fun visitAssignment(parseAssignment: ParseAssignment) =
        parseAssignment.children().forEach { it.accept(this) }

    override fun visitIndexedAssignment(parseIndexedAssignment: ParseIndexedAssignment) =
        parseIndexedAssignment.children().forEach { it.accept(this) }

    override fun visitVariableRead(parseVariableRead: ParseVariableRead) =
        parseVariableRead.children().forEach { it.accept(this) }

    override fun visitIndexOperator(parseIndexOperator: ParseIndexOperator) =
        parseIndexOperator.children().forEach { it.accept(this) }

    override fun visitNameDeclaration(parseNameDeclaration: ParseNameDeclaration) =
        parseNameDeclaration.children().forEach { it.accept(this) }

    override fun visitFieldAccess(parseFieldAccess: ParseFieldAccess) =
        parseFieldAccess.children().forEach { it.accept(this) }

    override fun visitFieldAssignment(parseFieldAssignment: ParseFieldAssignment) =
        parseFieldAssignment.children().forEach { it.accept(this) }

    override fun visitEffectDefinition(parseEffectDefinition: ParseEffectDefinition) =
        parseEffectDefinition.children().forEach { it.accept(this) }

    override fun visitHandle(parseHandle: ParseHandle) =
        parseHandle.children().forEach { it.accept(this) }

    override fun visitNot(parseNot: ParseNot) =
        parseNot.children().forEach { it.accept(this) }

    override fun visitBinaryOperator(parseBinaryOp: ParseBinaryOp) =
        parseBinaryOp.children().forEach { it.accept(this) }

    override fun visitBlock(parseBlock: ParseBlock) =
        parseBlock.children().forEach { it.accept(this) }

    override fun visitCast(parseCast: ParseCast) =
        parseCast.children().forEach { it.accept(this) }

    override fun visitGroup(parseGroup: ParseGroup) =
        parseGroup.children().forEach { it.accept(this) }

    override fun visitIs(parseIs: ParseIs) =
        parseIs.children().forEach { it.accept(this) }

    override fun visitIfElse(parseIfElse: ParseIfElse) =
        parseIfElse.children().forEach { it.accept(this) }

    override fun visitWhile(parseWhile: ParseWhile) =
        parseWhile.children().forEach { it.accept(this) }

    override fun visitBreak(parseBreak: ParseBreak) =
        parseBreak.children().forEach { it.accept(this) }

    override fun visitContinue(parseContinue: ParseContinue) =
        parseContinue.children().forEach { it.accept(this) }

    override fun visitWhen(parseWhen: ParseWhen) =
        parseWhen.children().forEach { it.accept(this) }

    override fun visitWeave(parseWeave: ParseWeave) =
        parseWeave.children().forEach { it.accept(this) }

    override fun visitPlaceholder(parseWeavePlaceholder: ParseWeavePlaceholder) =
        parseWeavePlaceholder.children().forEach { it.accept(this) }

    override fun visitReturn(parseReturn: ParseReturn) =
        parseReturn.children().forEach { it.accept(this) }

    override fun visitCreateRecord(parseCreateRecord: ParseCreateRecord) =
        parseCreateRecord.children().forEach { it.accept(this) }
}