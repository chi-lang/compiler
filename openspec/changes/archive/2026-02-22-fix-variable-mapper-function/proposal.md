## Why

`VariableMapper.visitFunction` reconstructs `Function` types using only the `types` field, discarding `typeParams` and `defaultArgs`. Since `VariableMapper` is the base class for `FreshenAboveVisitor` (used by `PolyType.instantiate`), every polymorphic function instantiation silently resets `defaultArgs` to `0` and erases `typeParams`. This causes arity errors at call sites where default arguments are omitted from polymorphic function calls.

## What Changes

- Fix `VariableMapper.visitFunction` in `TypeVisitor.kt` to use `function.copy(types = ...)` instead of constructing a new `Function(...)`, preserving `typeParams` and `defaultArgs` through type variable mapping
- Add tests verifying that polymorphic functions with default arguments retain their default arg count after type instantiation

## Capabilities

### New Capabilities

_(none)_

### Modified Capabilities

_(none -- this is a bug fix to internal type system machinery, no spec-level behavior changes)_

## Impact

- **Code:** `src/main/kotlin/gh/marad/chi/core/types/TypeVisitor.kt` line 16-17 (`VariableMapper.visitFunction`)
- **Downstream users:** `FreshenAboveVisitor` (same file, line 38-51), used by `PolyType.instantiate` for all polymorphic type instantiation
- **Risk:** Low -- the fix is a one-line change from constructor call to `copy()`, matching the pattern already used by `VariableReplacer` in `Types3.kt`
