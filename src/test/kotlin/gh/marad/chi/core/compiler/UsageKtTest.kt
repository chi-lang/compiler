package gh.marad.chi.core.compiler

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.addTypeDefinition
import gh.marad.chi.ast
import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows


class UsageKtTest {
    @Test
    fun `used block should mark last expression as used`() {
        // given: if-else assigned to variable - both branches are used blocks
        val expr = ast(
            """
                val x = if (true) {
                    5
                    "hello"
                } else {
                    "world"
                }
            """.trimIndent()
        ).shouldBeTypeOf<NameDeclaration>()

        // when
        val ifElse = expr.value.shouldBeTypeOf<IfElse>()
        val thenBlock = ifElse.thenBranch.shouldBeTypeOf<Block>()
        val elseBlock = ifElse.elseBranch.shouldBeTypeOf<Block>()

        // then: branches are used, so last expression in each branch should be used
        thenBlock.used shouldBe true
        thenBlock.body[0].used shouldBe false
        thenBlock.body[1].used shouldBe true
        elseBlock.used shouldBe true
        elseBlock.body.last().used shouldBe true
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
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("x", Type.int, mutable = true)
        val expr = ast("x = 5", ns).shouldBeTypeOf<Assignment>()

        // then
        expr.value.used.shouldBeTrue()
    }

    @Test
    fun `field assignment should mark value as read`() {
        // given
        val ns = TestCompilationEnv()
        val type = Type.record(
            TypeId("mod", "pkg", "Foo"),
            "bar" to Type.int
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
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("func", Type.fn(Type.int, Type.bool, Type.unit))
        val expr = ast("func(5, true)", ns).shouldBeTypeOf<FnCall>()

        // then
        expr.parameters.forEach { it.used.shouldBeTrue() }
    }

    @Test
    fun `index operator should mark index as used`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("x", Type.array(Type.int))
        val expr = ast("x[5]", ns).shouldBeTypeOf<IndexOperator>()

        // then
        expr.index.used.shouldBeTrue()
    }

    @Test
    fun `indexed assignment should mark index and value as used`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("x", Type.array(Type.int))
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

    // BUG-12 regression: visitBlock should propagate block.used, not unconditionally set true
    @Test
    fun `unused block should not mark last expression as used`() {
        // given: a bare lambda `{ ... }` parses as Fn whose body Block has used=false
        val expr = ast(
            """
                {
                   5
                   "hello"
                }
            """.trimIndent()
        ).shouldBeTypeOf<Fn>()

        // then: the Fn body block is not used, so last expression should also be not used
        expr.body.used shouldBe false
        expr.body.body[0].used shouldBe false
        expr.body.body[1].used shouldBe false  // BUG: currently true due to unconditional assignment
    }

    @Test
    fun `empty block should not error`() {
        // given: if-else with empty blocks should not crash in visitBlock
        assertDoesNotThrow {
            ast("val x = if (true) {} else {}").shouldBeTypeOf<NameDeclaration>()
        }
    }

    @Test
    fun `is check on type variable in unused block should compile without error`() {
        // given: function body block is not used, so 'x is T' (last expr) gets used=false
        // visitIs should NOT throw because the result is not consumed
        assertDoesNotThrow {
            ast("fn foo[T](x: T): bool { x is T }")
        }
    }

    @Test
    fun `is check on type variable in used context should still error`() {
        // given: 'x is T' assigned to a variable, so Is.used=true
        // visitIs should throw CompilerMessage because type variable check result is consumed
        assertThrows<CompilerMessage> {
            ast("fn foo[T](x: T): unit { val y: bool = x is T }")
        }
    }
}