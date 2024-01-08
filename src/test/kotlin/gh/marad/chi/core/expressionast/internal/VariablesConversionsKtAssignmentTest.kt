package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.core.Assignment
import gh.marad.chi.core.FieldAssignment
import gh.marad.chi.core.IndexedAssignment
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.shouldBeAtom
import gh.marad.chi.core.types.Types
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class VariablesConversionsKtAssignmentTest {
    @Test
    fun `generate assignment`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("variable", Types.int)

        // when
        val result = convertAst(
            ParseAssignment(
                variableName = "variable",
                value = LongValue(10),
                section = testSection
            ),
            ns
        ).shouldBeTypeOf<Assignment>()

        // then
        result.name shouldBe "variable"
        result.value.shouldBeAtom("10", Types.int)
        result.symbol shouldBe ns.getDefaultPackage().symbols.get("variable")
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `generate indexed assignment`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("variable", Types.int)

        // when
        val result = convertAst(
            ParseIndexedAssignment(
                variable = ParseVariableRead("variable"),
                index = LongValue(10),
                value = StringValue("hello"),
                section = testSection
            ),
            ns
        ).shouldBeTypeOf<IndexedAssignment>()

        // then
        result.variable.shouldBeVariable("variable")
        result.index.shouldBeAtom("10", Types.int)
        result.value.shouldBeAtom("hello", Types.string)
        result.sourceSection shouldBe testSection
    }

    @Test
    fun `generate field assignment`() {
        // given
        val ns = GlobalCompilationNamespace()
        ns.addSymbolInDefaultPackage("object", Types.int)

        // when
        val result = convertAst(
            ParseFieldAssignment(
                receiverName = "object",
                receiver = ParseVariableRead("object"),
                memberName = "field",
                value = LongValue(10),
                section = testSection,
                memberSection = null
            ),
            ns
        ).shouldBeTypeOf<FieldAssignment>()

        // then
        result.receiver.shouldBeVariable("object")
        result.fieldName shouldBe "field"
    }
}