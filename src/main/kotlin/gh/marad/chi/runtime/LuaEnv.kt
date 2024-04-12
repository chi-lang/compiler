package gh.marad.chi.runtime

import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
import gh.marad.chi.lua.formatLuaCode
import gh.marad.chi.runtime.TypeWriter.encodeType
import party.iroiro.luajava.Lua
import party.iroiro.luajava.lua54.Lua54

class LuaEnv(val prelude: MutableList<Import> = mutableListOf()) {
    val lua = Lua54().also { init(it) }

    fun eval(code: String, dontEvalOnlyShowLuaCode: Boolean = false): Boolean {
        val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT)
            ?: return false
        return if (dontEvalOnlyShowLuaCode ) {
            println(formatLuaCode(luaCode))
            true
        } else {
            lua.load(luaCode)
            val status = lua.pCall(0, 1)
            if (status != Lua.LuaError.OK) {
                val errorMessage = lua.get().toJavaObject()
                println("Error: $errorMessage")
                false
            } else {
                true
            }
        }
    }

    private fun init(lua: Lua) {
        lua.openLibraries()

        lua.register("chi_println") {
            val arg = it.get().toJavaObject()
            println(arg)
            0
        }

        lua.register("chi_compile") {
            val code = it.get().toJavaObject() as String
            val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT)
            if (luaCode == null) {
                it.pushNil()
                return@register 1
            }
            it.push(luaCode)
            1
        }

        lua.run("""
            chi = { 
                std = { 
                    lang = { 
                        _package = {
                            println    = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' },
                            compileLua = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.string))}' },
                            eval       = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.any))}' },
                            embedLua   = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Variable("t@embedLua", 1)))}' },
                            luaExpr    = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Variable("t@luaExpr", 1)))}' },
                        },
                        println = function(to_show)
                            io.write(tostring(to_show), "\n")
                            io.flush()
                        end,
                        compileLua = chi_compile,
                        eval = function(chi_code)
                            code = chi_compile(chi_code)
                            local f = load(code)
                            return f()
                        end,
                    } 
                },
                user = { default = { _package = {}, _types = {} } }
            }
            
            function define(module, package, name, public, mutable, type, value)
                local p = chi[module][package]
                p._package[name] = {
                    public = public,
                    mutable = mutable,
                    type = type,
                    value = value
                }
            end
            
            function chi_handle_effect(co, args, handlers)
                local ignored, effectName, args = coroutine.resume(co, args)
                if (coroutine.status(co) == "dead") then
                    -- in this case it's a result array
                    print(effectName)
                    return effectName[1] 
                end

                local handler = handlers[effectName]
                local newArgs
                if (handler ~= nil) then 
                    isResult, newArgs = handler(args)
                    if isResult then return newArgs[1] end
                else
                    newArgs = coroutine.yield(effectName, args)
                end
                return chi_handle_effect(co, newArgs, handlers)
            end
        """.trimIndent())
    }
}