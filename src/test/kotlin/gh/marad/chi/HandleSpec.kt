package gh.marad.chi

import gh.marad.chi.core.Fn
import gh.marad.chi.core.Handle
import gh.marad.chi.core.NameDeclaration
import io.kotest.matchers.should
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class HandleSpec {
    @Test
    fun `should find effect in package`() {
        // when
        val result = ast(
            """
                package modimage/test
                
                pub effect hello(name: string): string
                
                pub fn foo(): string {
                  handle {
                    hello("Eleven")
                  } with {
                    hello(name) -> resume("Hello ${'$'}name")
                  }
                }
            """.trimIndent()
        )

        // then
        result.shouldBeTypeOf<NameDeclaration>()
            .value.shouldBeTypeOf<Fn>()
            .body.body.first().shouldBeTypeOf<Handle>() should {

        }
    }

}