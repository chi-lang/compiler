-- chistr.lua — Chi string utilities, pure Lua
-- Depends on the project's custom 'utf8' global (see utf8.lua),
-- loaded by LuaEnv.kt before this module. Targets LuaJIT 2.1.

local chistr = {}

-- Iterator codepoints — returns function yielding successive codepoints (int).
-- Wraps utf8.codes (which returns pos, cp) and strips the byte position,
-- returning only the codepoint or nil when iteration ends.
chistr.codePoints = function(s)
    local iter = utf8.codes(s)
    return function()
        local _, cp = iter()
        return cp  -- nil when iteration ends
    end
end

chistr.reverse = function(s)
    return utf8.reverse(s)
end

chistr.trimStart = function(s)
    return s:match('^%s*(.*)')
end

chistr.trimEnd = function(s)
    return (s:gsub('%s+$', ''))
end

chistr.trim = function(s)
    return s:match('^%s*(.-)%s*$')
end

-- Plain replace (not regex) — equivalent of Java String.replace(CharSequence, CharSequence)
local function escape_pattern(s)
    return (s:gsub('([%(%)%.%%%+%-%*%?%[%^%$])', '%%%1'))
end
local function escape_repl(s)
    return (s:gsub('%%', '%%%%'))
end

chistr.replace = function(s, pattern, replacement)
    return (s:gsub(escape_pattern(pattern), escape_repl(replacement)))
end

-- Concatenation of any number of strings
chistr.concat = function(...)
    return table.concat({...})
end

-- Plain split (not regex) — equivalent of Java String.split with literal separator.
-- Note: #sep is byte length, but s:find with plain=true does byte-level search,
-- so byte-level advancement (from = i + sep_len) is correct even for multi-byte UTF-8.
chistr.split = function(s, sep)
    local result = {}
    if sep == '' then
        -- Empty separator: each codepoint separately
        for _, cp in utf8.codes(s) do
            result[#result+1] = utf8.char(cp)
        end
        return result
    end
    local from = 1
    local sep_len = #sep
    while true do
        local i = s:find(sep, from, true)  -- plain=true
        if not i then
            result[#result+1] = s:sub(from)
            break
        end
        result[#result+1] = s:sub(from, i - 1)
        from = i + sep_len
    end
    return result
end

return chistr
