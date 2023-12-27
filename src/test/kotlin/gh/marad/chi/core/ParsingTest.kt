package gh.marad.chi.core

import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import org.junit.jupiter.api.Test

class ParsingTest {

    @Test
    fun foo() {
        val code = """
            fn a() {
                return
            }
        """.trimIndent()

        val result = Compiler.compile(code, GlobalCompilationNamespace())
        println(result)
    }
}