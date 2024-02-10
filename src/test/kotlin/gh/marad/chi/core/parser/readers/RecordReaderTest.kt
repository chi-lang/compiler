package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.shouldBeLongValue
import gh.marad.chi.core.parser.testParse
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class RecordReaderTest {
    @Test
    fun `should read create record`() {
        val code = "{ x: 5, y: 8 }"
        val result = testParse(code)[0].shouldBeTypeOf<ParseCreateRecord>()

        result.fields[0].name shouldBe "x"
        result.fields[0].value.shouldBeLongValue(5)
        result.fields[1].name shouldBe "y"
        result.fields[1].value.shouldBeLongValue(8)
    }

}