package gh.marad.chi.core.types

private const val debug = true

data class Constraint(val expected: Type, val actual: Type) {
    private val from: StackTraceElement? = run {
        if (debug) {
            val ex = RuntimeException()
            ex.stackTrace.first {
                it.fileName != "Constraint.kt"
            }
        } else {
            null
        }
    }
    override fun toString(): String = "$expected = $actual  ($from)"
}