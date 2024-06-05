package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.Target
import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.compiler.CompileTables
import gh.marad.chi.core.namespace.CompilationEnv
import gh.marad.chi.core.parser.ChiSource

class InferenceContext(
    val pkg: Package,
    val ns: CompilationEnv,
    val compileTables: CompileTables
) {
    private var nextVariableId = 1
    fun freshVariable(level: Int) = Variable("a${nextVariableId++}", level)


    private val packageSymbols = LocalSymbols(compileTables = compileTables)
    private var localSymbols: LocalSymbols = packageSymbols
    class LocalSymbols(val parent: LocalSymbols? = null, val compileTables: CompileTables? = null) {
        val symbols = mutableMapOf<String, TypeScheme>()
        fun get(name: String): TypeScheme? {
            if (compileTables != null && !symbols.containsKey(name)) {
                compileTables.getLocalSymbol(name)?.type?.let {
                    symbols[name] = it
                    return it
                }
            }
            return symbols[name] ?: parent?.get(name)
        }
        fun define(name: String, typeScheme: TypeScheme) {
            symbols[name] = typeScheme
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
        try {
            return f()
        } finally {
            localSymbols = prev
        }
    }

    fun listLocalFunctionsForType(name: String, type: Type): List<Pair<DotTarget, TypeScheme>> {
        return localSymbols.symbols
            .filter {
                val symbolType: Type = when(val typeScheme = it.value) {
                    is PolyType -> typeScheme.body
                    is Type -> typeScheme
                }
                it.key == name && symbolType is Function && symbolType.types.size >= 2 && (symbolType.types[0] == type || symbolType.types[0] is Variable)
            }
            .map {
                DotTarget.LocalFunction to it.value
            }
            .toList()
    }

    fun listCurrentPackageFunctionsForType(name: String, type: Type): List<Pair<DotTarget, TypeScheme>> {
        val symbol = compileTables.getLocalSymbol(name)
        return if (symbol?.type != null) {
            val symbolType: Type = when(val typeScheme = symbol.type) {
                is PolyType -> typeScheme.body
                is Type -> typeScheme
                null -> TODO()
            }
            val fitsRequirements = symbolType is Function && symbolType.types.size >= 2 && run {
                try {
                    unify(listOf(Constraint(symbolType.types.first(), type, null, emptyList())))
                    true
                } catch (ex: CompilerMessage) {
                    false
                }
            }
            if (fitsRequirements) {
                listOf(DotTarget.PackageFunction(symbol.moduleName, symbol.packageName, symbol.name) to symbolType)
            } else {
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    fun listTypesPackageFunctionsForType(name: String, type: Type): List<Pair<DotTarget, TypeScheme>> {
        return if (type is HasTypeId) {
            type.getTypeIds()
                .mapNotNull { id -> ns.getSymbol(id.moduleName, id.packageName, name) }
                .flatMap { symbol ->
                    val symbolType: Type = when(val typeScheme = symbol.type!!) {
                        is PolyType -> typeScheme.body
                        is Type -> typeScheme
                    }
                    if (symbolType is Function && symbolType.types.size >= 2) {
                        try {
                            unify(listOf(Constraint(symbolType.types.first(), type, null, listOf())))
                            listOf(
                                DotTarget.PackageFunction(
                                    symbol.moduleName,
                                    symbol.packageName,
                                    symbol.name
                                ) to symbol.type
                            )
                        } catch (ex: CompilerMessage) {
                            emptyList()
                        }
                    } else {
                        emptyList()
                    }
                }
        } else {
            emptyList()
        }
    }

    fun getTargetType(target: Target, level: Int, sourceSection: ChiSource.Section?): Type {
        fun getLocalSymbol(symbols: LocalSymbols, name: String) =
            symbols.get(name)
                ?.instantiate(level, this::freshVariable)
                ?: err("Identifier ${target.name} not found!", sourceSection)

        return when (target) {
            is LocalSymbol -> getLocalSymbol(localSymbols, target.name)
            is PackageSymbol -> {
                if (target.moduleName == pkg.moduleName &&
                    target.packageName == pkg.packageName) {
                    // we are trying to lookup symbol from current package so we need to use local package symbols
                    getLocalSymbol(packageSymbols, target.name)
                } else {
                    // for other packages we just search the target package
                    val symbol = ns.getSymbol(target) ?: err("Identifier ${target.name} not found!", sourceSection)
                    symbol.type?.instantiate(level, this::freshVariable)
                        ?: err("Type not found for identifier ${target.name}", sourceSection)
                }
            }
        }
    }
}