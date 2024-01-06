package gh.marad.chi.core

import gh.marad.chi.ast
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.analyzer.analyze
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

@Suppress("unused")
class ObjectsSpec {
    @Test
    fun `should find that member doesn't exist`() {
        analyze(
            ast(
                """
                    data Test = Foo(i: int)
                    val x = Foo(10)
                    x.somethingElse
                """.trimIndent(), ignoreCompilationErrors = true
            )
        ).should { msgs ->
            msgs.shouldHaveSize(1)
            msgs[0].shouldBeTypeOf<MemberDoesNotExist>().should {
                it.level shouldBe Level.ERROR
                it.member shouldBe "somethingElse"
                it.type.toString() shouldBe "user::default::Test"
            }
        }
    }

    @Test
    fun `check types for variant constructor invocation`() {
        val msgs = analyze(
            ast(
                """
                    data Foo = Foo(i: int)
                    Foo("hello")
                """.trimIndent(), ignoreCompilationErrors = true
            )
        )

        msgs shouldHaveSize 1
        msgs[0].shouldBeTypeOf<TypeMismatch>() should {
            it.level shouldBe Level.ERROR
            it.expected shouldBe OldType.int
            it.actual shouldBe OldType.string
        }
    }
}