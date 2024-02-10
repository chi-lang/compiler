package gh.marad.chi.core.types

import gh.marad.chi.core.parser.ChiSource

object Types {
    @JvmStatic val any = SimpleType("std", "lang", "any")
    @JvmStatic val int = SimpleType("std", "lang", "int")
    @JvmStatic val float = SimpleType("std", "lang", "float")
    @JvmStatic val bool = SimpleType("std", "lang", "bool")
    @JvmStatic val unit = SimpleType("std", "lang", "unit")
    @JvmStatic val string = SimpleType("std", "lang", "string")

    @JvmStatic fun fn(vararg types: Type) = FunctionType(types.toList())

    @JvmStatic fun array(elementType: Type) = ProductType(
        "std", "collections.array", "array",
        types = listOf(),
        typeParams = listOf(elementType),
        typeSchemeVariables = if (elementType is TypeVariable) {
            listOf(elementType)
        } else {
            emptyList()
        },
    )

    @JvmStatic fun commonSupertype(a: Type, b: Type): Type {
        // todo wywal to wszystko i zacznij od nowa kombinujac z constraintami
        if (a == b) {
            return a
        }
        if (isSubtype(a, b)) {
            return a
        }
        if (isSubtype(b, a)) {
            return b
        }
        return any
    }

    @JvmStatic fun isSubtype(parent: Type, child: Type): Boolean {
        if (parent == any) {
            return true
        }
        if (parent !is SumType) {
            return false
        }

        if (child is SimpleType && parent.subtypes.contains(child.name)
            && parent.moduleName == child.moduleName
            && parent.packageName == child.packageName) {
            return true
        }

        if (child is ProductType && parent.subtypes.contains(child.name)
            && parent.moduleName == child.moduleName
            && parent.packageName == child.packageName) {
            parent.typeParams.zip(child.typeParams).all { (pp, cp) ->
                pp is TypeVariable || pp == cp
            }
            return true
        }
        return false
    }
}

sealed interface Type {
    fun contains(v: TypeVariable): Boolean
    fun substitute(v: TypeVariable, t: Type): Type
    fun isTypeScheme(): Boolean

    /// If type is a type scheme, typeVariables returns it's generalized type variables
    fun typeSchemeVariables(): List<TypeVariable>

    /// Lists all the type variables within this type (not only generalized ones)
    fun findTypeVariables(): List<TypeVariable>
    fun generalize(variables: List<TypeVariable>): Type
    fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type
    var sourceSection: ChiSource.Section?
}

data class SimpleType(val moduleName: String, val packageName: String, val name: String) : Type {
    override var sourceSection: ChiSource.Section? = null
    override fun contains(v: TypeVariable): Boolean = false
    override fun substitute(v: TypeVariable, t: Type) = this
    override fun isTypeScheme(): Boolean = false
    override fun typeSchemeVariables(): List<TypeVariable> = emptyList()
    override fun findTypeVariables(): List<TypeVariable> = emptyList()
    override fun generalize(variables: List<TypeVariable>): Type = this
    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type = this

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(moduleName)
        sb.append("::")
        sb.append(packageName)
        sb.append("::")
        sb.append(name)
        return sb.toString()
    }
}

data class TypeVariable(val name: String) : Type {
    override var sourceSection: ChiSource.Section? = null
    override fun contains(v: TypeVariable): Boolean = v == this
    override fun substitute(v: TypeVariable, t: Type): Type =
        if (v == this) { t } else { this }
    override fun isTypeScheme(): Boolean = false
    override fun typeSchemeVariables(): List<TypeVariable> = emptyList()
    override fun findTypeVariables(): List<TypeVariable> = listOf(this)
    override fun generalize(variables: List<TypeVariable>): Type = this

    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        mappings.firstOrNull { it.first == this }
            ?.let { (v, t) ->
                substitute(v, t)
            } ?: this


    override fun toString(): String = "'$name"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypeVariable

        return name == other.name
    }

    override fun hashCode(): Int = name.hashCode()
}

