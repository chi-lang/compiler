## Why

Anonymous functions and lambdas emitted by the Chi-to-Lua code generator use bare `function tmpN(...)` syntax, which in Lua creates global variables. This causes global namespace pollution and, critically, cross-module collisions when two separately compiled Chi modules share the same `tmp0`, `tmp1`, ... names (the counter resets per compilation unit), leading to silent wrong-function-called bugs.

## What Changes

- Prefix non-top-level function definitions in `LuaEmitter.emitFn` with `local` so lambdas are scoped correctly in Lua
- Fix the same global-leak pattern in `emitWhile`'s `foo()` helper, which also generates anonymous functions for while-loop condition evaluation

## Capabilities

### New Capabilities

- `lua-local-scoping`: Ensures all non-top-level function definitions emitted to Lua use `local` scoping to prevent global namespace pollution and cross-module collisions

### Modified Capabilities

_(none)_

## Impact

- **Code**: `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt` — `emitFn` (line ~264) and `foo` helper used by `emitWhile` (line ~579-603)
- **Runtime behavior**: All existing Chi programs that use lambdas, closures, or while-loops will have their Lua output changed from global to local function declarations. This is a correctness fix — previously working code continues to work, but cross-module conflicts are eliminated.
- **No API or dependency changes**
