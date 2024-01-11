package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

internal object ProgramReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.ProgramContext): ParseProgram {
        val split = ctx.expression().groupBy { isFunctionDeclaration(it) }

        return ParseProgram(
            packageDefinition = ctx.package_definition()?.let { PackageReader.read(source, it) },
            imports = ctx.import_definition().map { ImportReader.read(source, it) },
            typeDefinitions = ctx.variantTypeDefinition().map {
                VariantTypeDefinitionReader.read(parser, source, it)
            },
            functions = split[true]?.map { it.accept(parser) } ?: emptyList(),
            topLevelCode = split[false]?.map { it.accept(parser) } ?: emptyList(),
            section = getSection(source, ctx)
        )
    }

    private fun isFunctionDeclaration(ctx: ChiParser.ExpressionContext): Boolean =
        ctx is ChiParser.FuncWithNameContext || ctx is ChiParser.EffectDefContext

}

data class ParseProgram(
    val packageDefinition: ParsePackageDefinition?,
    val imports: List<Import>,
    val typeDefinitions: List<ParseVariantTypeDefinition>,
    val functions: List<ParseAst>,
    val topLevelCode: List<ParseAst>,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitProgram(this)
    override fun children(): List<ParseAst> = imports + typeDefinitions + traitDefinitions + functions + topLevelCode
}
