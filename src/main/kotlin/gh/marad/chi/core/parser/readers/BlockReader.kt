package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object BlockReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.BlockContext): ParseBlock =
        ParseBlock(
            body = ctx.expression().map { it.accept(parser) },
            section = getSection(source, ctx)
        )
}

data class ParseBlock(val body: List<ParseAst>, override val section: ChiSource.Section?) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitBlock(this)
    override fun children(): List<ParseAst> = body
}

