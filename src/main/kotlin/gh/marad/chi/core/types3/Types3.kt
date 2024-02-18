package gh.marad.chi.core.types3

import java.util.*
import kotlin.math.max

sealed interface TypeScheme {
    fun instantiate(level: Int, freshVar: (Int) -> Variable) : Type3
}

data class PolyType(val level: Int, val body: Type3) : TypeScheme {
    override fun instantiate(level: Int, freshVar: (Int) -> Variable): Type3 {
        val visitor = FreshenAboveVisitor(
            startingLevel = this.level,
            targetLevel = level,
            freshVar = freshVar
        )
        return body.accept(visitor)
    }
}

sealed  interface Type3 : TypeScheme {
    val level: Int
    override fun instantiate(level: Int, freshVar: (Int) -> Variable): Type3 = this
    fun <T> accept(visitor: TypeVisitor<T>): T
    fun children(): List<Type3>

    companion object {
        val any = Primitive(TypeId("std", "lang.types.any", "any"))
        val unit = Primitive(TypeId("std", "lang.types.unit", "unit"))
        val bool = Primitive(TypeId("std", "lang.types.bool", "bool"))
        val int = Primitive(TypeId("std", "lang.types.int", "int"))
        val float = Primitive(TypeId("std", "lang.types.float", "float"))
        val string = Primitive(TypeId("std", "lang.types.string", "string"))

        fun fn(vararg types: Type3) = Function(types.toList())
        fun record(vararg fields: Pair<String, Type3>): Record = Record(null, fields.map { Record.Field(it.first, it.second) })
        fun record(id: TypeId, vararg fields: Pair<String, Type3>): Record = Record(id, fields.map { Record.Field(it.first, it.second) })
        fun array(elementType: Type3) = Array(elementType)
        fun union(id: TypeId?, vararg types: Type3): Sum =
            types.reduceRight { lhs, rhs -> Sum.create(id, lhs, rhs) } as Sum
    }
}

data class TypeId(
    val moduleName: String,
    val packageName: String,
    val name: String) {
    override fun toString(): String = "$moduleName::$packageName::$name"
}

interface HasTypeId {
    fun getTypeId(): TypeId?
}

data class Primitive(val id: TypeId) : Type3, HasTypeId {
    override val level: Int = 0
    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitPrimitive(this)

    override fun children(): List<Type3> = emptyList()
    override fun toString(): String = id.name
    override fun getTypeId(): TypeId = id
}

data class Function(val types: List<Type3>) : Type3 {
    override val level: Int get() = types.maxOf { it.level }
    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitFunction(this)
    override fun children(): List<Type3> = types
    override fun toString(): String = "(" + types.joinToString(" -> ") + ")"
}

data class Record(val id: TypeId?, val fields: List<Field>) : Type3, HasTypeId {
    data class Field(val name: String, val type: Type3)

    override val level: Int get() = fields.maxOf { it.type.level }

    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitRecord(this)

    override fun children(): List<Type3> = fields.map { it.type }
    override fun toString(): String {
        val sb = StringBuilder()
        if (id != null) {
            sb.append(id)
        }
        sb.append('{')
        sb.append(fields.joinToString(", ") { it.name + ": " + it.type })
        sb.append('}')
        return sb.toString()
    }

    override fun getTypeId(): TypeId? = id
}

data class Sum(val id: TypeId?, val lhs: Type3, val rhs: Type3) : Type3, HasTypeId {
    override fun <T> accept(visitor: TypeVisitor<T>): T = visitor.visitSum(this)
    override fun children(): List<Type3> = listOf(lhs, rhs)
    override fun toString(): String = "$lhs | $rhs"
    override val level: Int = max(lhs.level, rhs.level)

    companion object {
        fun create(lhs: Type3, rhs: Type3) = create(null, lhs, rhs)

        fun create(id: TypeId?, lhs: Type3, rhs: Type3): Type3 {
            return if (lhs == rhs) {
                lhs
            } else {
                Sum(id, lhs, rhs)
            }
        }
    }

    override fun getTypeId(): TypeId? = id
}

data class Array(val elementType: Type3) : Type3, HasTypeId {
    override fun <T> accept(visitor: TypeVisitor<T>): T = visitor.visitArray(this)
    override fun children(): List<Type3> = listOf(elementType)
    override fun toString(): String = "array[$elementType]"
    override fun getTypeId(): TypeId = TypeId("std", "lang.types.array", "array")
    override val level: Int = elementType.level
}

data class Variable(
    val name: String,
    override val level: Int
) : Type3 {
    override fun <T> accept(visitor: TypeVisitor<T>): T =
        visitor.visitVariable(this)
    override fun children(): List<Type3> = emptyList()
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

