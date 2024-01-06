package gh.marad.chi.core.types

import gh.marad.chi.core.parser.ChiSource
import java.lang.RuntimeException

class TypeInferenceFailed(
    message: String,
    val section: ChiSource.Section?
) : RuntimeException(message + if (section != null) " at $section" else "")