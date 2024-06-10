package gh.marad.chi.core.types

interface TypeVisitor<T> {
    fun visitPrimitive(primitive: Primitive): T
    fun visitFunction(function: Function): T
    fun visitRecord(record: Record): T
    fun visitSum(sum: Sum): T
    fun visitVariable(variable: Variable): T
    fun visitArray(array: Array): T
    fun visitRecursive(recursive: Recursive): T
}

abstract class VariableMapper : TypeVisitor<Type> {
    override fun visitPrimitive(primitive: Primitive): Type = primitive

    override fun visitFunction(function: Function): Type =
        Function(function.types.map { it.accept(this) })

    override fun visitRecord(record: Record): Type =
        record.copy(fields = record.fields.map {
            it.copy(type = it.type.accept(this))
        })

    override fun visitSum(sum: Sum): Type =
        Sum.create(
            ids = sum.ids,
            lhs = sum.lhs.accept(this),
            rhs = sum.rhs.accept(this)
        )

    override fun visitArray(array: Array): Type =
        array.copy(elementType = array.elementType.accept(this))

    override fun visitRecursive(recursive: Recursive): Type =
        recursive.copy(type = recursive.type.accept(this))
}

class FreshenAboveVisitor(
    val startingLevel: Int,
    val targetLevel: Int,
    val freshVar: (Int) -> Variable
) : VariableMapper() {
    private val cache = mutableMapOf<Variable, Variable>()
    override fun visitVariable(variable: Variable): Type =
         if (variable.level <= startingLevel) {
            variable // do not refresh vars from environment
        } else {
            cache.getOrPut(variable) { freshVar(targetLevel) }
        }

}
