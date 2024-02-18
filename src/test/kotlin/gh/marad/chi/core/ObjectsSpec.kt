package gh.marad.chi.core

import gh.marad.chi.ast
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.MemberDoesNotExist
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types3.Type3
import gh.marad.chi.core.types3.TypeId
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldContainAll
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
                val x = { i: 5 }
                x.somethingElse
            """.trimIndent()
        ).should { msgs ->
            msgs.shouldHaveSize(1)
            msgs[0].shouldBeTypeOf<MemberDoesNotExist>().should {
                it.level shouldBe Level.ERROR
                it.member shouldBe "somethingElse"
                it.type shouldBe Type3.record("i" to Type3.int)
            }
        }
    }

    @Test
    fun `should be able to access object field`() {
        // when
        val result = ast(
            """
                val f = { i: 5 }
                f.i
            """.trimIndent()
        )

        // then
        result.shouldBeTypeOf<FieldAccess>().should {
            it.newType shouldBe Type3.int
        }
    }

    @Test
    fun `should define type aliases`() {
        // when
        val result = Compiler.compile(
            """
                package foo/bar
                type Circle = { radius: float }
                type Square = { side: float }
                type Shape = Circle | Square
            """.trimIndent(),
            GlobalCompilationNamespace()
        ).program.typeAliases

        // then
        val circle = Type3.record(TypeId("foo", "bar", "Circle"), "radius" to Type3.float)
        val square = Type3.record(TypeId("foo", "bar", "Square"), "side" to Type3.float)
        result.map { it.newType } shouldContainAll listOf(
            circle,
            square,
            Type3.union(TypeId("foo", "bar", "Shape"), circle, square)
        )
    }
}