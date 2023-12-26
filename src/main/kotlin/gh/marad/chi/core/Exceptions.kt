package gh.marad.chi.core

import gh.marad.chi.core.analyzer.CodePoint
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.SyntaxError
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

class MessageCollectingErrorListener : BaseErrorListener() {
    private val messages = mutableListOf<Message>()

    fun getMessages(): List<Message> = messages

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        val point = CodePoint(line, charPositionInLine)

        messages.add(SyntaxError(offendingSymbol, msg, point))
    }

}