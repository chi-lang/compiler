package gh.marad.chi.core.types

import gh.marad.chi.core.compiler.TypeTable

class InferenceContext(val typeGraph: TypeGraph, val typeTable: TypeTable) {
    private var nextTypeVariableNum = 0
    fun nextTypeVariable() = TypeVariable("t${nextTypeVariableNum++}")
}