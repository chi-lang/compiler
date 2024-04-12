@file:Suppress("unused")

package gh.marad.chi.core.analyzer

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.compile
import gh.marad.chi.core.namespace.GlobalCompilationNamespaceImpl
import gh.marad.chi.core.types.Type
import gh.marad.chi.messages
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
        val ns = GlobalCompilationNamespaceImpl()
        ns.addSymbolInDefaultPackage("x", Type.fn(Type.unit))
        messages("5", ns).shouldBeEmpty()
        messages("x", ns).shouldBeEmpty()
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
                error.expected shouldBe Type.int
                error.actual shouldBe Type.unit
            }
        }
    }

    @Test
    fun `should check that block return type matches what function expects`() {
        messages("fn foo(): int { {} }").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Type.int
                error.actual shouldBe Type.fn(Type.unit)
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
                error.expected shouldBe Type.int
                error.actual shouldBe Type.fn(Type.unit)
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
                error.expected shouldBe Type.int
                error.actual shouldBe Type.string
            }
        }
    }

    @Test
    fun `should accept return without any value`() {
        messages("""
            fn foo() {
                return
            }
        """.trimIndent()
        ).shouldHaveSize(0)
    }
}

class IfElseTypeCheckingSpec {
    @Test
    fun `if-else type is unit when branch types differ (or 'else' branch is missing)`() {
        messages("val x: unit = if(true) { 2 }").shouldBeEmpty()
        messages("val x: int = if(true) { 2 } else { 3 }").shouldBeEmpty()
        messages("val x: int = if(true) { 2 } else { {} }").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Type.int
                error.actual shouldBe Type.union(null, Type.int, Type.fn(Type.unit))
            }
        }
    }

    @Test
    fun `conditions should be boolean type`() {
        messages("if (1) { 2 }").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Type.bool
                error.actual shouldBe Type.int
            }
        }
    }
}

class PrefixOpSpec {
    @Test
    fun `should expect boolean type for '!' operator`() {
        messages("!true") shouldHaveSize 0
        messages("!1").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Type.bool
                error.actual shouldBe Type.int
            }
        }
    }
}

class WhileLoopSpec {
    @Test
    fun `condition should have boolean type`() {
        messages("while(true) {}") shouldHaveSize 0
        messages("while(1) {}").should {
            it.shouldHaveSize(1)
            it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                error.expected shouldBe Type.bool
                error.actual shouldBe Type.int
            }
        }
    }
}

class CastSpec {
    @Test
    fun `should cast values to different types`() {
        messages("val a: string = 5 as string").shouldBeEmpty()
    }

    @Test
    fun `cast should update name type in scope`() {
        // given
        val code = """
            type A = { a: int }
            type B = { b: float }
            val a = { a: 5 }
            a as B
            a.b
        """.trimIndent()

        // expect
        messages(code).shouldBeEmpty()
    }
}

class IsExprSpec {
    @Test
    fun `is expr should cooperate with if providing a scope`() {
        val code = """
            type A = { a: int }
            type B = { b: float }
            val a = { a: 5 }
            if (a is B) {
                a as B
                a.b
            }
        """.trimIndent()

        val errors = messages(code)

        errors.shouldBeEmpty()
    }

    @Test
    fun `should also work with imported types`() {
        val namespace = GlobalCompilationNamespaceImpl()
        val defCode = """
            package foo/bar
            type A = { a: int }
            type B = { b: float }
        """.trimIndent()
        compile(defCode, namespace)

        val code = """
            import foo/bar { A, B }
            val a = { a: 5 }
            if (a is B) {
                a as B
                a.b
            }
        """.trimIndent()
        compile(code, namespace)
    }

    @Test
    fun `should not allow importing variables and functions that are not public`() {
        val namespace = GlobalCompilationNamespaceImpl()
        val defCode = """
            package mymod/mypkg
            fn foo() { return }
            val bar = 0
            pub fn baz() { return }
            pub val faz = 0
        """.trimIndent()
        compile(defCode, namespace)

        val code = """
            import mymod/mypkg { foo, bar, baz, faz }
        """.trimIndent()
        val result = messages(code, namespace)

        result shouldHaveSize 2
        result[0].shouldBeTypeOf<CannotAccessInternalName>()
            .name shouldBe "foo"
        result[1].shouldBeTypeOf<CannotAccessInternalName>()
            .name shouldBe "bar"
    }
}