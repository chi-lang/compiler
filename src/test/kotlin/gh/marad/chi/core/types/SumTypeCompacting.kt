package gh.marad.chi.core.types

import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SumTypeCompacting {
    @Test
    fun `should remove redundant types in simple sum type`() {
        Sum.create(int, int) shouldBe int
        Sum.create(int, Sum.create(int, int)) shouldBe int
    }

    @Test
    fun `should remove redundant types in complex sum type`() {
        Sum.create(int, Sum.create(string, int)) shouldBe Sum.create(int, string)
    }
}