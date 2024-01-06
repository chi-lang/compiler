package gh.marad.chi.core.types

import gh.marad.chi.core.parser.ChiSource

data class Constraint(
    var actual: Type,
    var expected: Type,
    val section: ChiSource.Section?,
    /// This parameter has very specific use case. It's used
    /// for FnCall type inference to convey the parameter
    /// sections, to produce meaningful errors.
    /// It's also used for GenericType inference for
    /// generic type parameters
    val paramSections: List<ChiSource.Section?>? = null
) {
    fun substitute(v: TypeVariable, t: Type) {
        actual = actual.substitute(v,t)
        expected = expected.substitute(v,t)
    }
    override fun toString(): String = "$actual = $expected"
}