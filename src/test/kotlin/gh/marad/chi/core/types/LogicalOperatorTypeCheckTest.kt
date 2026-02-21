package gh.marad.chi.core.types

import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class LogicalOperatorTypeCheckTest {
    @Test
    fun `should accept bool operands for && operator`() {
        messages("true && false").shouldBeEmpty()
    }

    @Test
    fun `should accept bool operands for || operator`() {
        messages("true || false").shouldBeEmpty()
    }

    @Test
    fun `should reject non-bool operands for && operator`() {
        messages("5 && 3").should { msgs ->
            msgs.shouldNotBeEmpty()
            msgs.first().shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe Type.bool
                it.actual shouldBe Type.int
            }
        }
    }

    @Test
    fun `should reject non-bool operands for || operator`() {
        messages(""""a" || "b"""").should { msgs ->
            msgs.shouldNotBeEmpty()
            msgs.first().shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe Type.bool
                it.actual shouldBe Type.string
            }
        }
    }

    @Test
    fun `should still allow comparison operators on non-bool types`() {
        messages("3 < 5").shouldBeEmpty()
        messages(""""a" == "b"""").shouldBeEmpty()
    }
}
