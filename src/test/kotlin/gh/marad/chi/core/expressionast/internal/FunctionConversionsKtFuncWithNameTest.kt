package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Fn
import gh.marad.chi.core.NameDeclaration
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.FunctionType
import gh.marad.chi.core.types.TypeVariable
import gh.marad.chi.core.types.Types
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class FunctionConversionsKtFuncWithNameTest {

    @Test
    fun `named function should be converted to name declaration`() {
        // given
        val funcWithName = sampleFuncWithName.copy(
            public = true,
            name = "funcName",
            returnTypeRef = intTypeRef,
        )

        // when
        val result = convertAst(funcWithName)

        // then
        val nameDecl = result.shouldBeTypeOf<NameDeclaration>()
        nameDecl.public.shouldBeTrue()
        nameDecl.mutable.shouldBeFalse()
        nameDecl.name shouldBe "funcName"
        nameDecl.value.shouldBeTypeOf<Fn>()
    }

    @Test
    fun `when return type is not provided it is set to unit`() {
        // given
        val funcWithName = sampleFuncWithName.copy(
            returnTypeRef = null
        )

        // when
        val fn = convertAst(funcWithName)

        // then
        fn.shouldBeTypeOf<NameDeclaration>().should {
            it.expectedType shouldBe Types.fn(Types.unit)
            it.value.shouldBeTypeOf<Fn>()
        }
    }

    @Test
    fun `type parameters should be resolved in return type`() {
        // given
        val funcWithName = sampleFuncWithName.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            returnTypeRef = TypeNameRef(null, null, "T", sectionB)
        )

        // when
        val fn = convertAst(funcWithName)

        // then
        val T = TypeVariable("T")
        fn.shouldBeTypeOf<NameDeclaration>().should {
            it.value.shouldBeTypeOf<Fn>()
            it.expectedType shouldBe FunctionType(
                    listOf(T), listOf(T)
            )
        }

    }

    @Test
    fun `type parameters should be resolved in body`() {
        // given
        val funcWithName = sampleFuncWithName.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            body = ParseBlock(
                listOf(
                    sampleNameDeclaration.copy(
                        typeRef = TypeNameRef(null, null, "T", sectionB)
                    )
                ), testSection
            )
        )

        // when
        val fn = convertAst(funcWithName)

        // then
        fn.shouldBeTypeOf<NameDeclaration>().value
            .shouldBeTypeOf<Fn>().should {
                it.body.body.first().shouldBeTypeOf<NameDeclaration>()
                    .expectedType shouldBe TypeVariable("T")
            }
    }

    @Test
    fun `type parameters should be resolved in arguments`() {
        // given
        val funcWithName = sampleFuncWithName.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            formalArguments = listOf(FormalArgument("a", TypeNameRef(null, null, "T", sectionB), sectionC))
        )

        // when
        val fn = convertAst(funcWithName)
            .shouldBeTypeOf<NameDeclaration>().value
            .shouldBeTypeOf<Fn>()

        // then
        fn.parameters.first().type shouldBe TypeVariable("T")
    }


    private val sampleFuncWithName = ParseFuncWithName(
        public = true,
        name = "funcName",
        typeParameters = emptyList(),
        formalArguments = emptyList(),
        returnTypeRef = intTypeRef,
        body = ParseBlock(listOf(LongValue(10)), sectionA),
        testSection
    )

    private val sampleNameDeclaration = ParseNameDeclaration(
        public = false,
        mutable = false,
        symbol = Symbol("x", sectionA),
        typeRef = TypeNameRef(null, null, "int", sectionC),
        value = LongValue(10),
        sectionB
    )
}