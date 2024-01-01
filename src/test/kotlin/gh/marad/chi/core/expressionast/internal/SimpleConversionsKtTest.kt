package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.parser.readers.*
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class SimpleConversionsKtTest {
    @Test
    fun `generating simple atoms`() {
        convertAtom(LongValue(10, testSection)).shouldBeAtom("10", OldType.intType, testSection)
        convertAtom(FloatValue(0.5f, testSection)).shouldBeAtom("0.5", OldType.floatType, testSection)
        convertAtom(BoolValue(true, testSection)).shouldBeAtom("true", OldType.bool, testSection)
        convertAtom(BoolValue(false, testSection)).shouldBeAtom("false", OldType.bool, testSection)
        convertAtom(StringValue("test", testSection)).shouldBeAtom("test", OldType.string, testSection)
    }

    @Test
    fun `generating interpolated string`() {
        // when
        val result = convertInterpolatedString(
            defaultContext(),
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
            it.parts[0].shouldBeAtom("test", OldType.string)
            it.parts[1].shouldBeTypeOf<Cast>().should {
                it.expression.shouldBeAtom("10", OldType.intType)
                it.targetType shouldBe OldType.string
            }
        }
    }

    @Test
    fun `string text in interpolation should be converted to simple string atom`() {
        convertStringText(StringText("test", testSection))
            .shouldBeAtom("test", OldType.string, testSection)
    }

    @Test
    fun `code interpolations should be converted and cast to string`() {
        convertInterpolation(defaultContext(), ParseInterpolation(LongValue(10), testSection))
            .shouldBeTypeOf<Cast>() should {
            it.targetType shouldBe OldType.string
            it.expression.shouldBeAtom("10", OldType.intType)
        }
    }

    @Test
    fun `converting null package definition should produce null value`() {
        convertPackageDefinition(null).shouldBeNull()
    }

    @Test
    fun `package definition conversion`() {
        val result = convertPackageDefinition(
            ParsePackageDefinition(
                ModuleName("my.mod", sectionA),
                PackageName("my.pkg", sectionB),
                sectionC
            )
        )

        result.shouldNotBeNull().shouldBeTypeOf<Package>() should {
            it.moduleName shouldBe "my.mod"
            it.packageName shouldBe "my.pkg"
            it.sourceSection shouldBe sectionC
        }
    }

    @Test
    fun `block conversion`() {
        convertBlock(defaultContext(), ParseBlock(listOf(LongValue(10)), testSection)) should {
            it.body shouldHaveSize 1
            it.body[0].shouldBeAtom("10", OldType.intType)
            it.sourceSection shouldBe testSection
        }
    }

    @Test
    fun `convert binary operator`() {
        // when
        val result = convertBinaryOp(
            defaultContext(),
            ParseBinaryOp(
                op = "generic operation",
                left = StringValue("hello"),
                right = LongValue(20),
                section = testSection
            )
        )

        // then
        result.op shouldBe "generic operation"
        result.left.shouldBeAtom("hello", OldType.string)
        result.right.shouldBeAtom("20", OldType.intType)
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `convert cast`() {
        // when
        val result = convertCast(
            defaultContext(),
            ParseCast(
                value = LongValue(10),
                typeRef = TypeNameRef("string", sectionA),
                section = sectionB
            )
        )

        // then
        result.expression.shouldBeAtom("10", OldType.intType)
        result.targetType shouldBe OldType.string
        result.sourceSection shouldBe sectionB
    }

    @Test
    fun `convert not`() {
        convertNot(defaultContext(), ParseNot(BoolValue(true), testSection)).should {
            it.op shouldBe "!"
            it.expr.shouldBeAtom("true", OldType.bool)
            it.sourceSection shouldBe testSection
        }
    }
}