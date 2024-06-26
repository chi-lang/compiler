package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection

internal object ProgramReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.ProgramContext): ParseProgram {
        return ParseProgram(
            packageDefinition = ctx.package_definition()?.let { PackageReader.read(source, it) },
            imports = ctx.import_definition().map { ImportReader.read(source, it) },
            typeAliases = ctx.`typealias`().map {
                TypeAliasReader.read(parser, source, it)
            },
            typeDefinitions = ctx.variantTypeDefinition().map {
                VariantTypeDefinitionReader.read(parser, source, it)
            },
            code = ctx.expression().map { it.accept(parser) },
            getSection(source, ctx)
        )
    }

    private fun isFunctionDeclaration(ctx: ChiParser.ExpressionContext): Boolean =
        ctx is ChiParser.FuncWithNameContext || ctx is ChiParser.EffectDefContext

}

data class ParseProgram(
    val packageDefinition: PackageDefinition?,
    val imports: List<Import>,
    val typeAliases: List<ParseTypeAlias>,
    val typeDefinitions: List<ParseVariantTypeDefinition>,
    val code: List<ParseAst>,
    val section: ChiSource.Section?
)
