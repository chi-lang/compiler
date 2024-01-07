@file:Suppress("unused")

package gh.marad.chi.core.analyzer

import gh.marad.chi.ast
import gh.marad.chi.compile
import gh.marad.chi.core.OldType
import gh.marad.chi.core.OldType.Companion.bool
import gh.marad.chi.core.OldType.Companion.int
import gh.marad.chi.core.OldType.Companion.unit
import gh.marad.chi.core.compiler.Symbol
import gh.marad.chi.core.compiler.SymbolKind
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
import gh.marad.chi.messages
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class AssignmentTypeCheckingSpec  {
    @Test
    fun `should prohibit changing immutable values`() {
        messages("""
            val x = 10
            x = 15
        """.trimIndent()
        ).should { msgs ->
            msgs shouldHaveSize 1
            msgs[0].shouldBeTypeOf<CannotChangeImmutableVariable>().should {
                it.level shouldBe Level.ERROR
            }
        }
    }
}

class NameDeclarationTypeCheckingSpec {
    @Test
    fun `should return nothing for simple atom and variable read`() {
        val ns = GlobalCompilationNamespace()
        ns.getDefaultPackage().symbols.apply {
            add(Symbol("user", "default", "x", SymbolKind.Local, Types.fn(Types.unit), 0, true, true))
        }
        analyze(ast("5", ns, ignoreCompilationErrors = true)).shouldBeEmpty()
        analyze(ast("x", ns, ignoreCompilationErrors = true)).shouldBeEmpty()
    }

    @Test
    fun `should check if types match in name declaration with type definition`() {
        messages("val x: () -> int = 5").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<NotAFunction>()
        }
    }

    @Test
    fun `should pass valid name declarations`() {
        messages("val x: int = 5").shouldBeEmpty()
        messages("val x = 5").shouldBeEmpty()
    }
}

class FnTypeCheckingSpec {
    @Test
    fun `should not return errors on valid function definition`() {
        messages("{ x: int -> x }").shouldBeEmpty()
        messages("fn foo(x: int): int { x }").shouldBeEmpty()
    }

    @Test
    fun `should check for missing return value only if function expects the return type`() {
        messages("fn foo() {}").shouldBeEmpty()
        messages("fn foo(): int {}").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Types.int
                error.actual shouldBe Types.unit
            }
        }
    }

    @Test
    fun `should check that block return type matches what function expects`() {
        messages("fn foo(): int { {} }").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Types.int
                error.actual shouldBe Types.fn(Types.unit)
            }
        }
    }

    @Test
    fun `should also check types for expressions in function body`() {
        messages("""
            fn foo(x: int): int {
                val i: int = {}
                x
            }
        """.trimIndent()
        ).should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Types.int
                error.actual shouldBe Types.fn(Types.unit)
            }
        }
    }

    @Test
    fun `should check that return expression type matches the function return value`() {
        messages( """
            fn foo(): int {
                return "hello"
                5
            }
        """.trimIndent()
        ).should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Types.int
                error.actual shouldBe Types.string
            }
        }
    }

    @Test
    fun `should accept return without any value`() {
        analyze(
            ast(
                """
                        fn foo() {
                            return
                            5
                        }
                    """.trimIndent()
            )
        ).should {
            it.shouldHaveSize(0)
        }
    }
}

class IfElseTypeCheckingSpec : FunSpec() {
    init {
        test("if-else type is unit when branch types differ (or 'else' branch is missing)") {
            analyze(ast("val x: unit = if(true) { 2 }", ignoreCompilationErrors = true)).shouldBeEmpty()
            analyze(ast("val x: int = if(true) { 2 } else { 3 }", ignoreCompilationErrors = true)).shouldBeEmpty()
            analyze(ast("val x: int = if(true) { 2 } else { {} }", ignoreCompilationErrors = true)).should {
                it.shouldHaveSize(1)
                it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                    error.expected shouldBe int
                    error.actual shouldBe unit
                }
            }
        }

        test("conditions should be boolean type") {
            analyze(ast("if (1) { 2 }", ignoreCompilationErrors = true)).should {
                it.shouldHaveSize(1)
                it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                    error.expected shouldBe bool
                    error.actual shouldBe int
                }
            }
        }
    }
}

class PrefixOpSpec : FunSpec({
    test("should expect boolean type for '!' operator") {
        analyze(ast("!true", ignoreCompilationErrors = true)) shouldHaveSize 0
        analyze(ast("!1", ignoreCompilationErrors = true)).should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe bool
                error.actual shouldBe int
            }
        }
    }
})

class CastSpec : FunSpec({
})

class WhileLoopSpec : FunSpec({
    test("condition should have boolean type") {
        analyze(ast("while(true) {}", ignoreCompilationErrors = true)) shouldHaveSize 0
        analyze(ast("while(1) {}", ignoreCompilationErrors = true)).should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe bool
                error.actual shouldBe int
            }
        }
    }
})

class IsExprSpec : FunSpec({
    test("is expr should cooperate with if providing a scope") {
        val code = """
            data AB = A(a: int) | B(b: float)
            val a = A(10)
            if (a is B) {
                a.b
            }
        """.trimIndent()

        val errors = analyze(ast(code, ignoreCompilationErrors = false))

        errors.shouldBeEmpty()
    }

    test("is expr should fill variant within `when` branches") {
        val code = """
            data AB = A(a: int) | B(b: int) | C(c: int) | D(d: int)
            val x = A(10)
            when {
                x is A -> x.a
                x is B -> x.b
                x is C -> x.c
                x is D -> x.d
            }
        """.trimIndent()

        val errors = analyze(ast(code, ignoreCompilationErrors = false))

        errors.shouldBeEmpty()
    }

    test("should also work with imported types") {
        val namespace = GlobalCompilationNamespace()
        val defCode = """
            package foo/bar
            data AB = pub A(a: int) | B(pub b: float)
        """.trimIndent()
        compile(defCode, namespace)

        val code = """
            import foo/bar { AB }
            val a = A(10)
            if (a is B) {
                a.b
            }
        """.trimIndent()
        compile(code, namespace)
    }

    test("should not allow importing variables and functions that are not public") {
        val namespace = GlobalCompilationNamespace()
        val defCode = """
            package mymod/mypkg
            fn foo() { 0 }
            val bar = 0
            pub fn baz() { 0 }
            pub val faz = 0
        """.trimIndent()
        compile(defCode, namespace)

        val code = """
            import mymod/mypkg { foo, bar, baz, faz }
        """.trimIndent()
        val result = analyze(compile(code, namespace, ignoreCompilationErrors = true))

        result shouldHaveSize 2
        result[0].shouldBeTypeOf<ImportInternal>()
            .symbolName shouldBe "foo"
        result[1].shouldBeTypeOf<ImportInternal>()
            .symbolName shouldBe "bar"
    }
})