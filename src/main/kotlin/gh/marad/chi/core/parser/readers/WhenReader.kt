package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object WhenReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.WhenExpressionContext): ParseAst =
        ParseWhen(
            cases = ctx.whenConditionCase().map { readWhenConditionCase(parser, source, it) },
            elseCase = readElseCase(parser, source, ctx.whenElseCase()),
            section = getSection(source, ctx)
        )

    private fun readWhenConditionCase(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: ChiParser.WhenConditionCaseContext
    ): ParseWhenCase =
        ParseWhenCase(
            condition = ctx.condition.accept(parser),
            body = ctx.body.accept(parser),
            section = getSection(source, ctx)
        )

    private fun readElseCase(
        parser: ParserVisitor,
        source: ChiSource,
        whenElseCase: ChiParser.WhenElseCaseContext?
    ): ParseElseCase? = whenElseCase?.let {
        ParseElseCase(
            body = it.body.accept(parser),
            section = getSection(source, it)
        )
    }

}

data class ParseWhen(
    val cases: List<ParseWhenCase>,
    val elseCase: ParseElseCase?,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitWhen(this)
    override fun children(): List<ParseAst> =
        if (elseCase != null) {
            cases.flatMap { it.children() } + elseCase.body
        } else {
            cases.flatMap { it.children() }
        }
}

data class ParseWhenCase(
    val condition: ParseAst,
    val body: ParseAst,
    val section: ChiSource.Section?
) {
    fun children(): List<ParseAst> = listOf(condition, body)
}

data class ParseElseCase(
    val body: ParseAst,
    val section: ChiSource.Section?
)
