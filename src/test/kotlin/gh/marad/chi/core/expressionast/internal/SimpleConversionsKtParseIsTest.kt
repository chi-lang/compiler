package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Is
import gh.marad.chi.core.parser.readers.LongValue
import gh.marad.chi.core.parser.readers.ParseIs
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types3.Type3
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class SimpleConversionsKtParseIsTest {
    @Test
    fun `convert simple 'is'`() {
        // when
        val result = convertAst(
            ParseIs(
                value = LongValue(10),
                typeName = "string",
                section = testSection
            )
        ).shouldBeTypeOf<Is>()

        // then
        result.value.shouldBeAtom("10", Type3.int)
        result.typeOrVariant shouldBe "string"
    }
}