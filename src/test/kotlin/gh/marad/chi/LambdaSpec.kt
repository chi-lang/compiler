package gh.marad.chi

import gh.marad.chi.core.Fn
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types3.Type3
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class LambdaSpec {

    @Test
    fun `should infer the argument types inside of lambda passed to a function`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("foo", Type3.fn(Type3.fn(Type3.int, Type3.int, Type3.int), Type3.unit))

        // when
        val result = ast("foo({ a, b -> a + b })", ns)

        // then
        result.shouldBeTypeOf<FnCall>()
            .parameters.first().shouldBeTypeOf<Fn>()
            .should { fn ->
                fn.body.newType shouldBe Type3.int
            }
    }
}