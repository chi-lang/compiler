package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object WeaveReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.WeaveExprContext): ParseAst =
        ParseWeave(
            value = ctx.input.accept(parser),
            opTemplate = ctx.template.accept(parser),
            getSection(source, ctx)
        )
}

data class ParseWeave(
    val value: ParseAst,
    val opTemplate: ParseAst,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitWeave(this)
}

data class ParseWeavePlaceholder(
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitPlaceholder(this)
}