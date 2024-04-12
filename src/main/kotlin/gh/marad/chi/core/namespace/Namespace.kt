package gh.marad.chi.core.namespace

import gh.marad.chi.core.CompilationDefaults
import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.TypeAlias

interface GlobalCompilationNamespace {
    fun getPreludeImports(): List<PreludeImport>
    fun getDefaultPackage(): PackageDescriptor
    fun getOrCreatePackage(moduleName: String, packageName: String): PackageDescriptor
    fun getOrCreatePackage(pkg: Package): PackageDescriptor
    fun getSymbol(moduleName: String, packageName: String, symbolName: String): Symbol?
    fun getSymbol(target: PackageSymbol): Symbol?
    fun getTypeAlias(moduleName: String, packageName: String, typeAliasName: String): TypeAlias?
}

class GlobalCompilationNamespaceImpl(private val prelude: List<PreludeImport> = emptyList()) : GlobalCompilationNamespace {
    private val modules: MutableMap<String, ModuleDescriptor> = mutableMapOf()

    init {
        getDefaultPackage()
    }

    override fun getPreludeImports(): List<PreludeImport> = prelude

    override fun getDefaultPackage() =
        getOrCreatePackage(CompilationDefaults.defaultModule, CompilationDefaults.defaultPacakge)

    override fun getOrCreatePackage(moduleName: String, packageName: String): PackageDescriptor =
        getOrCreateModule(moduleName).getOrCreatePackage(packageName)

    override fun getOrCreatePackage(pkg: Package): PackageDescriptor =
        getOrCreateModule(pkg.moduleName).getOrCreatePackage(pkg.packageName)

    override fun getSymbol(moduleName: String, packageName: String, symbolName: String) =
        getOrCreatePackage(moduleName, packageName).getSymbol(symbolName)

    override fun getSymbol(target: PackageSymbol) =
        getOrCreatePackage(target.moduleName, target.packageName)
            .getSymbol(target.name)

    override fun getTypeAlias(moduleName: String, packageName: String, typeAliasName: String) =
        getOrCreatePackage(moduleName, packageName)
            .getTypeAlias(typeAliasName)

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
) {
    fun getSymbol(name: String) = symbols.get(name)
    fun getTypeAlias(name: String) = types.getAlias(name)
}