data class FunctionType(val types: List<Type>, val typeSchemeVariables: List<TypeVariable> = emptyList()) : Type {
    override var sourceSection: ChiSource.Section? = null
    override fun contains(v: TypeVariable): Boolean = types.any { it.contains(v) }
    override fun substitute(v: TypeVariable, t: Type): Type =
        copy(
            types = types.map { it.substitute(v, t) },
            typeSchemeVariables = typeSchemeVariables.substitute(v, t)
        ).also { it.sourceSection = sourceSection }
    override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
    override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
    override fun findTypeVariables(): List<TypeVariable> = types.flatMap { it.findTypeVariables() }
    override fun generalize(variables: List<TypeVariable>): Type =
        copy(typeSchemeVariables = variables).also { it.sourceSection = sourceSection }
    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        copy(
            types = types.map { it.instantiate(mappings) },
            typeSchemeVariables = typeSchemeVariables.instantiate(mappings)
        ).also { it.sourceSection = sourceSection }

    override fun toString(): String {
        val sb = StringBuilder()
        if (isTypeScheme()) {
            sb.append('[')
            sb.append(typeSchemeVariables.joinToString(", "))
            sb.append(']')
        }
        sb.append('(')
        sb.append(types.joinToString(" -> "))
        sb.append(')')
        return sb.toString()
    }
}

fun List<TypeVariable>.substitute(v: TypeVariable, t: Type) =
    if (t is TypeVariable) {
        this.map { it.substitute(v, t) as TypeVariable } - v
    } else {
        this - v
    }

fun List<TypeVariable>.instantiate(mappings: List<Pair<TypeVariable, Type>>): List<TypeVariable> =
    this - mappings.map { it.first }.toSet()
//    this.map { it.instantiate(mappings) }.filterIsInstance<TypeVariable>()

data class ProductType(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val types: List<Type>,
    val typeParams: List<Type>,
    val typeSchemeVariables: List<TypeVariable> = emptyList(),
) : Type {
    override var sourceSection: ChiSource.Section? = null
    override fun contains(v: TypeVariable): Boolean = types.any { it.contains(v) }

    override fun substitute(v: TypeVariable, t: Type): Type =
        copy(
            types = types.map { it.substitute(v, t) },
            typeParams = typeParams.map { it.substitute(v, t) },
            typeSchemeVariables = typeSchemeVariables.substitute(v, t)
        ).also { it.sourceSection = sourceSection }
    override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
    override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
    override fun findTypeVariables(): List<TypeVariable> = types.flatMap { it.findTypeVariables() }
    override fun generalize(variables: List<TypeVariable>): Type =
        copy(typeSchemeVariables = variables).also { it.sourceSection = sourceSection }
    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        copy(
            types = types.map { it.instantiate(mappings) },
            typeParams = typeParams.map { it.instantiate(mappings) },
            typeSchemeVariables = typeSchemeVariables.instantiate(mappings)
        ).also { it.sourceSection = sourceSection }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(moduleName)
        sb.append("::")
        sb.append(packageName)
        sb.append("::")
        sb.append(name)
        if (typeParams.isNotEmpty()) {
            sb.append('[')
            sb.append(typeParams.joinToString(", "))
            sb.append(']')
        }
        return sb.toString()
    }
}

data class SumType(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val typeParams: List<Type>,
    val subtypes: List<String>,
    val typeSchemeVariables: List<TypeVariable>,
) : Type {
    override var sourceSection: ChiSource.Section? = null
    override fun contains(v: TypeVariable): Boolean = typeParams.any { it.contains(v) }
    override fun substitute(v: TypeVariable, t: Type): Type =
        copy(
            typeParams = typeParams.map { it.substitute(v, t) },
            typeSchemeVariables = typeSchemeVariables.substitute(v, t)
        ).also { it.sourceSection = sourceSection }
    override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
    override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
    override fun findTypeVariables(): List<TypeVariable> = typeParams.flatMap { it.findTypeVariables() }
    override fun generalize(variables: List<TypeVariable>): Type =
        copy(typeSchemeVariables = variables).also { it.sourceSection = sourceSection }
    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        copy(
            typeParams = typeParams.map { it.instantiate(mappings) },
            typeSchemeVariables = typeSchemeVariables.instantiate(mappings)
        ).also { it.sourceSection = sourceSection }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(moduleName)
        sb.append("::")
        sb.append(packageName)
        sb.append("::")
        sb.append(name)
        if (typeParams.isNotEmpty()) {
            sb.append('[')
            sb.append(typeParams.joinToString(", "))
            sb.append(']')
        }
        return sb.toString()
    }
}
