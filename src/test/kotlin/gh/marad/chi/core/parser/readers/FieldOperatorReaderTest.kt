package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.shouldBeLongValue
import gh.marad.chi.core.parser.shouldBeVariable
import gh.marad.chi.core.parser.testParse
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class FieldOperatorReaderTest {
    @Test
    fun `read simple field access`() {
        val ast = testParse("object.field")
        ast[0].shouldBeTypeOf<ParseFieldAccess>() should {
            it.receiver.shouldBeVariable("object")
            it.memberName shouldBe "field"
        }
    }

    @Test
    fun `read field assignment`() {
        val ast = testParse("object.field = 10")
        ast[0].shouldBeTypeOf<ParseFieldAssignment>() should {
            it.receiver.shouldBeVariable("object")
            it.memberName shouldBe "field"
            it.value.shouldBeLongValue(10)
        }
    }

    @Test
    fun `read nested field assignment`() {
        val ast = testParse("foo.bar.baz.i = 42")
        ast[0].shouldBeTypeOf<ParseFieldAssignment>() should {
            it.memberName shouldBe "i"
            it.value.shouldBeLongValue(42)

            it.receiver.shouldBeTypeOf<ParseFieldAccess>() should {
                it.memberName shouldBe "baz"
                it.receiver.shouldBeTypeOf<ParseFieldAccess>() should {
                    it.memberName shouldBe "bar"
                    it.receiver.shouldBeVariable("foo")
                }
            }
        }
    }
}