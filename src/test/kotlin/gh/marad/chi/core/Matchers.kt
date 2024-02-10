package gh.marad.chi.core

import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.Type
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

fun Expression.shouldBeAtom(value: String, type: Type, sourceSection: ChiSource.Section? = null): Atom =
    shouldBeTypeOf<Atom>().also {
        should {
            it.value shouldBe value
            it.type shouldBe type
            if (sourceSection != null) {
                it.sourceSection shouldBe sourceSection
            }
        }
    }

fun Expression.shouldBeBlock(matcher: (Block) -> Unit) =
    shouldBeTypeOf<Block>().should(matcher)