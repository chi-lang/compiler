package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.compiler.Symbol
import gh.marad.chi.core.compiler.SymbolKind
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.Types
import gh.marad.chi.core.types.Types.int
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class VariablesConversionsKtMethodInvocationSyntaxTest {
    @Test
    fun `conversion of simple method invocation`() {
        // given object of type int and method of type (int, int) -> int
        val ns = GlobalCompilationNamespace()
        ns.getDefaultPackage().symbols.apply {
            add(Symbol("user", "default", "object", SymbolKind.Local, int, 0, true, true))
            add(Symbol("user", "default", "method", SymbolKind.Local, Types.fn(int, int, int), 0, true, true))
        }

        // when
        val expr = convertAst(
            ParseMethodInvocation(
                receiverName = "object",
                methodName = "method",
                receiver = ParseVariableRead("object", null),
                concreteTypeParameters = emptyList(),
                arguments = listOf(LongValue(10, null)),
                memberSection = null,
                section = null
            ),
            ns
        )

        // then
        expr.shouldBeTypeOf<FnCall>() should { call ->
            call.function.shouldBeVariable("method")
            call.callTypeParameters shouldBe emptyList()
            call.parameters shouldHaveSize 2
            call.parameters[0].shouldBeVariable("object")
            call.parameters[1].shouldBeAtom("10", int)
        }
    }

    @Test
    fun `conversion of package function invocation`() {
        // given imported package foo/bar as pkg
        val namespace = GlobalCompilationNamespace()
        val ctx = ConversionContext(namespace)
        ctx.imports.addImport(Import("foo", "bar", "pkg", emptyList(), withinSameModule = true, null))

        // when
        val expr = convertMethodInvocation(
            ctx, ParseMethodInvocation(
                receiverName = "pkg",
                methodName = "func",
                receiver = ParseVariableRead("pkg", null),
                concreteTypeParameters = emptyList(),
                arguments = listOf(LongValue(10, null)),
                memberSection = null,
                section = null,
            )
        )

        // then
        expr.shouldBeTypeOf<FnCall>() should { call ->
            call.function.shouldBeTypeOf<VariableAccess>() should {
                it.name shouldBe "func"
                it.moduleName shouldBe "foo"
                it.packageName shouldBe "bar"
            }
            call.callTypeParameters shouldBe emptyList()
            call.parameters shouldHaveSize 1
            call.parameters[0].shouldBeAtom("10", int)
        }
    }

    @Test
    fun `conversion should find functions within the package the type was defined in`() {
        // given a type and simple function in other package
        val ctx = ConversionContext(GlobalCompilationNamespace())
        val testType = ctx.addTypeDefinition(
            moduleName = testModule,
            packageName = testPackage,
            typeName = "Test"
        )
        ctx.addPublicSymbol(
            moduleName = testModule,
            packageName = testPackage,
            "method", OldType.fn(OldType.int, testType)
        )

        ctx.addPublicSymbol("object", testType)

        // when
        val expr = convertMethodInvocation(
            ctx, ParseMethodInvocation(
                receiverName = "object",
                methodName = "method",
                receiver = ParseVariableRead("object", null),
                concreteTypeParameters = emptyList(),
                arguments = emptyList(),
                memberSection = null,
                section = null,
            )
        )

        // then
        expr.shouldBeTypeOf<FnCall>() should { call ->
            call.function.shouldBeTypeOf<VariableAccess>() should {
                it.moduleName shouldBe testType.moduleName
                it.packageName shouldBe testType.packageName
                it.name shouldBe "method"
            }
        }
    }
}

private val testModule = ModuleName("test.mod", null)
private val testPackage = PackageName("test.pkg", null)
