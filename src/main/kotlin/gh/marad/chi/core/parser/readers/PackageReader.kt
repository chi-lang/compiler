package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.visitor.ParseAstVisitor

object PackageReader {
    fun read(source: ChiSource, ctx: ChiParser.Package_definitionContext): ParsePackageDefinition {
        val moduleName = CommonReader.readModuleName(ctx.module_name())
        val packageName = CommonReader.readPackageName(ctx.package_name())
        return ParsePackageDefinition(moduleName, packageName, getSection(source, ctx))
    }
}

data class ParsePackageDefinition(
    val moduleName: String, val packageName: String,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitPackageDefinition(this)
    override fun children(): List<ParseAst> = emptyList()
}

