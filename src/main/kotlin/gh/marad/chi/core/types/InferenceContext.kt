package gh.marad.chi.core.types


class InferenceContext(val typeTable: TypeLookupTable) {
    private var nextTypeVariableNum = 0
    fun nextTypeVariable() = TypeVariable("t${nextTypeVariableNum++}")
}