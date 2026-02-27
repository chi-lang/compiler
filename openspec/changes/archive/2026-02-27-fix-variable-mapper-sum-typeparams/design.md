## Context

`VariableMapper` in `TypeVisitor.kt` is the base class for all type variable substitution operations. Its subclass `FreshenAboveVisitor` is used by `PolyType.instantiate` to freshen type variables during polymorphic instantiation. The `visitSum` method currently calls `Sum.create(ids, lhs, rhs)` without forwarding `sum.typeParams`, causing the `typeParams` field to default to `emptyList()`.

The `Sum` data class (`Types3.kt:140`) stores `typeParams: List<String>` which records type parameter names for parametric sum types like `Option[T]`. Other visitor methods (`visitRecord`, `visitArray`, `visitRecursive`) correctly preserve their respective metadata through `copy()`.

## Goals / Non-Goals

**Goals:**
- Preserve `typeParams` on `Sum` types when they pass through `VariableMapper` (and all subclasses)
- Maintain consistency with the existing pattern used by other `visit*` methods in `VariableMapper`

**Non-Goals:**
- Refactoring `VariableMapper` to a different pattern (e.g., universal `copy`-based approach)
- Fixing `visitFunction` (BUG-05) — that is a separate change
- Adding type parameter validation or semantic changes to sum types

## Decisions

**Decision 1: Pass `typeParams` to `Sum.create()` rather than using `sum.copy()`**

The fix adds `typeParams = sum.typeParams` to the existing `Sum.create()` call rather than switching to `sum.copy(lhs = ..., rhs = ...)`.

Rationale: `Sum.create()` performs type flattening and deduplication (extracting nested sum subtypes into a flat list, deduplicating, then re-folding). Using `copy()` would bypass this normalization, which could produce denormalized sum types if the substituted `lhs`/`rhs` are themselves `Sum` types. The current code intentionally uses `create()` for this reason — we only need to thread `typeParams` through.

Alternative considered: `sum.copy(lhs = sum.lhs.accept(this), rhs = sum.rhs.accept(this))` — simpler but skips normalization.

## Risks / Trade-offs

- **[Low risk]** `Sum.create` may return a non-`Sum` type if deduplication collapses to a single type — in that case `typeParams` on the `Sum` wrapper are irrelevant since the wrapper doesn't exist. No mitigation needed; `create()` handles this correctly already.
- **[Low risk]** Existing tests may not cover parametric sum types through `FreshenAboveVisitor` — mitigated by adding a targeted test.
