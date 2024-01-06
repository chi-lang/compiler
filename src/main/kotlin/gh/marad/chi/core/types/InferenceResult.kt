package gh.marad.chi.core.types

data class InferenceResult(val type: Type, val constraints: Set<Constraint>, val env: Map<String, Type>)