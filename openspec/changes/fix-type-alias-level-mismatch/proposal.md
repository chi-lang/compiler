## Why

Type alias variable replacement in `Compiler.resolveType()` constructs replacement source variables at the call-site level, but alias bodies store variables at definition-site level (always 1). Since `Variable` equality compares both `name` and `level`, the replacement silently does nothing — leaking stale level-1 variables into resolved types. This causes the occurs check (added by the BUG-02 fix) to reject valid code like `pub fn valueOr[T](opt: Option[T], alternative: T): T` with a spurious "Infinite type" error, blocking compilation of the entire standard library.

## What Changes

- Fix `Compiler.resolveType` for `TypeConstructorRef` to extract actual `Variable` instances from the alias body (which carry the correct definition-site level) instead of reconstructing them at the call-site level.
- Add a `findTypeVariables` helper function that walks a type tree and collects the first `Variable` matching each type parameter name.
- Add a regression test verifying that generic type aliases (like `Option[T]`) can be used in generic function signatures without triggering infinite type errors.

## Capabilities

### New Capabilities

- `type-alias-resolution`: Type alias type parameter substitution correctly matches variables by their definition-site level, not the call-site level.

### Modified Capabilities

_(none — the occurs-check spec is unchanged; it should continue rejecting genuinely infinite types)_

## Impact

- `src/main/kotlin/gh/marad/chi/core/compiler/Compiler.kt` — the `TypeConstructorRef` branch of `resolveType` (around line 261)
- `stdlib/std/lang.option.chi` — currently fails to compile; will be unblocked
- Full stdlib build (`make` in `stdlib/`) — currently broken; will be restored
