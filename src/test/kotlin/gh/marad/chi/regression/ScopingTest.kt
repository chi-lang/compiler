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
}