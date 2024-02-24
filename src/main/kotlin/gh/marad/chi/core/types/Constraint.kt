package gh.marad.chi.core.types

import gh.marad.chi.core.parser.ChiSource

private const val debug = true

data class Constraint(val expected: Type, val actual: Type, val section: ChiSource.Section?) {
    private var from: StackTraceElement? = run {
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

    fun withReplacedVariable(replacer: VariableReplacer): Constraint {
        return copy(
            expected = replacer.replace(expected),
            actual = replacer.replace(actual)
        ).also {
            it.from = from
        }
    }

}