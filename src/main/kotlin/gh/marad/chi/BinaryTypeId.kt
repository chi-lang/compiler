package gh.marad.chi

enum class BinaryTypeId {
    Any,
    Bool,
    Float,
    Int,
    String,
    Unit,
    Fn,
    TypeVariable,
    Record,
    Sum,
    Array,
    TypeScheme;

    fun id(): Short {
        return ordinal.toShort()
    }

    companion object {
        fun fromId(typeId: kotlin.Int): BinaryTypeId {
            if (typeId >= values().size) {
                TODO("Unknown typeId: $typeId")
            }
            return values()[typeId]
        }
    }
}
