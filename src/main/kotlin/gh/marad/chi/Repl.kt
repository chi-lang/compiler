package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Type
import gh.marad.chi.lua.LuaEmitter
import gh.marad.chi.runtime.LuaCompilationEnv
import gh.marad.chi.runtime.LuaEnv
import org.jline.reader.EndOfFileException
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.TerminalBuilder
import party.iroiro.luajava.Lua.LuaError
import java.nio.file.Path
import kotlin.system.exitProcess

class Repl(
    private val env: LuaEnv,
    var showCompiledLuaCode: Boolean = false,
) {
    private val imports = mutableSetOf<Import>()

    private val commands: MutableMap<String, (Array<String>) -> Unit> = mutableMapOf(
        ".toggleLuaCode" to { _: Array<String> ->
            println("Toggling compiled lua code visibility.")
            showCompiledLuaCode = !showCompiledLuaCode
        },
        ".imports" to { _: Array<String> ->
            println("Active imports: ")
            imports.forEach { import ->
                val packageAlias = import.packageAlias?.let { " as $it" } ?: ""
                val entries = import.entries.joinToString(", ") { entry ->
                    val entryAlias = entry.alias?.let { " as $it" } ?: ""
                    "${entry.name}$entryAlias"
                }
                println(" - ${import.moduleName}/${import.packageName}$packageAlias { $entries }")
            }
        },
        ".clearImports" to { _: Array<String> ->
            imports.clear()
            println("Imports cleared!")
        },
    )

    private fun handleCommands(command: String): Boolean {
        val parts = command.split("\\s+".toRegex())
        val handler = commands[parts[0]]
        return if (handler != null) {
            handler(parts.drop(1).toTypedArray())
            true
        } else {
            false
        }
    }

    fun run() {
        val terminal = TerminalBuilder.builder().system(true).build()
        val dotCommandKeys = commands.keys.toMutableList()

        // .exit and .help are added here because they need terminal/lineReader references
        // .vi and .emacs are added after lineReader is created
        commands[".exit"] = { _: Array<String> ->
            println("Thank you! Bye!")
            terminal.close()
            exitProcess(0)
        }

        // placeholder for .help — updated after .vi/.emacs are added
        commands[".help"] = { _: Array<String> ->
            println("Available commands:")
            println("\t${commands.keys.sorted().joinToString(" ")}")
        }

        dotCommandKeys.add(".exit")
        dotCommandKeys.add(".help")
        dotCommandKeys.add(".vi")
        dotCommandKeys.add(".emacs")

        val lineReader = LineReaderBuilder.builder()
            .terminal(terminal)
            .parser(ChiReplParser())
            .completer(ChiReplCompleter(dotCommandKeys))
            .variable(LineReader.HISTORY_FILE, historyPath())
            .variable(LineReader.HISTORY_SIZE, 1000)
            .variable(LineReader.SECONDARY_PROMPT_PATTERN, ".. ")
            .build()

        // editing mode dot-commands
        commands[".vi"] = { _: Array<String> ->
            lineReader.keyMaps[LineReader.MAIN]?.let {
                lineReader.keyMaps[LineReader.MAIN] = lineReader.keyMaps[LineReader.VIINS]
            }
            println("Switched to vi mode.")
        }
        commands[".emacs"] = { _: Array<String> ->
            lineReader.keyMaps[LineReader.MAIN]?.let {
                lineReader.keyMaps[LineReader.MAIN] = lineReader.keyMaps[LineReader.EMACS]
            }
            println("Switched to emacs mode.")
        }

        env.lua.run("""
            function repl_print(value, chi_type)
                if chi_type == "unit" then return end
                local t = type(value)
                if t == "function" then chi_print("<function>")
                else
                    chi_print(value, false)
                end
                if chi_type then chi_print(" : " .. chi_type) end
                chi_println("")
            end
        """.trimIndent())

        try {
            while (true) {
                val code: String
                try {
                    code = lineReader.readLine("> ").trim()
                } catch (_: UserInterruptException) {
                    continue
                }

                if (code.isBlank()) continue

                if (handleCommands(code)) {
                    continue
                }

                var resultType: Type? = null
                val luaCode = if (code.trim().startsWith("@")) {
                    code.trimStart(' ', '@')
                } else {
                    try {
                        val ns = LuaCompilationEnv(env, imports)
                        val compilationResult = Compiler.compile(code, ns)
                        if (compilationResult.hasErrors()) {
                            compilationResult.messages.forEach {
                                println(Compiler.formatCompilationMessage(code, it))
                            }
                            continue
                        }
                        resultType = compilationResult.program.expressions.lastOrNull()?.type

                        imports.addAll(compilationResult.program.imports)

                        val emitter = LuaEmitter(compilationResult.program)
                        emitter.emit(emitModule = false)
                    } catch (ex: Exception) {
                        println("Error: ${ex.message}")
                        continue
                    }
                }

                if (showCompiledLuaCode) {
                    println("@ $luaCode")
                }
                env.lua.getGlobal("repl_print")
                env.lua.load(luaCode)
                val status = env.lua.pCall(0, 1)
                if (status != LuaError.OK) {
                    val errorMessage = env.lua.get().toJavaObject()
                    println("Error: $errorMessage")
                } else {
                    if (resultType != null) {
                        env.lua.push(resultType.toString())
                    } else {
                        env.lua.pushNil()
                    }
                    env.lua.pCall(2, 0)
                }
            }
        } catch (_: EndOfFileException) {
            println("\nThank you! Bye!")
        } finally {
            terminal.close()
        }
    }

    companion object {
        internal fun historyPath(chiHome: String? = System.getenv("CHI_HOME")): Path {
            return if (chiHome != null) {
                Path.of(chiHome, "repl_history")
            } else {
                Path.of(System.getProperty("user.home"), ".chi_repl_history")
            }
        }
    }
}
