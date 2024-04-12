package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.namespace.*
import gh.marad.chi.core.parser.readers.Import

class CompileTables(currentPackage: Package,
                    val ns: CompilationEnv,
                    val imports: List<Import>
    ) {
    private val packageAliasTable = PackageTable()
    private val importedSymbols = mutableMapOf<String, PackageSymbol>()
    private val localSymbolTable = SymbolTable()
    val localTypeTable = TypeTable()
    val pkg = ns.getOrCreatePackage(currentPackage)

    init {
        // add symbols and types defined in this package
        localTypeTable.add(pkg.types)
        imports.forEach { import ->
            val importedPkg = ns.getOrCreatePackage(import.moduleName, import.packageName)
            if (import.packageAlias != null) {
                packageAliasTable.add(import.packageAlias, importedPkg)
            }
            import.entries.forEach { entry ->
                val importedName = entry.alias ?: entry.name
                importedSymbols[importedName] = PackageSymbol(import.moduleName, import.packageName, entry.name)
                importedPkg.types.getAlias(entry.name)?.let { typeAlias ->
                    localTypeTable.add(importedName, typeAlias)
                }
            }
        }
    }


    fun getAliasedPackage(name: String): PackageDescriptor? {
        return packageAliasTable.get(name)
    }

    fun getLocalSymbol(name: String): Symbol? {
        return localSymbolTable.get(name) ?: importedSymbols[name]?.let(ns::getSymbol)  ?: pkg.getSymbol(name)
    }

    fun defineSymbol(symbol: Symbol) {
        localSymbolTable.add(symbol)
    }

    fun removeSymbol(name: String) {
        val symbol = localSymbolTable.get(name)
        if (symbol != null) {
            localSymbolTable.remove(name)
        }
    }

    fun defineTypeAlias(alias: TypeAlias) {
        localTypeTable.add(alias)
    }
}