package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.testParse
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class ForReaderTest {

    @Test
    fun `parse for loop`() {
        val code = "for a in [1, 2] { }"
        val ast = testParse(code)

        ast shouldHaveSize 1
        val loop = ast[0].shouldBeTypeOf<ParseFor>()
        loop.vars shouldContainExactly  listOf("a")
        loop.iterable.shouldBeTypeOf<ParseCreateArray>()
    }

    @Test
    fun `parse for key-value loop`() {
        val code = "for k, v in [1, 2] { }"
        val ast = testParse(code)

        ast shouldHaveSize 1
        val loop = ast[0].shouldBeTypeOf<ParseFor>()
        loop.vars shouldContainExactly  listOf("k", "v")
        loop.iterable.shouldBeTypeOf<ParseCreateArray>()
    }
}