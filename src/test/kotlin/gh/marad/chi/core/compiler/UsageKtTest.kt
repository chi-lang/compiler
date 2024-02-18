package gh.marad.chi.core.compiler

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.addTypeDefinition
import gh.marad.chi.ast
import gh.marad.chi.core.*
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types3.Type3
import gh.marad.chi.core.types3.TypeId
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class UsageKtTest {
    @Test
    fun `blocks should mark last expression as used`() {
        // given
        val expr = ast(
            """
                {
                   5
                   "hello"
                }
            """.trimIndent()
        ).shouldBeTypeOf<Fn>()

        // then
        expr.body.body[0].used shouldBe false
        expr.body.body[1].used shouldBe true
    }

    @Test
    fun `name declaration should mark value as used`() {
        // given
        val expr = ast("val a = 5").shouldBeTypeOf<NameDeclaration>()

        // then
        expr.value.used.shouldBeTrue()
    }

    @Test
    fun `assignment should mark value as used`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("x", Type3.int, mutable = true)
        val expr = ast("x = 5", ns).shouldBeTypeOf<Assignment>()

        // then
        expr.value.used.shouldBeTrue()
    }

    @Test
    fun `field assignment should mark value as read`() {
        // given
        val ns = GlobalCompilationNamespace()
        val type = Type3.record(
            TypeId("mod", "pkg", "Foo"),
            "bar" to Type3.int
        )
        ns.addTypeDefinition(type)
        ns.addSymbolInDefaultPackage("foo", type)
        val expr = ast("foo.bar = 5", ns).shouldBeTypeOf<FieldAssignment>()

        // then
        expr.value.used.shouldBeTrue()
    }

    @Test
    fun `function call should mark parameters as used`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("func", Type3.fn(Type3.int, Type3.bool, Type3.unit))
        val expr = ast("func(5, true)", ns).shouldBeTypeOf<FnCall>()

        // then
        expr.parameters.forEach { it.used.shouldBeTrue() }
    }

    @Test
    fun `index operator should mark index as used`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("x", Type3.array(Type3.int))
        val expr = ast("x[5]", ns).shouldBeTypeOf<IndexOperator>()

        // then
        expr.index.used.shouldBeTrue()
    }

    @Test
    fun `indexed assignment should mark index and value as used`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("x", Type3.array(Type3.int))
        val expr = ast("x[5] = 8", ns).shouldBeTypeOf<IndexedAssignment>()

        // then
        expr.value.used.shouldBeTrue()
        expr.index.used.shouldBeTrue()
    }

    @Test
    fun `return should mark value as used`() {
        // given
        val expr = ast("{ return 5 }").shouldBeTypeOf<Fn>()

        // when
        val ret = expr.body.body.last().shouldBeTypeOf<Return>()
        ret.value?.used shouldBe true
    }
}