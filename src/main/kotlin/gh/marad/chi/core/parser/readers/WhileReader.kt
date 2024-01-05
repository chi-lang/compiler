package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object WhileReader {
    fun readWhile(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.WhileLoopExprContext): ParseAst =
        ParseWhile(ctx.expression().accept(parser), ctx.block().accept(parser), getSection(source, ctx))

    fun readBreak(source: ChiSource, ctx: ChiParser.BreakExprContext): ParseAst =
        ParseBreak(getSection(source, ctx))

    fun readContinue(source: ChiSource, ctx: ChiParser.ContinueExprContext): ParseAst =
        ParseContinue(getSection(source, ctx))
}

data class ParseWhile(
    val condition: ParseAst,
    val body: ParseAst,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitWhile(this)
}

data class ParseBreak(override val section: ChiSource.Section?) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitBreak(this)
}

data class ParseContinue(override val section: ChiSource.Section?) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitContinue(this)
}

