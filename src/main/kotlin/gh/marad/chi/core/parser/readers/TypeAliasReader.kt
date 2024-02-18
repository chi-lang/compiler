package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection

internal object TypeAliasReader {
    fun read(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.TypealiasContext): ParseTypeAlias {
        return ParseTypeAlias(
            name = ctx.name.text,
            type = TypeReader.readTypeRef(parser, source, ctx.type()),
            typeParameters = CommonReader.readTypeParameters(source, ctx.generic_type_definitions()),
            section = getSection(source, ctx)
        )
    }
}

data class ParseTypeAlias(val name: String,
                          val type: TypeRef,
                          val typeParameters: List<TypeParameterRef>,
                          val section: ChiSource.Section?)