-- chi_loader.lua — Chi standalone program loader
-- Loads runtime + stdlib, then executes the user program
-- Usage: luajit chi_loader.lua program.lua [args...]

local chi_home = os.getenv("CHI_HOME")
if not chi_home then
    io.stderr:write("Error: CHI_HOME environment variable not set\n")
    os.exit(1)
end

local lib = chi_home .. "/lib/"

-- Check required files exist
local required = {"utf8.lua", "chistr.lua", "chi_runtime.lua", "std.chim"}
for _, name in ipairs(required) do
    local f = io.open(lib .. name, "r")
    if not f then
        io.stderr:write("Error: Missing " .. lib .. name .. "\n")
        io.stderr:write("Install the compiler runtime and stdlib to CHI_HOME\n")
        os.exit(1)
    end
    f:close()
end

-- Load runtime
utf8 = dofile(lib .. "utf8.lua")
chistr = dofile(lib .. "chistr.lua")
dofile(lib .. "chi_runtime.lua")
dofile(lib .. "std.chim")

-- Load and run user program
local program = arg[1]
if not program then
    io.stderr:write("Usage: chi-run <program.lua> [args...]\n")
    os.exit(1)
end

-- Shift arg table so program sees its own args as arg[1], arg[2], ...
local new_arg = {}
for i = 2, #arg do
    new_arg[i - 1] = arg[i]
end
new_arg[0] = program
arg = new_arg

dofile(program)
