package gh.marad.chi.core.types

data class TypeInfo(
    val type: Type,
    val isPublic: Boolean,
    val isVariant: Boolean,
    val baseType: Type,
    val fields: List<VariantTypeField>
)

data class VariantTypeField(
    val name: String,
    val type: Type,
    val isPublic: Boolean
)
