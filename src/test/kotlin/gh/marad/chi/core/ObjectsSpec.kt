package gh.marad.chi.core

import gh.marad.chi.ast
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.types.Types
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

@Suppress("unused")
class ObjectsSpec {
    @Test
    fun `should find that member doesn't exist`() {
        messages(
            """
                data Test = Foo(i: int)
                val x = Foo(10)
                x.somethingElse
            """.trimIndent()
        ).should { msgs ->
            msgs.shouldHaveSize(1)
            msgs[0].shouldBeTypeOf<MemberDoesNotExist>().should {
                it.level shouldBe Level.ERROR
                it.member shouldBe "somethingElse"
                it.type.toString() shouldBe "user::default::Foo"
            }
        }
    }

    @Test
    fun `check types for variant constructor invocation`() {
        val msgs = messages(
            """
                data Foo = Foo(i: int)
                Foo("hello")
            """.trimIndent()
        )

        msgs shouldHaveSize 1
        msgs[0].shouldBeTypeOf<TypeMismatch>() should {
            it.level shouldBe Level.ERROR
            it.expected shouldBe Types.int
            it.actual shouldBe Types.string
        }
    }

    @Test
    fun `should be able to access object field`() {
        // when
        val result = ast(
            """
                data Foo = Foo(i: int)
                val f = Foo(10)
                f.i
            """.trimIndent()
        )

        // then
        result.shouldBeTypeOf<FieldAccess>().should {
            it.newType shouldBe Types.int
        }
    }
}