package gh.marad.chi.runtime

import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.lua.formatLuaCode
import party.iroiro.luajava.Lua
import party.iroiro.luajava.luajit.LuaJit
import java.nio.ByteBuffer

class LuaEnv(val prelude: MutableList<Import> = mutableListOf()) {
    val lua = LuaJit()

    init { init() }

    fun eval(code: String, showLuaCode: Boolean = false, dryRun: Boolean = false, emitModule: Boolean = true): Boolean {
        val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT, emitModule) ?: return false
        if (showLuaCode) println(formatLuaCode(luaCode))
        if (dryRun) return true
        val loadStatus = lua.load(luaCode)
        if (loadStatus != Lua.LuaError.OK) { println("Load error: ${lua.get().toJavaObject()}"); return false }
        val status = lua.pCall(0, 1)
        if (status != Lua.LuaError.OK) {
            println("Error: ${lua.get().toJavaObject()}")
            repeat(lua.top) { println(lua.get().toJavaObject()) }
            return false
        }
        return true
    }

    fun setModuleLoader(loader: ModuleLoader) {
        lua.setExternalLoader { qualifier, _ ->
            val (modName, pkgName) = qualifier.split("/")
            val luaCode = loader.load(modName, pkgName)
            val bytes = luaCode.toByteArray()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.flip()
            buffer
        }
    }

    private fun init() {
        lua.openLibraries()
        loadResource("utf8", "/utf8.lua")
        loadResource("chistr", "/chistr.lua")
        lua.register("chi_compile") {
            val code = it.get().toJavaObject() as String
            val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT)
            if (luaCode == null) { it.pushNil(); return@register 1 }
            it.push(luaCode)
            1
        }
        evalLua(LuaEnv::class.java.getResourceAsStream("/chi_runtime.lua")!!.bufferedReader().readText())
    }

    private fun loadResource(globalName: String, path: String) {
        val code = LuaEnv::class.java.getResourceAsStream(path)!!.bufferedReader().readText()
        evalLua("$globalName = (function()\n$code\nend)()")
    }

    fun evalLua(luaCode: String) {
        val result = lua.run(luaCode)
        if (result != Lua.LuaError.OK) {
            val sb = StringBuilder()
            repeat(lua.top) { sb.appendLine(lua.get().toJavaObject()) }
            throw RuntimeException(sb.toString())
        }
    }
}
