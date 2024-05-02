package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.antlr.ChiParser.ForKVLoopContext
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object ForReader {
    fun readFor(visitor: ParserVisitor, source: ChiSource, ctx: ChiParser.ForLoopContext) =
        ParseFor(
            name = ctx.name.text,
            iterable = ctx.expression().accept(visitor),
            body = BlockReader.read(visitor, source, ctx.block()),
            getSection(source, ctx)
        )

    fun readForKV(visitor: ParserVisitor, source: ChiSource, ctx: ForKVLoopContext): ParseAst =
        ParseForKV(
            key = ctx.key.text,
            value = ctx.value.text,
            iterable = ctx.expression().accept(visitor),
            body = BlockReader.read(visitor, source, ctx.block()),
            getSection(source, ctx)
        )
}

data class ParseFor(
    val name: String,
    val iterable: ParseAst,
    val body: ParseBlock,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T {
        TODO("Not yet implemented")
    }

    override fun children(): List<ParseAst> {
        TODO("Not yet implemented")
    }
}

data class ParseForKV(
    val key: String,
    val value: String,
    val iterable: ParseAst,
    val body: ParseBlock,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T {
        TODO("Not yet implemented")
    }

    override fun children(): List<ParseAst> {
        TODO("Not yet implemented")
    }

}
