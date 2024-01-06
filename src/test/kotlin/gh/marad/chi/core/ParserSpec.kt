package gh.marad.chi.core

import gh.marad.chi.ast
import gh.marad.chi.core.compiler.Symbol
import gh.marad.chi.core.compiler.SymbolKind
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

@Suppress("unused")
class ParserSpec {

    @Test
    fun `should read lambda function invocation expression`() {
        ast("{ 1 }()")
            .shouldBeTypeOf<FnCall>()
            .should {
                it.function.shouldBeTypeOf<Fn>().should { fn ->
                    fn.parameters shouldBe emptyList()
                    fn.newType shouldBe Types.fn(Types.int)
                    fn.body.shouldBeBlock { block ->
                        block.body[0].shouldBeAtom("1", Types.int)
                    }
                }
                it.parameters shouldBe emptyList()
            }
    }

    @Test
    fun `should read nested function invocations`() {
        val ns = GlobalCompilationNamespace()
        ns.getDefaultPackage().symbols.apply {
            add(Symbol("user", "default", "a", SymbolKind.Local, Types.fn(Types.int, Types.int), 0, true, true))
            add(Symbol("user", "default", "b", SymbolKind.Local, Types.fn(Types.int, Types.int), 0, true, true))
            add(Symbol("user", "default", "x", SymbolKind.Local, Types.int, 0, true, true))
        }
        ast("a(b(x))", ns)
            .shouldBeTypeOf<FnCall>()
            .should { aFnCall ->
                aFnCall.parameters
                    .shouldHaveSize(1)
                    .first()
                    .shouldBeTypeOf<FnCall>().should { bFnCall ->
                        bFnCall.parameters
                            .shouldHaveSize(1)
                            .first()
                            .shouldBeTypeOf<VariableAccess>()
                            .should {
                                it.name.shouldBe("x")
                            }
                    }
            }
    }
}