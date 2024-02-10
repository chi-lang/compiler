package gh.marad.chi.core.types

import gh.marad.chi.core.namespace.GlobalCompilationNamespace

class InferenceContext(val ns: GlobalCompilationNamespace, val typeTable: TypeLookupTable) {
    private var nextTypeVariableNum = 0
    fun nextTypeVariable() = TypeVariable("t${nextTypeVariableNum++}")

    fun getTypePackageOrNull(type: Type) = when(type) {
        is SimpleType -> ns.getOrCreatePackage(type.moduleName, type.packageName)
        is ProductType -> ns.getOrCreatePackage(type.moduleName, type.packageName)
        is SumType -> ns.getOrCreatePackage(type.moduleName, type.packageName)
        is FunctionType -> null
        is TypeVariable -> null
    }
}