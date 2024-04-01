package gh.marad.chi

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.PreludeImport
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Type
import gh.marad.chi.lua.LuaEmitter
import party.iroiro.luajava.Lua.LuaError
import party.iroiro.luajava.lua54.Lua54

fun main() {
    val prelude = mutableListOf<PreludeImport>()
    prelude.add(PreludeImport("std", "lang", "println", "println"))

    val ns = GlobalCompilationNamespace(prelude)
    ns.getOrCreatePackage("std", "lang").symbols.add(
        Symbol(
            "std", "lang", "println",
            Type.fn(Type.string, Type.unit),
            true, false
        )
    )
    ns.getDefaultPackage().symbols.add(
        Symbol(
            "user", "default",
            "print", Type.fn(Type.any, Type.unit),
            true, false
        )
    )

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
                        println = { public=true, mutable=false, type='(string -> unit)' }
                    },
                    println = chi_println
                } 
            },
            user = { default = { _package = {} } }
        }
    """.trimIndent())


    while (true) {
        print("> ")
        val code = readlnOrNull()?.replace(";", "\n")
        if (code != null) {
            val luaCode = if (code.trim().startsWith("@")) {
                code.trimStart(' ', '@')
            } else {
                try {
                    val compilationResult = Compiler.compile(code, ns)
                    if (compilationResult.hasErrors()) {
                        compilationResult.messages.forEach {
                            println(Compiler.formatCompilationMessage(code, it))
                        }
                        continue
                    }
                    val emitter = LuaEmitter(compilationResult.program)
                    emitter.emit()
                } catch (ex: Exception) {
                    println("Error: ${ex.message}")
                    continue
                }
            }
            println("@ $luaCode")
            val status = lua.run(luaCode)
            if (status != LuaError.OK) {
                val errorMessage = lua.get().toJavaObject()
                print("Error: $errorMessage")
            }
        }
    }
}