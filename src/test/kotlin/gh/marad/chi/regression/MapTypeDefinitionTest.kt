package gh.marad.chi.regression

import gh.marad.chi.ast
import org.junit.jupiter.api.Test

class MapTypeDefinitionTest {
    @Test
    fun `map type definition should work`() {
        ast("""
            type Map[K,V] = { class: string }
            
            fn mapValues[K,V,R](m: Map[K,V], f: (K,V) -> R): Map[K,R] {
                { class: "Map" }
            }
        """.trimIndent())
    }
}