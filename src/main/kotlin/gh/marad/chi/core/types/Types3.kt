package gh.marad.chi.core.types

import java.util.*
import kotlin.math.max

sealed interface TypeScheme {
    fun instantiate(level: Int, freshVar: (Int) -> Variable) : Type
}

data class PolyType(val level: Int, val body: Type) : TypeScheme {
    override fun instantiate(level: Int, freshVar: (Int) -> Variable): Type {
        val visitor = FreshenAboveVisitor(
            startingLevel = this.level,
            targetLevel = level,
            freshVar = freshVar
        )
        return body.accept(visitor)
    }
}

sealed  interface Type : TypeScheme {
    val level: Int
    override fun instantiate(level: Int, freshVar: (Int) -> Variable): Type = this
    fun <T> accept(visitor: TypeVisitor<T>): T
    fun children(): List<Type>
    fun typeParams(): List<String>

    companion object {
        @JvmStatic val optionTypeId = TypeId("std", "lang.option", "Option")

        @JvmStatic val anyTypeId = TypeId("std", "lang.any", "any")
        @JvmStatic val any = Primitive(listOf(anyTypeId))
        @JvmStatic val unitTypeId = TypeId("std", "lang.unit", "unit")
        @JvmStatic val unit = Primitive(listOf(unitTypeId))
        @JvmStatic val boolTypeId = TypeId("std", "lang.bool", "bool")
        @JvmStatic val bool = Primitive(listOf(boolTypeId))
        @JvmStatic val intTypeId = TypeId("std", "lang.int", "int")
        @JvmStatic val int = Primitive(listOf(intTypeId))
        @JvmStatic val floatTypeId = TypeId("std", "lang.float", "float")
        @JvmStatic val float = Primitive(listOf(floatTypeId))
        @JvmStatic val stringTypeId = TypeId("std", "lang.string", "string")
        @JvmStatic val string = Primitive(listOf(stringTypeId))

        @JvmStatic fun fn(vararg types: Type) = Function(types.toList())
        @JvmStatic fun record(vararg fields: Pair<String, Type>): Record = Record(emptyList(), fields.map { Record.Field(it.first, it.second) })
        @JvmStatic fun record(id: TypeId, vararg fields: Pair<String, Type>): Record = Record(listOf(id), fields.map { Record.Field(it.first, it.second) })
        @JvmStatic fun array(elementType: Type) = Array(elementType)
        @JvmStatic fun union(id: TypeId?, vararg types: Type): Sum =
            types.reduceRight { lhs, rhs -> Sum.create(id?.let { listOf(id) } ?: emptyList(), lhs, rhs) } as Sum
        @JvmStatic fun option(type: Type) = Sum.create(listOf(optionTypeId), type, unit)
    }
}

data class TypeId(
    val moduleName: String,
    val packageName: String,
    val name: String) {
    override fun toString(): String = "$moduleName::$packageName::$name"
}

interface HasTypeId {
    fun getTypeIds(): List<TypeId>
    fun withAddedTypeId(id: TypeId): Type
    fun withAddedTypeIds(ids: List<TypeId>): Type
}

data class Primitive(val ids: List<TypeId>) : Type, HasTypeId {
    init {
        if (ids.isEmpty()) throw AssertionError("Primitive type has to have at least one type id.")
    }

    fun getPrincipalTypeId() = ids.first()

    override val level: Int = 0
    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitPrimitive(this)

    override fun children(): List<Type> = emptyList()
    override fun typeParams(): List<String> = emptyList()
    override fun toString(): String = ids.first().name
    override fun getTypeIds(): List<TypeId> = ids
    override fun withAddedTypeId(id: TypeId): Type {
        return if (id !in ids) {
            copy(ids = ids + id)
        } else {
            this
        }
    }

    override fun withAddedTypeIds(ids: List<TypeId>): Type {
        return copy(ids = this.ids + ids)
    }

}

data class Function(val types: List<Type>, val typeParams: List<String> = emptyList(), val defaultArgs: Int = 0) : Type {
    override val level: Int get() = types.maxOf { it.level }
    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitFunction(this)
    override fun children(): List<Type> = types
    override fun typeParams(): List<String> = typeParams
    override fun toString(): String = "(" + types.joinToString(" -> ") + ")"
}

data class Record(val ids: List<TypeId>, val fields: List<Field>, val typeParams: List<String> = emptyList()) : Type, HasTypeId {
    data class Field(val name: String, val type: Type)

    override val level: Int get() = fields.maxOfOrNull { it.type.level } ?: 0

    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitRecord(this)

    override fun children(): List<Type> = fields.map { it.type }
    override fun typeParams(): List<String> = typeParams
    override fun toString(): String {
        val sb = StringBuilder()
        ids.forEach {
            sb.append(it)
        }
        sb.append('{')
        sb.append(fields.joinToString(", ") { it.name + ": " + it.type })
        sb.append('}')
        return sb.toString()
    }

