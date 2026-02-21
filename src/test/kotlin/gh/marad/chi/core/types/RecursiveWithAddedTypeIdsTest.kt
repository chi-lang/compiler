package gh.marad.chi.core.types

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class RecursiveWithAddedTypeIdsTest {
    private val testTypeId1 = TypeId("test", "pkg", "TestType1")
    private val testTypeId2 = TypeId("test", "pkg", "TestType2")
    private val variable = Variable("a", 0)

    @Test
    fun `withAddedTypeIds should preserve Recursive wrapper when inner type is HasTypeId`() {
        // given: a Recursive wrapping a Record (which implements HasTypeId)
        val innerRecord = Record(listOf(testTypeId1), emptyList())
        val recursive = Recursive(variable, innerRecord)

        // when
        val result = recursive.withAddedTypeIds(listOf(testTypeId2))

        // then
        result.shouldBeInstanceOf<Recursive>()
        val resultRecursive = result as Recursive
        resultRecursive.variable shouldBe variable
        val resultInner = resultRecursive.type.shouldBeInstanceOf<Record>()
        resultInner.ids shouldBe listOf(testTypeId1, testTypeId2)
    }

    @Test
    fun `withAddedTypeIds should return this when inner type is not HasTypeId`() {
        // given: a Recursive wrapping a Function (which does NOT implement HasTypeId)
        val innerFunction = Function(listOf(Type.int, Type.string))
        val recursive = Recursive(variable, innerFunction)

        // when
        val result = recursive.withAddedTypeIds(listOf(testTypeId1))

        // then
        result.shouldBeInstanceOf<Recursive>()
        result shouldBe recursive
    }

    @Test
    fun `withAddedTypeIds and withAddedTypeId should both preserve Recursive wrapper`() {
        // given
        val innerRecord = Record(listOf(testTypeId1), emptyList())
        val recursive = Recursive(variable, innerRecord)

        // when
        val resultSingular = recursive.withAddedTypeId(testTypeId2)
        val resultPlural = recursive.withAddedTypeIds(listOf(testTypeId2))

        // then: both should be Recursive instances
        resultSingular.shouldBeInstanceOf<Recursive>()
        resultPlural.shouldBeInstanceOf<Recursive>()

        // and: both should have the same inner type IDs
        val singularInner = (resultSingular as Recursive).type.shouldBeInstanceOf<Record>()
        val pluralInner = (resultPlural as Recursive).type.shouldBeInstanceOf<Record>()
        singularInner.ids shouldBe listOf(testTypeId1, testTypeId2)
        pluralInner.ids shouldBe listOf(testTypeId1, testTypeId2)
    }
}
