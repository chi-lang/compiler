package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.readers.CommonReader.readModuleName
import gh.marad.chi.core.parser.readers.CommonReader.readPackageName
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

object ImportReader {
    fun read(source: ChiSource, ctx: ChiParser.Import_definitionContext): ParseImportDefinition =
        ParseImportDefinition(
            moduleName = readModuleName(ctx.module_name()),
            packageName = readPackageName(ctx.package_name()),
            packageAlias = readPackageAlias(ctx.package_import_alias()),
            entries = ctx.import_entry().map { readImportEntry(source, it) },
            section = getSection(source, ctx)
        )

    private fun readPackageAlias(ctx: ChiParser.Package_import_aliasContext?): String? =
        ctx?.text

    private fun readImportEntry(source: ChiSource, ctx: ChiParser.Import_entryContext): ParseImportDefinition.Entry =
        ParseImportDefinition.Entry(
            name = ctx.import_name().text,
            alias = readImportNameAlias(ctx.name_import_alias()),
            section = getSection(source, ctx)
        )

    private fun readImportNameAlias(ctx: ChiParser.Name_import_aliasContext?): String? =
        ctx?.text

}

data class ParseImportDefinition(
    val moduleName: String, val packageName: String, val packageAlias: String?, val entries: List<Entry>,
    override val section: ChiSource.Section?
) : ParseAst {
    data class Entry(val name: String, val alias: String?, val section: ChiSource.Section?)

    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitImportDefinition(this)
    override fun children(): List<ParseAst> = emptyList()
}

