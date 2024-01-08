package gh.marad.chi.core.types

import gh.marad.chi.core.namespace.GlobalCompilationNamespace

class InferenceContext(val ns: GlobalCompilationNamespace, val typeTable: TypeLookupTable) {
    private var nextTypeVariableNum = 0
    fun nextTypeVariable() = TypeVariable("t${nextTypeVariableNum++}")
}