package gh.marad.chi.core.types

import gh.marad.chi.compile
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.utils.printAst
import org.junit.jupiter.api.Test

class TypeIdPassingTest {
    @Test
    fun `should use declared name type id`() {
        val ns = TestCompilationEnv()

        compile(
            """
                package foo/bar
                type Point = { x: int, y: int }
                pub fn point(): Point { {x: 1, y: 2} }
                pub fn sum(p: Point): int { p.x + p.y }
            """.trimIndent(),
            ns
        )

        val result = compile(
            """
                import foo/bar { point }
                point().sum()
            """.trimIndent(),
            ns
        )

        printAst(result.expressions)

    }



}