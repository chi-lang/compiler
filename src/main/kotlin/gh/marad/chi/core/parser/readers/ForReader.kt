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
            iterable = ctx.iterable.accept(visitor),
            state = ctx.state?.accept(visitor),
            init = ctx.init?.accept(visitor),
            body = BlockReader.read(visitor, source, ctx.block()),
            varSections = ctx.ID().map { getSection(source, it.symbol) },
            iterableSection = getSection(source, ctx.iterable),
            stateSection = ctx.state?.let { getSection(source, it) },
            initSection = ctx.init?.let { getSection(source, it) },
            bodySection = getSection(source, ctx.block()),
            getSection(source, ctx)
        )
}

data class ParseFor(
    val vars: List<String>,
    val iterable: ParseAst,
    val state: ParseAst?,
    val init: ParseAst?,
    val body: ParseBlock,
    val varSections: List<ChiSource.Section>,
    val iterableSection: ChiSource.Section,
    val stateSection: ChiSource.Section?,
    val initSection: ChiSource.Section?,
    val bodySection: ChiSource.Section,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T {
        return visitor.visitFor(this)
    }

    override fun children(): List<ParseAst> =
        listOf(iterable, body)
}
