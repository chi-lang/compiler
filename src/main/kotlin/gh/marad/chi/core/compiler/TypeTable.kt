package gh.marad.chi.core.compiler

import gh.marad.chi.core.types.SimpleType
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Types

class TypeTable {
    private val typeMap = mutableMapOf<String, TypeInfo>()

    init {
        addSimpleType(Types.any)
        addSimpleType(Types.int)
        addSimpleType(Types.float)
        addSimpleType(Types.bool)
        addSimpleType(Types.unit)
        addSimpleType(Types.string)
        addSimpleType(Types.array)
    }

    fun add(info: TypeInfo) {
        typeMap[info.name] = info
    }

    fun get(name: String): TypeInfo? = typeMap[name]

    fun find(type: SimpleType) = typeMap.values.firstOrNull {
        it.type == type
    }

    fun forEach(f: (TypeInfo) -> Unit) {
        typeMap.values.forEach(f)
    }

    private fun addSimpleType(t: SimpleType) {
        add(
            TypeInfo(
                name = t.name,
                type = t,
                isPublic = true,
                isVariantConstructor = false,
                parent = null,
                fields = emptyList()
            )

        )
    }
}

data class TypeInfo(
    val name: String,
    val type: Type,
    val isPublic: Boolean,
    val isVariantConstructor: Boolean,
    val parent: TypeInfo?,
    val fields: List<VariantField>
)

data class VariantField(
    val name: String,
    val type: Type,
    val public: Boolean,
)