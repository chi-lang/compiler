-- chi_runtime.lua — Chi language runtime (pure Lua)
-- Requires: utf8 (global), chistr (global) — loaded before this file

-- String constructor
function chi_new_string(value)
    return tostring(value)
end

-- String conversion
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

-- Type checking
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

-- I/O
chi_print = function(to_show, flush)
    io.write(chi_tostring(to_show))
    if not flush then io.flush() end
end

chi_println = function(to_show, flush)
    io.write(chi_tostring(to_show), "\n")
    if not flush then io.flush() end
end

-- Module loading
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

-- Record iteration
function chi_record_pairs(record)
    f,s,i = pairs(record)
    local next = function(state, last)
        k,v = f(state, last)
        if k then
            return k, v
        else
            return k, v
        end
    end
    return next, s, i
end

-- Metatables
array_meta_table = {
    isArray = true,
    __tostring = function(arr)
        local s = {}
        for _, v in ipairs(arr) do
            table.insert(s, chi_tostring(v))
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
            table.insert(s, chi_tostring(k) .. ": " .. chi_tostring(v))
        end
        return "{" .. table.concat(s, ", ") .. "}"
    end
}

-- Global chi namespace (used by define)
chi = {}

-- Package definition helper
function define(module, package, name, public, mutable, type, value)
    local p = chi[module][package]
    p._package[name] = {
        public = public,
        mutable = mutable,
        type = type,
        value = value
    }
end

-- Effect handling
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

-- Type decoder (for Lua-literal type format)
function chi_decode_type(spec)
    return load("return " .. spec)()
end

-- Standalone mode stub for chi_compile
if not chi_compile then
    chi_compile = function()
        error("chi_compile not available in standalone mode")
    end
end

-- Bootstrap packages
-- Type strings use the Lua table literal format (no JVM dependency)
local _T_fn_any_unit       = '{tag="fn",types={{tag="any"},{tag="unit"}},typeParams={},defaults=0}'
local _T_fn_string_string  = '{tag="fn",types={{tag="string"},{tag="string"}},typeParams={},defaults=0}'
local _T_fn_string_any     = '{tag="fn",types={{tag="string"},{tag="any"}},typeParams={},defaults=0}'
local _T_fn_string_a       = '{tag="fn",types={{tag="string"},{tag="var",name="a",level=1}},typeParams={},defaults=0}'
local _T_fn_string_unit    = '{tag="fn",types={{tag="string"},{tag="unit"}},typeParams={},defaults=0}'
local _T_fn_any_string     = '{tag="fn",types={{tag="any"},{tag="string"}},typeParams={},defaults=0}'

package.loaded['std/lang'] = {
    _types = {},
    _package = {
        println    = { public=true, mutable=false, type=_T_fn_any_unit },
        print      = { public=true, mutable=false, type=_T_fn_any_unit },
        compileLua = { public=true, mutable=false, type=_T_fn_string_string },
        eval       = { public=true, mutable=false, type=_T_fn_string_any },
        embedLua   = { public=true, mutable=false, type=_T_fn_string_a },
        luaExpr    = { public=true, mutable=false, type=_T_fn_string_a },
        reload     = { public=true, mutable=false, type=_T_fn_string_unit },
        loadModule = { public=true, mutable=false, type=_T_fn_string_unit },
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
        toString = { public=true, mutable=false, type=_T_fn_any_string }
    },
    _types = {},
    toString = chi_tostring
}

package.loaded['user/default'] = {
    _package = {},
    _types = {}
}
