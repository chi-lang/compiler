package gh.marad.chi.runtime

import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
import gh.marad.chi.lua.formatLuaCode
import gh.marad.chi.runtime.TypeWriter.encodeType
import party.iroiro.luajava.Lua
import party.iroiro.luajava.lua54.Lua54
import java.nio.ByteBuffer

class LuaEnv(val prelude: MutableList<Import> = mutableListOf()) {
    val lua = Lua54().also { init(it) }

    fun eval(code: String, dontEvalOnlyShowLuaCode: Boolean = false, emitModule: Boolean = true): Boolean {
        val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT, emitModule)
            ?: return false
        return if (dontEvalOnlyShowLuaCode) {
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

    fun setModuleLoader(loader: ModuleLoader) {
        lua.setExternalLoader { qualifier, L ->
            val (modName, pkgName) = qualifier.split("/")
            val luaCode = loader.load(modName, pkgName)
            val bytes = luaCode.toByteArray()
            val buffer = ByteBuffer.allocateDirect(bytes.size)
            buffer.put(bytes)
            buffer.flip()
            buffer
        }
    }

    private fun init(lua: Lua) {
        lua.openLibraries()

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
            function chi_tostring(value)
                local t = type(value)
                if t == 'function' then
                    return '<function>'
                elseif t == 'nil' then
                    return 'unit'
                else 
                    return tostring(value)
                end
            end
            
            chi_print = function(to_show, flush)
                io.write(chi_tostring(to_show))
                if not flush then io.flush() end
            end
            
            chi_println = function(to_show, flush)
                io.write(chi_tostring(to_show), "\n")
                if not flush then io.flush() end
            end
            
            chi_reload_module = function(module)
                package.loaded[module] = nil
                require(module)
            end
            
            chi = {}
            package.loaded['std/lang'] = {
                _package = {
                    println    = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' },
                    compileLua = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.string))}' },
                    eval       = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.any))}' },
                    embedLua   = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Variable("a", 1)))}' },
                    luaExpr    = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Variable("a", 1)))}' },
                    reload     = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.unit))}' },
                },
                print = chi_print,
                println = chi_println,
                compileLua = chi_compile,
                eval = function(chi_code)
                    code = chi_compile(chi_code)
                    local f = load(code)
                    return f()
                end,
                reload = chi_reload_module,
            }
            
            package.loaded['user/default'] = {
                _package = {},
                _types = {}
            }
            
            
            array_meta_table = {
                __tostring = function(arr)
                    local s = {}
                    for _, v in ipairs(arr) do
                        table.insert(s, chi_tostring(v))
                    end
                    return "[" .. table.concat(s, ", ") .. "]"
                end
            }
            
            record_meta_table = {
                __tostring = function(rec)
                    local s = {}
                    for k, v in pairs(rec) do
                        table.insert(s, chi_tostring(k) .. ": " .. chi_tostring(v))
                    end
                    return "{" .. table.concat(s, ", ") .. "}"
                end
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