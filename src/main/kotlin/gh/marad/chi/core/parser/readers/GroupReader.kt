package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object GroupReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.GroupExprContext): ParseAst =
        ParseGroup(
            value = ctx.expression().accept(parser),
            getSection(source, ctx)
        )
}

data class ParseGroup(val value: ParseAst, override val section: ChiSource.Section?) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitGroup(this)
    override fun children(): List<ParseAst> = listOf(value)
}