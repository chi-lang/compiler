package gh.marad.chi.core.namespace

class PackageTable {
    private val packageMap = mutableMapOf<String, PackageDescriptor>()

    fun add(alias: String, pkg: PackageDescriptor) {
        packageMap[alias] = pkg
    }

    fun get(name: String): PackageDescriptor? = packageMap[name]
}