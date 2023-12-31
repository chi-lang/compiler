package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.ChiSource

sealed interface ParseAst {
    val section: ChiSource.Section?
}

data class ModuleName(val name: String, val section: ChiSource.Section?)
data class PackageName(val name: String, val section: ChiSource.Section?)
data class Symbol(val name: String, val section: ChiSource.Section?)


data class FormalArgument(val name: String, val typeRef: TypeRef, val section: ChiSource.Section?)

