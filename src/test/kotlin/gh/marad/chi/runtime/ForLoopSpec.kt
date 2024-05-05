package gh.marad.chi.runtime

import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.types.Type
import gh.marad.chi.messages
import gh.marad.chi.runtime.TestEnv.eval
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
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
    fun `loop with array should check type for variable`() {
        val msgs = messages("""
            var wrong = 0
            for s in ["hello", "world"] {
                wrong = s
            }
        """.trimIndent())

        msgs shouldHaveSize 1
        msgs[0].shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Type.int
            it.actual shouldBe Type.string
        }
    }

    @Test
    fun `should add indexes for arrays`() {
        // this example is convoluted because it also checks if types are inferred correctly
        val result = eval("""
            var idxsum = 0
            var result = ""
            for idx, x in ["hello", "world"] {
                idxsum += idx
                result += "${'$'}x;"
            }
            "${'$'}result ${'$'}idxsum"
        """.trimIndent())

        result shouldBe "hello;world; 3"
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

    @Test
    fun `should work with generator functions`() {
        val result = eval("""
            var x = 0
            fn gen(s: {}, last: int): int|unit {
                val x = if last is unit { 0 } else { last as int } + 1
                return if x <= 3 {
                    x
                } else {
                    unit
                }
            }
            
            var sum = 0
            for a in gen, {}, 0 {
                sum += a
            }
            sum
        """.trimIndent())

        result shouldBe 6
    }

    @Test
    fun `should work with simple generator function`() {
        val result = eval("""
            var x = 0
            fn gen(): int|unit {
                x += 1
                if x <= 3 { x }
            }
            
            var sum = 0
            for a in gen {
                sum += a
            }
            sum
        """.trimIndent())

        result shouldBe 6
    }

    @Test
    fun `should work with lambda functions`() {
        val result = eval("""
            var x = 0
            var sum = 0
            for a in { 
                x += 1  
                if x <= 3 { x } 
            } { 
                sum += a 
            }
            sum
        """.trimIndent())

        result shouldBe 6
    }

    @Test
    fun `should work with state`() {
        val result = eval("""
            var sum = 0
            for a in { state: { v:int } , last -> 
                state.v = state.v + 1
                if state.v < 4 { state.v } 
            }, { v: 0 }, 0 {
                sum += a
            }
            sum
        """.trimIndent(), showLuaCode = true)

        result shouldBe 6
    }
}