package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.readers.TypeReader.readTypeRef
import org.antlr.v4.runtime.tree.TerminalNode

internal object CommonReader {
    fun readModuleName(ctx: ChiParser.ModuleNameContext?): String =
        ctx?.text ?: ""

    fun readPackageName(ctx: ChiParser.PackageNameContext?): String =
        ctx?.text ?: ""

    fun readSymbol(source: ChiSource, id: TerminalNode): Symbol =
        Symbol(
            name = id.text,
            section = getSection(source, id.symbol, id.symbol)
        )

    fun readTypeParameters(
        source: ChiSource,
        ctx: ChiParser.Generic_type_definitionsContext?
    ): List<TypeParameterRef> =
        ctx?.ID()?.map { TypeParameterRef(it.text, getSection(source, it.symbol, it.symbol)) }
            ?: emptyList()


    fun readFuncArgumentDefinitions(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: ChiParser.ArgumentsWithOptionalTypesContext?
    ): List<FormalArgument> =
        ctx?.argumentWithOptionalType()?.map {
            FormalArgument(
                name = it.ID().text,
                typeRef = it.type()?.let { readTypeRef(parser, source, it) },
                defaultValue = null,
                getSection(source, it)
            )
        } ?: emptyList()

    fun readFuncArgumentDefinitions(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: ChiParser.ArgumentsWithTypesContext?
    ): List<FormalArgument> =
        ctx?.argumentWithType()?.map {
            FormalArgument(
                name = it.ID().text,
                typeRef = readTypeRef(parser, source, it.type()),
                defaultValue = it.defaultValue?.let { parser.visit(it) },
                getSection(source, it)
            )
        } ?: emptyList()

}