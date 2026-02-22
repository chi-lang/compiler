## Why

The `UsageMarker.visitBlock` in `Usage.kt:69-73` unconditionally marks the last expression in every block as `used = true`, ignoring whether the block itself is used. This violates the propagation pattern used by all other compound expressions (e.g., `visitIfElse` correctly propagates `ifElse.used` to branches). The bug causes incorrect `Is` expression compile errors inside discarded blocks and affects LuaEmitter behavior for expressions that check the `used` flag.

## What Changes

- Fix `UsageMarker.visitBlock` to propagate `block.used` to the last expression instead of unconditionally setting `true`
- Add tests verifying that `used` flag propagation works correctly for blocks in both used and unused contexts
- Add tests for the `Is` expression inside discarded blocks (should not throw compile error when block is unused)

## Capabilities

### New Capabilities

- `block-used-propagation`: Correct propagation of the `used` flag through block expressions in the usage marking compiler pass

### Modified Capabilities

_(none — no existing specs are affected)_

## Impact

- **Code:** `src/main/kotlin/gh/marad/chi/core/compiler/Usage.kt` — one-line change in `visitBlock`
- **Behavioral:** Blocks whose results are discarded will no longer mark their last expression as used. This fixes false-positive compile errors for `is` checks on type variables inside unused blocks, and ensures LuaEmitter generates correct code for unused block results.
- **Risk:** Low — the fix aligns `visitBlock` with the established pattern used by `visitIfElse`, `visitInfixOp`, `visitPrefixOp`, `visitCast`, and `visitHandle`.
