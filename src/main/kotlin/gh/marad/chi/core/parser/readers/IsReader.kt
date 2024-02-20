package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object IsReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.IsExprContext): ParseAst =
        ParseIs(ctx.expression().accept(parser), TypeReader.readTypeRef(parser, source, ctx.type()), getSection(source, ctx))
}

data class ParseIs(
    val value: ParseAst,
    val typeRef: TypeRef,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitIs(this)
    override fun children(): List<ParseAst> = listOf(value)
}
