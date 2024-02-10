package gh.marad.chi.core.types3

class InferenceContext {
    private var nextVariableId = 1
    fun freshVariable(level: Int) = Variable("a${nextVariableId++}", level)
}