package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object ArrayReader {
    fun read(visitor: ParserVisitor, source: ChiSource, ctx: ChiParser.CreateArrayContext): ParseAst =
        ParseCreateArray(
            ctx.expression().map { it.accept(visitor) },
            getSection(source, ctx)
        )
}

data class ParseCreateArray(val values: List<ParseAst>, override val section: ChiSource.Section?) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T =
        visitor.visitCreateArray(this)

    override fun children(): List<ParseAst> = values
}