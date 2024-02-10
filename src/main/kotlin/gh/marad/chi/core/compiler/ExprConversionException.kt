package gh.marad.chi.core.compiler

import gh.marad.chi.core.parser.ChiSource

class ExprConversionException(info: String, val section: ChiSource.Section?)
    : RuntimeException("AST conversion failed. This is a compiler error, sorry! :(\n" +
        "Error occurred at $section: $info"
    )