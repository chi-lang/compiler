package gh.marad.chi.core

import gh.marad.chi.core.analyzer.typesMatch
import java.util.*

sealed interface OldType {
    val name: String
    val moduleName: String
    val packageName: String
    fun isPrimitive(): Boolean
    fun isNumber(): Boolean

    // can index operator be used
    fun isIndexable(): Boolean = false

    // what type should index be?
    fun expectedIndexType(): OldType = undefined

    // what is the type of indexed element
    fun indexedElementType(): OldType = undefined

    fun getAllSubtypes(): List<OldType>
    fun isTypeConstructor(): Boolean = false
    fun construct(concreteTypes: Map<GenericTypeParameter, OldType>): OldType = this
    fun applyTypeParameters(typeParameters: List<OldType>): OldType = this

    fun isCompositeType(): Boolean

    fun toDisplayString(): String = name

    companion object {
        @JvmStatic
        val intType = IntType()

        //        val i64 = I64Type()
        @JvmStatic
        val floatType = FloatType()

        //        val f64 = F64Type()
        @JvmStatic
        val unit = UnitType()

        @JvmStatic
        val bool = BoolType()

        @JvmStatic
        val string = StringType()

        @JvmStatic
        val undefined = UndefinedType()

        @JvmStatic
        val any = AnyType()

        @JvmStatic
        fun fn(returnType: OldType, vararg argTypes: OldType) =
            FnType(genericTypeParameters = emptyList(), paramTypes = argTypes.toList(), returnType)

        @JvmStatic
        fun genericFn(
            genericTypeParameters: List<GenericTypeParameter>,
            returnType: OldType,
            vararg argTypes: OldType
        ) = FnType(genericTypeParameters, argTypes.toList(), returnType)

        @JvmStatic
        fun array(elementType: OldType) = ArrayType(elementType)

        @JvmStatic
        fun typeParameter(name: String) = GenericTypeParameter(name)
    }
}

data class UndefinedType(override val name: String = "undefined") : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "lang"
    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false
    override fun getAllSubtypes(): List<OldType> = emptyList()
    override fun isCompositeType(): Boolean = false
    override fun toDisplayString(): String = "<undefined>"
    override fun toString(): String = name
}

sealed interface PrimitiveType : OldType {
    override val moduleName: String get() = "std"
    override val packageName: String get() = "lang"
    override fun isPrimitive(): Boolean = true
    override fun isNumber(): Boolean = false
    override fun isCompositeType(): Boolean = false
    override fun getAllSubtypes(): List<OldType> = emptyList()
}

sealed interface NumberType : PrimitiveType {
    override fun isPrimitive(): Boolean = true
    override fun isNumber(): Boolean = true
    override fun isCompositeType(): Boolean = false
}

data class IntType internal constructor(override val name: String = "int") : NumberType {
    override fun toString(): String = name
}

data class FloatType internal constructor(override val name: String = "float") : NumberType {
    override fun toString(): String = name
}

data class UnitType internal constructor(override val name: String = "unit") : PrimitiveType {
    override fun toString(): String = name
}

data class BoolType internal constructor(override val name: String = "bool") : PrimitiveType {
    override fun toString(): String = name
}

data class StringType(override val name: String = "string") : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "string"
    override fun isPrimitive(): Boolean = true
    override fun isNumber(): Boolean = false
    override fun isCompositeType(): Boolean = false

    override fun isIndexable(): Boolean = true
    override fun expectedIndexType(): OldType = OldType.intType
    override fun indexedElementType(): OldType = OldType.string
    override fun getAllSubtypes(): List<OldType> = emptyList()
    override fun toString(): String = name
}

