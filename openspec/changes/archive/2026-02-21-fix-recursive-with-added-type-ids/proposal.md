## Why

`Recursive.withAddedTypeIds` unwraps the `Recursive` wrapper and returns the inner type directly, causing recursive type aliases (linked lists, trees) to lose their recursive identity when multiple type IDs are added. The singular `withAddedTypeId` correctly preserves the wrapper using `copy()`, but the plural variant does not follow the same pattern.

## What Changes

- Fix `Recursive.withAddedTypeIds` in `Types3.kt` to preserve the `Recursive` wrapper by using `copy(type = type.withAddedTypeIds(ids))` instead of returning the inner type directly
- Fix the `else` branch to return `this` (the Recursive instance) instead of `type` (the inner type)
- Add tests verifying that `Recursive` wrapper is preserved when adding multiple type IDs

## Capabilities

### New Capabilities

- `recursive-type-id-preservation`: Ensures that `Recursive` type wrappers are correctly preserved when type IDs are added via `withAddedTypeIds`, maintaining consistency with `withAddedTypeId`

### Modified Capabilities

_(none)_

## Impact

- **File**: `src/main/kotlin/gh/marad/chi/core/types/Types3.kt` lines 257-263
- **Scope**: Type system — `Recursive` data class only
- **Risk**: Low — mirrors the existing correct pattern from `withAddedTypeId`
- **Downstream effect**: Recursive type aliases with multiple type IDs will now retain their recursive structure during type checking, preventing incorrect type comparisons
