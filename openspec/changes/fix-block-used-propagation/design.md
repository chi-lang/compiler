## Context

The Chi compiler's `UsageMarker` pass (`Usage.kt`) traverses the expression AST and propagates a `used` flag that indicates whether an expression's result value is consumed by its parent. This flag drives two behaviors:

1. **`Is` expression validation** (`visitIs`, line 124-131): type-variable checks are only an error when `used = true`
2. **Lua emission**: the emitter conditionally generates assignment code based on `used`

All compound expressions correctly propagate `used` from parent to children — `visitIfElse` sets `ifElse.thenBranch.used = ifElse.used`, `visitInfixOp` sets `infixOp.left.used = infixOp.used`, etc. However, `visitBlock` unconditionally sets `it.used = true` on the last body expression, ignoring `block.used`.

## Goals / Non-Goals

**Goals:**
- Fix `visitBlock` to propagate `block.used` to the last expression, consistent with all other compound expression visitors
- Add test coverage for both the used and unused block cases
- Verify the `Is` expression inside an unused block no longer throws a false compile error

**Non-Goals:**
- Refactoring the `UsageMarker` visitor pattern or `used` flag mechanism
- Fixing other `used`-flag-related issues outside of `Block`
- Changing LuaEmitter behavior (it already respects the `used` flag correctly)

## Decisions

**Decision 1: One-line fix using `block.used` propagation**

Change `it.used = true` to `it.used = block.used` on line 71 of `Usage.kt`. This mirrors the pattern in `visitIfElse` (line 84-85), `visitInfixOp` (line 90-91), `visitPrefixOp` (line 96), `visitCast` (line 101), and `visitHandle` (line 139).

Alternative considered: adding special-case logic for nested blocks. Rejected because the existing propagation pattern handles nesting naturally — each block propagates to its last expression, which recursively propagates further.

**Decision 2: Test via the `Is` expression diagnostic**

The most observable consequence of this bug is that `is` checks on type variables inside unused blocks throw a false compile error. Tests will compile Chi code with `is` checks inside blocks in both used and unused positions, verifying the error is present only when the block result is consumed.

Alternative considered: testing `used` flag directly on AST nodes. This is more unit-test-like but requires reaching into internal AST state. Using the compiler message output is more robust and tests the full pipeline.

## Risks / Trade-offs

**[Risk] Existing code depends on the unconditional `used = true` behavior** → Low risk. The `used` flag defaults to `false` on all expressions. The only consumer of `used` that could change behavior is `visitIs` (compile error gating) and LuaEmitter (code generation). Both already handle `used = false` correctly. The fix aligns with how every other compound expression works.

**[Risk] Blocks at program top level may need `used = true`** → Not a concern. Top-level expressions have `used = false` by default, which is correct — their results are discarded. The `markUsed` entry point in `Usage.kt:8-11` does not set `used` on top-level expressions.
