package gh.marad.chi.regression

import gh.marad.chi.ast
import gh.marad.chi.core.types.Type
import gh.marad.chi.runtime.LuaEnv
import gh.marad.chi.runtime.TestEnv.eval
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class PassPreciseTypeInformationWhileInferingTypes {
    @Test
    fun `foo`() {
        val env = LuaEnv()

        eval("""
            package std/lang.string
            
            type CodePoint = int
            
            fn a(s: string): CodePoint { 0 }
            fn b(cp: CodePoint): bool { true }
        """.trimIndent(), env)

        val result = eval("""
            val x = "foo".a()
            x.b()               
        """.trimIndent(), env)

        result shouldBe true
    }
}