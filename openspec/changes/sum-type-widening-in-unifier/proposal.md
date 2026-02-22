## Why

The unifier's occurs check rejects valid sum-type widening as infinite types. When a type variable `T` is unified with a sum type containing `T` as a direct branch (e.g., `T | unit` = `Option[T]`), the occurs check fires before the sum decomposition logic can recognize this as valid widening. This blocks compilation of `std/lang.option.chi` and therefore the entire stdlib — `asOption`, `map`, and `ifPresent` all fail with spurious "Infinite type" errors.

## What Changes

- The occurs check in the unifier becomes sum-type-aware: when binding a variable to a sum type where the variable is a direct branch, the binding is accepted as widening instead of rejected as infinite type.
- Genuine infinite types where the variable appears nested inside a non-sum constructor within the sum (e.g., `(T) -> int | unit`) are still rejected.
- Both the `expected is Variable` and `actual is Variable` branches in the `when` chain are updated symmetrically.

## Capabilities

### New Capabilities

- `sum-type-widening`: Specifies that the unifier recognizes assigning a type variable to a sum type containing that variable as a direct branch as valid widening (not an infinite type), while still rejecting genuine infinite types where the variable is nested inside constructors within the sum.

### Modified Capabilities

- `occurs-check`: The occurs check requirement is refined — it must still reject circular bindings in general, but must permit sum-type widening where the variable is only a direct branch of the sum.

## Impact

- `src/main/kotlin/gh/marad/chi/core/types/Unification.kt` — primary change site: `occursIn` function and both variable-binding branches in `unify`
- `stdlib/std/lang.option.chi` — currently blocked, will compile after fix
- All downstream stdlib modules depending on `Option[T]`
