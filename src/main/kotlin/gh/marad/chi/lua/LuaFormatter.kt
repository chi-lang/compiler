package gh.marad.chi.lua

/**
 * Naive Lua pretty-printer that is aware of string literals.
 * Only used for display (`chi -l`), never for actual VM execution.
 */
fun formatLuaCode(luaCode: String): String {
    // Phase 1: Tokenize into string-literals and code segments
    val tokens = tokenizeLua(luaCode)

    // Phase 2: Insert newlines only in code segments
    val withNewlines = StringBuilder()
    for (token in tokens) {
        if (token.isString) {
            withNewlines.append(token.text)
        } else {
            var code = token.text
            code = code.replace(Regex("(function[^(]*\\([^)]*\\)|\\bdo\\b|\\bthen\\b)")) { result ->
                result.value + "\n"
            }
            code = code.replace(Regex("\\bend\\b")) { result ->
                "\n" + result.value
            }
            code = code.replace(";", "\n")
            withNewlines.append(code)
        }
    }

    // Phase 3: Indent
    val lines = withNewlines.toString().split("\n")
    val sb = StringBuilder()
    var indent = 0
    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.isEmpty()) continue

        if (trimmed == "end" || trimmed == "end}" || trimmed == "end)") {
            indent = maxOf(0, indent - 1)
        }
        repeat(indent) { sb.append("  ") }
        sb.appendLine(trimmed)
        if (trimmed.contains("function") || trimmed.contains("then") || trimmed.contains("do")) {
            indent += 1
        }
    }

    return sb.toString()
}

private data class LuaToken(val text: String, val isString: Boolean)

/**
 * Split Lua code into alternating code and string-literal tokens.
 * Handles single-quoted and double-quoted strings with backslash escapes.
 */
private fun tokenizeLua(code: String): List<LuaToken> {
    val tokens = mutableListOf<LuaToken>()
    val buf = StringBuilder()
    var i = 0

    while (i < code.length) {
        val ch = code[i]
        if (ch == '\'' || ch == '"') {
            // Flush code before the string
            if (buf.isNotEmpty()) {
                tokens.add(LuaToken(buf.toString(), isString = false))
                buf.clear()
            }
            // Consume the string literal (including quotes)
            val quote = ch
            val strBuf = StringBuilder()
            strBuf.append(quote)
            i++
            while (i < code.length) {
                val sc = code[i]
                if (sc == '\\' && i + 1 < code.length) {
                    strBuf.append(sc)
                    strBuf.append(code[i + 1])
                    i += 2
                } else if (sc == quote) {
                    strBuf.append(sc)
                    i++
                    break
                } else {
                    strBuf.append(sc)
                    i++
                }
            }
            tokens.add(LuaToken(strBuf.toString(), isString = true))
        } else {
            buf.append(ch)
            i++
        }
    }

    if (buf.isNotEmpty()) {
        tokens.add(LuaToken(buf.toString(), isString = false))
    }

    return tokens
}
