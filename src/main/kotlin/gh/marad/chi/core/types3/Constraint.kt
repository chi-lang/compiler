package gh.marad.chi.core.types3

data class Constraint(val expected: Type3, val actual: Type3) {
    override fun toString(): String = "$expected = $actual"
}