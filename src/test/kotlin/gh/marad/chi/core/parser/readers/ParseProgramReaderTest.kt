package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.antlr.ChiLexer
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Test


class ParseProgramReaderTest {
    @Test
    fun `read program`() {
        val code = """
            package mod/pkg
            import othermod/otherpkg as oth
            val bar = 5
            fn foo() {}
            effect foo2()
            data Baz()
        """.trimIndent()
        val program = parseProgram(code)

        program.packageDefinition.shouldNotBeNull().should {
            it.moduleName shouldBe "mod"
            it.packageName shouldBe "pkg"
        }

        program.imports.should {
            it shouldHaveSize 1
            it[0].moduleName shouldBe "othermod"
            it[0].packageName shouldBe "otherpkg"
            it[0].packageAlias.shouldNotBeNull() shouldBe "oth"
            it[0].entries.shouldBeEmpty()
        }

        program.typeDefinitions.should {
            it shouldHaveSize 1
            it[0].typeName shouldBe "Baz"
        }

        program.code.should {
            it shouldHaveSize 3

            it[0].shouldBeTypeOf<ParseNameDeclaration>()
                .symbol.name shouldBe "bar"
            it[1].shouldBeTypeOf<ParseFuncWithName>()
            it[2].shouldBeTypeOf<ParseEffectDefinition>()
        }
    }
}

fun parseProgram(code: String): ParseProgram {
    val source = ChiSource(code)
    val charStream = CharStreams.fromString(source.code)

    val lexer = ChiLexer(charStream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = ChiParser(tokenStream)
    val visitor = ParserVisitor(source)
    return ProgramReader.read(visitor, source, parser.program())
}

