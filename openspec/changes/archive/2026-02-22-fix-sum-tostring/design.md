## Context

`Sum.toString()` in `Types3.kt:144-151` is used to render sum types in compiler error messages. The current implementation has a dead code path: the `else` branch uses `ids.toString() ?: "$lhs | $rhs"`, but `ids.toString()` (on a `List<TypeId>`) can never return null. This means anonymous sum types (where `ids` is empty) display as `"[]"` rather than the intended `"$lhs | $rhs"` format.

The `if` branch (Option types where `optionTypeId in ids`) works correctly. Only the `else` branch (non-Option sum types) is broken.

## Goals / Non-Goals

**Goals:**
- Fix `Sum.toString()` so anonymous sum types display as `"$lhs | $rhs"` (e.g., `"string | float"`)
- Named sum types with non-empty `ids` display as `"$ids[$lhs | $rhs]"` to preserve type identity context
- Remove unreachable dead code

**Non-Goals:**
- Changing the Option type branch (`if (Type.optionTypeId in ids)`) -- it works correctly
- Modifying `Sum.create`, `Sum.removeType`, or any other `Sum` methods
- Changing how types are represented internally -- only the string representation changes

## Decisions

**Decision: Use conditional formatting based on `ids.isEmpty()`**

Replace the dead `ids.toString() ?: "$lhs | $rhs"` with:
```kotlin
if (ids.isEmpty()) "$lhs | $rhs" else "$ids[$lhs | $rhs]"
```

Rationale: This matches the intent of the original dead code (the `?:` fallback was meant to handle the empty-ids case). The format `"$lhs | $rhs"` for anonymous sums is readable and consistent with standard type notation. Named sums include `ids` for disambiguation.

Alternative considered: Always showing `"$lhs | $rhs"` regardless of `ids`. Rejected because named sum types benefit from showing their type identity.

## Risks / Trade-offs

- **[Low risk]** Error message format changes could affect any tooling that parses compiler output. Mitigation: This is a bug fix restoring intended behavior; the current `"[]"` output is clearly broken and unlikely to be depended upon.
- **[Low risk]** Nested sum types may produce verbose strings like `"int | string | float"`. Mitigation: This is the correct representation and matches user expectations.
