package gh.marad.chi.core.modules

import gh.marad.chi.addSymbol
import gh.marad.chi.ast
import gh.marad.chi.compile
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.OldType
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.VariableAccess
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.SymbolType
import gh.marad.chi.core.types.Types
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class ImportSpec {

    // TODO this should be moved to FnCall generation tests
    @Test
    fun `using simplified name for names defined in current module`() {
        // when
        val result = ast(
            """
                package user/default
                val foo = { 1 }
                foo()
            """.trimIndent()
        )

        // then
        result.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>()
                .target.shouldBeTypeOf<PackageSymbol>()
                .should { fn ->
                    fn.symbol.moduleName shouldBe "user"
                    fn.symbol.packageName shouldBe "default"
                    fn.symbol.name shouldBe "foo"
                }
        }
    }

    // TODO this should be moved to FnCall generation tests
    @Test
    fun `importing function from package`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbol("std", "time", "millis", Types.fn(Types.int), public = true)

        // when
        val result = compile(
            """
                import std/time { millis }
                millis()
            """.trimIndent(), namespace = ns
        )

        // then
        val call = result.expressions.first()
        call.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>()
                .target.shouldBeTypeOf<PackageSymbol>()
                .should { fn ->
                    fn.symbol.moduleName shouldBe "std"
                    fn.symbol.packageName shouldBe "time"
                    fn.symbol.name shouldBe "millis"
                }
        }
    }

    // TODO this should be moved to FnCall generation tests
    @Test
    fun `import function with alias`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbol("std", "time", "millis", Types.fn(Types.int), public = true)

        // when
        val result = compile(
            """
                import std/time { millis as coreMillis }
                coreMillis()
            """.trimIndent(), namespace = ns
        )

        // then
        val call = result.expressions.first()
        call.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>()
                .target.shouldBeTypeOf<PackageSymbol>()
                .should { fn ->
                    fn.symbol.moduleName shouldBe "std"
                    fn.symbol.packageName shouldBe "time"
                    fn.symbol.name shouldBe "millis"
                }
        }
    }

    // TODO this should be moved to FnCall generation tests
    @Test
    fun `whole package alias`() {
        // when
        val result = ast(
            """
                import std/time as time
                time.millis()
            """.trimIndent()
        )

        // then
        result.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>()
                .target.shouldBeTypeOf<PackageSymbol>()
                .should { fn ->
                    fn.symbol.moduleName shouldBe "std"
                    fn.symbol.packageName shouldBe "time"
                    fn.symbol.name shouldBe "millis"
                }
        }
    }


    @Test
    fun `import package and functions and alias everything`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.getOrCreatePackage("std", "time")
            .scope.addSymbol("millis", OldType.fn(OldType.int), SymbolType.Local)

        // when
        val result = compile(
            """
                import std/time as time { millis as coreMillis }
                time.millis
                coreMillis
            """.trimIndent(), namespace = ns
        )

        // then
        result.expressions// drop implicit package and import
            .forEach { expr ->
                expr.shouldBeTypeOf<VariableAccess>()
                    .target.shouldBeTypeOf<PackageSymbol>()
                    .should { target ->
                        target.symbol.moduleName shouldBe "std"
                        target.symbol.packageName shouldBe "time"
                        target.symbol.name shouldBe "millis"
                    }
            }
    }
}