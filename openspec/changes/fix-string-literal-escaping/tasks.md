## 1. Lua String Escaping in Emitter

- [x] 1.1 Add private `escapeLuaString(value: String): String` function to `LuaEmitter.kt` that escapes `\` → `\\`, `'` → `\'`, newline → `\n`, CR → `\r`, tab → `\t`, null → `\0`
- [x] 1.2 Update `emitAtom` string branch (`LuaEmitter.kt:214`) to call `escapeLuaString` on `term.value` before interpolating into the Lua string literal
- [x] 1.3 Remove the TODO comment on line 211

## 2. Fix Silent Drop of Unrecognized String Parts

- [x] 2.1 Add `throw` keyword before `CompilerMessage.from(...)` in `AtomReader.kt:81`

## 3. Tests

- [x] 3.1 Add test: string with newline (`"hello\nworld"`) produces valid Lua output and evaluates correctly
- [x] 3.2 Add test: string with backslash (`"path\\to\\file"`) produces valid Lua output and evaluates correctly
- [x] 3.3 Add test: string with tab (`"col1\tcol2"`) produces valid Lua output and evaluates correctly
- [x] 3.4 Add test: string with single quote character evaluates correctly
- [x] 3.5 Add test: plain string without special characters still works unchanged