data class FnType(
    val genericTypeParameters: List<GenericTypeParameter>,
    val paramTypes: List<OldType>,
    val returnType: OldType
) : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "lang"
    override val name = "(${paramTypes.joinToString(", ") { it.toDisplayString() }}) -> ${returnType.toDisplayString()}"
    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false
    override fun isCompositeType(): Boolean = false
    override fun isTypeConstructor(): Boolean =
        paramTypes.any { it.isTypeConstructor() } || returnType.isTypeConstructor()

    override fun construct(concreteTypes: Map<GenericTypeParameter, OldType>): OldType =
        copy(
            paramTypes = paramTypes.map { it.construct(concreteTypes) },
            returnType = returnType.construct(concreteTypes)
        )

    override fun applyTypeParameters(typeParameters: List<OldType>): OldType =
        construct(
            genericTypeParameters.zip(typeParameters).toMap()
        )

    override fun getAllSubtypes(): List<OldType> = paramTypes + returnType
    override fun toString(): String = name
}

data class OverloadedFnType(val typeSet: Set<FnType>) : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "lang"
    val types = typeSet.map { FnTypeContainer(it) }.toSet()
    override val name: String = "overloadedFn"
    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false
    override fun getAllSubtypes(): List<OldType> = emptyList()

    override fun isCompositeType(): Boolean = false

    fun addFnType(fnType: FnType) = copy(typeSet = (types.map { it.fnType } + fnType).toSet())
    fun getType(paramTypes: List<OldType>): FnType? =
        findCandidates(paramTypes).singleOrNull()

    private fun findCandidates(actualTypes: List<OldType>): List<FnType> {
        val candidates = types.filter {
            val genericParamToTypeFromPassedParameters =
                matchCallTypes(
                    it.fnType.paramTypes,
                    actualTypes
                )
            actualTypes.size == it.fnType.paramTypes.size
                    && it.fnType.paramTypes.zip(actualTypes).all { (expected, actual) ->
                typesMatch(
                    expected.construct(genericParamToTypeFromPassedParameters),
                    actual,
                )
            }
        }
        val withScores =
            candidates.map { Pair(it, scoreParamTypes(it.fnType.paramTypes, actualTypes)) }
                .sortedByDescending { it.second }
        return if (withScores.isEmpty()) {
            emptyList()
        } else {
            val maxScore = withScores[0].second
            withScores.filter { it.second == maxScore }.map { it.first.fnType }
        }
    }

    private fun scoreParamTypes(expectedTypes: List<OldType>, actualTypes: List<OldType>): Int {
        return expectedTypes.zip(actualTypes).fold(0) { acc, (expected, actual) ->
            when {
                expected == OldType.any -> acc
                expected == actual -> acc + 3
                expected.isPrimitive() -> acc + 2
                expected.isCompositeType() -> acc + 1
                else -> acc
            }
        }
    }

    class FnTypeContainer(val fnType: FnType) {
        override fun toString(): String = fnType.toString()
        override fun hashCode(): Int = Objects.hashCode(fnType.paramTypes)
        override fun equals(other: Any?): Boolean =
            other != null && other is FnTypeContainer
                    && fnType.paramTypes == other.fnType.paramTypes
    }
}


data class GenericTypeParameter(val typeParameterName: String) : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "lang"
    override val name: String = typeParameterName

    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false

    override fun isCompositeType(): Boolean = false

    override fun isTypeConstructor(): Boolean = true
    override fun getAllSubtypes(): List<OldType> = emptyList()
    override fun construct(concreteTypes: Map<GenericTypeParameter, OldType>): OldType =
        concreteTypes[this] ?: this
}

data class ArrayType(val elementType: OldType) : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "collections.array"
    override val name: String = "array[${elementType.name}]"

    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false
    override fun isCompositeType(): Boolean = false
    override fun isIndexable(): Boolean = true
    override fun expectedIndexType(): OldType = OldType.intType
    override fun indexedElementType(): OldType = elementType
    override fun getAllSubtypes(): List<OldType> = listOf(elementType)
    override fun isTypeConstructor(): Boolean = elementType.isTypeConstructor()
    override fun construct(concreteTypes: Map<GenericTypeParameter, OldType>) =
        copy(elementType = elementType.construct(concreteTypes))

    override fun applyTypeParameters(typeParameters: List<OldType>) =
        copy(elementType = typeParameters[0])

}

