package gh.marad.chi

import gh.marad.chi.core.FnCall
import gh.marad.chi.core.LocalSymbol
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.VariableAccess
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FunctionCallingSpec {
    @Test
    fun `should call function from package that type is declared in`() {
        // given
        val ns = GlobalCompilationNamespace()
        val type = ns.addProductType("mod", "pkg", "Type", emptyList(), public = true)
        ns.addSymbol("mod", "pkg", "bar", Types.fn(type, Types.int), public = true)
        ns.addSymbolInDefaultPackage("foo", type)

        // when
        val result = ast(
            """
                foo.bar()
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<FnCall>().should {
            it.newType shouldBe Types.int
            it.function.shouldBeTypeOf<VariableAccess>()
                .target shouldBe PackageSymbol("mod", "pkg", "bar")
        }
    }

    @Test
    fun `should find locally defined functions`() {
        // given
        val result = ast(
            """
                fn bar(i: int): float { 5.0 } 
                val foo = 5
                foo.bar()
            """.trimIndent()
        )

        // then
        result.shouldBeTypeOf<FnCall>().should {
            it.newType shouldBe Types.float
            it.function.shouldBeTypeOf<VariableAccess>()
                .target shouldBe LocalSymbol("bar")
        }
    }

    @Test
    fun `should work with multiple chained function calls`() {
        // given
        val ns = GlobalCompilationNamespace()
        val type = ns.addProductType("mod", "pkg", "Type", emptyList(), public = true)
        ns.addSymbol("mod", "pkg", "bar", Types.fn(type, Types.int), public = true)
        ns.addSymbolInDefaultPackage("foo", type)

        // when
        val result = ast(
            """
                fn baz(i: int): float { 5.0 }
                foo.bar().baz()
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<FnCall>().should { bazCall ->
            bazCall.newType shouldBe Types.float
            bazCall.function.shouldBeTypeOf<VariableAccess>()
                .target shouldBe LocalSymbol("baz")

            bazCall.parameters[0].shouldBeTypeOf<FnCall>().should { barCall ->
                barCall.newType shouldBe Types.int
                barCall.function.shouldBeTypeOf<VariableAccess>()
                    .target shouldBe PackageSymbol("mod", "pkg", "bar")
                barCall.parameters[0].shouldBeTypeOf<VariableAccess>()
                    .target shouldBe PackageSymbol(
                    ns.getDefaultPackage().moduleName,
                    ns.getDefaultPackage().packageName,
                    "foo")
            }
        }

    }
}