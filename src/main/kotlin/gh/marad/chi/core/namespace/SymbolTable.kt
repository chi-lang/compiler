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

    fun remove(symbol: String) {
        symbolMap.remove(symbol)
    }

    fun get(name: String): Symbol? = symbolMap[name]

    fun hasSymbol(name: String) = symbolMap.containsKey(name)

    fun iterator() = symbolMap.values.iterator()
}

data class Symbol(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val type: TypeScheme?,
    val public: Boolean,
    val mutable: Boolean,
)

