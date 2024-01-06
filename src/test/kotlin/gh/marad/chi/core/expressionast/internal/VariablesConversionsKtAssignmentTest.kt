package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types.Types
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class VariablesConversionsKtAssignmentTest {
    @Test
    fun `generate assignment`() {
        val ctx = defaultContext()
        val result = convertAssignment(
            ctx, ParseAssignment(
                variableName = "variable",
                value = LongValue(10),
                section = testSection
            )
        )

        result.name shouldBe "variable"
        result.value.shouldBeAtom("10", Types.int)
        result.symbol shouldBe true
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `generate indexed assignment`() {
        val ctx = defaultContext()
        val result = convertIndexedAssignment(
            ctx, ParseIndexedAssignment(
                variable = ParseVariableRead("variable"),
                index = LongValue(10),
                value = StringValue("hello"),
                section = testSection
            )
        )

        result.variable.shouldBeVariable("variable")
        result.index.shouldBeAtom("10", Types.int)
        result.value.shouldBeAtom("hello", Types.string)
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `generate field assignment`() {
        val ctx = defaultContext()
        val result = convertFieldAssignment(
            ctx,
            ParseFieldAssignment(
                receiverName = "object",
                receiver = ParseVariableRead("object"),
                memberName = "field",
                value = LongValue(10),
                section = testSection,
                memberSection = null
            )
        )

        result.receiver.shouldBeVariable("object")
        result.fieldName shouldBe "field"
    }
}