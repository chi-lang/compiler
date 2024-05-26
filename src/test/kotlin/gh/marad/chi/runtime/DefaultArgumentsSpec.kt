package gh.marad.chi.runtime

import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import gh.marad.chi.messages
import gh.marad.chi.runtime.TestEnv.eval
import io.kotest.assertions.fail
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class DefaultArgumentsSpec {
    @Test
    fun `should apply default arg values`() {
        val result = eval("""
            fn foo(a: int, b: int = 5): int {
                a + b
            }
            foo(1)
        """.trimIndent())

        result shouldBe 6
    }

    @Test
    fun `default arguments should work for imported functions`() {
        val env = LuaEnv()

        eval("""
            package bar/baz
            pub fn foo(a: int, b: int = 5): int {
                a + b
            }
        """.trimIndent(), env)

        val result = eval("""
            import bar/baz { foo }
            foo(1)
        """.trimIndent(), env)

        result shouldBe 6
    }

    @Test
    fun `default value can be a local function call`() {
        val result = eval("""
            fn foo(): int { 5 }
            fn bar(a: int, b: int = foo()): int {
                a + b
            }
            bar(1)
        """.trimIndent())

        result shouldBe 6
    }

    @Test
    fun `should work with method invocation style`() {
        val result = eval("""
            fn foo(a: int, b: int = 5): int {
                a + b
            }
            1.foo()
        """.trimIndent())

        result shouldBe 6
    }

    @Test
    fun `should work for imported functions with method invocation syntax`() {
        val env = LuaEnv()

        eval("""
            package bar/baz
            pub fn foo(a: int, b: int = 5): int {
                a + b
            }
        """.trimIndent(), env)

        val result = eval("""
            import bar/baz { foo }
            1.foo()
        """.trimIndent(), env)

        result shouldBe 6
    }

    @Test
    fun `should fail if default argument value type does not match`() {
        val result = messages("""
            fn foo(a: int, b: int = "hello"): int {
                a + b
            }
            foo(1)
        """.trimIndent())

        result shouldHaveSize 1
        result.first().shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe int
            it.actual shouldBe string
        }
    }


    @Test
    fun `default arguments todo`() {
        // TODO
        //  - add support for default arguments in lambda notation
        fail("Default arguments still need work!")
    }
}