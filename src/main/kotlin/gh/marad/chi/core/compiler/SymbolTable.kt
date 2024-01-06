package gh.marad.chi.core.compiler

import gh.marad.chi.core.types.Type

class SymbolTable {
    private val symbolMap = mutableMapOf<String, Symbol>()

    fun add(symbol: Symbol) {
        symbolMap[symbol.name] = symbol
    }

    fun add(table: SymbolTable) {
        symbolMap.putAll(table.symbolMap)
    }

    fun remove(symbol: String) {
        symbolMap.remove(symbol)
    }

    fun get(name: String): Symbol? = symbolMap[name]

    fun forEach(f: (Symbol) -> Unit) {
        symbolMap.values.forEach(f)
    }

    fun hasSymbol(name: String) = symbolMap.containsKey(name)
}

data class Symbol(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val kind: SymbolKind,
    val type: Type?,
    val slot: Int,
    val public: Boolean,
    val mutable: Boolean
)

enum class SymbolKind {
    Local,
    Argument
}
