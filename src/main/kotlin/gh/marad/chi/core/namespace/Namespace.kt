package gh.marad.chi.core.namespace

import gh.marad.chi.core.CompilationDefaults
import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.TypeAlias

interface CompilationEnv {
    fun getPreludeImports(): List<PreludeImport>
    fun getOrCreatePackage(moduleName: String, packageName: String): PackageDescriptor
    fun getOrCreatePackage(pkg: Package): PackageDescriptor
    fun getSymbol(moduleName: String, packageName: String, symbolName: String): Symbol?
    fun getSymbol(target: PackageSymbol): Symbol?
    fun getTypeAlias(moduleName: String, packageName: String, typeAliasName: String): TypeAlias?
}

interface PackageDescriptor {
    val moduleName: String
    val packageName: String
    fun getSymbol(name: String): Symbol?
    fun getTypeAlias(name: String): TypeAlias?
}

class TestCompilationEnv(private val prelude: List<PreludeImport> = emptyList()) : CompilationEnv {
    private val modules: MutableMap<String, TestModuleDescriptor> = mutableMapOf()

    init {
        getDefaultPackage()
    }

    override fun getPreludeImports(): List<PreludeImport> = prelude

    fun getDefaultPackage() =
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

    private fun getOrCreateModule(moduleName: String) = modules.getOrPut(moduleName) { TestModuleDescriptor(moduleName) }

    fun addSymbol(symbol: Symbol) {
        val descriptor = getOrCreatePackage(symbol.moduleName, symbol.packageName) as TestPackageDescriptor
        descriptor.symbols.add(symbol)
    }
}


data class PreludeImport(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val alias: String?
)

class TestModuleDescriptor(
    val moduleName: String,
    private val packageDescriptors: MutableMap<String, TestPackageDescriptor> = mutableMapOf()
) {
    fun getOrCreatePackage(packageName: String): TestPackageDescriptor =
        packageDescriptors.getOrPut(packageName) {
            TestPackageDescriptor(moduleName, packageName)
        }
}

data class TestPackageDescriptor(
    override val moduleName: String,
    override val packageName: String,
    val symbols: SymbolTable = SymbolTable(),
    val types: TypeTable = TypeTable(),
) : PackageDescriptor {
    override fun getSymbol(name: String) = symbols.get(name)
    override fun getTypeAlias(name: String) = types.getAlias(name)
}

