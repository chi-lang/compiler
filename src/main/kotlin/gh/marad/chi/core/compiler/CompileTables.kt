package gh.marad.chi.core.compiler

import gh.marad.chi.core.Package
import gh.marad.chi.core.namespace.*
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.SumType

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

    fun defineType(info: TypeInfo) {
        localTypeTable.add(info)
        ns.getOrCreatePackage(info.moduleName, info.packageName).types.add(info)
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
            importPkg.types.get(entry.name)?.let { typeInfo ->
                // if symbol was not found - try the sum type
                localTypeTable.add(importedName, typeInfo)
                if (typeInfo.type is SumType) {
                    typeInfo.type.subtypes.map { subtypeName ->
                        // if sum type was found then import type's constructors into local symbol table
                        importPkg.symbols.get(subtypeName)?.let(localSymbolTable::add)
                        // as well as add all the types to local type table
                        importPkg.types.get(subtypeName)?.let(localTypeTable::add)
                    }
                }
            }
        }
    }

}