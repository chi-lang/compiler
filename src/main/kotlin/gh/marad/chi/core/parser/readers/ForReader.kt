package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object ForReader {
    fun readFor(visitor: ParserVisitor, source: ChiSource, ctx: ChiParser.ForLoopContext) =
        ParseFor(
            vars = ctx.ID().map { it.text },
            iterable = ctx.expression().accept(visitor),
            body = BlockReader.read(visitor, source, ctx.block()),
            getSection(source, ctx)
        )
}

data class ParseFor(
    val vars: List<String>,
    val iterable: ParseAst,
    val body: ParseBlock,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T {
        return visitor.visitFor(this)
    }

    override fun children(): List<ParseAst> =
        listOf(iterable, body)
}
