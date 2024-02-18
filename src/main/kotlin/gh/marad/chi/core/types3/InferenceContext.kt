package gh.marad.chi.core.types3

import gh.marad.chi.core.*
import gh.marad.chi.core.Target
import gh.marad.chi.core.namespace.GlobalCompilationNamespace

class InferenceContext(
    val pkg: Package,
    val ns: GlobalCompilationNamespace
) {
    private var nextVariableId = 1
    fun freshVariable(level: Int) = Variable("a${nextVariableId++}", level)


    private val packageSymbols = LocalSymbols()
    private var localSymbols: LocalSymbols = packageSymbols
    class LocalSymbols(val parent: LocalSymbols? = null) {
        val symbols = mutableMapOf<String, TypeScheme>()
        fun get(name: String): TypeScheme? = symbols[name] ?: parent?.get(name)
        fun define(name: String, typeScheme: TypeScheme) {
            symbols[name] = typeScheme
        }
    }

    init {
        ns.getOrCreatePackage(pkg).symbols.forEach { name, symbol ->
            symbol.newType?.let {
                packageSymbols.define(name, it)
            }
        }
    }

    fun defineLocalSymbol(name: String, typeScheme: TypeScheme) {
        localSymbols.define(name, typeScheme)
    }

    fun updateSymbolType(target: Target, typeScheme: TypeScheme) {
        when(target) {
            is LocalSymbol -> localSymbols.define(target.name, typeScheme)
            is PackageSymbol -> {
                if (target.moduleName == pkg.moduleName && target.packageName == pkg.packageName) {
                    packageSymbols.define(target.name, typeScheme)
                }
            }
        }
    }

    fun <T> withNewLocalScope(f: () -> T): T {
        val prev = localSymbols
        localSymbols = LocalSymbols(prev)
        return f().also { localSymbols = prev }
    }

    fun listLocalFunctionsForType(name: String, type: Type3): List<Pair<DotTarget, TypeScheme>> {
        return localSymbols.symbols
            .filter {
                val symbolType: Type3 = when(val typeScheme = it.value) {
                    is PolyType -> typeScheme.body
                    is Type3 -> typeScheme
                }
                it.key == name && symbolType is Function && symbolType.types.size >= 2 && (symbolType.types[0] == type || symbolType.types[0] is Variable)
            }
            .map {
                DotTarget.LocalFunction to it.value
            }
            .toList()
    }

    fun listCurrentPackageFunctionsForType(name: String, type: Type3): List<Pair<DotTarget, TypeScheme>> {
        return packageSymbols.symbols
            .filter {
                val symbolType: Type3 = when(val typeScheme = it.value) {
                    is PolyType -> typeScheme.body
                    is Type3 -> typeScheme
                }
                it.key == name && symbolType is Function && symbolType.types.size >= 2 && (symbolType.types[0] == type || symbolType.types[0] is Variable)
            }
            .map {
                DotTarget.PackageFunction(pkg.moduleName, pkg.packageName, name) to it.value
            }
            .toList()
    }

    fun listTypesPackageFunctionsForType(name: String, type: Type3): List<Pair<DotTarget, TypeScheme>> {
        return if (type is HasTypeId) {
            type.getTypeId()
                ?.let { id -> ns.getOrCreatePackage(id.moduleName, id.packageName).symbols.get(name) }
                ?.let { symbol ->
                    val symbolType: Type3 = when(val typeScheme = symbol.newType!!) {
                        is PolyType -> typeScheme.body
                        is Type3 -> typeScheme
                    }
                    if (symbolType is Function && symbolType.types.size >= 2 && (symbolType.types[0] == type || symbolType.types[0] is Variable)) {
                        listOf(DotTarget.PackageFunction(symbol.moduleName, symbol.packageName, symbol.name) to symbol.newType)
                    } else {
                        emptyList()
                    }
                }
                ?: emptyList()
        } else {
            emptyList()
        }
    }

    fun getTargetType(target: Target, level: Int): Type3 {
        fun getLocalSymbol(symbols: LocalSymbols, name: String) =
            symbols.get(name)
                ?.instantiate(level, this::freshVariable)
                ?: err("Identifier ${target.name} not found!")

        return when (target) {
            is LocalSymbol -> getLocalSymbol(localSymbols, target.name)
            is PackageSymbol -> {
                // FIXME replace 'localSymbols' with reading from a package
                //       once a package supports new types
                if (target.moduleName == pkg.moduleName &&
                    target.packageName == pkg.packageName) {
                    // we are trying to lookup symbol from current package so we need to use local package symbols
                    getLocalSymbol(packageSymbols, target.name)
                } else {
                    // for other packages we just search the target package
                    val symbol = ns.getSymbol(target) ?: err("Identifier ${target.name} not found!")
                    symbol.newType?.instantiate(level, this::freshVariable)
                        ?: err("Type not found for identifier ${target.name}")
                }
            }
        }
    }
}