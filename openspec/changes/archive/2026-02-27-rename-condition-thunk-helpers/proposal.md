## Why

The `LuaEmitter.kt` file contains a private function named `foo` with a parameter named `bar` (line 589). These placeholder names make it difficult to understand the function's purpose — extracting sub-expressions from while-loop conditions into temporary thunk functions for re-evaluation — without reading and mentally tracing the implementation. This is a straightforward readability improvement.

## What Changes

- Rename private function `foo` to `extractConditionThunks` throughout `LuaEmitter.kt`
- Rename parameter `bar` to `thunkDeclarations` throughout `LuaEmitter.kt`
- Remove a stale commented-out `InfixOp` constructor call inside the function

## Capabilities

### Modified Capabilities
- `lua-emitter-readability`: Improved naming in the while-loop condition emission helpers. No behavioral change.

## Impact

- **Code**: `src/main/kotlin/gh/marad/chi/lua/LuaEmitter.kt` — rename only, no logic changes.
- **Tests**: No test changes needed; this is a rename with no behavioral change.
