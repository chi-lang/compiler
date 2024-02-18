package gh.marad.chi.core

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.ast
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types3.Type3
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class ReadingVariablesSpec {
    @Test
    fun `should read package variable`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("foo", Type3.int)

        // when
        val result = ast("foo", ns)

        // then
        result.shouldBeTypeOf<VariableAccess>() should {
            it.newType shouldBe Type3.int
            it.target.shouldBeTypeOf<PackageSymbol>().should { symbol ->
                symbol.moduleName shouldBe CompilationDefaults.defaultModule
                symbol.packageName shouldBe CompilationDefaults.defaultPacakge
                symbol.name shouldBe "foo"
            }
        }
    }

    @Test
    fun `should read package variable from function`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("foo", Type3.int)

        // when
        val result = ast(
            """
              fn bar(): int { foo }  
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<NameDeclaration>()
            .value.shouldBeTypeOf<Fn>()
            .body.body.first().shouldBeTypeOf<VariableAccess>().should {
                it.newType shouldBe Type3.int
                it.target.shouldBeTypeOf<PackageSymbol>() should { symbol ->
                    symbol.moduleName shouldBe CompilationDefaults.defaultModule
                    symbol.packageName shouldBe CompilationDefaults.defaultPacakge
                    symbol.name shouldBe "foo"
                }
            }
    }
}