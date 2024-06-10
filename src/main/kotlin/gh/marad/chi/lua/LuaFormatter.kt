package gh.marad.chi.lua

fun formatLuaCode(luaCode: String): String {
    val statements = luaCode
        .replace("function[^(]*\\([^)]*\\)|do|then".toRegex()) { result ->
            result.value + "\n"
        }
        .replace("\\bend\\b".toRegex()) { result ->
            "\n" + result.value
        }
        .replace(";", "\n")
        .split("\n")
    val sb = StringBuilder()

    var indent = 0
    statements.forEach {
        if (it.trim() == "end") {
            indent -= 1
        }
        repeat(indent) {
            sb.append("  ")
        }
        sb.appendLine(it.trim())
        if (it.contains("function") || it.contains("then") || it.contains("do")) {
            indent += 1
        }
    }

    return sb.toString()
}