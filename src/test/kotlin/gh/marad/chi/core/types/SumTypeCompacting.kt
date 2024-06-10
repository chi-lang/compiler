package gh.marad.chi.core.types

import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import gh.marad.chi.core.types.Type.Companion.unit
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

    @Test
    fun `should automativally recognize Option type`() {
        (Sum.create(int, unit) as HasTypeId).getTypeIds() shouldBe listOf(Type.optionTypeId)
        (Sum.create(unit, int) as HasTypeId).getTypeIds() shouldBe listOf(Type.optionTypeId)
        (Sum.create(Sum.create(int, string), unit) as HasTypeId).getTypeIds() shouldBe listOf(Type.optionTypeId)
        (Sum.create(unit, Sum.create(int, string)) as HasTypeId).getTypeIds() shouldBe listOf(Type.optionTypeId)
    }
}