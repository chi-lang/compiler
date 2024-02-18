package gh.marad.chi.core.namespace

import gh.marad.chi.core.types.TypeScheme

class SymbolTable {
    private val symbolMap = mutableMapOf<String, Symbol>()

    fun add(symbol: Symbol) {
        symbolMap[symbol.name] = symbol
    }

    fun add(alias: String, symbol: Symbol) {
        symbolMap[alias] = symbol
    }

    fun add(table: SymbolTable) {
        symbolMap.putAll(table.symbolMap)
    }

    fun remove(symbol: String) {
        symbolMap.remove(symbol)
    }

    fun get(name: String): Symbol? = symbolMap[name]

    fun forEach(f: (String, Symbol) -> Unit) {
        symbolMap.forEach(f)
    }

    fun hasSymbol(name: String) = symbolMap.containsKey(name)
}

data class Symbol(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val newType: TypeScheme?,
    val public: Boolean,
    val mutable: Boolean
) {
    fun qualifiedName() = "$moduleName::$packageName::$name"
}

