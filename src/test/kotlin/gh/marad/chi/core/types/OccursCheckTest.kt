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
import org.junit.jupiter.api.assertTimeout
import java.time.Duration
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

    // ========================================
    // Sum-type widening tests
    // ========================================

    @Test
    fun `unify should accept variable as direct branch of sum type (widening)`() {
        val v = Variable("T", 0)
        // 'T = Sum('T, unit) — variable is a direct branch, valid widening
        val sumType = Sum(emptyList(), v, Type.unit)
        val constraint = Constraint(v, sumType, null, emptyList())
        // Should NOT throw — this is valid widening
        val solutions = unify(listOf(constraint))
        solutions.any { it.first == v } shouldBe true
    }

    @Test
    fun `asOption function should compile without errors`() {
        messages("""
            type Option[T] = T | unit
            fn asOption[T](value: T): Option[T] { value }
        """.trimIndent()).shouldBeEmpty()
    }

    @Test
    fun `map over Option should compile without errors`() {
        messages("""
            type Option[T] = T | unit
            fn map[T,R](opt: Option[T], f: (T) -> R): Option[R] {
                if opt is unit { unit } else { opt is T
                f(opt) }
            }
        """.trimIndent()).shouldBeEmpty()
    }

    @Test
    fun `ifPresent over Option should compile without errors`() {
        messages("""
            type Option[T] = T | unit
            fn ifPresent[T](opt: Option[T], f: (T) -> unit) {
                if opt is unit { unit } else { opt is T
                f(opt) }
            }
        """.trimIndent()).shouldBeEmpty()
    }

    // ========================================
    // Regression: genuine infinite types still rejected
    // ========================================

    @Test
    fun `self-application should still produce infinite type error after widening fix`() {
        messages("val f = { x -> x(x) }").should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<InfiniteType>()
        }
    }

    @Test
    fun `unify should reject variable nested inside function type within sum`() {
        val v = Variable("T", 0)
        // 'T = Sum(Function('T, int), unit) — variable nested in function, genuine infinite type
        val fnType = Function(listOf(v, Type.int))
        val sumType = Sum(emptyList(), fnType, Type.unit)
        val constraint = Constraint(v, sumType, null, emptyList())
        assertThrows<CompilerMessage> {
            unify(listOf(constraint))
        }.msg.shouldBeTypeOf<InfiniteType>()
    }

    // ========================================
    // Recursive type unification tests
    // ========================================

    @Test
    fun `unify two identical Recursive types should terminate`() {
        assertTimeout(Duration.ofSeconds(5)) {
            // Simulate: type List = { head: int, tail: List | unit }
            // Two instances with the same sentinel variable (same type alias, no freshening)
            val sentinel = Variable("List", -1)
            val body = Record(
                emptyList(),
                listOf(
                    Record.Field("head", Type.int),
                    Record.Field("tail", Sum(emptyList(), sentinel, Type.unit))
                )
            )
            val recType = Recursive(sentinel, body)

            // Unifying a Recursive with itself should succeed trivially
            val constraint = Constraint(recType, recType, null, emptyList())
            val solutions = unify(listOf(constraint))
            // No variable bindings expected since both sides are identical
            solutions.shouldBeEmpty()
        }
    }

    @Test
    fun `unify two Recursive types with different sentinel variables should terminate`() {
        assertTimeout(Duration.ofSeconds(5)) {
            // This simulates what happens after PolyType.instantiate() freshens variables:
            // The same recursive type alias produces two Recursive wrappers with different
            // sentinel variables but structurally identical bodies.
            // Use multiple self-referential fields to create exponential branching (like chicc's Type)
            val sentinel1 = Variable("Type_1", -1)
            val body1 = Record(
                emptyList(),
                listOf(
                    Record.Field("tag", Type.string),
                    Record.Field("types", Array(sentinel1)),
                    Record.Field("lhs", Sum(emptyList(), sentinel1, Type.unit)),
                    Record.Field("rhs", Sum(emptyList(), sentinel1, Type.unit)),
                    Record.Field("inner", Sum(emptyList(), sentinel1, Type.unit))
                )
            )
            val recType1 = Recursive(sentinel1, body1)

            val sentinel2 = Variable("Type_2", -1)
            val body2 = Record(
                emptyList(),
                listOf(
                    Record.Field("tag", Type.string),
                    Record.Field("types", Array(sentinel2)),
                    Record.Field("lhs", Sum(emptyList(), sentinel2, Type.unit)),
                    Record.Field("rhs", Sum(emptyList(), sentinel2, Type.unit)),
                    Record.Field("inner", Sum(emptyList(), sentinel2, Type.unit))
                )
            )
            val recType2 = Recursive(sentinel2, body2)

            // These represent the same type but with different internal variable names.
            // Unification must terminate (not infinite-loop by repeatedly unfolding).
            val constraint = Constraint(recType1, recType2, null, emptyList())
            val solutions = unify(listOf(constraint))
            // Should bind sentinel1 to sentinel2 (or vice versa) when unifying the bodies
        }
    }

    @Test
    fun `unify Recursive type inside Array should terminate`() {
        assertTimeout(Duration.ofSeconds(5)) {
            // Simulates: array[Type] = array[Type] where Type is recursive
            val sentinel1 = Variable("Type_1", -1)
            val body1 = Record(
                emptyList(),
                listOf(
                    Record.Field("tag", Type.string),
                    Record.Field("children", Array(sentinel1))
                )
            )
            val recType1 = Recursive(sentinel1, body1)

            val sentinel2 = Variable("Type_2", -1)
            val body2 = Record(
                emptyList(),
                listOf(
                    Record.Field("tag", Type.string),
                    Record.Field("children", Array(sentinel2))
                )
            )
            val recType2 = Recursive(sentinel2, body2)

            val constraint = Constraint(Array(recType1), Array(recType2), null, emptyList())
            val solutions = unify(listOf(constraint))
        }
    }
}
