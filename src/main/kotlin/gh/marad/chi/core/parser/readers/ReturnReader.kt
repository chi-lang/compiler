package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object ReturnReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.ReturnExprContext): ParseAst =
        ParseReturn(
            value = ctx.expression()?.accept(parser),
            section = getSection(source, ctx)
        )
}

data class ParseReturn(
    val value: ParseAst?,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitReturn(this)
    override fun children(): List<ParseAst> =
        if (value != null) {
            listOf(value)
        } else {
            emptyList()
        }
}