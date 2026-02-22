package gh.marad.chi.core.types

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class TypeUnionTest {
    @Test
    fun `Type union with duplicate types returns single type without crashing`() {
        // BUG-11 fix: Type.union() no longer crashes with ClassCastException
        // when all types deduplicate to a single type.
        val result = Type.union(null, Type.int, Type.int)
        result shouldBe Type.int
    }

    @Test
    fun `Type union with distinct types returns Sum`() {
        val result = Type.union(null, Type.int, Type.string)
        result.shouldBeInstanceOf<Sum>()
    }

    @Test
    fun `Type union with mixed duplicates returns deduplicated Sum`() {
        val result = Type.union(null, Type.int, Type.string, Type.int)
        result.shouldBeInstanceOf<Sum>()
        // Should contain int and string, deduplicated
        val types = mutableSetOf<Type>()
        fun collect(t: Type) {
            when (t) {
                is Sum -> { collect(t.lhs); collect(t.rhs) }
                else -> types.add(t)
            }
        }
        collect(result)
        types shouldBe setOf(Type.int, Type.string)
    }
}
