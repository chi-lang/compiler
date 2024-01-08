package gh.marad.chi.core.types

import gh.marad.chi.core.analyzer.CompilerMessageException
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.parser.ChiSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import kotlin.random.Random

class InferenceKtHindleyMillnerUnificationTest {
    @Test
    fun `should solve simple true constraint`() {
        // given
        val simpleTrueConstraint = Constraint(Types.int, Types.int, null)

        // when
        val solution = unify(setOf(simpleTrueConstraint))

        // then
        solution shouldHaveSize 0
    }

    @Test
    fun `should find simple type mapping`() {
        // given
        val t = TypeVariable("T")
        val u = TypeVariable("U")
        val constraint1 = Constraint(Types.int, t, null)
        val constraint2 = Constraint(u, Types.bool, null)

        // when
        val solution = unify(setOf(constraint1, constraint2))

        // then
        solution shouldContain (t to Types.int)
        solution shouldContain (u to Types.bool)
    }

    @Test
    fun `should fail type inference if type variable is used in the other component of constraint`() {
        // given
        val t = TypeVariable("T")
        val section = randomSourceSection()

        // expect
        shouldThrow<TypeInferenceFailed> {
            unify(setOf(Constraint(t, FunctionType(listOf(Types.int, t)), section)))
        }.section shouldBe section

        // and
        shouldThrow<TypeInferenceFailed> {
            unify(setOf(Constraint(FunctionType(listOf(Types.int, t)), t, section)))
        }.section shouldBe section
    }

    @Test
    fun `should fail type inference if compared types don't match`() {
        // given
        val a = Types.int
        val b = Types.bool
        val section = randomSourceSection()

        // expect
        shouldThrow<CompilerMessageException> {
            unify(setOf(Constraint(a, b, section)))
        }.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.bool
            it.actual shouldBe Types.int
        }
    }

    @Test
    fun `should fail type inference if compared types are of different kinds`() {
        // given
        val a = Types.int
        val b = Types.fn(Types.int, Types.bool)
        val section = randomSourceSection()

        // expect
        shouldThrow<CompilerMessageException> {
            unify(setOf(Constraint(a, b, section)))
        }.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe b
            it.actual shouldBe a
        }
    }

    @Test
    fun `should decompose function types`() {
        // given
        val t = TypeVariable("T")
        val a = Types.fn(Types.int, t)
        val b = Types.fn(Types.int, Types.bool)

        // when
        val solution = unify(setOf(Constraint(a, b, null)))

        // then
        solution shouldContain (t to Types.bool)
    }

    @Test
    fun `should report proper param source section when function inference fails`() {
        // given
        val a = Types.fn(Types.float, Types.int, Types.bool)
        val b = Types.fn(Types.float, Types.string, Types.bool)
        val floatParamSection = randomSourceSection()
        val intParamSection = randomSourceSection()
        val boolParamSection = randomSourceSection()
        val fnParamSections = listOf(floatParamSection, intParamSection, boolParamSection)

        // when
        val ex = shouldThrow<CompilerMessageException> {
            unify(setOf(Constraint(a, b, randomSourceSection(), fnParamSections)))
        }

        // then
        ex.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.string
            it.actual shouldBe Types.int
        }
    }

    @Test
    fun `should decompose generic types`() {
        // given
        val t = TypeVariable("a")
        val a = Types.array(Types.int)
        val b = Types.array(t)

        // when
        val solution = unify(setOf(Constraint(a, b, null)))

        // then
        solution shouldContain (t to Types.int)
    }

    @Test
    fun `should fail inference if generic types don't match`() {
        // give
        val a = ProductType("std", "lang", "Map", typeParams = listOf(Types.string, Types.int), types = emptyList())
        val b = ProductType("std", "lang", "Map", typeParams = listOf(Types.bool, Types.int), types = emptyList())
        val mapSection = randomSourceSection()
        val boolSection = randomSourceSection()
        val intSection = randomSourceSection()
        val paramSections = listOf(mapSection, boolSection, intSection)
        val section = randomSourceSection()

        // when
        val ex = shouldThrow<CompilerMessageException> {
            unify(setOf(Constraint(a, b, section, paramSections)))
        }

        // and
        ex.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.bool
            it.actual shouldBe Types.string
        }
    }

    private fun randomSourceSection(): ChiSource.Section {
        val codePoints = (0..100).map {
            Random.nextInt('a'.code, 'z'.code)
        }.toIntArray()
        val source = String(codePoints, 0, codePoints.size)
        return ChiSource.Section(ChiSource(source), 10, 20)
    }
}