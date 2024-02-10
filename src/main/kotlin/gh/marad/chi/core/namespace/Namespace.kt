package gh.marad.chi.core.namespace

import gh.marad.chi.core.CompilationDefaults
import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol

class GlobalCompilationNamespace(val prelude: List<PreludeImport> = emptyList()) {
    private val modules: MutableMap<String, ModuleDescriptor> = mutableMapOf()

    init {
        getDefaultPackage()
    }

    fun getDefaultPackage() =
        getOrCreatePackage(CompilationDefaults.defaultModule, CompilationDefaults.defaultPacakge)

    fun getOrCreatePackage(moduleName: String, packageName: String): PackageDescriptor =
        getOrCreateModule(moduleName).getOrCreatePackage(packageName)

    fun getOrCreatePackage(pkg: Package): PackageDescriptor =
        getOrCreateModule(pkg.moduleName).getOrCreatePackage(pkg.packageName)

    fun getSymbol(target: PackageSymbol) =
        getOrCreatePackage(target.moduleName, target.packageName)
            .symbols.get(target.name)

    private fun getOrCreateModule(moduleName: String) = modules.getOrPut(moduleName) { ModuleDescriptor(moduleName) }
}


data class PreludeImport(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val alias: String?
)

class ModuleDescriptor(
    val moduleName: String,
    private val packageDescriptors: MutableMap<String, PackageDescriptor> = mutableMapOf()
) {
    fun getOrCreatePackage(packageName: String): PackageDescriptor =
        packageDescriptors.getOrPut(packageName) {
            PackageDescriptor(moduleName, packageName)
        }
}

data class PackageDescriptor(
    val moduleName: String,
    val packageName: String,
    val symbols: SymbolTable = SymbolTable(),
    val types: TypeTable = TypeTable(),
)

