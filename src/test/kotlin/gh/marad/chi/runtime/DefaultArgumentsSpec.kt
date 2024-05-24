package gh.marad.chi.runtime

import gh.marad.chi.runtime.TestEnv.eval
import io.kotest.assertions.fail
import io.kotest.matchers.shouldBe
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
    fun `default arguments todo`() {
        // TODO
        //  - support default args for imported functions
        //  - verify that default arg is a static value, a lambda or a function call
        //  - support default args for method invocation style
        //  - typecheck the default arg with expected type
        //  - run type inference on default argument
        //  - restrict allowed expressions for default values
        fail("Default arguments still need work!")
    }
}