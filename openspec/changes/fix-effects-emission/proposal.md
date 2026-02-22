## Why

The Chi compiler's front-end (parser, type checker, AST conversion) fully supports `effect` definitions and `handle` expressions, but the Lua code emission in `LuaEmitter.kt` is entirely commented out. Any Chi program using effects compiles through all front-end phases, then crashes with `kotlin.NotImplementedError` at the `else -> TODO()` fallthrough during emission. This is BUG-15 from `specs/bugs.md`.

## What Changes

- Re-enable and update the Lua emission code for `EffectDefinition` in `LuaEmitter.kt` — the commented-out code references a removed `qualifiedName()` function and a removed `needResult` parameter on `emitExpr`, both of which need updating to current APIs.
- Re-enable and update the Lua emission code for `Handle` in `LuaEmitter.kt` — same API mismatches as above.
- Add tests verifying that effect definition and handle expressions compile and execute correctly end-to-end.

## Capabilities

### New Capabilities
- `effects-emission`: Lua code generation for `EffectDefinition` and `Handle` expression AST nodes, enabling effects to compile and run end-to-end.

### Modified Capabilities
_(none — no existing spec-level requirements are changing)_

## Impact

- **Code**: `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt` — uncomment and update the `Handle` and `EffectDefinition` branches in `emitExpr`.
- **Runtime**: The existing `chi_handle_effect` Lua runtime function in `LuaEnv.kt` is already operational and does not need changes.
- **Tests**: New test cases in `src/test/kotlin/gh/marad/chi/` for effects emission (currently zero test coverage for this feature).
