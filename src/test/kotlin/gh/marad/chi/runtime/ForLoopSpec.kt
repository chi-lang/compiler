package gh.marad.chi.runtime

import gh.marad.chi.runtime.TestEnv.eval
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ForLoopSpec {
    @Test
    fun `should read simple for loop`() {
        val result = eval("""
            var sum = 0
            for a in [1, 2, 3] {
                sum += a
            }
            sum
        """.trimIndent())

        result shouldBe 6

    }

    @Test
    fun `should read key-value for loop`() {
        val result = eval("""
            var r = ""
            for k,v in { a: 1 } {
                r = "${'$'}k ${'$'}v"
            }
            r
        """.trimIndent())

        result shouldBe "a 1"
    }
}