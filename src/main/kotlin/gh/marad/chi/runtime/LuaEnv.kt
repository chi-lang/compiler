package gh.marad.chi.runtime

import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.GlobalCompilationNamespaceImpl
import gh.marad.chi.core.namespace.PreludeImport
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId
import gh.marad.chi.core.types.Variable
import gh.marad.chi.lua.formatLuaCode
import gh.marad.chi.runtime.TypeWriter.decodeType
import gh.marad.chi.runtime.TypeWriter.encodeType
import party.iroiro.luajava.Lua
import party.iroiro.luajava.lua54.Lua54
import party.iroiro.luajava.value.LuaValue

class LuaEnv(val prelude: MutableList<PreludeImport> = mutableListOf()) {
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
                            println = { public=true, mutable=false, type='${encodeType(Type.fn(Type.any, Type.unit))}' },
                            compileLua = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.string))}' },
                            eval = { public=true, mutable=false, type='${encodeType(Type.fn(Type.string, Type.any))}' },
                        },
                        println = chi_println,
                        compileLua = chi_compile,
                        eval = function(chi_code)
                            code = chi_compile(chi_code)
                            local f = load(code)
                            return f()
                        end,
                    } 
                },
                user = { default = { _package = {} } }
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

    fun buildGlobalCompilationNamespace(): GlobalCompilationNamespace {
        val ns = GlobalCompilationNamespaceImpl(prelude)

        ns.getOrCreatePackage("std", "lang").symbols.add(
            Symbol("std", "lang", "embedLua",
                Type.fn(Type.string, Variable("t@embedLua", 1)),
                public = true, mutable = false)
        )

        ns.getOrCreatePackage("std", "lang").symbols.add(
            Symbol("std", "lang", "luaExpr",
                Type.fn(Type.string, Variable("t@luaExpr", 1)),
                public = true, mutable = false)
        )


        val modules = luaListChildren(lua, "chi")
        modules.forEach { (moduleName, _) ->
            val chiModule = moduleName.replace("_", ".")
            val packages = luaListChildren(lua, "chi.$moduleName")
            packages.forEach { (packageName, _) ->
                val chiPackage = packageName.replace("_", ".")
                val packageDef = ns.getOrCreatePackage(chiModule, chiPackage)

                val types = luaListChildren(lua, "chi.$moduleName.$packageName._types")
                types.forEach { (typeName, typeValue) ->
                    val encodedType = typeValue.toJavaObject() as String
                    packageDef.types.add(TypeAlias(
                        TypeId(chiModule, chiPackage, typeName),
                        decodeType(encodedType)
                    ))
                }

                val symbols = luaListChildren(lua, "chi.$moduleName.$packageName._package")
                symbols.forEach { (symbolName, table) ->
                    @Suppress("UNCHECKED_CAST") val table = table.toJavaObject() as Map<String, Any>
                    val symbol =
                        Symbol(
                            chiModule, chiPackage, symbolName,
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


}