## Why

The Chi compiler resolves string escape sequences (like `\n`, `\t`, `\\`) into real characters during parsing (in `AtomReader.kt`), but the Lua emitter (`LuaEmitter.kt:214`) dumps these raw characters directly into single-quoted Lua string literals with zero escaping. This causes Lua syntax errors and silent data corruption for any string containing special characters. There is an explicit `TODO` on line 211 acknowledging this.

## What Changes

- Add a Lua string escaping function in `LuaEmitter.kt` that escapes special characters before interpolating string values into Lua source code
- The escaping must handle at minimum: `\` → `\\`, `'` → `\'`, newline → `\n`, carriage return → `\r`, tab → `\t`, null byte → `\0`
- Also fix the missing `throw` in `AtomReader.kt:81` where unrecognized string parts are silently dropped (related: a `CompilerMessage` is created but never thrown)

## Capabilities

### New Capabilities

- `string-escaping`: Correct escaping of Chi string literal values when emitted as Lua string literals

### Modified Capabilities

_None — no existing specs are affected._

## Impact

- **Code**: `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt` (emitAtom function), `src/main/kotlin/gh/marad/chi/core/parser/readers/AtomReader.kt` (else branch)
- **User-facing**: Strings containing newlines, tabs, backslashes, or single quotes will produce correct Lua output instead of syntax errors or corrupted data
- **Risk**: Low — this is a bugfix with well-defined scope; no API or language changes
