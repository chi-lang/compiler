package gh.marad.chi.core.parser.visitor

import gh.marad.chi.core.parser.readers.*

interface ParseAstVisitor<T> {
    fun visit(node: ParseAst): T
    fun visitProgram(program: ParseProgram): T
    fun visitPackageDefinition(parsePackageDefinition: ParsePackageDefinition): T
    fun visitImportDefinition(parseImportDefinition: ParseImportDefinition): T
    fun visitVariantTypeDefinition(parseVariantTypeDefinition: ParseVariantTypeDefinition): T
    fun visitTraitDefinition(parseTraitDefinition: ParseTraitDefinition): T
    fun visitTraitFunctionDefinition(parseTraitFunctionDefinition: ParseTraitFunctionDefinition): T
    fun visitLong(longValue: LongValue): T
    fun visitFloat(floatValue: FloatValue): T
    fun visitBool(boolValue: BoolValue): T
    fun visitString(stringValue: StringValue): T
    fun visitInterpolatedString(parseInterpolatedString: ParseInterpolatedString): T
    fun visitStringInterpolation(parseInterpolation: ParseInterpolation): T
    fun visitInterpolatedStringText(stringText: StringText): T
    fun visitLambda(parseLambda: ParseLambda): T
    fun visitFuncWithName(parseFuncWithName: ParseFuncWithName): T
    fun visitFnCall(parseFnCall: ParseFnCall): T
    fun visitAssignment(parseAssignment: ParseAssignment): T
    fun visitIndexedAssignment(parseIndexedAssignment: ParseIndexedAssignment): T
    fun visitVariableRead(parseVariableRead: ParseVariableRead): T
    fun visitIndexOperator(parseIndexOperator: ParseIndexOperator): T
    fun visitNameDeclaration(parseNameDeclaration: ParseNameDeclaration): T
    fun visitFieldAccess(parseFieldAccess: ParseFieldAccess): T
    fun visitFieldAssignment(parseFieldAssignment: ParseFieldAssignment): T
    fun visitMethodInvocation(parseMethodInvocation: ParseMethodInvocation): T
    fun visitEffectDefinition(parseEffectDefinition: ParseEffectDefinition): T
    fun visitHandle(parseHandle: ParseHandle): T
    fun visitNot(parseNot: ParseNot): T
    fun visitBinaryOperator(parseBinaryOp: ParseBinaryOp): T
    fun visitBlock(parseBlock: ParseBlock): T
    fun visitCast(parseCast: ParseCast): T
    fun visitGroup(parseGroup: ParseGroup): T
    fun visitIs(parseIs: ParseIs): T
    fun visitIfElse(parseIfElse: ParseIfElse): T
    fun visitWhile(parseWhile: ParseWhile): T
    fun visitBreak(parseBreak: ParseBreak): T
    fun visitContinue(parseContinue: ParseContinue): T
    fun visitWhen(parseWhen: ParseWhen): T
    fun visitWeave(parseWeave: ParseWeave): T
    fun visitPlaceholder(parseWeavePlaceholder: ParseWeavePlaceholder): T
    fun visitReturn(parseReturn: ParseReturn): T
}