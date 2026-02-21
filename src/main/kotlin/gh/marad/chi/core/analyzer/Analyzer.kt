package gh.marad.chi.core.analyzer

import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable

enum class Level { ERROR }

data class CodePoint(val line: Int, val column: Int) {
    override fun toString(): String = "$line:$column"
}

fun ChiSource.Section?.toCodePoint(): CodePoint? =
    this?.let { CodePoint(startLine, startColumn) }

sealed interface Message {
    val level: Level
    val message: String
    val codePoint: CodePoint?
}

data class ErrorMessage(val msg: String, override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String
        get() = run {
            val sb = StringBuilder()
            sb.append(msg)
            if (codePoint != null) {
                sb.append(" at ")
                sb.append(codePoint)
            }
            sb.toString()
        }
}

data class InvalidModuleName(val moduleName: String, override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String = "Invalid module name '$moduleName' at $codePoint"
}

data class InvalidPackageName(val packageName: String, override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String = "Invalid package name '$packageName' at $codePoint"
}

data class SyntaxError(val offendingSymbol: Any?, val msg: String?, override val codePoint: CodePoint?) :
    Message {
    override val level: Level = Level.ERROR
    override val message: String =
        "Syntax error at $codePoint.${if (msg != null) "Error: $msg" else ""}"
}

data class TypeMismatch(val expected: Type, val actual: Type, override val codePoint: CodePoint?) :
    Message {
    override val level = Level.ERROR
    override val message =
        "Expected type is '$expected' but got '$actual' at $codePoint"
}

data class NotAFunction(override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String = "This is not a function $codePoint."
}

data class FunctionArityError(
    val expectedCount: Int,
    val actualCount: Int,
    override val codePoint: CodePoint?
) :
    Message {
    override val level: Level = Level.ERROR
    override val message: String =
        "Function requires $expectedCount parameters, but was called with $actualCount at $codePoint"
}

data class UnrecognizedName(val name: String, override val codePoint: CodePoint?) : Message {
    override val level = Level.ERROR
    override val message = "Name '$name' was not recognized at $codePoint"
}

data class CannotAccessInternalName(val name: String, override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String
        get() = "$name is not public and is not from this module"
}

data class TypeIsNotIndexable(val type: Type, override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String = "Type '$type' is cannot be indexed"
}

data class CannotChangeImmutableVariable(override val codePoint: CodePoint?) : Message {
    override val level: Level = Level.ERROR
    override val message: String = "Cannot change immutable variable"
}

data class MemberDoesNotExist(val type: Type, val member: String, override val codePoint: CodePoint?) :
    Message {
    override val level: Level = Level.ERROR
    override val message: String
        get() = "Type $type does not have field '$member'. If this should be a function call then consider adding explicit type."
}

data class InfiniteType(val variable: Variable, val type: Type, override val codePoint: CodePoint?) :
    Message {
    override val level: Level = Level.ERROR
    override val message: String =
        "Infinite type: cannot construct the infinite type '$variable' = '$type' at $codePoint"
}

