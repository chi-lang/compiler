package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.OldType
import gh.marad.chi.core.parser.readers.LongValue
import gh.marad.chi.core.parser.readers.ParseFnCall
import gh.marad.chi.core.parser.readers.ParseLambda
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types.Types
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class FunctionConversionsKtFnCallTest {
    @Test
    fun `should generate function call`() {
        // given
        val fnCall = ParseFnCall(
            name = "funcName",
            function = sampleLambda,
            concreteTypeParameters = listOf(intTypeRef),
            arguments = listOf(LongValue(10)),
            testSection
        )

        // when
        val call = convertFnCall(defaultContext(), fnCall)

        // then
        call.parameters.first().shouldBeAtom("10", Types.int)
        call.callTypeParameters.first() shouldBe Types.int
    }

    private val sampleLambda = ParseLambda(
        formalArguments = listOf(intArg("a")),
        body = emptyList(),
        sectionA
    )
}