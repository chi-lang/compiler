package gh.marad.chi.core.namespace

import gh.marad.chi.core.types.Type

class FnSymbolTable {
    private val symbolMap = mutableMapOf<String, FnSymbol>()
    private var nextArgumentSlot = 0
    private var nextLocalSlot = 0

    fun addArgument(name: String, type: Type?) {
        symbolMap[name] = FnSymbol(name, SymbolKind.Argument, type, mutable = false).also {
            it.slot = nextArgumentSlot++
        }
    }

    fun addLocal(name: String, type: Type?, mutable: Boolean) {
        symbolMap[name] = FnSymbol(name, SymbolKind.Local, type, mutable).also {
            it.slot = nextLocalSlot++
        }
    }

    fun get(name: String): FnSymbol? = symbolMap[name]

    fun remove(symbol: String) {
        symbolMap.remove(symbol)
    }
}

data class FnSymbol(
    val name: String,
    val kind: SymbolKind,
    val type: Type?,
    val mutable: Boolean
) {
    var slot: Int = -1
}

enum class SymbolKind {
    Local,
    Argument
}
