package gh.marad.chi.core

import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.antlr.ChiLexer
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.expressionast.generateExpressionsFromParsedProgram
import gh.marad.chi.core.expressionast.internal.convertImportDefinition
import gh.marad.chi.core.expressionast.internal.convertPackageDefinition
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.readers.ProgramReader
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.DefaultErrorStrategy

internal fun parseProgram(source: String, namespace: GlobalCompilationNamespace): Pair<Program, List<Message>> {
    val errorListener = MessageCollectingErrorListener()
    val charStream = CharStreams.fromString(source)
    val lexer = ChiLexer(charStream)
    lexer.removeErrorListeners()
    lexer.addErrorListener(errorListener)
    val tokenStream = CommonTokenStream(lexer)
    val parser = ChiParser(tokenStream)
    parser.errorHandler = DefaultErrorStrategy()
    parser.removeErrorListeners()
    parser.addErrorListener(errorListener)
    val chiSource = ChiSource(source)
    val visitor = ParserVisitor(chiSource)
    val parseResult = parser.program()
    val program = if (errorListener.getMessages().isNotEmpty()) {
        Program(null, emptyList(), emptyList())
    } else {
        val parsedProgram = ProgramReader.read(visitor, chiSource, parseResult)
        generateExpressionsFromParsedProgram(parsedProgram, namespace)
    }
    return Pair(
        program,
        errorListener.getMessages()
    )
}
