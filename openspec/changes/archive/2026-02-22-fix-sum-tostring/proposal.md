## Why

`Sum.toString()` in `Types3.kt:144-151` has a dead `?:` branch that makes type error messages unreadable. When the compiler encounters anonymous sum types (e.g., an if-else returning `string` in one branch and `float` in another), the user sees `"[]"` instead of `"string | float"` because `ids.toString()` is never null, making the `?: "$lhs | $rhs"` fallback unreachable.

## What Changes

- Fix the `else` branch of `Sum.toString()` to display `"$lhs | $rhs"` when `ids` is empty (anonymous sum types), and `"$ids[$lhs | $rhs]"` when `ids` is non-empty (named sum types)
- Remove the dead `?:` operator that can never trigger

## Capabilities

### New Capabilities

- `sum-type-display`: Correct string representation of Sum types for error messages

### Modified Capabilities

_(none -- no existing specs are affected)_

## Impact

- **File**: `src/main/kotlin/gh/marad/chi/core/types/Types3.kt` -- `Sum.toString()` method (lines 144-151)
- **User-facing**: Type error messages involving anonymous sum types become readable (e.g., `"string | float"` instead of `"[]"`)
- **Risk**: Low -- purely cosmetic change to `toString()`, no impact on type checking logic or code generation
