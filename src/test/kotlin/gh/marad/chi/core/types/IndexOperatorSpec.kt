package gh.marad.chi.core.types

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.types.Type.Companion.array
import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

@Suppress("unused")
class IndexOperatorSpec {
    val ns = TestCompilationEnv().also {
        it.addSymbolInDefaultPackage("arr", array(int))
    }

    @Test
    fun `should not allow to index arrays with types other than integer`() {
        // when
        val result = messages(
            """
                arr["invalid-index"]
            """.trimIndent(),
            ns
        )

        // then
        result.should { msgs ->
            msgs shouldHaveSize 1
            msgs.first().shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe Type.int
                it.actual shouldBe Type.string
            }
        }
    }

    @Test
    fun `should not allow indexing arrays in assignment with non-integer types`() {
        // when
        val result = messages(
            """
                arr["invalid-index"] = 5
            """.trimIndent(),
            ns
        )

        // then
        result.should { msgs ->
            msgs shouldHaveSize 1
            msgs.first().shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe Type.int
                it.actual shouldBe Type.string
            }
        }
    }

    @Test
    fun `should not allow indexing non-indexable types`() {
        // when
        val result = messages(
            """
                5[2]
            """.trimIndent()
        )

        // then
        result.should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe int
                it.actual.shouldBeTypeOf<Array>()
            }
        }
    }

    @Test
    fun `should not allow assign by index to non-indexable types`() {
        // when
        val result = messages(
            """
                5[2] = 10
            """.trimIndent()
        )

        // then
        result.should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe int
                it.actual shouldBe array(int)
            }
        }
    }

    @Test
    fun `assigned value should match the element type`() {
        // when
        val result = messages(
            """
                arr[2] = "i should be an int"
            """.trimIndent(), ns
        )

        // then
        result.should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe Type.int
                it.actual shouldBe Type.string
            }
        }
    }
}