package gh.marad.chi.core.types

import gh.marad.chi.core.LocalSymbol
import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.Target
import gh.marad.chi.core.compiler.CompileTables
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.ChiSource

class InferenceEnv(private val pkgDef: Package, tables: CompileTables, private val ns: GlobalCompilationNamespace) {
    private val packageEnv = mutableMapOf<String, Type>()
    private var currentLocalEnv: MutableMap<String, Type> = packageEnv

    init {
        tables.localSymbolTable.forEach { name, symbol ->
            if (symbol.type != null) {
                packageEnv[name] = symbol.type
            }
        }
    }

    fun <T> withNewLocalEnv(f: () -> T): T {
        val prevEnv = currentLocalEnv
        currentLocalEnv = mutableMapOf()
        return f().also { currentLocalEnv = prevEnv }
    }

    fun applySubstitutionToAllTypes(solutions: List<Pair<TypeVariable, Type>>) {
        val keys = currentLocalEnv.keys
        for (key in keys) {
            val value = currentLocalEnv[key]!!
            currentLocalEnv.put(key, applySubstitution(value, solutions))
        }
    }

    fun findAllTypeVariables(): List<TypeVariable> =
        currentLocalEnv.flatMap { it.value.findTypeVariables() }

    fun getTypeOrNull(name: String): Type? = currentLocalEnv[name]
    fun getType(name: String, section: ChiSource.Section?): Type {
        return currentLocalEnv[name]
            ?: packageEnv[name]
            ?: throw TypeInferenceFailed("Symbol $name not found in scope.", section)
    }

    fun getNames(): List<String> = currentLocalEnv.keys.toList()

    fun getType(target: Target, section: ChiSource.Section?): Type {
        val type = when(target) {
            is LocalSymbol -> currentLocalEnv[target.name]
            is PackageSymbol -> {
                if (target.moduleName == pkgDef.moduleName && target.packageName == pkgDef.packageName) {
                    // reading name from current package
                    packageEnv[target.name]
                } else {
                    // reading from other package
                    ns.getOrCreatePackage(target.moduleName, target.packageName)
                        .symbols.get(target.name)?.type
                }
            }
        }
        return type ?: throw TypeInferenceFailed("Type not found for $target.", section)

    }

    fun setType(name: String, type: Type) {
        currentLocalEnv[name] = type
    }
}