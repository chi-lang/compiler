package gh.marad.chi

import gh.marad.chi.core.Fn
import gh.marad.chi.core.FnCall
import gh.marad.chi.core.namespace.GlobalCompilationNamespaceImpl
import gh.marad.chi.core.types.Type
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class LambdaSpec {

    @Test
    fun `should infer the argument types inside of lambda passed to a function`() {
        // given
        val ns = GlobalCompilationNamespaceImpl()
        ns.addSymbolInDefaultPackage("foo", Type.fn(Type.fn(Type.int, Type.int, Type.int), Type.unit))

        // when
        val result = ast("foo({ a, b -> a + b })", ns)

        // then
        result.shouldBeTypeOf<FnCall>()
            .parameters.first().shouldBeTypeOf<Fn>()
            .should { fn ->
                fn.body.type shouldBe Type.int
            }
    }
}