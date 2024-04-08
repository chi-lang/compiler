package gh.marad.chi.runtime

import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.lua.LuaEmitter

class LuaCompiler(val luaEnv: LuaEnv) {

    class Error(val messages: List<Message>): RuntimeException("Compilation errors")
    enum class ErrorStrategy { PRINT, THROW }

    fun compileToLua(chiCode: String, errorStrategy: ErrorStrategy = ErrorStrategy.THROW): String? {
        val ns = luaEnv.buildGlobalCompilationNamespace()
        val result = Compiler.compile(chiCode, ns)
        if (result.hasErrors()) {
            when (errorStrategy) {
                ErrorStrategy.THROW -> throw Error(result.messages)
                ErrorStrategy.PRINT -> {
                    result.messages.forEach { message ->
                        println(Compiler.formatCompilationMessage(chiCode, message))
                    }
                    return null
                }
            }
        }
        val emitter = LuaEmitter(result.program)
        return emitter.emit(returnLastValue = true)
    }
}