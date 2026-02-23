package gh.marad.chi

import org.jline.reader.EOFError
import org.jline.reader.ParsedLine
import org.jline.reader.Parser
import org.jline.reader.impl.DefaultParser

class ChiReplParser : Parser {
    private val defaultParser = DefaultParser()

    override fun parse(line: String, cursor: Int, context: Parser.ParseContext): ParsedLine {
        if (context == Parser.ParseContext.ACCEPT_LINE) {
            val depth = computeBracketDepth(line)
            if (depth > 0) {
                throw EOFError(-1, cursor, "Unclosed bracket")
            }
        }
        return defaultParser.parse(line, cursor, context)
    }

    companion object {
        fun computeBracketDepth(input: String): Int {
            var depth = 0
            var inString = false
            var i = 0
            // Stack to track interpolation brace depth within strings.
            // When we encounter ${ inside a string, we push the current depth
            // and start counting braces. When the matching } is found (depth
            // returns to the pushed value), we re-enter string mode.
            val interpolationStack = ArrayDeque<Int>()

            while (i < input.length) {
                val c = input[i]

                if (inString) {
                    when {
                        c == '\\' -> {
                            // Skip escaped character
                            i += 2
                            continue
                        }
                        c == '$' && i + 1 < input.length && input[i + 1] == '{' -> {
                            // Enter string interpolation — real code inside
                            inString = false
                            interpolationStack.addLast(depth)
                            depth++
                            i += 2
                            continue
                        }
                        c == '"' -> {
                            inString = false
                        }
                    }
                } else {
                    when (c) {
                        '"' -> inString = true
                        '(', '{', '[' -> depth++
                        ')', ']' -> depth--
                        '}' -> {
                            depth--
                            // Check if we're returning from a string interpolation
                            if (interpolationStack.isNotEmpty() && depth == interpolationStack.last()) {
                                interpolationStack.removeLast()
                                inString = true
                            }
                        }
                    }
                }
                i++
            }
            return depth
        }
    }
}
