package gh.marad.chi.core

import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.antlr.ChiLexer
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.readers.ParseProgram
import gh.marad.chi.core.parser.readers.ProgramReader
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.DefaultErrorStrategy

internal fun parseSource(source: ChiSource): Pair<ParseProgram, List<Message>> {
    val errorListener = MessageCollectingErrorListener()
    val charStream = CharStreams.fromString(source.code)
    val lexer = ChiLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(errorListener)
    val tokenStream = CommonTokenStream(lexer)
    val parser = ChiParser(tokenStream)
    parser.errorHandler = DefaultErrorStrategy()
    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)
    val visitor = ParserVisitor(source)
    val program = ProgramReader.read(visitor, source, parser.program())
    return Pair(program, errorListener.getMessages())
}