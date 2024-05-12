package gh.marad.chi.runtime

import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
import gh.marad.chi.lua.formatLuaCode
import gh.marad.chi.runtime.TypeWriter.encodeType
import party.iroiro.luajava.Lua
import party.iroiro.luajava.luajit.LuaJit
import java.nio.ByteBuffer

class LuaEnv(val prelude: MutableList<Import> = mutableListOf()) {
    val lua = LuaJit().also { init(it) }

    fun eval(code: String, showLuaCode: Boolean = false, dryRun: Boolean = false, emitModule: Boolean = true): Boolean {
        val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT, emitModule)
            ?: return false
        if (showLuaCode) {
            println(formatLuaCode(luaCode))
        }
        if (dryRun) {
            return true
        }
        lua.load(luaCode)
        val status = lua.pCall(0, 1)
        return if (status != Lua.LuaError.OK) {
            val errorMessage = lua.get().toJavaObject()
            println("Error: $errorMessage")
            false
        } else {
            true
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

        val result = lua.run("""
            String = java.import('java.lang.String')
            chistr = require("gh.marad.chi.runtime.ChiString.open")
            function chi_new_string(value)
                return java.new(String,value)
            end
            function chi_tostring(value)
                local t = type(value)
                if t == 'function' then
                    return '<function>'
                elseif t == 'nil' then
                    return 'unit'
                elseif t == 'userdata' then
                    return tostring(java.luaify(value))
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
            
            chi_load_module = function(path)
                local f=io.open(path,'r')
                if f == nil then
                    chi_println("Error!")
                end
                local r=f:read('a')
                f:close()
                local loader = load(r)
                loader()
            end
            
            chi = {}
            package.loaded['std/lang'] = {
                _package = {
                    println    = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' },
                    print      = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' },
                    compileLua = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.string))}' },
                    eval       = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.any))}' },
                    embedLua   = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Variable("a", 1)))}' },
                    luaExpr    = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Variable("a", 1)))}' },
                    reload     = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.unit))}' },
                    loadModule = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.unit))}' },
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
                loadModule = chi_load_module
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
        if (result != Lua.LuaError.OK) {
            repeat(lua.top) {
                println(lua.get().toJavaObject())
            }
        }
    }
}