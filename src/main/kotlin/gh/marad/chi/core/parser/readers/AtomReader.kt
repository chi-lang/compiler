package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.antlr.ChiLexer
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import gh.marad.chi.core.parser.mergeSections
import gh.marad.chi.core.parser.visitor.ParseAstVisitor
import org.antlr.v4.runtime.tree.TerminalNode

internal object AtomReader {
    fun readTerminal(source: ChiSource, node: TerminalNode): ParseAst? {
        val section = getSection(source, node.symbol, node.symbol)
        return when (node.symbol.type) {
            ChiLexer.NUMBER -> readNumber(source, node)
            ChiLexer.NEWLINE -> null
            ChiLexer.TRUE -> BoolValue(true, section)
            ChiLexer.FALSE -> BoolValue(false, section)
            ChiLexer.ID -> VariableReader.readVariable(source, node)
            ChiLexer.PLACEHOLDER -> ParseWeavePlaceholder(section)
            ChiLexer.UNIT -> UnitValue(section)
            else -> throw CompilerMessage.from(
                "Unsupported terminal type ${node.symbol.type}: '${node.symbol.text}'",
                getSection(source, node.symbol))
        }
    }

    private fun readNumber(source: ChiSource, node: TerminalNode): ParseAst =
        if (node.text.contains(".")) {
            FloatValue(node.text.toFloat(), getSection(source, node.symbol, node.symbol))
        } else {
            LongValue(node.text.toLong(), getSection(source, node.symbol, node.symbol))
        }

    fun readString(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.StringContext): ParseAst {

        var sb = StringBuilder()
        val parts = mutableListOf<StringPart>()
        var currentSection: ChiSource.Section? = null

        fun updateSection(ctx: ChiParser.StringPartContext) {
            val lastSection = currentSection
            val section = getSection(source, ctx)
            currentSection =
                if (lastSection == null) section
                else mergeSections(lastSection, section)
        }

        fun appendTextBeingBuilt() {
            parts.add(StringText(sb.toString(), currentSection))
            sb = StringBuilder()
            currentSection = null
        }

        ctx.stringPart().forEach { part ->
            updateSection(part)
            when {
                part.ID_INTERP() != null -> {
                    val idTerminal = part.ID_INTERP()
                    val variableName = idTerminal.text.drop(1) // drop the  '$' sign at the beginning
                    val value =
                        ParseVariableRead(variableName, getSection(source, idTerminal.symbol, idTerminal.symbol))

                    appendTextBeingBuilt()
                    parts.add(ParseInterpolation(value, getSection(source, part)))
                }
                part.ENTER_EXPR() != null -> {
                    val value = part.expression().accept(parser)
                    appendTextBeingBuilt()
                    parts.add(ParseInterpolation(value, getSection(source, part)))
                }
                part.TEXT() != null -> sb.append(part.TEXT().text)
                part.ESCAPED_DOLLAR() != null -> sb.append("$")
                part.ESCAPED_QUOTE() != null -> sb.append("\"")
                part.ESCAPED_NEWLINE() != null -> sb.append("\n")
                part.ESCAPED_CR() != null -> sb.append("\r")
                part.ESCAPED_SLASH() != null -> sb.append("\\")
                part.ESCAPED_TAB() != null -> sb.append("\t")
                else -> CompilerMessage.from("Unsupported string part: $part!", getSection(source, ctx))
            }
        }

        if (sb.isNotEmpty()) {
            parts.add(StringText(sb.toString(), currentSection))
        }

        val withoutEmptyParts = parts.filter { !(it is StringText && it.text.isEmpty()) }

        val singlePart = withoutEmptyParts.singleOrNull()
        return when {
            parts.isEmpty() -> StringValue("", getSection(source, ctx))
            singlePart != null && singlePart is StringText -> {
                StringValue(singlePart.text, getSection(source, ctx))
            }
            else -> {
                ParseInterpolatedString(withoutEmptyParts, getSection(source, ctx))
            }
        }
    }
}

data class UnitValue(override val section: ChiSource.Section?) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitUnit(this)

    override fun children(): List<ParseAst> = emptyList()
}

data class LongValue(val value: Long, override val section: ChiSource.Section? = null) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitLong(this)
    override fun children(): List<ParseAst> = emptyList()
}

data class FloatValue(val value: Float, override val section: ChiSource.Section? = null) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitFloat(this)
    override fun children(): List<ParseAst> = emptyList()
}

data class BoolValue(val value: Boolean, override val section: ChiSource.Section? = null) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitBool(this)
    override fun children(): List<ParseAst> = emptyList()
}

data class StringValue(val value: String, override val section: ChiSource.Section? = null) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitString(this)
    override fun children(): List<ParseAst> = emptyList()
}

sealed interface StringPart : ParseAst
data class StringText(val text: String, override val section: ChiSource.Section?) : StringPart {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitInterpolatedStringText(this)
    override fun children(): List<ParseAst> = emptyList()
}

data class ParseInterpolation(val value: ParseAst, override val section: ChiSource.Section?) : StringPart {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitStringInterpolation(this)
    override fun children(): List<ParseAst> = listOf(value)
}

data class ParseInterpolatedString(
    val parts: List<StringPart>,
    override val section: ChiSource.Section?
) : ParseAst {
    override fun <T> accept(visitor: ParseAstVisitor<T>): T = visitor.visitInterpolatedString(this)
    override fun children(): List<ParseAst> = parts
}
