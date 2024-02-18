package gh.marad.chi.core.namespace

import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.types.SimpleType
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeVariable
import gh.marad.chi.core.types.Types

class TypeTable {
    private val aliases = mutableMapOf<String, TypeAlias>()
    private val typeMap = mutableMapOf<String, TypeInfo>()

    init {
        addSimpleType(Types.any)
        addSimpleType(Types.int)
        addSimpleType(Types.float)
        addSimpleType(Types.bool)
        addSimpleType(Types.unit)
        addSimpleType(Types.string)
        add(
            TypeInfo(
                moduleName = "std",
                packageName = "lang",
                name = "array",
                type = Types.array(TypeVariable("T")),
                supertype = Types.any,
                isPublic = true,
                fields = emptyList()
            )

        )
    }

    fun add(typeAlias: TypeAlias) {
        aliases[typeAlias.typeId.name] = typeAlias
    }

    fun get(alias: String, typeAlias: TypeAlias) {
        aliases[alias] = typeAlias
    }

    fun getAlias(name: String): TypeAlias? = aliases[name]

    fun add(info: TypeInfo) {
        typeMap[info.name] = info
    }

    fun add(alias: String, info: TypeInfo) {
        typeMap[alias] = info
    }

    fun add(table: TypeTable) {
        typeMap.putAll(table.typeMap)
        aliases.putAll(table.aliases)
    }

    fun get(name: String): TypeInfo? = typeMap[name]

    fun forEach(f: (String, TypeInfo) -> Unit) {
        typeMap.forEach(f)
    }

    private fun addSimpleType(t: SimpleType) {
        add(
            TypeInfo(
                moduleName = t.moduleName,
                packageName = t.packageName,
                name = t.name,
                type = t,
                supertype = Types.any,
                isPublic = true,
                fields = emptyList()
            )

        )
    }
}

data class TypeInfo(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val type: Type,
    val supertype: Type,
    val isPublic: Boolean,
    val fields: List<VariantField>,
)

data class VariantField(
    val name: String,
    val type: Type,
    val public: Boolean,
)