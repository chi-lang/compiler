@file:Suppress("unused")

package gh.marad.chi.core.types

import gh.marad.chi.ast
import gh.marad.chi.core.NameDeclaration
import gh.marad.chi.messages
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class AnyTypeSpec : FunSpec({
    test("should read 'any' type") {
        ast(
            """
                val x: any = 1
            """.trimIndent(), ignoreCompilationErrors = true
        ).shouldBeTypeOf<NameDeclaration>() should {
            it.expectedType shouldBe Types.any
        }
    }

    test("'any' type should be matched by any other type") {
        val msgs = messages("val x: any = 1")
        msgs.shouldBeEmpty()
    }
})