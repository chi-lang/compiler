package gh.marad.chi.core.types

import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.TypeInfo

class TypeLookupTable(
    private val ns: GlobalCompilationNamespace,
) {
    fun find(type: Type): TypeInfo? {
        val (moduleName, packageName, name) = when (type) {
            is SimpleType -> arrayOf(type.moduleName, type.packageName, type.name)
            is ProductType -> arrayOf(type.moduleName, type.packageName, type.name)
            is SumType -> arrayOf(type.moduleName, type.packageName, type.name)
            else -> TODO("Finding type $type is not supported!")
        }
        return ns.getOrCreatePackage(moduleName, packageName).types.get(name)
    }
}