package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type
import gh.marad.chi.lua.LuaEmitter
import gh.marad.chi.runtime.LuaCompilationEnv
import gh.marad.chi.runtime.LuaEnv
import party.iroiro.luajava.Lua.LuaError
import kotlin.system.exitProcess

class Repl(
    private val env: LuaEnv,
    var showCompiledLuaCode: Boolean = false,
) {
    private val imports = mutableSetOf<Import>()

    private val commands: MutableMap<String, (Array<String>) -> Unit> = mutableMapOf(
        ".exit" to { _: Array<String> ->
            println("Thank you! Bye!")
            exitProcess(0)
        },
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

    ).also {
        it[".help"] = { _: Array<String> ->
            println("Available commands:")
            println("\t${it.keys.joinToString(" ")}")
        }
    }

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
        while (true) {
            print("> ")
            val code = readlnOrNull()?.replace(";", "\n")?.trim()

            if (!code.isNullOrBlank()) {

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
                        emitter.emit(returnLastValue = true)
                    } catch (ex: Exception) {
                        println("Error: ${ex.message}")
                        continue
                    }
                }

                if (showCompiledLuaCode) {
                    println("@ $luaCode")
                }
                env.lua.load(luaCode)
                val status = env.lua.pCall(0, 1)
                if (status != LuaError.OK) {
                    val errorMessage = env.lua.get().toJavaObject()
                    println("Error: $errorMessage")
                } else {
                    val luaValue = env.lua.get()
                    var result = luaValue.toJavaObject()
                    if (resultType is Function) {
                        result = "<function>"
                    }
                    if (resultType != Type.unit) {
                        if (result != null && resultType != null) {
                            println("$result : $resultType")
                        } else if (result != null) {
                            println("$result")
                        }
                    }
                }
            }
        }
    }
}