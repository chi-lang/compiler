## Why

`Type.union()` in `Types3.kt:48-49` crashes with a `ClassCastException` when called with duplicate types (e.g., `Type.union(null, int, int)`). The method delegates to `Sum.create` which flattens and deduplicates types via a `Set`. When all types collapse to a single distinct type, `reduce` returns that type directly (e.g., a `Primitive`), but `union()` unconditionally casts the result `as Sum`, causing the crash. This is an internal compiler crash with no useful diagnostic for the user.

## What Changes

- Change `Type.union()` return type from `Sum` to `Type` and remove the unsafe `as Sum` cast
- Audit and update all call sites that expect `Sum` to handle the broader `Type` return
- Add test coverage for the duplicate-type edge case

## Capabilities

### New Capabilities

- `type-union-safety`: Safe handling of `Type.union()` when input types deduplicate to a single type, preventing `ClassCastException`

### Modified Capabilities

_(none — no existing spec requirements change)_

## Impact

- **File:** `src/main/kotlin/gh/marad/chi/core/types/Types3.kt` — `Type.union()` signature and implementation
- **Call sites:** `src/test/kotlin/gh/marad/chi/core/ObjectsSpec.kt`, `src/test/kotlin/gh/marad/chi/core/analyzer/TypeCheckingSpec.kt` — test code that calls `Type.union()`
- **No breaking API changes** — callers that already handle `Type` will work; callers that store the result as `Sum` will need updating
