package gh.marad.chi.core.types3

interface TypeVisitor<T> {
    fun visitPrimitive(primitive: Primitive): T
    fun visitFunction(function: Function): T
    fun visitRecord(record: Record): T
    fun visitSum(sum: Sum): T
    fun visitVariable(variable: Variable): T
    fun visitArray(array: Array): T
}

abstract class VariableMapper : TypeVisitor<Type3> {
    override fun visitPrimitive(primitive: Primitive): Type3 = primitive

    override fun visitFunction(function: Function): Type3 =
        Function(function.types.map { it.accept(this) })

    override fun visitRecord(record: Record): Type3 =
        record.copy(fields = record.fields.map {
            it.copy(type = it.type.accept(this))
        })

    override fun visitSum(sum: Sum): Type3 =
        Sum.create(
            lhs = sum.lhs.accept(this),
            rhs = sum.rhs.accept(this)
        )

    override fun visitArray(array: Array): Type3 =
        array.copy(elementType = array.elementType.accept(this))
}

class FreshenAboveVisitor(
    val startingLevel: Int,
    val targetLevel: Int,
    val freshVar: (Int) -> Variable
) : VariableMapper() {
    private val cache = mutableMapOf<Variable, Variable>()
    override fun visitVariable(variable: Variable): Type3 =
         if (variable.level <= startingLevel) {
            variable // do not refresh vars from environment
        } else {
            cache.getOrPut(variable) { freshVar(targetLevel) }
        }

}
