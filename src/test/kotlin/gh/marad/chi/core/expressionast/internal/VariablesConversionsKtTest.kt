package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.addSymbol
import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.addTypeDefinition
import gh.marad.chi.ast
import gh.marad.chi.core.*
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.parser.readers.LongValue
import gh.marad.chi.core.parser.readers.ParseFieldAccess
import gh.marad.chi.core.parser.readers.ParseIndexOperator
import gh.marad.chi.core.parser.readers.ParseVariableRead
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test


class VariablesConversionsKtTest {
    @Test
    fun `convert local variable read`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("variable", null)

        // when
        convertAst(ParseVariableRead("variable", testSection), ns)
            .shouldBeTypeOf<VariableAccess>()
            .target.shouldBeTypeOf<PackageSymbol>()
            .should {
                it.name shouldBe "variable"
                it.moduleName shouldBe CompilationDefaults.defaultModule
                it.packageName shouldBe CompilationDefaults.defaultPacakge
            }
    }

    @Test
    fun `convert variable read from another package in the same module`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbol("foo", "bar", "variable", Type.int, public = true)

        // when
        val result = ast("""
            import foo/bar { variable }
            variable
        """.trimIndent(), ns).shouldBeTypeOf<VariableAccess>()

        // then
        result.target.shouldBeTypeOf<PackageSymbol>().should {
            it.moduleName shouldBe "foo"
            it.packageName shouldBe "bar"
            it.name shouldBe "variable"
        }
    }

    @Test
    fun `generate index operator`() {
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("variable")
        val result = convertAst(
            ParseIndexOperator(
                variable = ParseVariableRead("variable"),
                index = LongValue(10),
                section = testSection
            ),
            ns
        ).shouldBeTypeOf<IndexOperator>()

        result.variable.shouldBeVariable("variable")
        result.index.shouldBeAtom("10", Type.int)
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `should generate variable access through package name`() {
        // given
        val ns = TestCompilationEnv()
        ns.addSymbol("foo", "bar", "variable", Type.int, public = true)

        // when
        val result = ast("""
            import foo/bar as bar
            bar.variable
        """.trimIndent(), ns).shouldBeTypeOf<VariableAccess>()


        // then
        result.target.shouldBeTypeOf<PackageSymbol>() should {
            it.name shouldBe "variable"
            it.moduleName shouldBe "foo"
            it.packageName shouldBe "bar"
        }
    }

    @Test
    fun `should generate field access`() {
        // given
        val ns = TestCompilationEnv()
        val type = Type.record(
            TypeId("mod", "pkg", "A"),
            "field" to Type.int
        )
        ns.addTypeDefinition(type)
        ns.addSymbolInDefaultPackage("object", type)

        // when
        val result = convertAst(
            sampleFieldAccess.copy(
                receiver = ParseVariableRead("object"),
                memberName = "field"
            ),
            ns
        )

        // then
        result.shouldBeTypeOf<FieldAccess>() should {
            it.receiver.shouldBeVariable("object")
            it.fieldName shouldBe "field"
            it.memberSection shouldBe sectionA
        }
    }

    @Test
    fun `should generate field access with type defined in other module`() {
        // given
        val ns = TestCompilationEnv()
        val type = Type.record(
            TypeId("mod", "pkg", "A"),
            "field" to Type.int
        )
        ns.addTypeDefinition(type)
        ns.addSymbolInDefaultPackage("object", type)

        // when
        val result = convertAst(
            sampleFieldAccess.copy(
                receiver = ParseVariableRead("object"),
                memberName = "field"
            ),
            ns
        )

        // then
        result.shouldBeTypeOf<FieldAccess>()
    }

    private val sampleFieldAccess = ParseFieldAccess(
        receiverName = "object",
        receiver = ParseVariableRead("object"),
        memberName = "field",
        memberSection = sectionA,
        section = sectionB
    )

}