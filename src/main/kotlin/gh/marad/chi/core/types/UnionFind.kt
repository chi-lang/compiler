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
     *
     * Uses [resolving] to track variables currently being resolved, preventing
     * infinite recursion for self-referential bindings (e.g., T -> Sum(T, unit)
     * which is valid for sum type widening).
     */
    fun resolve(type: Type): Type = resolveWith(type, mutableSetOf())

    private fun resolveWith(type: Type, resolving: MutableSet<Variable>): Type = when (type) {
        is Variable -> {
            if (type in resolving) {
                // Self-referential binding — return the variable as-is to break the cycle
                type
            } else {
                val found = find(type)
                if (found is Variable && found == type) type
                else if (found is Variable) {
                    // found is a different variable — resolve it too
                    resolveWith(found, resolving)
                }
                else {
                    resolving.add(type)
                    val result = resolveWith(found, resolving)
                    resolving.remove(type)
                    result
                }
            }
        }
        is Primitive -> type
        is Function -> {
            val newTypes = type.types.map { resolveWith(it, resolving) }
            if (newTypes.zip(type.types).all { (a, b) -> a === b }) type
            else type.copy(types = newTypes)
        }
        is Record -> {
            val newFields = type.fields.map { field ->
                val resolved = resolveWith(field.type, resolving)
                if (resolved === field.type) field else field.copy(type = resolved)
            }
            if (newFields.zip(type.fields).all { (a, b) -> a === b }) type
            else type.copy(fields = newFields)
        }
        is Sum -> {
            val newLhs = resolveWith(type.lhs, resolving)
            val newRhs = resolveWith(type.rhs, resolving)
            if (newLhs === type.lhs && newRhs === type.rhs) type
            else Sum.create(type.ids, newLhs, newRhs, type.typeParams)
        }
        is Array -> {
            val newElem = resolveWith(type.elementType, resolving)
            if (newElem === type.elementType) type
            else type.copy(elementType = newElem)
        }
        is Recursive -> {
            val newInner = resolveWith(type.type, resolving)
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
