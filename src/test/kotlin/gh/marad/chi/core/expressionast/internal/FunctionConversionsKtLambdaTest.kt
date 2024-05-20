package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Fn
import gh.marad.chi.core.parser.readers.FormalArgument
import gh.marad.chi.core.parser.readers.LongValue
import gh.marad.chi.core.parser.readers.ParseLambda
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types.Type
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FunctionConversionsKtLambdaTest {
    @Test
    fun `should generate fn definition form lambda`() {
        // given
        val lambda = ParseLambda(
            formalArguments = emptyList(),
            body = listOf(LongValue(10)),
            testSection
        )

        // when
        val fn = convertAst(lambda).shouldBeTypeOf<Fn>()

        // then
        fn.parameters.shouldBeEmpty()
        fn.body.body[0].shouldBeAtom("10", Type.int)
        fn.sourceSection shouldBe testSection
    }

    @Test
    fun `should transfer function parameters from lambda`() {
        // given
        val lambda = sampleLambda.copy(
            formalArguments = listOf(FormalArgument("name", intTypeRef, section=sectionB))
        )

        // when
        val fn = convertAst(lambda).shouldBeTypeOf<Fn>()

        // then
        fn.parameters should {
            it shouldHaveSize 1
            it[0].name shouldBe "name"
            it[0].type shouldBe Type.int
            it[0].sourceSection shouldBe sectionB
        }
    }

    private val sampleLambda = ParseLambda(
        formalArguments = emptyList(),
        body = emptyList(),
        testSection
    )

}