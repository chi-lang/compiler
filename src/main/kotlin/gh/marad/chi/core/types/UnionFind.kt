package gh.marad.chi.core.types

/**
 * Union-find data structure for type variable bindings.
 *
 * Instead of eagerly substituting variables throughout the constraint queue
 * (which is O(N) per solution = O(N²) total), we store bindings and resolve
 * them lazily via path-compressed find().
 */
class UnionFind {
    private val bindings = mutableMapOf<Variable, Type>()

    /**
     * Find the representative type for a variable, following chains.
     * Path compression: if a -> b -> int, after find(a), a -> int directly.
     */
    fun find(variable: Variable): Type {
        val bound = bindings[variable] ?: return variable
        if (bound is Variable && bound != variable) {
            val root = find(bound)
            // Path compression
            if (root !== bound) {
                bindings[variable] = root
            }
            return root
        }
        return bound
    }

    /**
     * Bind a variable to a type.
     */
    fun bind(variable: Variable, type: Type) {
        bindings[variable] = type
    }

    /**
     * Deeply resolve a type, replacing all bound variables with their values.
     * Unbound variables are left as-is. This does a single structural traversal.
     */
    fun resolve(type: Type): Type = when (type) {
        is Variable -> {
            val found = find(type)
            if (found is Variable && found == type) type
            else if (found is Variable) found
            else resolve(found)  // resolve the bound type too, in case it contains variables
        }
        is Primitive -> type
        is Function -> {
            val newTypes = type.types.map { resolve(it) }
            if (newTypes.zip(type.types).all { (a, b) -> a === b }) type
            else type.copy(types = newTypes)
        }
        is Record -> {
            val newFields = type.fields.map { field ->
                val resolved = resolve(field.type)
                if (resolved === field.type) field else field.copy(type = resolved)
            }
            if (newFields.zip(type.fields).all { (a, b) -> a === b }) type
            else type.copy(fields = newFields)
        }
        is Sum -> {
            val newLhs = resolve(type.lhs)
            val newRhs = resolve(type.rhs)
            if (newLhs === type.lhs && newRhs === type.rhs) type
            else Sum.create(type.ids, newLhs, newRhs, type.typeParams)
        }
        is Array -> {
            val newElem = resolve(type.elementType)
            if (newElem === type.elementType) type
            else type.copy(elementType = newElem)
        }
        is Recursive -> {
            val newInner = resolve(type.type)
            if (newInner === type.type) type
            else type.copy(type = newInner)
        }
    }

    /**
     * Resolve a constraint's expected and actual types through the union-find.
     */
    fun resolveConstraint(constraint: Constraint): Constraint {
        val newExpected = resolve(constraint.expected)
        val newActual = resolve(constraint.actual)
        if (newExpected === constraint.expected && newActual === constraint.actual) return constraint
        return constraint.copy(expected = newExpected, actual = newActual)
    }

    /**
     * Return all bindings as a list of (Variable, fully-resolved Type) pairs.
     * Used at the end to produce the solution list.
     */
    fun allBindings(): List<Pair<Variable, Type>> {
        return bindings.keys.map { v ->
            v to resolve(find(v))
        }
    }
}
