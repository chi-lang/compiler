## Context

The Chi compiler pipeline for string literals works in three stages:

1. **Lexer** (`ChiLexer.g4`): Tokenizes escape sequences as distinct token types (`ESCAPED_NEWLINE`, `ESCAPED_TAB`, `ESCAPED_SLASH`, `ESCAPED_QUOTE`, `ESCAPED_CR`, `ESCAPED_DOLLAR`)
2. **Parser** (`AtomReader.kt`): Resolves escape tokens into real characters (e.g., `ESCAPED_NEWLINE` → `\n` byte) and builds `StringText`/`ParseInterpolation` parts
3. **Emitter** (`LuaEmitter.kt:208-225`): Writes the string value into a Lua single-quoted literal via `java.new(String,'${term.value}')`

The problem is in stage 3: after the parser resolves escapes, `term.value` contains raw bytes (real newline, real tab, etc.). These are interpolated directly into Lua source without re-escaping, producing broken Lua syntax.

A secondary issue exists in `AtomReader.kt:81` where the `else` branch creates a `CompilerMessage` but never throws it, silently dropping unrecognized string parts.

## Goals / Non-Goals

**Goals:**

- Correctly escape all special characters in string values before emitting them into Lua string literals
- Fix the missing `throw` for unrecognized string parts in `AtomReader.kt`

**Non-Goals:**

- Changing the string representation in the Chi AST (strings will continue to hold resolved characters)
- Supporting Lua long-string (`[[ ]]`) syntax — it doesn't support escape sequences and has edge cases with nested `]]`
- Changing how string interpolation works
- Addressing the `STRING_ESCAPE` dead rule in `ChiLexer.g4` DEFAULT_MODE

## Decisions

### 1. Escape function approach: character-by-character replacement

Add a private `escapeLuaString` function in `LuaEmitter.kt` that takes a raw string and returns a Lua-safe escaped string. The function will replace characters using a `when` expression over each character:

| Raw character          | Lua escape |
| ---------------------- | ---------- |
| `\`                    | `\\`       |
| `'`                    | `\'`       |
| newline (`\n`)         | `\n`       |
| carriage return (`\r`) | `\r`       |
| tab (`\t`)             | `\t`       |
| null byte (`\0`)       | `\0`       |

**Rationale**: This is straightforward, efficient via `StringBuilder`, and mirrors how Lua itself handles escape sequences. Single-quoted strings were chosen by the original emitter and we keep that convention.

### 2. Keep single-quoted Lua strings

The existing emitter uses `java.new(String,'...')`. We will keep single-quoted strings rather than switching to double-quoted or long-string syntax. Single quotes only need `'` escaped (not `"`), matching the existing pattern.

### 3. Fix missing throw as a separate one-line change

The `AtomReader.kt:81` fix is adding `throw` before `CompilerMessage.from(...)`. This is a trivial one-word fix that prevents silent data loss.

## Risks / Trade-offs

- **[Low risk] Incomplete escape set**: If Chi later supports additional escape sequences (e.g., `\a`, `\b`, `\v`, hex/unicode escapes), the Lua escape function will need updating. Mitigation: the function is centralized and easy to extend.
- **[Low risk] Interpolated strings**: String interpolation goes through a different code path (`emitInterpolation`), but the final concatenated result still passes through `emitAtom` for literal segments. The fix covers all string atoms uniformly.
