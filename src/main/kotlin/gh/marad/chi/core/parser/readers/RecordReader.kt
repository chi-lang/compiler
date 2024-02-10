package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object RecordReader {
    fun read(visitor: ParserVisitor, source: ChiSource, ctx: ChiParser.CreateRecordContext): ParseAst =
        ParseCreateRecord(
            ctx.ID().zip(ctx.expression()).map { (name, value) ->
                ParseCreateRecord.Field(name.text, visitor.visit(value))
            },
            getSection(source, ctx)
        )
}

data class ParseCreateRecord(val fields: List<Field>, override val section: ChiSource.Section?): ParseAst {
    data class Field(val name: String, val value: ParseAst)
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitCreateRecord(this)
    override fun children(): List<ParseAst> = fields.map { it.value }
}