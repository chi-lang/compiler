package gh.marad.chi.core.weaveexpr

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.internal.convertAst
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.parser.shouldBeStringValue
import gh.marad.chi.core.parser.testParse
import gh.marad.chi.core.types.Type
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class WeaveExprSpec {
    @Test
    fun `simple parsing weave with placeholder`() {
        val code = """
            "hello" ~> str.toUpper(_)
        """.trimIndent()
        val ast = testParse(code)

        ast[0].shouldBeTypeOf<ParseWeave>() should { weave ->
            weave.value.shouldBeStringValue("hello")
            weave.opTemplate.shouldBeTypeOf<ParseFnCall>() should { call ->
                call.arguments.first().shouldBeTypeOf<ParseWeavePlaceholder>()
            }
            weave.section?.getCode() shouldBe code
        }
    }

    @Test
    fun `parsing weave chain`() {
        val code = """
            "2hello" 
                ~> str.toUpper(_)
                ~> _[0] as int
                ~> 2 + _
        """.trimIndent()
        val ast = testParse(code)

        ast[0].shouldBeTypeOf<ParseWeave>() should { weave1 ->
            val weave2 = weave1.opTemplate.shouldBeTypeOf<ParseWeave>()
            val weave3 = weave2.opTemplate.shouldBeTypeOf<ParseWeave>()
            val op1 = weave2.value
            val op2 = weave3.value
            val op3 = weave3.opTemplate

            weave1.value.shouldBeStringValue("2hello")
            op1.shouldBeTypeOf<ParseFnCall>()
            op2.shouldBeTypeOf<ParseCast>()
            op3.shouldBeTypeOf<ParseBinaryOp>()
        }
    }

    @Test
    fun `converting to expression`() {
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("toUpper")
        val code = """
            "hello" ~> toUpper(_)
        """.trimIndent()
        val ast = testParse(code)
        val expr = convertAst(ast[0], ns)

        val body = expr.shouldBeTypeOf<Block>().body
        val tempVar = body[0].shouldBeTypeOf<NameDeclaration>()
        body[1].shouldBeTypeOf<FnCall>() should {
            it.parameters.first().shouldBeTypeOf<VariableAccess>()
                .target.name shouldBe tempVar.name
        }
    }

    @Test
    fun `converting chain to expressions`() {
        val ns = TestCompilationEnv()
        ns.addSymbolInDefaultPackage("toUpper")
        val code = """
            "2hello" 
                ~> toUpper(_)
                ~> _[0] as int
                ~> 2 + _
        """.trimIndent()
        val ast = testParse(code)
        val expr = convertAst(ast[0], ns)

        val body = expr.shouldBeTypeOf<Block>().body
        body[0].shouldBeTypeOf<NameDeclaration>()
            .value.shouldBeAtom("2hello", Type.string)
        val body2 = body[1].shouldBeTypeOf<Block>().body
        body2[0].shouldBeTypeOf<NameDeclaration>()
            .value.shouldBeTypeOf<FnCall>()
        val body3 = body2[1].shouldBeTypeOf<Block>().body
        body3[0].shouldBeTypeOf<NameDeclaration>()
            .value.shouldBeTypeOf<Cast>()
        body3[1].shouldBeTypeOf<InfixOp>()
    }
}