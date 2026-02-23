package gh.marad.chi

import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

class ChiReplCompleter(
    dotCommands: Collection<String>
) : Completer {

    private val dotCommandList = dotCommands.sorted()

    override fun complete(reader: LineReader, line: ParsedLine, candidates: MutableList<Candidate>) {
        completeCandidates(line.word(), candidates)
    }

    fun completeCandidates(word: String, candidates: MutableList<Candidate>) {
        if (word.startsWith(".")) {
            dotCommandList
                .filter { it.startsWith(word) }
                .forEach { candidates.add(Candidate(it)) }
        } else {
            keywords
                .filter { it.startsWith(word) }
                .forEach { candidates.add(Candidate(it)) }
        }
    }

    companion object {
        val keywords = listOf(
            "pub", "val", "var", "fn", "if", "else", "as",
            "while", "for", "in", "package", "import", "data",
            "when", "match", "is", "break", "continue",
            "effect", "handle", "with", "return",
            "trait", "type", "unit", "true", "false"
        ).sorted()
    }
}