    override fun getTypeIds(): List<TypeId> = ids
    override fun withAddedTypeId(id: TypeId): Type {
        return if (id !in ids) {
            copy(ids = ids + id)
        } else {
            this
        }
    }

    override fun withAddedTypeIds(ids: List<TypeId>): Type {
        return copy(ids = this.ids + ids)
    }
}

data class Sum(val ids: List<TypeId>, val lhs: Type, val rhs: Type, val typeParams: List<String> = emptyList()) : Type, HasTypeId {
    override fun <T> accept(visitor: TypeVisitor<T>): T = visitor.visitSum(this)
    override fun children(): List<Type> = listOf(lhs, rhs)
    override fun typeParams(): List<String> = typeParams
    override fun toString(): String {
        return if (Type.optionTypeId in ids) {
            val subtypes = listTypes(this) - Type.unit
            "$ids[${subtypes.joinToString("|")}]"
        } else {
            if (ids.isEmpty()) "$lhs | $rhs" else "$ids[$lhs | $rhs]"
        }
    }
    override val level: Int = max(lhs.level, rhs.level)
    fun removeType(type: Type): Type {
        val types = listTypes(this) - type
        return types.reduce { a, b -> Sum(ids, a, b, typeParams) }
    }

    companion object {
        fun create(lhs: Type, rhs: Type) = create(emptyList(), lhs, rhs)

        fun create(ids: List<TypeId>, lhs: Type, rhs: Type, typeParams: List<String> = emptyList()): Type {
            val types = listTypes(lhs) + listTypes(rhs)
            val finalId: List<TypeId> = if (types.contains(Type.unit) && Type.optionTypeId !in ids) ids + Type.optionTypeId else ids
            return types.reduce { a, b -> Sum(finalId.toMutableList(), a, b, typeParams)}
        }

        private fun listTypes(type: Type): Set<Type> = when(type) {
            is Sum -> listTypes(type.lhs) + listTypes(type.rhs)
            else -> setOf(type)
        }
    }

    override fun getTypeIds(): List<TypeId> = ids
    override fun withAddedTypeId(id: TypeId): Type {
        return if (id !in ids) {
            copy(ids = ids + id)
        } else {
            this
        }
    }

    override fun withAddedTypeIds(ids: List<TypeId>): Type {
        return copy(ids = this.ids + ids)
    }
}

data class Array(val elementType: Type, val typeParams: List<String> = emptyList()) : Type, HasTypeId {
    private val ids = mutableListOf(TypeId("std", "lang.array", "array"))
    override val level: Int = elementType.level
    override fun <T> accept(visitor: TypeVisitor<T>): T = visitor.visitArray(this)
    override fun children(): List<Type> = listOf(elementType)
    override fun typeParams(): List<String> = typeParams
    override fun toString(): String = "array[$elementType]"
    override fun getTypeIds(): List<TypeId> = ids
    override fun withAddedTypeId(id: TypeId): Array {
        return if (id !in ids) {
            val result = copy()
            result.ids.addAll(ids.drop(1))
            result.ids.add(id)
            result
        } else {
            this
        }
    }

    override fun withAddedTypeIds(ids: List<TypeId>): Type {
        val result = copy()
        result.ids.addAll(this.ids.drop(1))
        result.ids.addAll(ids)
        return result
    }
}

data class Variable(
    val name: String,
    override val level: Int
) : Type {
    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitVariable(this)
    override fun children(): List<Type> = emptyList()
    override fun typeParams(): List<String> = emptyList()
    override fun toString(): String = "'$name"
    override fun hashCode(): Int = Objects.hash(name, level)
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Variable

        if (name != other.name) return false
        if (level != other.level) return false

        return true
    }
}

data class Recursive(
    val variable: Variable,
    val type: Type
) : Type, HasTypeId {
    override fun getTypeIds(): List<TypeId> {
        return if (type is HasTypeId) {
            type.getTypeIds()
        } else {
            emptyList()
        }
    }

    override fun withAddedTypeId(id: TypeId): Type {
        return if (type is HasTypeId && !type.getTypeIds().contains(id)) {
            copy(type = type.withAddedTypeId(id))
        } else {
            this
        }
    }

    override fun withAddedTypeIds(ids: List<TypeId>): Type {
        return if (type is HasTypeId) {
            copy(type = type.withAddedTypeIds(ids))
        } else {
            this
        }
    }


    override val level: Int = type.level
    override fun <T> accept(visitor: TypeVisitor<T>): T = visitor.visitRecursive(this)
    override fun children(): List<Type> = listOf(type)
    override fun typeParams(): List<String> = type.typeParams()
    override fun toString(): String = "${variable.name}.$type"

    fun unfold(): Type = mapType(type, listOf(variable to this))
}
