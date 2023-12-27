package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.shouldBeLongValue
import gh.marad.chi.core.parser.testParse
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class ReturnReaderTest {

    @Test
    fun `parsing basic return expression`() {
        val code = "return 10"
        val ast = testParse(code)

        ast shouldHaveSize 1
        val retExpr = ast[0].shouldBeTypeOf<ParseReturn>()
        retExpr.value?.shouldBeLongValue(10)
    }

    @Test
    fun `parsing return expression without value`() {
        val code = "return"
        val ast = testParse(code)

        ast shouldHaveSize 1
        val retExpr = ast[0].shouldBeTypeOf<ParseReturn>()
        retExpr.value.shouldBeNull()
    }

}