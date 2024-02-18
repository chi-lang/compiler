package gh.marad.chi.core.types3

private const val debug = true

data class Constraint(val expected: Type3, val actual: Type3) {
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