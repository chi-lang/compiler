package gh.marad.chi.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AutocastingSpec : FunSpec({
    test("should allow upcasting numbers") {
        OldType.intType.canCastTo(OldType.floatType) shouldBe true
    }

    test("canCastTo should not allow downcasting") {
        OldType.floatType.canCastTo(OldType.intType) shouldBe false
    }

    test("should allow downcasting numbers") {
        OldType.floatType.canDowncastTo(OldType.intType) shouldBe true
    }
})