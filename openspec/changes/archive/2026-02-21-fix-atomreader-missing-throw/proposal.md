## Why

The `else` branch in `AtomReader`'s string part parsing creates a `CompilerMessage` but never throws it (BUG-03, CRITICAL). Any unrecognized string escape or part type is silently dropped, producing truncated/corrupted strings with no error message.

## What Changes

- Add missing `throw` keyword before `CompilerMessage.from(...)` in `AtomReader.kt:81` so unrecognized string parts produce a compiler error instead of being silently discarded.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `string-escaping`: The spec already requires unrecognized string parts to throw a `CompilerMessage` (requirement: "Unrecognized string parts SHALL produce a compiler error"). The implementation does not comply — this change fixes that.

## Impact

- **File:** `src/main/kotlin/gh/marad/chi/core/parser/readers/AtomReader.kt` (line 81)
- One-word fix (`throw` keyword addition)
- No API changes, no new dependencies
- Programs that previously compiled with silently-dropped string parts will now correctly fail with a compiler error (this is the intended behavior)
