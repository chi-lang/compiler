package gh.marad.chi.core

import gh.marad.chi.ast
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.ProductType
import gh.marad.chi.core.types.SimpleType
import gh.marad.chi.core.types.SumType
import gh.marad.chi.core.types.Types
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
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

    @Test
    fun `should define types`() {
        // when
        val result = Compiler.compile(
            """
                package foo/bar
                data A = B(i: int) | C
            """.trimIndent(),
            GlobalCompilationNamespace()
        ).program.definedTypes

        // then
        result.map { it.type } shouldContainAll listOf(
            SumType("foo", "bar", "A", emptyList(), listOf("B", "C"), emptyList()),
            ProductType("foo", "bar", "B", listOf(Types.int), emptyList(), emptyList()),
            SimpleType("foo", "bar", "C")
        )
    }

    @Test
    fun `simplified declaration should define only one type`() {
        // when
        val result = Compiler.compile(
            """
                package foo/bar
                data A(i: int)
            """.trimIndent(),
            GlobalCompilationNamespace()
        ).program.definedTypes

        // then
        result.map { it.type } shouldContainExactly listOf(
            ProductType("foo", "bar", "A", listOf(Types.int), emptyList(), emptyList())
        )
    }
}