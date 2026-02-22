package gh.marad.chi.core.types

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class VariableMapperSumTypeParamsTest {

    /** A trivial VariableMapper that replaces one variable with a given type. */
    private class TestMapper(val from: Variable, val to: Type) : VariableMapper() {
        override fun visitVariable(variable: Variable): Type =
            if (variable == from) to else variable
    }

    @Test
    fun `visitSum should preserve typeParams after variable mapping`() {
        val varT = Variable("T", 1)
        val sum = Sum(
            ids = listOf(Type.optionTypeId),
            lhs = varT,
            rhs = Type.unit,
            typeParams = listOf("T")
        )

        val mapper = TestMapper(from = varT, to = Type.int)
        val result = sum.accept(mapper)

        // The result should be a Sum with typeParams preserved
        result as Sum
        result.typeParams shouldBe listOf("T")
        // lhs should be mapped from varT to int
        result.lhs shouldBe Type.int
        result.rhs shouldBe Type.unit
    }

    @Test
    fun `FreshenAboveVisitor should preserve typeParams on Sum types during instantiation`() {
        val varT = Variable("T", 2)
        val sum = Sum(
            ids = listOf(Type.optionTypeId),
            lhs = varT,
            rhs = Type.unit,
            typeParams = listOf("T")
        )

        var freshCounter = 0
        val visitor = FreshenAboveVisitor(
            startingLevel = 1,
            targetLevel = 3,
            freshVar = { level -> Variable("fresh${freshCounter++}", level) }
        )

        val result = sum.accept(visitor)

        result as Sum
        result.typeParams shouldBe listOf("T")
    }

    @Test
    fun `visitSum should preserve empty typeParams`() {
        val varA = Variable("a", 1)
        val sum = Sum(
            ids = emptyList(),
            lhs = varA,
            rhs = Type.string,
            typeParams = emptyList()
        )

        val mapper = TestMapper(from = varA, to = Type.int)
        val result = sum.accept(mapper)

        result as Sum
        result.typeParams shouldBe emptyList()
    }
}
