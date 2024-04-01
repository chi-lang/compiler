package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.PreludeImport
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Type
import gh.marad.chi.lua.LuaEmitter
import party.iroiro.luajava.Lua
import party.iroiro.luajava.Lua.LuaError
import party.iroiro.luajava.lua54.Lua54
import party.iroiro.luajava.value.LuaValue
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.util.*

fun luaListChildren(lua: Lua, symbol: String): List<Pair<String, LuaValue>> {
    val env = lua.execute("""
        local x = {}
        for k, v in pairs($symbol) do
            table.insert(x, {k, v})
        end
        return x
    """.trimIndent()) ?: return emptyList()

    val data = env[0]
    var i = 1
    val elements = mutableListOf<Pair<String, LuaValue>>()
    while (true) {
        val tmp = data?.get(i++) ?: break
        if(tmp.type() == Lua.LuaType.NIL) break
        val name = tmp.get(1)!!.toJavaObject()!!.toString()
        val value = tmp.get(2)!!
        elements.add(name to value)
    }

    elements.sortBy { it.first }
    return elements.filter { it.second.type() == Lua.LuaType.TABLE } + elements.filter { it.second.type() != Lua.LuaType.TABLE }
}

fun decodeType(spec: String): Type {
    val byteArray = Base64.getDecoder().decode(spec)
    return TypeWriter.readType(DataInputStream(ByteArrayInputStream(byteArray)))
}

fun encodeType(type: Type): String {
    val baos = ByteArrayOutputStream()
    TypeWriter.writeType(type, DataOutputStream(baos))
    return Base64.getEncoder().encodeToString(baos.toByteArray())
}

fun buildGlobalCompilationNamespace(lua: Lua, prelude: List<PreludeImport> = emptyList()): GlobalCompilationNamespace {
    val ns = GlobalCompilationNamespace(prelude)
    val modules = luaListChildren(lua, "chi")
    modules.forEach { (moduleName, _) ->
        val packages = luaListChildren(lua, "chi.$moduleName")
        packages.forEach { (packageName, _) ->
            val packageDef = ns.getOrCreatePackage(moduleName, packageName)
            val symbols = luaListChildren(lua, "chi.$moduleName.$packageName._package")
            symbols.forEach { (symbolName, table) ->
                @Suppress("UNCHECKED_CAST") val table = table.toJavaObject() as Map<String, Any>
                val symbol =
                    Symbol(
                        moduleName, packageName, symbolName,
                        type = (table["type"] as String?)?.let { decodeType(it) },
                        public = table["public"] as Boolean,
                        mutable = table["mutable"] as Boolean
                    )
                packageDef.symbols.add(symbol)
            }
        }

    }
    return ns
}


fun main() {
    val prelude = mutableListOf<PreludeImport>()
    prelude.add(PreludeImport("std", "lang", "println", "println"))


    val lua = Lua54()
    lua.openLibraries()

    lua.register("chi_println") {
        val arg = it.get().toJavaObject()
        println(arg)
        0
    }

    lua.run("""
        chi = { 
            std = { 
                lang = { 
                    _package = {
                        println = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' }
                    },
                    println = chi_println
                } 
            },
            user = { default = { _package = {} } }
        }
    """.trimIndent())

    buildGlobalCompilationNamespace(lua)


    while (true) {
        print("> ")
        val code = readlnOrNull()?.replace(";", "\n")?.trim()

        if (!code.isNullOrBlank()) {
            val luaCode = if (code.trim().startsWith("@")) {
                code.trimStart(' ', '@')
            } else {
                try {
                    val ns = buildGlobalCompilationNamespace(lua, prelude)
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
            }//.replace(Regex(";+"), ";").trim(' ', ';')
            println("@ $luaCode")
            lua.load(luaCode)
            val status = lua.pCall(0, 1)
            if (status != LuaError.OK) {
                val errorMessage = lua.get().toJavaObject()
                println("Error: $errorMessage")
            } else {
                val luaValue = lua.get()
                val type = luaValue.type().name
                val result = luaValue.toJavaObject()
                println("$result : $type")
            }
        }
    }
}