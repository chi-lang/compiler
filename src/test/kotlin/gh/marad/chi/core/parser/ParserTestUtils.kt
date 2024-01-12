package gh.marad.chi.core.parser

import gh.marad.chi.core.MessageCollectingErrorListener
import gh.marad.chi.core.antlr.ChiLexer
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.readers.*
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.DefaultErrorStrategy

fun parser(source: ChiSource): ChiParser {
    val charStream = CharStreams.fromString(source.code)

    val lexer = ChiLexer(charStream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = ChiParser(tokenStream)
    parser.errorHandler = DefaultErrorStrategy()
    parser.removeErrorListeners()
    var errorListener = MessageCollectingErrorListener()
    parser.addErrorListener(errorListener)
    return parser
}

fun testParse(code: String): List<ParseAst> {
    val source = ChiSource(code)
    val charStream = CharStreams.fromString(source.code)

    val lexer = ChiLexer(charStream)
    val tokenStream = CommonTokenStream(lexer)
    val parser = ChiParser(tokenStream)
    parser.errorHandler = DefaultErrorStrategy()
    parser.removeErrorListeners()
    var errorListener = MessageCollectingErrorListener()
    parser.addErrorListener(errorListener)
    val visitor = ParserVisitor(source)
    val block = visitor.visitProgram(parser.program()) as ParseBlock
    if (errorListener.getMessages().isNotEmpty()) {
        errorListener.getMessages().forEach {
            println(it)
        }
        throw AssertionError("There where compilation errors!")
    }
    return block.body
}

fun ParseAst.shouldBeStringValue(value: String) {
    this.shouldBeTypeOf<StringValue>().value shouldBe value
}

fun ParseAst.shouldBeLongValue(value: Int) {
    this.shouldBeTypeOf<LongValue>().value shouldBe value.toLong()
}

fun ParseAst.shouldBeVariable(variableName: String) {
    this.shouldBeTypeOf<ParseVariableRead>().variableName shouldBe variableName
}

fun TypeRef.shouldBeTypeNameRef(typeName: String) {
    this.shouldBeTypeOf<TypeNameRef>().typeName shouldBe typeName
}