package gh.marad.chi

import gh.marad.chi.core.Fn
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.Types
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class LambdaSpec {

    @Test
    fun `should infer the argument types inside of lambda passed to a function`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("foo", Types.fn(Types.fn(Types.int, Types.int), Types.unit))

        // when
        val result = ast("foo({ a, b -> a + b })", ns)

        // then
        result.shouldBeTypeOf<FnCall>()
            .parameters.first().shouldBeTypeOf<Fn>()
            .should { fn ->
                fn.parameters.map { it.type } shouldBe listOf(Types.int, Types.int)
                fn.body.type shouldBe Types.int
            }
    }

}