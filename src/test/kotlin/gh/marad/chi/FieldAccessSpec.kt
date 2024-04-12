package gh.marad.chi

import gh.marad.chi.core.*
import gh.marad.chi.core.namespace.GlobalCompilationNamespaceImpl
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FieldAccessSpec {
    @Test
    fun `should call function from package that type is declared in`() {
        // given
        val ns = GlobalCompilationNamespaceImpl()

        val type = Type.record(TypeId("mod", "pkg", "Type"))
        ns.addTypeDefinition(type)
        ns.addSymbol("mod", "pkg", "bar", Type.fn(type, Type.int), public = true)
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
            it.type shouldBe Type.int
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
            it.type shouldBe Type.float
            it.function.shouldBeTypeOf<VariableAccess>()
                .target shouldBe LocalSymbol("bar")
        }
    }

    @Test
    fun `should work with multiple chained function calls`() {
        // given
        val ns = GlobalCompilationNamespaceImpl()
        val type = Type.record(TypeId("mod", "pkg", "Type"))
        ns.addTypeDefinition(type)
        ns.addSymbol("mod", "pkg", "bar", Type.fn(type, Type.int), public = true)
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
            bazCall.type shouldBe Type.float
            bazCall.function.shouldBeTypeOf<VariableAccess>()
                .target shouldBe LocalSymbol("baz")

            bazCall.parameters[0].shouldBeTypeOf<FnCall>().should { barCall ->
                barCall.type shouldBe Type.int
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

    @Test
    fun `should prefer reading field value over any function`() {
        // given
        val ns = GlobalCompilationNamespaceImpl()
        val type = Type.record(TypeId("mod", "pkg", "Type"), "bar" to Type.string)
        ns.addTypeDefinition(type)
        ns.addSymbol("mod", "pkg", "Type", Type.fn(Type.string, type), public = true)
        ns.addSymbol("mod", "pkg", "bar", Type.fn(type, Type.int), public = true)

        // when
        val result = ast(
            """
                import mod/pkg { Type }
                fn bar(t: Type): float { 5.0 }
                Type("hello").bar
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<FieldAccess>().should {
            it.target shouldBe DotTarget.Field
            it.type shouldBe Type.string
        }
    }

    @Test
    fun `should prefer local function over function from receiver types package`() {
        // given
        val ns = GlobalCompilationNamespaceImpl()
        val type = Type.record(TypeId("mod", "pkg", "Type"))
        ns.addTypeDefinition(type)
        ns.addSymbol("mod", "pkg", "bar", Type.fn(type, Type.int), public = true)
        ns.addSymbolInDefaultPackage("foo", type)

        // when
        val result = ast(
            """
                import mod/pkg { Type }
                fn bar(t: Type): float { 5.0 }
                foo.bar
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<FieldAccess>().should {
            it.target shouldBe DotTarget.LocalFunction
            it.type shouldBe Type.fn(type, Type.float)
        }
    }

    @Test
    fun `should read function from aliased package`() {
        // given
        val ns = GlobalCompilationNamespaceImpl()
        ns.addSymbol("mod", "pack", "foo", Type.int, public = true)

        // when
        val result = ast(
            """
            import mod/pack as pkg
            pkg.foo  
            """.trimIndent(),
            ns
        )

        // then
        result.shouldBeTypeOf<VariableAccess>()
            .target.shouldBeTypeOf<PackageSymbol>()
            .should { symbol ->
                symbol.moduleName shouldBe "mod"
                symbol.packageName shouldBe "pack"
                symbol.name shouldBe "foo"
            }
    }
}