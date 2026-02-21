package gh.marad.chi.lua

import gh.marad.chi.compile
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContainIgnoringCase
import gh.marad.chi.runtime.LuaEnv
import gh.marad.chi.runtime.TestEnv.eval
import org.junit.jupiter.api.Test

class LuaEmitterScopingTest {

    private fun emitLua(code: String): String {
        val program = compile(code)
        val emitter = LuaEmitter(program)
        return emitter.emit(emitModule = false)
    }

    @Test
    fun `nested lambda should emit local function not global`() {
        val lua = emitLua("""
            fn apply(x: int): int {
                val f = { y: int -> y + 1 }
                f(x)
            }
            apply(5)
        """.trimIndent())

        lua shouldContain "local function tmp"
    }

    @Test
    fun `nested lambda should evaluate correctly with local scoping`() {
        val result = eval("""
            fn apply(x: int): int {
                val f = { y: int -> y + 1 }
                f(x)
            }
            apply(5)
        """.trimIndent())

        result shouldBe 6.0
    }

    @Test
    fun `while loop with complex condition should use local scoping`() {
        val lua = emitLua("""
            var i = 0
            while i < 10 {
                i += 1
            }
            i
        """.trimIndent())

        // The emitted Lua should not contain a bare "function tmp" (global),
        // any tmp functions should be locally scoped
        val lines = lua.lines()
        for (line in lines) {
            if (line.trimStart().startsWith("function tmp")) {
                throw AssertionError(
                    "Found global function declaration in emitted Lua: '$line'. " +
                    "Expected 'local function tmp...' instead."
                )
            }
        }
    }

    @Test
    fun `while loop should evaluate correctly with local scoping`() {
        val result = eval("""
            {
                var i = 0
                while i < 5 {
                    i += 1
                }
                i
            }()
        """.trimIndent())

        result shouldBe 5.0
    }

    @Test
    fun `top-level functions should remain package-scoped not local`() {
        val lua = emitLua("""
            fn add(a: int, b: int): int { a + b }
            add(1, 2)
        """.trimIndent())

        lua shouldContain "function __P_.add("
    }
}
