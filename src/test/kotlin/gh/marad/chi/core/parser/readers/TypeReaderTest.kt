package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.testParse
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class TypeReaderTest {
    @Test
    fun `parse function type reference`() {
        val code = "val x: (int, string) -> unit = 0"
        val ast = testParse(code)
        ast shouldHaveSize 1
        val typeRef = ast[0].shouldBeTypeOf<ParseNameDeclaration>()
            .typeRef.shouldBeTypeOf<FunctionTypeRef>()

        typeRef.argumentTypeRefs.map {
            it.shouldBeTypeOf<TypeNameRef>()
        }.map { it.typeName } shouldBe listOf("int", "string")

        typeRef.returnType.shouldBeTypeOf<TypeNameRef>()
            .typeName shouldBe "unit"

        typeRef.section?.getCode() shouldBe "(int, string) -> unit"
    }

    @Test
    fun `parse generic type reference`() {
        val code = "val x: HashMap[string, int] = 0"
        val ast = testParse(code)
        val typeRef = ast[0].shouldBeTypeOf<ParseNameDeclaration>()
            .typeRef.shouldBeTypeOf<TypeConstructorRef>()

        typeRef.baseType.shouldBeTypeOf<TypeNameRef>().typeName shouldBe "HashMap"
        typeRef.typeParameters.map { it.shouldBeTypeOf<TypeNameRef>() }
            .map { it.typeName } shouldBe listOf("string", "int")
        typeRef.section?.getCode() shouldBe "HashMap[string, int]"
    }

    @Test
    fun `parse sum type`() {
        val code = "val x: int | string = 0"
        val ast = testParse(code)
        val typeRef = ast[0].shouldBeTypeOf<ParseNameDeclaration>()
            .typeRef.shouldBeTypeOf<SumTypeRef>()

        typeRef.lhs.shouldBeTypeOf<TypeNameRef>().typeName shouldBe "int"
        typeRef.rhs.shouldBeTypeOf<TypeNameRef>().typeName shouldBe "string"
    }

    @Test
    fun `parse record type`() {
        val code = "val x: { x: int, y: float } = 0"
        val ast = testParse(code)
        val typeRef = ast[0].shouldBeTypeOf<ParseNameDeclaration>()
            .typeRef.shouldBeTypeOf<RecordTypeRef>()

        typeRef.fields[0].shouldBeTypeOf<RecordTypeRef.Field>()
            .typeRef.shouldBeTypeOf<TypeNameRef>().typeName shouldBe "int"
        typeRef.fields[1].shouldBeTypeOf<RecordTypeRef.Field>()
            .typeRef.shouldBeTypeOf<TypeNameRef>().typeName shouldBe "float"
    }

}