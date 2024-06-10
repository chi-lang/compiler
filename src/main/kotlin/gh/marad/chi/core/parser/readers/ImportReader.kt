package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.readers.CommonReader.readModuleName
import gh.marad.chi.core.parser.readers.CommonReader.readPackageName

object ImportReader {
    fun read(source: ChiSource, ctx: ChiParser.Import_definitionContext): Import =
        Import(
            moduleName = readModuleName(ctx.moduleName()),
            packageName = readPackageName(ctx.packageName()),
            packageAlias = readPackageAlias(ctx.package_import_alias()),
            entries = ctx.import_entry().map { readImportEntry(source, it) },
            section = getSection(source, ctx)
        )

    private fun readPackageAlias(ctx: ChiParser.Package_import_aliasContext?): String? =
        ctx?.text

    private fun readImportEntry(source: ChiSource, ctx: ChiParser.Import_entryContext): Import.Entry =
        Import.Entry(
            name = ctx.import_name().text,
            alias = readImportNameAlias(ctx.name_import_alias()),
            section = getSection(source, ctx)
        )

    private fun readImportNameAlias(ctx: ChiParser.Name_import_aliasContext?): String? =
        ctx?.text

}

data class Import(
    val moduleName: String, val packageName: String, val packageAlias: String?, val entries: List<Entry>,
    val section: ChiSource.Section?
) {
    data class Entry(val name: String, val alias: String?, val section: ChiSource.Section?)
}

