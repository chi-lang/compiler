package gh.marad.chi.core.analyzer

import gh.marad.chi.core.parser.ChiSource

class CompilerMessage(val msg: Message) : RuntimeException(msg.message) {
    companion object {
        fun from(msg: String) = CompilerMessage(ErrorMessage(msg, null))
        fun from(msg: String, section: ChiSource.Section?) = CompilerMessage(ErrorMessage(msg, section.toCodePoint()))
    }
}
