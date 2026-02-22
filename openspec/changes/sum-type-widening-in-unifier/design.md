## Context

The unifier in `Unification.kt` processes type constraints via a `when` chain. The `expected is Variable` and `actual is Variable` branches (lines 31-47) perform an occurs check before binding. The `expected is Sum` branch (line 77) decomposes sum types by trying to unify branches individually. The problem: when a variable is being bound to a sum type containing that variable as a direct branch, the variable branches fire first and the occurs check rejects what is actually valid sum-type widening (`T` is a valid `Option[T]`).

The `Sum` class (in `Types3.kt`) stores a binary tree of types (`lhs`/`rhs`) and has a `companion.listTypes()` that flattens all branches into a `Set<Type>`. This function is currently `private`.

## Goals / Non-Goals

**Goals:**
- Allow the unifier to accept `Variable = Sum(... Variable ...)` when the variable is a direct branch of the sum (widening).
- Continue rejecting genuine infinite types where the variable appears nested inside a constructor within the sum (e.g., `T = (T) -> int | unit`).
- Unblock compilation of `std/lang.option.chi` (asOption, map, ifPresent).

**Non-Goals:**
- Changing the sum type decomposition logic (the `expected is Sum` branch).
- Modifying the Typer's constraint generation.
- Adding subtyping — this is widening via binding, not a subtype relation.

## Decisions

### 1. Sum-aware occurs check variant over branch reordering

**Decision:** Add an `occursInExcludingSumBranches` function that skips direct sum branches when checking for the variable, and use it in both variable-binding branches instead of `occursIn`.

**Rationale:** The alternative approach (reordering branches to add `expected is Variable && actual is Sum` guards before the general variable case) is simpler but less precise. It would skip the occurs check entirely for any variable-in-sum situation, even when the variable also appears nested inside a function type within another branch (e.g., `Sum(Function([variable, int]), unit)`). The sum-aware variant catches these genuine infinite types.

**Alternatives considered:**
- *Reorder + early return*: Simpler, but accepts `'T = ('T -> int) | unit` which is a genuine infinite type. Would need revisiting later.
- *Remove occurs check for sums*: Unsound, reverts the BUG-02 fix.

### 2. Implementation: modify `occursIn` call site, not `occursIn` itself

**Decision:** Keep the existing `occursIn` function unchanged. Add a new `occursInExcludingSumBranches` function alongside it. Replace the `occursIn` calls in the two variable-binding branches with calls to the new function.

**Rationale:** `occursIn` is a correct general-purpose occurs check. Other callers (if any appear in the future) should get the strict behavior by default. The sum-widening exception is specific to the variable-binding context in `unify`.

### 3. Expose `Sum.listTypes` as internal

**Decision:** Change `Sum.companion.listTypes` from `private` to `internal` so `occursInExcludingSumBranches` can use it.

**Rationale:** The function already exists and correctly flattens a binary sum tree into a flat set of branches. Making it accessible avoids duplicating the flattening logic.

## Risks / Trade-offs

- **[Risk] Over-permissive widening** — If the sum-aware check has a bug, it could silently accept genuinely circular types. → Mitigation: test with nested variable occurrences (`T = (T) -> int | unit`) to verify rejection still works.
- **[Risk] Constraint ordering sensitivity** — The unifier sorts constraints so variables come last (`sortedBy { it.expected !is Variable }`). Different orderings could produce different sum-widening situations. → Mitigation: the fix handles both `expected is Variable` and `actual is Variable` symmetrically, covering both orderings.
- **[Trade-off] Slightly more complex occurs check path** — Two functions instead of one. Acceptable for a targeted fix in a small codebase.
