package gh.marad.chi.core

import gh.marad.chi.core.OldType.Companion.any
import gh.marad.chi.core.OldType.Companion.fn
import gh.marad.chi.core.OldType.Companion.int
import gh.marad.chi.core.OldType.Companion.string
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe

class FunctionOverloadingSpec : FunSpec({

    test("should choose function type with correct parameter types") {
        val overloadedFnType = OverloadedFnType(
            setOf(
                fn(int, int),
                fn(int, string)
            )
        )

        overloadedFnType.getType(listOf(int)) shouldBe fn(int, int)
        overloadedFnType.getType(listOf(string)) shouldBe fn(int, string)
    }

    test("should match parameter count and order") {
        val overloadedFnType = OverloadedFnType(
            setOf(
                fn(int, int),
                fn(int, int, string),
                fn(int, string, int)
            )
        )


        overloadedFnType.getType(listOf(int)) shouldBe fn(int, int)
        overloadedFnType.getType(listOf(int, string)) shouldBe fn(int, int, string)
        overloadedFnType.getType(listOf(string, int)) shouldBe fn(int, string, int)
    }

    test("should prefer more concrete types over any") {
        val overloadedFnType = OverloadedFnType(setOf(fn(int, any), fn(int, int), fn(int, string)))
        overloadedFnType.getType(listOf(int)) shouldBe fn(int, int)
    }

    test("should not find type if decision is not certain") {
        val overloadedFnType = OverloadedFnType(
            setOf(
                fn(int, any, int),
                fn(int, int, any),
            )
        )

        overloadedFnType.getType(listOf(int, int)).shouldBeNull()
    }
})