package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.EffectDefinition
import gh.marad.chi.core.OldType
import gh.marad.chi.core.Package
import gh.marad.chi.core.compiler.ExprConversionVisitor
import gh.marad.chi.core.compiler.SymbolTable
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.readers.ParseEffectDefinition
import gh.marad.chi.core.parser.readers.TypeNameRef
import gh.marad.chi.core.parser.readers.TypeParameterRef
import gh.marad.chi.core.types.FunctionType
import gh.marad.chi.core.types.TypeVariable
import gh.marad.chi.core.types.Types
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class EffectConversionsKtDefinitionTest {
    @Test
    fun `should be defined in current package`() {
        // given
        val context = defaultContext()

        // when
        val result = convertEffectDefinition(context, sampleEffectDefinition)
            .shouldBeTypeOf<EffectDefinition>()

        // then
        result.moduleName shouldBe context.currentModule
        result.packageName shouldBe context.currentPackage
        result.name shouldBe sampleEffectDefinition.name
    }

    @Test
    fun `type parameters should be resolved in arguments`() {
        // given
        val definition = sampleEffectDefinition.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            formalArguments = listOf(arg("t", typeName = "T"))
        )

        // when
        val result = convertEffectDefinition(defaultContext(), definition)
            .shouldBeTypeOf<EffectDefinition>()

        // then
        result.parameters shouldHaveSize 1
        result.parameters.first() should {
            it.name shouldBe "t"
            it.type shouldBe OldType.typeParameter("T")
        }
    }

    @Test
    fun `type prameters should be resolved in return type`() {
        // given
        val definition = sampleEffectDefinition.copy(
            typeParameters = listOf(TypeParameterRef("T", sectionA)),
            returnTypeRef = TypeNameRef("T", sectionB)
        )

        // when
        val ns = GlobalCompilationNamespace()
        val pkg = ns.getDefaultPackage()
        val result = ExprConversionVisitor(Package(pkg.moduleName, pkg.packageName), pkg.symbols, pkg.types)
            .visit(definition)
            .shouldBeTypeOf<EffectDefinition>()

        // then
        val T = TypeVariable("T")
        result.newType shouldBe FunctionType(
            listOf(T),
            listOf(T)
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