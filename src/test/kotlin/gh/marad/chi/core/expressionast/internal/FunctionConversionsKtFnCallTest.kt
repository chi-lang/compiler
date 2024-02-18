package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.FnCall
import gh.marad.chi.core.parser.readers.LongValue
import gh.marad.chi.core.parser.readers.ParseFnCall
import gh.marad.chi.core.parser.readers.ParseLambda
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types3.Type3
import io.kotest.matchers.types.shouldBeTypeOf
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
        val call = convertAst(fnCall).shouldBeTypeOf<FnCall>()

        // then
        call.parameters.first().shouldBeAtom("10", Type3.int)
    }

    private val sampleLambda = ParseLambda(
        formalArguments = listOf(intArg("a")),
        body = emptyList(),
        sectionA
    )
}