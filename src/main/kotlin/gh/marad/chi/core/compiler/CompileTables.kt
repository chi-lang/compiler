package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.namespace.*
import gh.marad.chi.core.parser.readers.Import

class CompileTables(val currentPackage: Package,
                    val ns: CompilationEnv,
                    imports: List<Import>
    ) {
    private val packageAliasTable = PackageTable()
    private val importedSymbols = mutableMapOf<String, PackageSymbol>()
    private val localSymbolTable = SymbolTable()
    val pkg = ns.getOrCreatePackage(currentPackage)
    val localTypeTable = TypeTable(pkg)

    init {
        // add symbols and types defined in this package
        imports.forEach { import ->
            val importedPkg = ns.getOrCreatePackage(import.moduleName, import.packageName)
            if (import.packageAlias != null) {
                packageAliasTable.add(import.packageAlias, importedPkg)
            }
            import.entries.forEach { entry ->
                val importedName = entry.alias ?: entry.name
                importedSymbols[importedName] = PackageSymbol(import.moduleName, import.packageName, entry.name)
                ns.getTypeAlias(import.moduleName, import.packageName, entry.name)?.let { typeAlias ->
                    localTypeTable.add(importedName, typeAlias)
                }
            }
        }
    }

    fun getSymbolTable() = localSymbolTable

    fun getAliasedPackage(name: String): PackageDescriptor? {
        return packageAliasTable.get(name)
    }

    fun getLocalSymbol(name: String): Symbol? {
        return localSymbolTable.get(name)
            ?: importedSymbols[name]?.let(ns::getSymbol)
            ?: ns.getSymbol(currentPackage.moduleName, currentPackage.packageName, name)
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