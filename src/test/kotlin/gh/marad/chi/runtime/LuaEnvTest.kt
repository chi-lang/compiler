package gh.marad.chi.runtime

import gh.marad.chi.runtime.TestEnv.eval
import io.kotest.assertions.fail
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class LuaEnvTest {
    @Test
    fun `should evaluate atoms`() {
        eval("5") shouldBe 5.0
        eval("5.5") shouldBe 5.5
        eval("\"hello\"") shouldBe "hello"
        eval("true") shouldBe true
        eval("false") shouldBe false
        eval("{ a: 10 }").shouldBeTypeOf<HashMap<Any, Any>>() should {
            it["a"] shouldBe 10.0
        }
        eval("[1, 2, 3]").shouldBeTypeOf<HashMap<Any, Any>>() should {
            it[1.0] shouldBe 1.0
            it[2.0] shouldBe 2.0
            it[3.0] shouldBe 3.0
        }
    }

    @Test
    fun `should evaluate name declaration and variable read`() {
        val env = LuaEnv()
        eval("val x = 8", env) shouldBe 8.0
        eval("x", env) shouldBe 8.0
    }

    @Test
    fun `should define function and call it`() {
        val env = LuaEnv()
        eval("val id = { x -> x }", env)
        eval("id(5)", env) shouldBe 5.0
    }

    @Test
    fun `should work with blocks and interpolate strings`() {
        eval("""
            val name = "John"
            fn hello(who: string): string {
                "Hello ${'$'}who!"
            }
            name.hello()
        """.trimIndent()) shouldBe "Hello John!"
    }

    @Test
    fun `should be able to index just declared array`() {
        eval("[1, 2, 3][1]") shouldBe 1.0
    }

    @Test
    fun `should assign to variable`() {
        eval("""
            var x = 5
            x = 10
            x
        """.trimIndent()) shouldBe 10.0
    }

    @Test
    fun `should assign to array index`() {
        eval("""
            var a = [1, 2, 3]
            a[1] = 10
            a[1]
        """.trimIndent()) shouldBe 10.0
    }

    @Test
    fun `casting does not change the value`() {
        eval("5 as string") shouldBe 5.0
        eval("\"5\" as int") shouldBe "5"
    }

    @Test
    fun `should be able to read the field of just declared record`() {
        eval("{ a: 10 }.a") shouldBe 10.0
    }

    @Test
    fun `should assign to record field`() {
        eval("""
            val r = { a: 10 }
            r.a = 5
            r.a
        """.trimIndent()) shouldBe 5.0
    }

    @Test
    fun `if-else should work`() {
        eval("if true { 5 } else { 10 }") shouldBe 5.0
        eval("if false { 5 } else { 10 }") shouldBe 10.0
    }

    @Test
    fun `infix operators should work`() {
        eval("5 + 10") shouldBe 15.0
        eval("\"hello \" + \"world\"") shouldBe "hello world"
    }

    @Test
    fun `prefix operator should work`() {
        eval("!true") shouldBe false
    }

    @Test
    fun `while loop`() {
        eval("""
            {
                var i = 0
                while i < 5 {
                    i += 1
                }
                i
            }()
        """.trimIndent()) shouldBe 5.0
    }

    @Test
    fun `should define and load types`() {
        val env = LuaEnv()
        eval("""
            package some/pkg
            type A = int | unit
        """.trimIndent(), env)

        eval("""
            package ohter/pkg
            import some/pkg { A }
            val a: A = 5
        """.trimIndent(), env) shouldBe 5.0

    }

}