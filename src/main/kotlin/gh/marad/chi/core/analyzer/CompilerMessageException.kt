package gh.marad.chi.core.analyzer

class CompilerMessageException(val msg: Message)
    : RuntimeException(msg.message)