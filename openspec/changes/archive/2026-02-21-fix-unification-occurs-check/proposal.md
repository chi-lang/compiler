## Why

The Hindley-Milner type inference engine in Chi's compiler is missing the "occurs check" during unification. When binding a type variable `'a` to a type `T`, the algorithm must verify that `'a` does not appear free in `T`. Without this check, circular constraints like `'a = ('a -> int)` are silently accepted, allowing infinite/recursive types to pass type checking. This is a critical type soundness bug (BUG-02) that causes nonsensical types to propagate downstream.

## What Changes

- Add an occurs check function that walks a type to detect whether a given type variable appears inside it
- Guard both `expected is Variable` and `actual is Variable` branches in `unify()` to reject circular bindings
- Add a new `InfiniteType` error message to the analyzer message hierarchy
- Produce a clear "infinite type" compiler error when circular types are detected (e.g., `val f = { x -> x(x) }`)

## Capabilities

### New Capabilities

- `occurs-check`: The occurs check that prevents infinite/recursive type variable bindings during unification

### Modified Capabilities

_(none)_

## Impact

- **Code**: `Unification.kt` (core fix), `Analyzer.kt` (new message type)
- **Behavior**: Programs that previously compiled with circular type variable bindings will now produce a compile-time error. This is strictly a correctness improvement -- no valid programs are affected.
- **Dependencies**: None. The fix is self-contained within the type inference subsystem.
