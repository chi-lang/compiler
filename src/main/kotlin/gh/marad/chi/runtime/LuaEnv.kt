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
    val lua = LuaJit()

    init {
        init()
    }

    fun eval(code: String, showLuaCode: Boolean = false, dryRun: Boolean = false, emitModule: Boolean = true): Boolean {
        val luaCode = LuaCompiler(this).compileToLua(code, LuaCompiler.ErrorStrategy.PRINT, emitModule)
            ?: return false
        if (showLuaCode) {
            println(formatLuaCode(luaCode))
        }
        if (dryRun) {
            return true
        }
        val loadStatus = lua.load(luaCode)
        if (loadStatus != Lua.LuaError.OK) {
            val errorMessage = lua.get().toJavaObject()
            println("Load error: $errorMessage")
            return false
        }
        val status = lua.pCall(0, 1)
        return if (status != Lua.LuaError.OK) {
            val errorMessage = lua.get().toJavaObject()
            println("Error: $errorMessage")
            repeat(lua.top) {
                println(lua.get().toJavaObject())
            }
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

    private fun init() {
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

        evalLua("String = java.import('java.lang.String')")
        evalLua("chistr = require('gh.marad.chi.runtime.ChiString.open')")
        evalLua("""
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
                    return value
                else 
                    return tostring(value)
                end
            end
        """.trimIndent())

        evalLua("""
            function chi_is_float(value)
                if type(value) == "number" then
                    local f = value % 1
                    if f ~= 0 then 
                        return true
                    end
                end
                return false
            end
            
            function chi_is_int(value)
                if type(value) == "number" then
                    local f = value % 1
                    if f == 0 then
                        return true
                    end
                end
                return false
            end
            
            function chi_is_record(value)
                local meta = getmetatable(value)
                if meta then
                    return meta.isRecord or false
                end
                return false
            end
            
            function chi_is_array(value)
                local meta = getmetatable(value)
                if meta then
                    return meta.isArray or false
                end
                return false
            end
        """.trimIndent())

        evalLua("""
            chi_print = function(to_show, flush)
                io.write(java.luaify(chi_tostring(to_show)))
                if not flush then io.flush() end
            end
            
            chi_println = function(to_show, flush)
                io.write(java.luaify(chi_tostring(to_show)), "\n")
                if not flush then io.flush() end
            end
        """.trimIndent())

        evalLua("""
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
                local loader, error = load(r)
                if loader == nil then
                    chi_println('Error loading ' .. path .. ':')
                    chi_println(error)
                else 
                    loader()
                end
            end
        """.trimIndent())

        evalLua("""
            function chi_record_pairs(record)
                f,s,i = pairs(record)
                local next = function(state,last)
                    k,v = f(state,java.luaify(last))
                    if k then 
                        return chi_new_string(k),v
                    else 
                        return k,v
                    end
                end
                return next,s,i
            end
        """.trimIndent())

        val result = lua.run("""
            chi = {}
            package.loaded['std/lang'] = {
                _types = {},
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
            
            package.loaded['std/lang.any'] = {
                _package = {
                    toString = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.string))}' }
                },
                _types = {},
                toString = chi_tostring
            }
            
            package.loaded['user/default'] = {
                _package = {},
                _types = {}
            }
            
            
            array_meta_table = {
                isArray = true,
                __tostring = function(arr)
                    local s = {}
                    for _, v in ipairs(arr) do
                        table.insert(s, java.luaify(chi_tostring(v)))
                    end
                    return "[" .. table.concat(s, ", ") .. "]"
                end
            }
            
            function chi_new_array(...)
                local array = { ... }
                setmetatable(array, array_meta_table)
                return array
            end
            
            record_meta_table = {
                isRecord = true,
                __tostring = function(rec)
                    local s = {}
                    for k, v in pairs(rec) do
                        table.insert(s, java.luaify(chi_tostring(k)) .. ": " .. java.luaify(chi_tostring(v)))
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
            
            function chi_handle_effect(co, resumeValue, handlers)
                local ok, effectName, effectArgs = coroutine.resume(co, resumeValue)
                if not ok then error(effectName) end
                if coroutine.status(co) == "dead" then
                    return effectName
                end
                local handler = handlers[effectName]
                if handler ~= nil then
                    local result = handler(effectArgs)
                    return chi_handle_effect(co, result, handlers)
                else
                    local outerResult = coroutine.yield(effectName, effectArgs)
                    return chi_handle_effect(co, outerResult, handlers)
                end
            end    
        """.trimIndent())
        if (result != Lua.LuaError.OK) {
            val sb = StringBuilder()
            repeat(lua.top) {
                sb.appendLine(lua.get().toJavaObject())
            }
            throw RuntimeException(sb.toString())
        }
    }

    fun evalLua(luaCode: String) {
        val result = lua.run(luaCode)
        if (result != Lua.LuaError.OK) {
            val sb = StringBuilder()
            repeat(lua.top) {
                sb.appendLine(lua.get().toJavaObject())
            }
            throw RuntimeException(sb.toString())
        }
    }
}