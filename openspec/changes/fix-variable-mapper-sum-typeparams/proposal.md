## Why

`VariableMapper.visitSum` in `TypeVisitor.kt` reconstructs `Sum` types via `Sum.create()` without forwarding `typeParams`, causing parametric sum types (e.g., `Option[T]`) to silently lose their type parameter names during variable mapping — including during polymorphic type instantiation via `FreshenAboveVisitor`. This is the same class of bug as BUG-05 (`visitFunction` dropping `defaultArgs`/`typeParams`).

## What Changes

- Fix `VariableMapper.visitSum` to pass `sum.typeParams` through to `Sum.create()`, preserving type parameter names during type variable substitution.
- Add test coverage verifying parametric sum types retain their `typeParams` after instantiation through `FreshenAboveVisitor`.

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

_(none — this is a bug fix in existing type inference internals with no spec-level behavior change)_

## Impact

- **File**: `src/main/kotlin/gh/marad/chi/core/types/TypeVisitor.kt` — one-line change in `VariableMapper.visitSum`
- **Downstream**: All code paths using `VariableMapper` subclasses (`FreshenAboveVisitor`) on sum types — primarily `PolyType.instantiate` for polymorphic sum type instantiation
- **Risk**: Low — mirrors the existing correct pattern used in `visitRecord`, `visitArray`, and `visitRecursive`
