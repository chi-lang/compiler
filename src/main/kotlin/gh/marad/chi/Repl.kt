package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.lua.LuaEmitter
import gh.marad.chi.runtime.LuaEnv
import party.iroiro.luajava.Lua.LuaError
import kotlin.system.exitProcess

class Repl(
    private val env: LuaEnv,
    var showCompiledLuaCode: Boolean = false,
) {

    private val commands: MutableMap<String, (Array<String>) -> Unit> = mutableMapOf(
        ".exit" to { _: Array<String> ->
            println("Thank you! Bye!")
            exitProcess(0)
        },
        ".toggleLuaCode" to { _: Array<String> ->
            println("Toggling compiled lua code visibility.")
            showCompiledLuaCode = !showCompiledLuaCode
        },
    ).also {
        it[".help"] = { args: Array<String> ->
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

    fun run(initialCommands: List<String> = emptyList()) {
        val preload = initialCommands.toMutableList()
        while (true) {
            print("> ")
            val code = if (preload.isNotEmpty()) {
                preload.removeFirst().also { println(it) }
            } else readlnOrNull()?.replace(";", "\n")?.trim()


            if (!code.isNullOrBlank()) {

                if (handleCommands(code)) {
                    continue
                }

                val luaCode = if (code.trim().startsWith("@")) {
                    code.trimStart(' ', '@')
                } else {
                    try {
                        val ns = env.buildGlobalCompilationNamespace()
                        val compilationResult = Compiler.compile(code, ns)
                        if (compilationResult.hasErrors()) {
                            compilationResult.messages.forEach {
                                println(Compiler.formatCompilationMessage(code, it))
                            }
                            continue
                        }
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
//                    val type = luaValue.type().name
                    val result = luaValue.toJavaObject()
                    println(result)
//                    println("$result : $type")
                }
            }
        }
    }
}