package gh.marad.chi.core.types

object Types {
    val any = SimpleType("std", "lang", "any")
    val int = SimpleType("std", "lang", "int")
    val float = SimpleType("std", "lang", "float")
    val bool = SimpleType("std", "lang", "bool")
    val unit = SimpleType("std", "lang", "unit")
    val string = SimpleType("std", "lang", "string")
    val array = SimpleType("std", "lang", "array")

    fun generic(vararg types: Type) = GenericType(types.toList())
    fun fn(vararg types: Type) = FunctionType(types.toList())

    fun array(elementType: Type) = GenericType(
        listOf(
            array,
            elementType
        ),
        if (elementType is TypeVariable) {
            listOf(elementType)
        } else {
            emptyList()
        }
    )
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
}

data class SimpleType(val moduleName: String, val packageName: String, val name: String) : Type {
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

data class TypeVariable(val name: String, val typeScheme: Boolean = false) : Type {
    override fun contains(v: TypeVariable): Boolean = v == this
    override fun substitute(v: TypeVariable, t: Type): Type =
        if (v == this) { t } else { this }
    override fun isTypeScheme(): Boolean = typeScheme
    override fun typeSchemeVariables(): List<TypeVariable> = if (isTypeScheme()) listOf(this) else emptyList()
    override fun findTypeVariables(): List<TypeVariable> = listOf(this)
    override fun generalize(variables: List<TypeVariable>): Type = if (variables.contains(this)) {
        copy(typeScheme = true)
    } else {
        this
    }

    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        mappings.firstOrNull { it.first == this }
            ?.let { (v, t) ->
                substitute(v, t)
            } ?: this

    override fun toString(): String = "'$name"
}

data class FunctionType(val types: List<Type>, val typeSchemeVariables: List<TypeVariable> = emptyList()) : Type {
    init {
        assert(types.size >= 2) {
            "Function type requires at least two types. Types given: $types"
        }
    }

    override fun contains(v: TypeVariable): Boolean = types.any { it.contains(v) }
    override fun substitute(v: TypeVariable, t: Type): Type =
        copy(types = types.map { it.substitute(v, t) },
            typeSchemeVariables = if (t is TypeVariable) {
                typeSchemeVariables.map { it.substitute(v,t) as TypeVariable }
            } else {
                typeSchemeVariables
            })
    override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
    override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
    override fun findTypeVariables(): List<TypeVariable> = types.flatMap { it.findTypeVariables() }
    override fun generalize(variables: List<TypeVariable>): Type = copy(typeSchemeVariables = variables)
    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        copy(
            types = types.map { it.instantiate(mappings) },
            typeSchemeVariables = typeSchemeVariables - mappings.map { it.first }.toSet()
        )

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

data class GenericType(
    val types: List<Type>,
    val typeSchemeVariables: List<TypeVariable> = emptyList(),
) : Type {
    init {
        assert(types.size >= 2) { "Generic type needs base type and at least one parameter type. Types given: $types" }
    }

    override fun contains(v: TypeVariable): Boolean = types.contains(v)
    override fun substitute(v: TypeVariable, t: Type): Type =
        copy(types = types.map { it.substitute(v, t) },
            typeSchemeVariables = if (t is TypeVariable) {
                typeSchemeVariables.map { it.substitute(v,t) as TypeVariable }
            } else {
                typeSchemeVariables
            })

    override fun isTypeScheme(): Boolean = typeSchemeVariables.isNotEmpty()
    override fun typeSchemeVariables(): List<TypeVariable> = typeSchemeVariables
    override fun findTypeVariables(): List<TypeVariable> = types.flatMap { it.findTypeVariables() }
    override fun generalize(variables: List<TypeVariable>): Type = copy(typeSchemeVariables = variables)
    override fun instantiate(mappings: List<Pair<TypeVariable, Type>>): Type =
        copy(
            types = types.map { it.instantiate(mappings) },
            typeSchemeVariables = typeSchemeVariables - mappings.map { it.first }.toSet()
        )

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(types.first())
        val tail = types.drop(1)
        if (tail.isNotEmpty()) {
            sb.append('[')
            sb.append(tail.joinToString(", "))
            sb.append(']')
        }
        return sb.toString()
    }
}