data class AnyType(override val name: String = "any") : OldType {
    override val moduleName: String = "std"
    override val packageName: String = "lang"
    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false
    override fun getAllSubtypes(): List<OldType> = emptyList()
    override fun isCompositeType(): Boolean = false
    override fun toString(): String = name
}

sealed interface CompositeType : OldType {
    override fun isCompositeType(): Boolean = true
    fun memberType(member: String): OldType?
    fun hasMember(member: String): Boolean = false
    fun isPublic(member: String): Boolean
}

data class VariantType(
    override val moduleName: String,
    override val packageName: String,
    val simpleName: String,
    val genericTypeParameters: List<GenericTypeParameter>,
    val concreteTypeParameters: Map<GenericTypeParameter, OldType>,
    var variant: Variant?,
) : CompositeType {

    fun withVariant(variant: Variant?): VariantType =
        copy(variant = variant)

    override val name: String = "$moduleName/$packageName.$simpleName"
    override fun isPrimitive(): Boolean = false
    override fun isNumber(): Boolean = false
    override fun toDisplayString(): String =
        "$name${concreteTypeParametersToDisplayString()}"

    override fun toString(): String = toDisplayString()

    private fun concreteTypeParametersToDisplayString(): String =
        if (concreteTypeParameters.isNotEmpty()) "[${
            concreteTypeParameters.entries.joinToString(", ") { "${it.key.name}=${it.value.toDisplayString()}" }
        }]"
        else ""

    override fun hasMember(member: String): Boolean = variant?.let {
        it.fields.any { it.name == member }
    } ?: false

    override fun memberType(member: String): OldType? = variant?.let {
        it.fields.find { it.name == member }?.type
    }

    override fun isPublic(member: String): Boolean = variant?.let {
        it.fields.find { it.name == member }?.public
    } ?: false

    override fun getAllSubtypes(): List<OldType> {
        val result = mutableListOf<OldType>()
        result.addAll(variant?.fields?.map {
            it.type
        } ?: emptyList())
        result.addAll(concreteTypeParameters.values)
        return result
    }

    override fun isTypeConstructor(): Boolean = genericTypeParameters.isNotEmpty()

    override fun construct(concreteTypes: Map<GenericTypeParameter, OldType>): OldType {
        val variant = variant
        return copy(
            concreteTypeParameters = applyConcreteTypes(concreteTypes),
            variant = variant?.copy(
                fields = variant.fields.map {
                    if (it.type.isTypeConstructor()) {
                        it.copy(type = it.type.construct(concreteTypes))
                    } else {
                        it
                    }
                }
            ))
    }

    private fun applyConcreteTypes(concreteTypes: Map<GenericTypeParameter, OldType>): Map<GenericTypeParameter, OldType> =
        if (concreteTypeParameters.isNotEmpty()) {
            concreteTypeParameters.mapValues { it.value.construct(concreteTypes) }
        } else {
            concreteTypes
        }

    override fun applyTypeParameters(typeParameters: List<OldType>): OldType =
        construct(genericTypeParameters.zip(typeParameters).toMap())

    data class Variant(val public: Boolean, val variantName: String, val fields: List<VariantField>)
    data class VariantField(val public: Boolean, val name: String, val type: OldType)

    override fun hashCode(): Int = Objects.hash(moduleName, packageName, simpleName)
    override fun equals(other: Any?): Boolean =
        other is VariantType
                && other.moduleName == moduleName
                && other.packageName == packageName
                && other.simpleName == simpleName
                && other.concreteTypeParameters.keys.intersect(concreteTypeParameters.keys)
            .all {
                other.concreteTypeParameters[it] == concreteTypeParameters[it]
            }
}