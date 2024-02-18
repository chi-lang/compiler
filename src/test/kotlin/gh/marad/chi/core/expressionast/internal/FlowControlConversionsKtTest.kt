package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types3.Type3
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FlowControlConversionsKtTest {
    @Test
    fun `should eliminate group expression`() {
        convertAst(ParseGroup(LongValue(10, sectionB), sectionA))
            .shouldBeTypeOf<Atom>()
            .should {
                it.shouldBeAtom("10", Type3.int)
                it.sourceSection shouldBe sectionB
            }
    }

    @Test
    fun `generate simple if expression without else branch`() {
        val result = convertAst(
            ParseIfElse(
                condition = BoolValue(true),
                thenBody = LongValue(1),
                elseBody = null,
                section = testSection
            )
        ).shouldBeTypeOf<IfElse>()

        result.condition.shouldBeAtom("true", Type3.bool)
        result.thenBranch.shouldBeAtom("1", Type3.int)
        result.elseBranch.shouldBeNull()
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `generate else branch`() {
        val result = convertAst(
            ParseIfElse(
                condition = BoolValue(true),
                thenBody = LongValue(1),
                elseBody = LongValue(2),
                section = testSection
            )
        ).shouldBeTypeOf<IfElse>()

        result.elseBranch.shouldNotBeNull()
            .shouldBeAtom("2", Type3.int)
    }

    @Test
    fun `generate if-else series from when syntax`() {
        // when
        val result = convertAst(
            ParseWhen(
                cases = listOf(
                    ParseWhenCase(condition = BoolValue(true), body = LongValue(1), sectionA),
                    ParseWhenCase(condition = BoolValue(false), body = LongValue(2), sectionB)
                ),
                elseCase = ParseElseCase(LongValue(0), sectionC),
                testSection
            )
        ).shouldBeTypeOf<IfElse>()

        // then
        result.condition.shouldBeAtom("true", Type3.bool)
        result.thenBranch.shouldBeAtom("1", Type3.int)
        result.sourceSection shouldBe sectionA
        result.elseBranch.shouldBeTypeOf<IfElse>().should {
            it.condition.shouldBeAtom("false", Type3.bool)
            it.thenBranch.shouldBeAtom("2", Type3.int)
            it.sourceSection shouldBe sectionB
            it.elseBranch.shouldNotBeNull().shouldBeAtom("0", Type3.int)
        }
    }

    @Test
    fun `else case is optional in when`() {
        // given
        val result = convertAst(
            ParseWhen(
                cases = listOf(
                    ParseWhenCase(condition = BoolValue(true), body = LongValue(1), sectionA),
                    ParseWhenCase(condition = BoolValue(false), body = LongValue(2), sectionB)
                ),
                elseCase = null,
                testSection
            )
        ).shouldBeTypeOf<IfElse>()

        // then
        result.elseBranch.shouldBeTypeOf<IfElse>()
            .elseBranch.shouldBeNull()
    }

    @Test
    fun `should convert empty when`() {
        // when
        val result = convertAst(
            ParseWhen(cases = emptyList(), elseCase = null, testSection)
        )

        // then
        result shouldBe Atom.unit(testSection)
    }

    @Test
    fun `should convert when with single else case`() {
        // when
        val result = convertAst(
            ParseWhen(
                cases = emptyList(),
                elseCase = ParseElseCase(
                    body = BoolValue(true, sectionB),
                    sectionA
                ),
                testSection
            )
        )

        // then
        result shouldBe Atom.bool(true, sectionB)
    }

    @Test
    fun `generate while`() {
        // when
        val result = convertAst(ParseWhile(condition = BoolValue(true), body = LongValue(1), testSection))
            .shouldBeTypeOf<WhileLoop>()

        // then
        result.condition.shouldBeAtom("true", Type3.bool)
        result.loop.shouldBeAtom("1", Type3.int)
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `generate break`() {
        convertAst(ParseBreak(testSection))
            .shouldBeTypeOf<Break>()
            .sourceSection shouldBe testSection
    }

    @Test
    fun `generate continue`() {
        convertAst(ParseContinue(testSection))
            .shouldBeTypeOf<Continue>()
            .sourceSection shouldBe testSection
    }
}