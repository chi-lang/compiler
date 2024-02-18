package gh.marad.chi.core

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.ast
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types3.Type3
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
                    fn.newType shouldBe Type3.fn(Type3.int)
                    fn.body.shouldBeBlock { block ->
                        block.body[0].shouldBeAtom("1", Type3.int)
                    }
                }
                it.parameters shouldBe emptyList()
            }
    }

    @Test
    fun `should read nested function invocations`() {
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("a", Type3.fn(Type3.int, Type3.int))
        ns.addSymbolInDefaultPackage("b", Type3.fn(Type3.int, Type3.int))
        ns.addSymbolInDefaultPackage("x", Type3.int)
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
                                it.target.name.shouldBe("x")
                            }
                    }
            }
    }
}