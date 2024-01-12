package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.shouldBeTypeNameRef
import gh.marad.chi.core.parser.testParse
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class TraitReaderTest {
    fun `reading basic trait definition`() {
        val ast = testParse("""
            trait Greeter {
                fn hello(name: string): bool
            }
        """.trimIndent())


        ast shouldHaveSize 1
        val traitDef = ast[0].shouldBeTypeOf<ParseTraitDefinition>()
        traitDef.name shouldBe "Greeter"
        traitDef.typeParameters.shouldBeEmpty()
        traitDef.functions shouldHaveSize 1

        val func = traitDef.functions[0].shouldBeTypeOf<ParseTraitFunctionDefinition>()
        func.name shouldBe "hello"
        func.returnTypeRef.shouldBeTypeNameRef("bool")
        func.formalArguments shouldHaveSize 1
        func.formalArguments.first().should {
            it.name shouldBe "name"
            it.typeRef.shouldBeTypeNameRef("string")
        }
    }

    fun `read generic type parameters`() {
        val ast = testParse("""
            trait Iterable[T] {
                fn iterator(self: Self[T]): Iterator[T]
            }
        """.trimIndent())

        ast shouldHaveSize 1
        val traitDef = ast[0].shouldBeTypeOf<ParseTraitDefinition>()
        traitDef.typeParameters.first().should {
            it.name shouldBe "T"
        }

        traitDef.functions.first().should {
            val returnTypeRef = it.returnTypeRef.shouldBeTypeOf<TypeConstructorRef>()
            returnTypeRef.baseType.shouldBeTypeNameRef("Iterator")
            returnTypeRef.typeParameters.first().shouldBeTypeNameRef("T")
        }
    }
}