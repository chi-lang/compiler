package gh.marad.chi.core.modules

import gh.marad.chi.addSymbol
import gh.marad.chi.ast
import gh.marad.chi.asts
import gh.marad.chi.compile
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.VariableAccess
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class ImportSpec {

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
                    fn.moduleName shouldBe "user"
                    fn.packageName shouldBe "default"
                    fn.name shouldBe "foo"
                }
        }
    }

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
                    fn.moduleName shouldBe "std"
                    fn.packageName shouldBe "time"
                    fn.name shouldBe "millis"
                }
        }
    }

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
                    fn.moduleName shouldBe "std"
                    fn.packageName shouldBe "time"
                    fn.name shouldBe "millis"
                }
        }
    }

    @Test
    fun `whole package alias`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbol("std", "time", "millis", Types.fn(Types.int), public = true)

        // when
        val result = ast(
            """
                import std/time as time
                time.millis()
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>()
                .target.shouldBeTypeOf<PackageSymbol>()
                .should { fn ->
                    fn.moduleName shouldBe "std"
                    fn.packageName shouldBe "time"
                    fn.name shouldBe "millis"
                }
        }
    }


    @Test
    fun `import package and functions and alias everything`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbol("std", "time", "millis", Types.fn(Types.int), public = true)

        // when
        val result = asts(
            """
                import std/time as time { millis as coreMillis }
                time.millis
                coreMillis
            """.trimIndent(),
            ns
        )

        // then
        result.forEach { expr ->
            expr.shouldBeTypeOf<VariableAccess>()
                .target.shouldBeTypeOf<PackageSymbol>()
                .should { target ->
                    target.moduleName shouldBe "std"
                    target.packageName shouldBe "time"
                    target.name shouldBe "millis"
                }
        }
    }
}