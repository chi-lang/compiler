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
        """.trimIndent(), showLuaCode = true)

        result shouldBe 6
    }

    @Test
    fun `should add add indexes for arrays`() {
        val result = eval("""
            var sum = 0
            for idx, x in [5, 6, 7] {
                sum += idx * x
            }
            sum
        """.trimIndent())

        result shouldBe (1*5 + 2*6 + 3*7)
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

    // TODO error if only one var name is supplied
    // TODO ignore one value with _

    @Test
    fun `should work with generator functions`() {
        val result = eval("""
            var x = 0
            fn gen(s: any, last: int|unit): int|unit {
                val x = if last is unit { 0 } else { last as int } + 1
                return if x <= 3 {
                    x
                } else {
                    unit
                }
            }
            
            var sum = 0
            for a in gen {
                sum += a
            }
            sum
        """.trimIndent())

        result shouldBe 6
    }

    // TODO: make it work with generator without arguments
    // TODO: check generator function type
    // TODO: fix variable types
}