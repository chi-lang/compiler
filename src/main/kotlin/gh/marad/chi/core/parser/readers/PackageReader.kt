package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.getSection

object PackageReader {
    fun read(source: ChiSource, ctx: ChiParser.Package_definitionContext): PackageDefinition {
        val moduleName = CommonReader.readModuleName(ctx.moduleName())
        val packageName = CommonReader.readPackageName(ctx.packageName())
        return PackageDefinition(moduleName, packageName, getSection(source, ctx))
    }
}

data class PackageDefinition(
    val moduleName: String, val packageName: String,
    val section: ChiSource.Section?
)

