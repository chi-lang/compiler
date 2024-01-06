package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object IfElseReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.If_exprContext): ParseAst =
        ParseIfElse(
            condition = ctx.condition.accept(parser),
            thenBody = ctx.then_expr.accept(parser),
            elseBody = ctx.else_expr?.let { readElse(parser, source, it) },
            section = getSection(source, ctx)
        )

    private fun readElse(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.If_expr_elseContext): ParseAst {
        return if (ctx.block() != null) {
            ctx.block().accept(parser)
        } else {
            ctx.if_expr().accept(parser)
        }
    }
}

data class ParseIfElse(
    val condition: ParseAst,
    val thenBody: ParseAst,
    val elseBody: ParseAst?,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitIfElse(this)
    override fun children(): List<ParseAst> =
        if (elseBody != null) {
            listOf(condition, thenBody, elseBody)
        } else {
            listOf(condition, thenBody)
        }
}
