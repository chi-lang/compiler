package gh.marad.chi.core.modules

import gh.marad.chi.ErrorMessagesException
import gh.marad.chi.ast
import gh.marad.chi.compile
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.OldType
import gh.marad.chi.core.VariableAccess
import gh.marad.chi.core.analyzer.SyntaxError
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.SymbolType
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

@Suppress("unused")
class ImportSpec : FunSpec({

    // TODO this should be moved to FnCall generation tests
    test("using simplified name for names defined in current module") {
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
            call.function.shouldBeTypeOf<VariableAccess>().should { fn ->
                fn.moduleName shouldBe "user"
                fn.packageName shouldBe "default"
                fn.name shouldBe "foo"
            }
        }
    }

    // TODO this should be moved to FnCall generation tests
    test("importing function from package") {
        // given
        val ns = GlobalCompilationNamespace()
        ns.getOrCreatePackage("std", "time")
            .scope.addSymbol("millis", OldType.fn(OldType.int), SymbolType.Local)

        // when
        val result = compile(
            """
            import std/time { millis }
            millis()
        """.trimIndent(), namespace = ns, ignoreCompilationErrors = true
        )

        // then
        val call = result.expressions.first()
        call.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>().should { fn ->
                fn.moduleName shouldBe "std"
                fn.packageName shouldBe "time"
                fn.name shouldBe "millis"
            }
        }
    }

    // TODO this should be moved to FnCall generation tests
    test("import function with alias") {
        // given
        val ns = GlobalCompilationNamespace()
        ns.getOrCreatePackage("std", "time")
            .scope.addSymbol("millis", OldType.fn(OldType.int), SymbolType.Local)

        // when
        val result = compile(
            """
            import std/time { millis as coreMillis }
            coreMillis()
        """.trimIndent(), namespace = ns, ignoreCompilationErrors = true
        )

        // then
        val call = result.expressions.first()
        call.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>().should { fn ->
                fn.moduleName shouldBe "std"
                fn.packageName shouldBe "time"
                fn.name shouldBe "millis"
            }
        }
    }

    // TODO this should be moved to FnCall generation tests
    test("whole package alias") {
        // when
        val result = ast(
            """
            import std/time as time
            time.millis()
        """.trimIndent(), ignoreCompilationErrors = true
        )

        // then
        result.shouldBeTypeOf<FnCall>().should { call ->
            call.function.shouldBeTypeOf<VariableAccess>().should { fn ->
                fn.moduleName shouldBe "std"
                fn.packageName shouldBe "time"
                fn.name shouldBe "millis"
            }
        }
    }


    test("import package and functions and alias everything") {
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
            """.trimIndent(), namespace = ns, ignoreCompilationErrors = true
        )

        // then
        result.expressions// drop implicit package and import
            .forEach { expr ->
                expr.shouldBeTypeOf<VariableAccess>().should { va ->
                    va.moduleName shouldBe "std"
                    va.packageName shouldBe "time"
                    va.name shouldBe "millis"
                }
            }
    }

    test("should not allow empty package alias name") {
        // when
        val ex = shouldThrow<ErrorMessagesException> {
            ast(
                """
                    import module/pkg as 
                """.trimIndent()
            )
        }

        // when
        val messages = ex.errors

        // then
        messages shouldHaveSize 1
        messages[0].shouldBeTypeOf<SyntaxError>()
    }

})
