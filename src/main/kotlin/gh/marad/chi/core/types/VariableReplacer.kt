package gh.marad.chi.core.types

class VariableReplacer(private val toReplace: Variable, private val substitution: Type) : TypeVisitor<Type> {
    fun replace(type: Type): Type {
        return type.accept(this)
    }

    override fun visitPrimitive(primitive: Primitive): Type = primitive

    override fun visitFunction(function: Function): Type =
        function.copy(types = function.types.map { replace(it) })

    override fun visitRecord(record: Record): Type =
        record.copy(fields = record.fields.map { it.copy(type = replace(it.type)) })

    override fun visitSum(sum: Sum): Type =
        Sum.create(sum.ids, replace(sum.lhs), replace(sum.rhs))

    override fun visitArray(array: Array): Type =
        array.copy(elementType = replace(array.elementType))

    override fun visitRecursive(recursive: Recursive): Type =
        recursive.copy(type = replace(recursive.type))

    override fun visitVariable(variable: Variable): Type =
        if (variable == toReplace) {
            substitution
        } else {
            variable
        }
}

/**
 * Replaces multiple variables in a single traversal.
 * Instead of creating N VariableReplacer objects and doing N full traversals,
 * this does a single pass with O(1) lookup per variable.
 */
class BulkVariableReplacer(private val substitutions: Map<Variable, Type>) : TypeVisitor<Type> {
    // Track variables currently being resolved to detect cycles
    private val resolving = mutableSetOf<Variable>()

    fun replace(type: Type): Type = type.accept(this)

    override fun visitPrimitive(primitive: Primitive): Type = primitive

    override fun visitFunction(function: Function): Type {
        val newTypes = function.types.map { replace(it) }
        return if (newTypes.zip(function.types).all { (a, b) -> a === b }) function
        else function.copy(types = newTypes)
    }

    override fun visitRecord(record: Record): Type {
        val newFields = record.fields.map { field ->
            val resolved = replace(field.type)
            if (resolved === field.type) field else field.copy(type = resolved)
        }
        return if (newFields.zip(record.fields).all { (a, b) -> a === b }) record
        else record.copy(fields = newFields)
    }

    override fun visitSum(sum: Sum): Type {
        val newLhs = replace(sum.lhs)
        val newRhs = replace(sum.rhs)
        return if (newLhs === sum.lhs && newRhs === sum.rhs) sum
        else Sum.create(sum.ids, newLhs, newRhs, sum.typeParams)
    }

    override fun visitArray(array: Array): Type {
        val newElem = replace(array.elementType)
        return if (newElem === array.elementType) array
        else array.copy(elementType = newElem)
    }

    override fun visitRecursive(recursive: Recursive): Type {
        val newType = replace(recursive.type)
        return if (newType === recursive.type) recursive
        else recursive.copy(type = newType)
    }

    override fun visitVariable(variable: Variable): Type {
        val sub = substitutions[variable] ?: return variable
        // Detect cycles: if we're already resolving this variable, return the substitution as-is
        if (!resolving.add(variable)) return sub
        try {
            // The substitution itself might contain variables that need replacing
            return replace(sub)
        } finally {
            resolving.remove(variable)
        }
    }
}