package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.NameDeclaration
import gh.marad.chi.core.parser.readers.LongValue
import gh.marad.chi.core.parser.readers.ParseNameDeclaration
import gh.marad.chi.core.parser.readers.Symbol
import gh.marad.chi.core.parser.readers.TypeNameRef
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types3.Type3
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class VariablesConversionsKtNameDeclarationTest {
    @Test
    fun `generate name declaration`() {
        val result = convertAst(
            ParseNameDeclaration(
                public = true,
                mutable = false,
                symbol = Symbol("variable", sectionA),
                typeRef = TypeNameRef(null, null, "int", sectionB),
                value = LongValue(10),
                section = sectionC
            )
        ).shouldBeTypeOf<NameDeclaration>()

        result.public.shouldBeTrue()
        result.mutable.shouldBeFalse()
        result.name shouldBe "variable"
        result.value.shouldBeAtom("10", Type3.int)
        result.expectedType shouldBe Type3.int
        result.sourceSection shouldBe sectionC
    }
}