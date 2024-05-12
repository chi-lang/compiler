package gh.marad.chi.core.parser

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.antlr.ChiParserBaseVisitor
import gh.marad.chi.core.parser.readers.*
import org.antlr.v4.runtime.tree.TerminalNode

internal class ParserVisitor(private val source: ChiSource) : ChiParserBaseVisitor<ParseAst>() {

    override fun visitProgram(ctx: ChiParser.ProgramContext): ParseAst {
        ctx.removeLastChild() // remove EOF
        val body = ctx.children.mapNotNull { it.accept(this) }
        return ParseBlock(body, getSection(source, ctx))
    }

    override fun visitTerminal(node: TerminalNode): ParseAst? =
        AtomReader.readTerminal(source, node)

    override fun visitString(ctx: ChiParser.StringContext): ParseAst =
        AtomReader.readString(this, source, ctx)

    override fun visitNameDeclarationExpr(ctx: ChiParser.NameDeclarationExprContext): ParseAst =
        NameDeclarationReader.read(this, source, ctx.name_declaration())

    override fun visitWhenExpression(ctx: ChiParser.WhenExpressionContext): ParseAst =
        WhenReader.read(this, source, ctx)

    override fun visitGroupExpr(ctx: ChiParser.GroupExprContext): ParseAst =
        GroupReader.read(this, source, ctx)

    override fun visitIf_expr(ctx: ChiParser.If_exprContext): ParseAst {
        return IfElseReader.read(this, source, ctx)
    }

    override fun visitLambda(ctx: ChiParser.LambdaContext): ParseAst =
        FuncReader.readLambda(this, source, ctx)

    override fun visitFunc_with_name(ctx: ChiParser.Func_with_nameContext): ParseAst =
        FuncReader.readFuncWithName(this, source, ctx)

    override fun visitFnCallExpr(ctx: ChiParser.FnCallExprContext): ParseAst =
        FuncReader.readFnCall(this, source, ctx)

    override fun visitFnCallLambdaExpr(ctx: ChiParser.FnCallLambdaExprContext): ParseAst =
        FuncReader.readFnCallWithLambda(this, source, ctx)

    override fun visitBlock(ctx: ChiParser.BlockContext): ParseAst =
        BlockReader.read(this, source, ctx)

    override fun visitIndexOperator(ctx: ChiParser.IndexOperatorContext): ParseAst =
        VariableReader.readVariableIndexed(this, source, ctx)

    override fun visitAssignment(ctx: ChiParser.AssignmentContext): ParseAst =
        VariableReader.readAssignment(this, source, ctx)

    override fun visitIndexedAssignment(ctx: ChiParser.IndexedAssignmentContext): ParseAst =
        VariableReader.readIndexedAssignment(this, source, ctx)

    override fun visitNotOp(ctx: ChiParser.NotOpContext): ParseAst =
        ArithmeticLogicReader.readNot(this, source, ctx)

    override fun visitBinOp(ctx: ChiParser.BinOpContext): ParseAst =
        ArithmeticLogicReader.readBinaryOp(this, source, ctx)

    override fun visitOpEqualExpr(ctx: ChiParser.OpEqualExprContext): ParseAst =
        OpEqualReader.readAssignment(this, source, ctx)

    override fun visitCast(ctx: ChiParser.CastContext): ParseAst =
        CastReader.readCast(this, source, ctx)

    override fun visitFieldAccessExpr(ctx: ChiParser.FieldAccessExprContext): ParseAst =
        FieldOperatorReader.readFieldAccess(this, source, ctx)

    override fun visitFieldAssignment(ctx: ChiParser.FieldAssignmentContext): ParseAst =
        FieldOperatorReader.readFieldAssignment(this, source, ctx)

    override fun visitWhileLoopExpr(ctx: ChiParser.WhileLoopExprContext): ParseAst =
        WhileReader.readWhile(this, source, ctx)

    override fun visitBreakExpr(ctx: ChiParser.BreakExprContext): ParseAst =
        WhileReader.readBreak(source, ctx)

    override fun visitContinueExpr(ctx: ChiParser.ContinueExprContext): ParseAst =
        WhileReader.readContinue(source, ctx)

    override fun visitIsExpr(ctx: ChiParser.IsExprContext): ParseAst =
        IsReader.read(this, source, ctx)

    override fun visitNegationExpr(ctx: ChiParser.NegationExprContext): ParseAst =
        ParseBinaryOp("-", LongValue(0), ctx.expression().accept(this), getSection(source, ctx))

    override fun visitWeaveExpr(ctx: ChiParser.WeaveExprContext): ParseAst =
        WeaveReader.read(this, source, ctx)

    override fun visitEffectDefinition(ctx: ChiParser.EffectDefinitionContext): ParseAst =
        EffectReader.readEffectDefinition(this, source, ctx)

    override fun visitHandleExpression(ctx: ChiParser.HandleExpressionContext): ParseAst =
        EffectReader.readHandle(this, source, ctx)

    override fun visitReturnExpr(ctx: ChiParser.ReturnExprContext): ParseAst =
        ReturnReader.read(this, source, ctx)

    override fun visitCreateRecord(ctx: ChiParser.CreateRecordContext): ParseAst =
        RecordReader.read(this, source, ctx)

    override fun visitCreateArray(ctx: ChiParser.CreateArrayContext): ParseAst =
        ArrayReader.read(this, source, ctx)

    override fun visitForLoop(ctx: ChiParser.ForLoopContext): ParseAst =
        ForReader.readFor(this, source, ctx)
}