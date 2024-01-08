package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.Types
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class SimpleConversionsKtTest {
    @Test
    fun `generating simple atoms`() {
        convertAst(LongValue(10, testSection)).shouldBeAtom("10", Types.int, testSection)
        convertAst(FloatValue(0.5f, testSection)).shouldBeAtom("0.5", Types.float, testSection)
        convertAst(BoolValue(true, testSection)).shouldBeAtom("true", Types.bool, testSection)
        convertAst(BoolValue(false, testSection)).shouldBeAtom("false", Types.bool, testSection)
        convertAst(StringValue("test", testSection)).shouldBeAtom("test", Types.string, testSection)
    }

    @Test
    fun `generating interpolated string`() {
        // when
        val result = convertAst(
            ParseInterpolatedString(
                section = testSection,
                parts = listOf(
                    StringText("test", sectionA),
                    ParseInterpolation(LongValue(10), sectionB)
                )
            )
        )

        // then
        result.shouldBeTypeOf<InterpolatedString>() should {
            it.parts shouldHaveSize 2
            it.parts[0].shouldBeAtom("test", Types.string)
            it.parts[1].shouldBeTypeOf<Cast>().should {
                it.expression.shouldBeAtom("10", Types.int)
                it.targetType shouldBe Types.string
            }
        }
    }

    @Test
    fun `string text in interpolation should be converted to simple string atom`() {
        convertAst(StringText("test", testSection))
            .shouldBeAtom("test", Types.string, testSection)
    }

    @Test
    fun `code interpolations should be converted and cast to string`() {
        convertAst(
            ParseInterpolation(LongValue(10), testSection)
        ).shouldBeTypeOf<Cast>() should {
            it.targetType shouldBe Types.string
            it.expression.shouldBeAtom("10", Types.int)
        }
    }

    @Test
    fun `block conversion`() {
        convertAst(ParseBlock(listOf(LongValue(10)), testSection))
            .shouldBeTypeOf<Block>().should {
                it.body shouldHaveSize 1
                it.body[0].shouldBeAtom("10", Types.int)
                it.sourceSection shouldBe testSection
            }
    }

    @Test
    fun `convert binary operator`() {
        // when
        val result = convertAst(
            ParseBinaryOp(
                op = "generic operation",
                left = StringValue("hello"),
                right = LongValue(20),
                section = testSection
            )
        ).shouldBeTypeOf<InfixOp>()

        // then
        result.op shouldBe "generic operation"
        result.left.shouldBeAtom("hello", Types.string)
        result.right.shouldBeAtom("20", Types.int)
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `convert cast`() {
        // when
        val result = convertAst(
            ParseCast(
                value = LongValue(10),
                typeRef = TypeNameRef("string", sectionA),
                section = sectionB
            )
        ).shouldBeTypeOf<Cast>()

        // then
        result.expression.shouldBeAtom("10", Types.int)
        result.targetType shouldBe Types.string
        result.sourceSection shouldBe sectionB
    }

    @Test
    fun `convert not`() {
        convertAst(ParseNot(BoolValue(true), testSection))
            .shouldBeTypeOf<PrefixOp>()
            .should {
                it.op shouldBe "!"
                it.expr.shouldBeAtom("true", Types.bool)
                it.sourceSection shouldBe testSection
            }
    }
}