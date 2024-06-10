package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection

internal object TraitReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.TraitDefinitionContext) =
        ParseTraitDefinition(
            name = ctx.name.text,
            typeParameters = CommonReader.readTypeParameters(source, ctx.generic_type_definitions()),
            functions = ctx.traitFunctionDefinition().map { readTraitFunction(parser, source, it) },
            section = getSection(source, ctx)
        )

    private fun readTraitFunction(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.TraitFunctionDefinitionContext) =
        ParseTraitFunctionDefinition(
            name = ctx.funcName.text,
            formalArguments = CommonReader.readFuncArgumentDefinitions(parser, source, ctx.func_argument_definitions()?.argumentsWithTypes()),
            returnTypeRef = ctx.func_return_type()?.type()?.let { TypeReader.readTypeRef(parser, source, it) } ?: TypeRef.unit,
            section = getSection(source, ctx)
        )

}

data class ParseTraitDefinition(
    val name: String,
    val typeParameters: List<TypeParameterRef>,
    val functions: List<ParseTraitFunctionDefinition>,
    val section: ChiSource.Section?
)

data class ParseTraitFunctionDefinition(
    val name: String,
    val formalArguments: List<FormalArgument>,
    val returnTypeRef: TypeRef,
    val section: ChiSource.Section?
)