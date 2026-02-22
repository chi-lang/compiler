package gh.marad.chi.core.types

import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.junit.jupiter.api.Test

class ConstraintDebugTest {
    @Test
    fun `constraint should not capture stack trace by default`() {
        // given
        val constraint = Constraint(Type.int, Type.bool, null, emptyList())

        // then - toString() includes "(from)" field; when debug is off, from is null
        constraint.toString() shouldContain "(null)"
    }
}
