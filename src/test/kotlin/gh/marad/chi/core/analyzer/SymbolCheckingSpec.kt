package gh.marad.chi.core.analyzer

import gh.marad.chi.addSymbol
import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.addTypeDefinition
import gh.marad.chi.core.CompilationDefaults
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId
import gh.marad.chi.messages
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

@Suppress("unused")
class SymbolCheckingSpec {
    @Test
    fun `should check that name is defined`() {
        // when
        val result = messages("x")

        // then
        result shouldHaveSize 1
        result.first().shouldBeTypeOf<UnrecognizedName>().should {
            it.name shouldBe "x"
        }
    }

    @Test
    fun `should find variable defined before use`() {
        // when
        val result = messages("""
            val x = 5
            x
        """.trimIndent())

        // then
        result shouldHaveSize 0

    }

    @Test
    fun `should find variable defined in package`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("x", Type.int)

        // when
        val result = messages("x", ns)

        // then
        result shouldHaveSize 0
    }

    @Test
    fun `should not accept variable defined in other scope`() {
        // when
        val result = messages("""
            fn a() { 
              val x = 5
            }
            
            x
        """.trimIndent())

        // then
        result shouldHaveSize 1
        result.first().shouldBeTypeOf<UnrecognizedName>()
            .name.shouldBe("x")
    }

    @Test
    fun `should accept defined arguments`() {
        // when
        val result = messages("fn a(x: int) { x }")

        // then
        result shouldHaveSize 0
    }

    @Test
    fun `should accept imported symbols`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbol("foo", "bar", "x", Type.int, public = true)

        // when
        val result = messages("""
            import foo/bar { x }
            x
        """.trimIndent(), ns)

        // then
        result shouldHaveSize 0
    }

    @Test
    fun `should check that function in FnCall is defined in scope`() {
        // when
        val result = messages("f()")

        // then
        result shouldHaveSize 1
        result.first().shouldBeTypeOf<UnrecognizedName>()
            .name shouldBe "f"
    }

    @Test
    fun `should not emit error message if function is defined in scope`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("f", Type.fn(Type.int))

        // when
        val result = messages("f()", ns)

        // then
        result shouldHaveSize 0
    }

    @Test
    fun `should accept using non-public symbols from the same module`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbol(CompilationDefaults.defaultModule, "otherPackage", "x", Type.int, public = false)

        // when
        val result = messages("""
            import ${CompilationDefaults.defaultModule}/otherPackage { x }
            x
        """.trimIndent(), ns)

        // then
        result shouldHaveSize 0
    }

    @Test
    fun `should not accept using non-public symbols from other modules`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbol("otherModule", "foo", "x", public = false)

        // when
        val result = messages("""
            import otherModule/foo { x }
            x 
        """.trimIndent(), ns)

        // then
        result shouldHaveSize 1
        result.first().shouldBeTypeOf<CannotAccessInternalName>()
            .name shouldBe "x"
    }

    //@Test
    fun `should not allow using non-public fields in type from other module`() {
        val ns = TestCompilationEnv()
        ns.addTypeDefinition(Type.record(
            TypeId("foo", "bar", "Foo"),
            "i" to Type.int,
            "f" to Type.float
        ))

        // when
        val code = """
            import foo/bar { Foo }
            val foo = { i: 5, f: 8.9 }
            foo.i
            foo.f
        """.trimIndent()
        val result = messages(code, ns)

        // then
        result shouldHaveSize 1
        result.first().shouldBeTypeOf<CannotAccessInternalName>()
            .name shouldBe "f"
    }
}