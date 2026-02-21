## Context

The Chi compiler's type system uses a `Recursive` wrapper type (`Types3.kt:237-273`) to represent recursive type aliases (e.g., linked lists, trees). The `Recursive` class implements `HasTypeId`, which requires two methods for adding type identifiers:

- `withAddedTypeId(id)` — singular, correctly uses `copy()` to preserve the wrapper
- `withAddedTypeIds(ids)` — plural, **incorrectly** delegates to the inner type and returns it unwrapped

The `HasTypeId` interface is used during type construction and type checking to associate type IDs with structural types. When the `Recursive` wrapper is lost, the type system can no longer recognize the type as recursive, leading to incorrect type comparisons.

## Goals / Non-Goals

**Goals:**
- Fix `Recursive.withAddedTypeIds` to preserve the `Recursive` wrapper, matching the pattern used in `withAddedTypeId`
- Fix the `else` branch to return `this` instead of the bare inner `type`
- Add test coverage for this behavior

**Non-Goals:**
- Refactoring the `HasTypeId` interface or other `HasTypeId` implementations
- Changing the behavior of `withAddedTypeId` (it is already correct)
- Addressing other type system bugs (BUG-05, BUG-13, etc.)

## Decisions

**Use `copy()` to preserve the wrapper** — Mirror the existing correct pattern from `withAddedTypeId`. The fix replaces `type.withAddedTypeIds(ids)` with `copy(type = type.withAddedTypeIds(ids))` and changes the `else` branch from returning `type` to returning `this`.

Alternatives considered:
- Delegating to repeated `withAddedTypeId` calls: Rejected because `withAddedTypeIds` exists as a batch operation and the inner type's implementation may optimize for batch adds.
- Adding a shared helper method: Over-engineering for a two-line fix.

## Risks / Trade-offs

- **[Low risk]** Behavioral change in type ID propagation for recursive types. Mitigated by: the fix aligns with the singular method's behavior, which is known-correct and already used in the codebase.
- **[Low risk]** No existing tests for this code path. Mitigated by: adding new test cases.
