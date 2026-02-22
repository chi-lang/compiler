## Context

`VariableMapper` in `TypeVisitor.kt` is the abstract base class for all type-variable mapping operations. Its `visitFunction` method reconstructs `Function` types by calling the `Function(types)` constructor, which uses default values for `typeParams` (empty list) and `defaultArgs` (0). This silently drops metadata that `Function` carries.

The primary downstream user is `FreshenAboveVisitor`, used by `PolyType.instantiate` to freshen type variables when a polymorphic binding is referenced. Every instantiation of a polymorphic function type currently loses `defaultArgs` and `typeParams`.

The correct pattern already exists in `VariableReplacer` (in `Types3.kt`), which uses `function.copy(types = ...)` to preserve all fields.

## Goals / Non-Goals

**Goals:**
- Preserve `defaultArgs` and `typeParams` fields when `VariableMapper` maps over `Function` types
- Ensure polymorphic functions with default arguments work correctly after instantiation

**Non-Goals:**
- Fixing the similar `visitSum` bug (BUG-13) -- that is a separate change
- Refactoring `VariableMapper` more broadly
- Adding new type system features

## Decisions

**Use `copy()` instead of constructor call**

Change `Function(function.types.map { it.accept(this) })` to `function.copy(types = function.types.map { it.accept(this) })`.

*Rationale:* This is the minimal fix. It matches the pattern used by `VariableReplacer`, is idiomatic Kotlin for data classes, and automatically preserves any future fields added to `Function`. The alternative of explicitly passing all three constructor arguments (`Function(mapped, function.typeParams, function.defaultArgs)`) works but is fragile against future field additions.

## Risks / Trade-offs

- **[Risk] Behavioral change in type instantiation** -- Polymorphic functions that previously (incorrectly) had `defaultArgs=0` after instantiation will now retain their original `defaultArgs`. This is the correct behavior, but any code accidentally relying on the broken behavior could be affected.
  *Mitigation:* Run full test suite. The fix aligns with documented `Function` semantics.

- **[Risk] Minimal change scope** -- Only `visitFunction` is fixed; `visitSum` has a similar bug (BUG-13).
  *Mitigation:* BUG-13 is tracked separately and will be addressed in its own change.
