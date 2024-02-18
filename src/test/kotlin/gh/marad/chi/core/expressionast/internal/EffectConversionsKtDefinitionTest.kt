package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.EffectDefinition
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.readers.ParseEffectDefinition
import gh.marad.chi.core.parser.readers.TypeNameRef
import gh.marad.chi.core.parser.readers.TypeParameterRef
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Variable
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class EffectConversionsKtDefinitionTest {
    @Test
    fun `should be defined in current package`() {
        // given
        val ns = GlobalCompilationNamespace()

        // when
        val result = convertAst(sampleEffectDefinition, ns)


        // then
        result.shouldBeTypeOf<EffectDefinition>()
        result.moduleName shouldBe ns.getDefaultPackage().moduleName
        result.packageName shouldBe ns.getDefaultPackage().packageName
        result.name shouldBe sampleEffectDefinition.name
    }

    @Test
    fun `type parameters should be resolved in arguments`() {
        // given
        val ns = GlobalCompilationNamespace()
        val definition = sampleEffectDefinition.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            formalArguments = listOf(arg("t", typeName = "T"))
        )

        // when
        val result = convertAst(definition, ns)
            .shouldBeTypeOf<EffectDefinition>()

        // then
        result.parameters shouldHaveSize 1
        result.parameters.first() should {
            it.name shouldBe "t"
            it.type shouldBe Variable("T", 0)
        }
    }

    @Test
    fun `type prameters should be resolved in return type`() {
        // given
        val ns = GlobalCompilationNamespace()
        val definition = sampleEffectDefinition.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            returnTypeRef = TypeNameRef(null, null, "T", sectionB)
        )

        // when
        val result = convertAst(definition, ns)
            .shouldBeTypeOf<EffectDefinition>()

        // then
        val T = Variable("T", 0)
        result.newType shouldBe Function(
            listOf(T),
        )
    }

    private val sampleEffectDefinition = ParseEffectDefinition(
        public = true,
        name = "effectName",
        typeParameters = emptyList(),
        formalArguments = emptyList(),
        returnTypeRef = intTypeRef,
        testSection
    )
}