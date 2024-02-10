package gh.marad.chi.core.compiler

import gh.marad.chi.core.Program
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.Message

data class CompilationResult(
    val messages: List<Message>,
    val program: Program,
) {
    fun hasErrors(): Boolean = messages.any { it.level == Level.ERROR }
    fun errors() = messages.filter { it.level == Level.ERROR }
}