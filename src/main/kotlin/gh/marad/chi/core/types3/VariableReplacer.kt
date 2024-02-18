package gh.marad.chi.core.types3

class VariableReplacer(private val toReplace: Variable, private val substitution: Type3) : TypeVisitor<Type3> {
    fun replace(type: Type3): Type3 {
        return type.accept(this)
    }

    override fun visitPrimitive(primitive: Primitive): Type3 = primitive

    override fun visitFunction(function: Function): Type3 =
        function.copy(types = function.types.map { replace(it) })

    override fun visitRecord(record: Record): Type3 =
        record.copy(fields = record.fields.map { it.copy(type = replace(it.type)) })

    override fun visitSum(sum: Sum): Type3 =
        Sum.create(sum.id, replace(sum.lhs), replace(sum.rhs))

    override fun visitArray(array: Array): Type3 =
        array.copy(elementType = replace(array.elementType))

    override fun visitVariable(variable: Variable): Type3 =
        if (variable == toReplace) {
            substitution
        } else {
            variable
        }
}