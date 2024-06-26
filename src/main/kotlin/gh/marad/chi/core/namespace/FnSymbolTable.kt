package gh.marad.chi.core.namespace

import gh.marad.chi.core.types.Type

class FnSymbolTable(private val parent: FnSymbolTable? = null) {

    private val symbolMap = mutableMapOf<String, FnSymbol>()

    fun addArgument(name: String, type: Type?) {
        symbolMap[name] = FnSymbol(name, SymbolKind.Argument, type, mutable = false)
    }

    fun addLocal(name: String, type: Type?, mutable: Boolean) {
        symbolMap[name] = FnSymbol(name, SymbolKind.Local, type, mutable)
    }

    fun get(name: String): FnSymbol? = symbolMap[name] ?: parent?.get(name)

    fun remove(symbol: String) {
        symbolMap.remove(symbol)
    }
}

data class FnSymbol(
    val name: String,
    val kind: SymbolKind,
    val type: Type?,
    val mutable: Boolean
)

enum class SymbolKind {
    Local,
    Argument
}
