package gh.marad.chi.regression

import gh.marad.chi.ast
import org.junit.jupiter.api.Test

class ScopingTest {
    @Test
    fun `should see outer scope`() {
        ast("""
            fn foo() {
                val value = 5
                val f = { 
                    value + 5
                }
                f()
            }
        """.trimIndent())
    }

    @Test
    fun `function scope should see package scope`() {
        ast("""
            val x = 5
            fn foo(): int { x }
        """.trimIndent())
    }
}