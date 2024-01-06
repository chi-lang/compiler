package gh.marad.chi.core.types

import gh.marad.chi.ast
import gh.marad.chi.core.OldType
import gh.marad.chi.core.analyzer.TypeIsNotIndexable
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.analyzer.analyze
import gh.marad.chi.core.compiler.Symbol
import gh.marad.chi.core.compiler.SymbolKind
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

@Suppress("unused")
class IndexOperatorSpec : FunSpec({
    val ns = GlobalCompilationNamespace()
    ns.getDefaultPackage().symbols.apply {
        add(Symbol("user", "default", "arr", SymbolKind.Local, Types.array(Types.int), 0, true, true))
    }

    test("should not allow to index arrays with types other than integer") {
        analyze(
            ast(
                """
                    arr["invalid-index"]
                """.trimIndent(), ns, ignoreCompilationErrors = true
            )
        ).should { msgs ->
            msgs shouldHaveSize 1
            msgs.first().shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe OldType.int
                it.actual shouldBe OldType.string
            }
        }
    }

    test("should not allow indexing arrays in assignment with non-integer types") {
        analyze(
            ast(
                """
                    arr["invalid-index"] = 5
                """.trimIndent(), ns, ignoreCompilationErrors = true,
            )
        ).should { msgs ->
            msgs shouldHaveSize 1
            msgs.first().shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe OldType.int
                it.actual shouldBe OldType.string
            }
        }
    }

    test("should not allow indexing non-indexable types") {
        analyze(
            ast(
                """
                    5[2]
                """.trimIndent(), ignoreCompilationErrors = true
            )
        ).should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<TypeIsNotIndexable>().should {
                it.type shouldBe OldType.int
            }
        }
    }

    test("should not allow assign by index to non-indexable types") {
        analyze(
            ast(
                """
                    5[2] = 10
                """.trimIndent(), ignoreCompilationErrors = true
            )
        ).should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<TypeIsNotIndexable>().should {
                it.type shouldBe OldType.int
            }
        }
    }

    test("assigned value should match the element type") {
        analyze(
            ast(
                """
                    arr[2] = "i should be an int"
                """.trimIndent(), ns, ignoreCompilationErrors = true,
            )
        ).should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<TypeMismatch>().should {
                it.expected shouldBe OldType.int
                it.actual shouldBe OldType.string
            }
        }
    }
})