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