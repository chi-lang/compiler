package gh.marad.chi.core

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.ast
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
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
        ns.addSymbolInDefaultPackage("a", Types.fn(Types.int, Types.int))
        ns.addSymbolInDefaultPackage("b", Types.fn(Types.int, Types.int))
        ns.addSymbolInDefaultPackage("x", Types.int)
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