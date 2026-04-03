-- utf8.lua — pure Lua UTF-8 implementation for LuaJIT 2.1
-- Implements subset of Lua 5.3 utf8 module needed by Chi stdlib
-- Uses LuaJIT bit library instead of Lua 5.3 bitwise operators
-- Known limitation: utf8.lower/utf8.upper are ASCII-only (no Unicode case folding)

local M = {}
local bit = bit  -- LuaJIT built-in bit library
local band = bit.band
local bor  = bit.bor
local rshift = bit.rshift
local lshift = bit.lshift

-- Returns length of UTF-8 sequence starting at byte b (0-255 value)
local function char_len(b)
    if b < 0x80 then return 1
    elseif b < 0xC0 then return nil  -- continuation byte — error
    elseif b < 0xE0 then return 2
    elseif b < 0xF0 then return 3
    elseif b < 0xF8 then return 4
    else return nil end  -- 0xF8+ are invalid UTF-8
end

-- Decodes codepoint at position i (1-based byte position)
-- Returns: codepoint, next_position
local function decode(s, i)
    local b = string.byte(s, i)
    if not b then return nil end
    local len = char_len(b)
    if not len then return nil end
    if len == 1 then
        return b, i + 1
    end
    local cp
    if len == 2 then
        cp = band(b, 0x1F)
    elseif len == 3 then
        cp = band(b, 0x0F)
    else
        cp = band(b, 0x07)
    end
    for j = 1, len - 1 do
        local cb = string.byte(s, i + j)
        if not cb or cb < 0x80 or cb >= 0xC0 then return nil end
        cp = bor(lshift(cp, 6), band(cb, 0x3F))
    end
    return cp, i + len
end

-- utf8.codes(s) — iterator returning (byte_pos, codepoint)
-- Compatible with Lua 5.3 utf8.codes
M.codes = function(s)
    local i = 1
    return function()
        if i > #s then return nil end
        local cp, next_i = decode(s, i)
        if not cp then
            error("invalid UTF-8 byte at position " .. i)
        end
        local pos = i
        i = next_i
        return pos, cp
    end
end

-- utf8.len(s) — number of codepoints (not bytes)
M.len = function(s)
    local n = 0
    for _ in M.codes(s) do n = n + 1 end
    return n
end

-- utf8.codepoint(s, i, j) — codepoint(s) at position i..j (1-based codepoint index)
M.codepoint = function(s, i, j)
    i = i or 1
    j = j or i
    local idx = 0
    local results = {}
    for _, cp in M.codes(s) do
        idx = idx + 1
        if idx >= i and idx <= j then
            results[#results+1] = cp
        end
        if idx > j then break end
    end
    return unpack(results)
end

-- utf8.char(...) — builds string from codepoints
M.char = function(...)
    local bytes = {}
    for _, cp in ipairs({...}) do
        if cp < 0x80 then
            bytes[#bytes+1] = string.char(cp)
        elseif cp < 0x800 then
            bytes[#bytes+1] = string.char(
                bor(0xC0, rshift(cp, 6)),
                bor(0x80, band(cp, 0x3F)))
        elseif cp < 0x10000 then
            bytes[#bytes+1] = string.char(
                bor(0xE0, rshift(cp, 12)),
                bor(0x80, band(rshift(cp, 6), 0x3F)),
                bor(0x80, band(cp, 0x3F)))
        else
            bytes[#bytes+1] = string.char(
                bor(0xF0, rshift(cp, 18)),
                bor(0x80, band(rshift(cp, 12), 0x3F)),
                bor(0x80, band(rshift(cp, 6), 0x3F)),
                bor(0x80, band(cp, 0x3F)))
        end
    end
    return table.concat(bytes)
end

-- utf8.sub(s, i, j) — substring by codepoint indices (1-based, like string.sub)
-- Negative indices count from end
M.sub = function(s, i, j)
    local total = M.len(s)
    -- Normalize negative indices
    if i < 0 then i = total + i + 1 end
    if j and j < 0 then j = total + j + 1 end
    j = j or total
    if i < 1 then i = 1 end
    if j > total then j = total end
    if i > j then return '' end

    local byte_start = nil
    local byte_end = nil
    local idx = 0
    for pos, _ in M.codes(s) do
        idx = idx + 1
        if idx == i then byte_start = pos end
        if idx == j then
            local b = string.byte(s, pos)
            local len = char_len(b)
            byte_end = pos + len - 1
            break
        end
    end
    if not byte_start then return '' end
    if not byte_end then
        -- j >= total: go to end of string
        return string.sub(s, byte_start)
    end
    return string.sub(s, byte_start, byte_end)
end

-- utf8.reverse(s) — reverses order of codepoints
M.reverse = function(s)
    local cps = {}
    for _, cp in M.codes(s) do
        cps[#cps+1] = cp
    end
    local result = {}
    for i = #cps, 1, -1 do
        result[#result+1] = M.char(cps[i])
    end
    return table.concat(result)
end

-- utf8.lower/upper — works correctly for ASCII via string.lower/upper
-- For non-ASCII (e.g. ą→Ą) this is best-effort (no Unicode data in LuaJIT)
-- Known limitation of Phase 1 — to be addressed in later phases via ICU or mapping tables
M.lower = function(s) return string.lower(s) end
M.upper = function(s) return string.upper(s) end

-- utf8.offset(s, n, i) — byte offset of n-th codepoint
-- Compatible with Lua 5.3 semantics:
--   n > 0: n-th codepoint starting from byte i (default 1)
--   n = 0: start of codepoint containing byte i
--   n < 0: |n|-th codepoint counting backwards from byte i (default #s+1)
M.offset = function(s, n, i)
    local slen = #s
    i = i or (n >= 0 and 1 or slen + 1)
    if n == 0 then
        -- find start of codepoint containing byte i
        while i > 1 do
            local b = string.byte(s, i)
            if not b or b < 0x80 or b >= 0xC0 then break end
            i = i - 1
        end
        return i
    elseif n > 0 then
        -- count n codepoints forward from byte i
        local count = 0
        local pos = i
        while pos <= slen do
            count = count + 1
            if count == n then return pos end
            local b = string.byte(s, pos)
            local clen = char_len(b) or 1
            pos = pos + clen
        end
        -- one past the end is valid for n == count + 1
        if count + 1 == n then return slen + 1 end
        return nil
    else -- n < 0
        -- count |n| codepoints backward from byte i
        local count = 0
        local pos = i - 1
        while pos >= 1 do
            local b = string.byte(s, pos)
            if b < 0x80 or b >= 0xC0 then
                -- this is a codepoint start (ASCII or lead byte)
                count = count - 1
                if count == n then return pos end
            end
            pos = pos - 1
        end
        return nil
    end
end

return M
