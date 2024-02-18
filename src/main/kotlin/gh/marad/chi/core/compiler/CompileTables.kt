package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.namespace.*
import gh.marad.chi.core.parser.readers.Import

class CompileTables(currentPackage: Package, val ns: GlobalCompilationNamespace) {
    val packageTable = PackageTable()
    val localSymbolTable = SymbolTable()
    val localTypeTable = TypeTable()

    init {
        // add symbols and types defined in this package
        val pkg = ns.getOrCreatePackage(currentPackage)
        localSymbolTable.add(pkg.symbols)
        localTypeTable.add(pkg.types)
    }

    fun defineSymbol(symbol: Symbol) {
        localSymbolTable.add(symbol)
        ns.getOrCreatePackage(symbol.moduleName, symbol.packageName).symbols.add(symbol)
    }

    fun removeSymbol(name: String) {
        val symbol = localSymbolTable.get(name)
        if (symbol != null) {
            localSymbolTable.remove(name)
            ns.getOrCreatePackage(symbol.moduleName, symbol.packageName)
                .symbols.remove(symbol.name)
        }
    }

    fun defineTypeAlias(alias: TypeAlias) {
        localTypeTable.add(alias)
        ns.getOrCreatePackage(alias.typeId.moduleName, alias.typeId.packageName)
            .types.add(alias)
    }

    fun addImports(imports: List<Import>) {
        imports.forEach(this::addImport)
    }

    fun addImport(import: Import) {
        val importPkg = ns.getOrCreatePackage(import.moduleName, import.packageName)
        if (import.packageAlias != null) {
            packageTable.add(import.packageAlias, importPkg)
        }

        import.entries.forEach { entry ->
            val importedName =  entry.alias ?: entry.name
            // find the symbol in target package
            importPkg.symbols.get(entry.name)?.let { symbol ->
                // import it to local symbol table
                localSymbolTable.add(importedName, symbol)
            }
            importPkg.types.getAlias(entry.name)?.let { typeAlias ->
                localTypeTable.add(importedName, typeAlias)
            }
        }
    }

}