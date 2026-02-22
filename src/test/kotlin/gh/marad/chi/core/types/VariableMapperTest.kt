package gh.marad.chi.core.types

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class VariableMapperTest {
    private var nextVarId = 100

    private fun freshVar(level: Int): Variable = Variable("fresh${nextVarId++}", level)

    @Test
    fun `FreshenAboveVisitor should preserve defaultArgs on Function types`() {
        // given: a Function type with defaultArgs = 2 and a type variable that will be freshened
        val typeVar = Variable("a", 1)
        val funcType = Function(
            types = listOf(typeVar, Type.int, Type.string),
            typeParams = emptyList(),
            defaultArgs = 2
        )
        val polyType = PolyType(level = 0, body = funcType)

        // when: we instantiate (which uses FreshenAboveVisitor -> VariableMapper.visitFunction)
        val result = polyType.instantiate(level = 1, freshVar = ::freshVar)

        // then: defaultArgs should be preserved
        result.shouldBeInstanceOf<Function>()
        (result as Function).defaultArgs shouldBe 2
    }

    @Test
    fun `FreshenAboveVisitor should preserve typeParams on Function types`() {
        // given: a Function type with typeParams and a type variable that will be freshened
        val typeVar = Variable("a", 1)
        val funcType = Function(
            types = listOf(typeVar, Type.int),
            typeParams = listOf("T", "U"),
            defaultArgs = 0
        )
        val polyType = PolyType(level = 0, body = funcType)

        // when: we instantiate
        val result = polyType.instantiate(level = 1, freshVar = ::freshVar)

        // then: typeParams should be preserved
        result.shouldBeInstanceOf<Function>()
        (result as Function).typeParams shouldBe listOf("T", "U")
    }

    @Test
    fun `FreshenAboveVisitor should preserve both defaultArgs and typeParams together`() {
        // given: a Function type with both typeParams and defaultArgs
        val typeVar = Variable("a", 1)
        val funcType = Function(
            types = listOf(typeVar, typeVar, Type.int),
            typeParams = listOf("T"),
            defaultArgs = 1
        )
        val polyType = PolyType(level = 0, body = funcType)

        // when: we instantiate
        val result = polyType.instantiate(level = 1, freshVar = ::freshVar)

        // then: both should be preserved
        result.shouldBeInstanceOf<Function>()
        val resultFunc = result as Function
        resultFunc.defaultArgs shouldBe 1
        resultFunc.typeParams shouldBe listOf("T")
    }

    @Test
    fun `FreshenAboveVisitor should still freshen type variables in Function types`() {
        // given: a Function with a type variable above the starting level
        val typeVar = Variable("a", 1)
        val funcType = Function(
            types = listOf(typeVar, Type.int),
            defaultArgs = 1
        )
        val polyType = PolyType(level = 0, body = funcType)

        // when: we instantiate
        val result = polyType.instantiate(level = 2, freshVar = ::freshVar)

        // then: the type variable should be freshened, but defaultArgs preserved
        result.shouldBeInstanceOf<Function>()
        val resultFunc = result as Function
        resultFunc.defaultArgs shouldBe 1
        // The first type should be a fresh variable, not the original
        resultFunc.types[0].shouldBeInstanceOf<Variable>()
        (resultFunc.types[0] as Variable).name shouldBe "fresh100"
        // The second type (int) should be unchanged
        resultFunc.types[1] shouldBe Type.int
    }

    @Test
    fun `Function with zero defaultArgs and empty typeParams is unaffected by fix`() {
        // given: a Function with defaults (regression check)
        val typeVar = Variable("a", 1)
        val funcType = Function(
            types = listOf(typeVar, Type.int),
            typeParams = emptyList(),
            defaultArgs = 0
        )
        val polyType = PolyType(level = 0, body = funcType)

        // when
        val result = polyType.instantiate(level = 1, freshVar = ::freshVar)

        // then
        result.shouldBeInstanceOf<Function>()
        val resultFunc = result as Function
        resultFunc.defaultArgs shouldBe 0
        resultFunc.typeParams shouldBe emptyList()
    }
}
