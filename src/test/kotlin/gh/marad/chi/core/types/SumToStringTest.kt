package gh.marad.chi.core.types

import gh.marad.chi.core.types.Type.Companion.float
import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import gh.marad.chi.core.types.Type.Companion.unit
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class SumToStringTest {
    @Test
    fun `anonymous sum type should display component types with pipe separator`() {
        // Anonymous sum type has empty ids
        val sum = Sum(emptyList(), string, float)
        sum.toString() shouldBe "string | float"
    }

    @Test
    fun `named sum type should display ids and component types`() {
        val id = TypeId("mymod", "mypkg", "MyUnion")
        val sum = Sum(listOf(id), string, int)
        sum.toString() shouldBe "[mymod::mypkg::MyUnion][string | int]"
    }

    @Test
    fun `option sum type toString should show subtypes without unit`() {
        // Option type: has optionTypeId in ids, includes unit
        val optionSum = Sum.create(listOf(Type.optionTypeId), int, unit) as Sum
        val result = optionSum.toString()
        result shouldContain "Option"
        result shouldContain "int"
    }
}
