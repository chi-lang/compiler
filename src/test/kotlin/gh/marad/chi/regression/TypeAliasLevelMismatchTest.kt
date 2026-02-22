package gh.marad.chi.regression

import gh.marad.chi.ast
import gh.marad.chi.core.analyzer.InfiniteType
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class TypeAliasLevelMismatchTest {

    @Test
    fun `generic type alias used in generic function signature should compile without error`() {
        // Reproduces the bug: Option[T] in a generic function triggers
        // "Infinite type" due to level mismatch in type alias variable replacement
        ast("""
            package test/type_alias_test
            
            type Option[T] = T | unit
            
            pub fn valueOr[T](opt: Option[T], alternative: T): T {
                if opt is unit {
                    alternative
                } else {
                    opt as T
                }
            }
        """.trimIndent())
    }

    @Test
    fun `generic type alias with multiple type parameters should resolve correctly`() {
        ast("""
            package test/type_alias_test2
            
            type Either[A, B] = A | B
            
            fn first[A, B](e: Either[A, B]): Either[A, B] {
                e
            }
        """.trimIndent())
    }

    @Test
    fun `non-generic type alias should be unaffected`() {
        ast("""
            package test/type_alias_test3
            
            type Name = string
            
            fn greet(n: Name): string {
                n
            }
        """.trimIndent())
    }

    @Test
    fun `self-application should still produce infinite type error`() {
        // BUG-02 regression guard: occurs check must still reject genuinely infinite types
        val msgs = messages("val f = { x -> x(x) }")
        msgs shouldHaveSize 1
        msgs[0].shouldBeTypeOf<InfiniteType>()
    }
}
