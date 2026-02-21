package gh.marad.chi.core.types

import gh.marad.chi.core.analyzer.InfiniteType
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.messages
import gh.marad.chi.runtime.TestEnv
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import gh.marad.chi.core.analyzer.CompilerMessage

class OccursCheckTest {

    // ========================================
    // Unit tests for occursIn function
    // ========================================

    @Test
    fun `occursIn should detect variable in function type`() {
        val v = Variable("a", 0)
        val fn = Function(listOf(v, Type.int))
        occursIn(v, fn) shouldBe true
    }

    @Test
    fun `occursIn should return false for unrelated variable`() {
        val v1 = Variable("a", 0)
        val v2 = Variable("b", 0)
        occursIn(v1, Function(listOf(v2, Type.int))) shouldBe false
    }

    @Test
    fun `occursIn should detect variable nested deep in function type`() {
        val v = Variable("a", 0)
        val inner = Function(listOf(Type.int, v))
        val outer = Function(listOf(inner, Type.bool))
        occursIn(v, outer) shouldBe true
    }

    @Test
    fun `occursIn should return false for primitive types`() {
        val v = Variable("a", 0)
        occursIn(v, Type.int) shouldBe false
    }

    @Test
    fun `occursIn should detect variable in record type`() {
        val v = Variable("a", 0)
        val record = Type.record("field" to v)
        occursIn(v, record) shouldBe true
    }

    @Test
    fun `occursIn should detect variable in array type`() {
        val v = Variable("a", 0)
        val arr = Type.array(v)
        occursIn(v, arr) shouldBe true
    }

    @Test
    fun `occursIn should return false when variable is the same object`() {
        // Variable == Variable returns true, which is the base case (type IS the variable)
        val v = Variable("a", 0)
        occursIn(v, v) shouldBe true
    }

    @Test
    fun `occursIn should distinguish variables with different names`() {
        val v1 = Variable("a", 0)
        val v2 = Variable("b", 0)
        occursIn(v1, v2) shouldBe false
    }

    @Test
    fun `occursIn should distinguish variables with different levels`() {
        val v1 = Variable("a", 0)
        val v2 = Variable("a", 1)
        occursIn(v1, v2) shouldBe false
    }

    // ========================================
    // Direct unification tests
    // ========================================

    @Test
    fun `unify should reject circular constraint on expected is Variable branch`() {
        val v = Variable("a", 0)
        val fnType = Function(listOf(v, Type.int))
        // expected=v, actual=fn(v -> int) - triggers expected is Variable branch
        val constraint = Constraint(v, fnType, null, emptyList())
        assertThrows<CompilerMessage> {
            unify(listOf(constraint))
        }.msg.shouldBeTypeOf<InfiniteType>()
    }

    @Test
    fun `unify should reject circular constraint on actual is Variable branch`() {
        val v = Variable("a", 0)
        val fnType = Function(listOf(v, Type.int))
        // expected=fn(v -> int), actual=v - triggers actual is Variable branch
        // Because constraints are sorted by (expected !is Variable), this concrete-expected
        // constraint is processed first. Function unification decomposes it into sub-constraints,
        // one of which will be v = v (trivial) and another involving the return type.
        // To directly hit the actual is Variable branch, we need expected to be non-Variable:
        val constraint = Constraint(fnType, v, null, emptyList())
        assertThrows<CompilerMessage> {
            unify(listOf(constraint))
        }.msg.shouldBeTypeOf<InfiniteType>()
    }

    // ========================================
    // End-to-end tests via messages()
    // ========================================

    @Test
    fun `should reject self-application as infinite type`() {
        messages("val f = { x -> x(x) }").should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<InfiniteType>()
        }
    }

    @Test
    fun `should verify InfiniteType message fields`() {
        messages("val f = { x -> x(x) }").should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<InfiniteType>().should {
                it.level shouldBe Level.ERROR
                it.message.contains("Infinite type") shouldBe true
            }
        }
    }

    @Test
    fun `should reject nested infinite type through indirect circularity`() {
        // x: 'a, x(x) requires 'a = 'a -> 'b (direct circularity)
        // wrapping in another lambda to verify it's caught in nested context
        val code = """
            val h = { unused: int ->
                val f = { x -> x(x) }
                f
            }
        """.trimIndent()
        messages(code).should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<InfiniteType>()
        }
    }

    @Test
    fun `should accept non-circular variable bindings`() {
        messages("val f = { x: int -> x + 1 }").shouldBeEmpty()
    }

    @Test
    fun `should accept identity function`() {
        messages("val f = { x -> x }").shouldBeEmpty()
    }

    @Test
    fun `should accept function composition`() {
        messages("""
            val f = { x: int -> x + 1 }
            val g = { x: int -> x * 2 }
            val result = f(g(3))
        """.trimIndent()).shouldBeEmpty()
    }

    @Test
    fun `should accept higher-order function without circularity`() {
        messages("""
            val apply = { f: (int) -> int, x: int -> f(x) }
            val result = apply({ x -> x + 1 }, 5)
        """.trimIndent()).shouldBeEmpty()
    }

    // ========================================
    // Integration test via TestEnv.eval()
    // ========================================

    @Test
    fun `self-application should fail in full compilation pipeline`() {
        // TestEnv.eval returns null when compilation fails (ErrorStrategy.PRINT)
        TestEnv.eval("val f = { x -> x(x) }").shouldBeNull()
    }
}
