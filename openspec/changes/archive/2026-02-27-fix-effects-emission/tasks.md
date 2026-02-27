## 1. Failing Test (reproduce bug)

- [x] 1.1 Add failing test: effect definition crashes with NotImplementedError — write a test that compiles a Chi program with `effect` definition and asserts it throws `NotImplementedError` during Lua emission. This confirms the bug exists before any fix is applied.
- [x] 1.2 Add failing test: handle expression crashes with NotImplementedError — write a test that compiles a Chi program with `handle`/`with` and asserts it throws `NotImplementedError` during Lua emission.

## 2. Runtime Helper

- [x] 2.1 Fix `chi_handle_effect` Lua runtime in `LuaEnv.kt` — simplified handler protocol: `resume(x)` is identity, handler returns single value, runtime resumes coroutine with handler result. Removed debug `print`, fixed dead-coroutine return to scalar, fixed error handling.

## 3. EffectDefinition Emission

- [x] 3.1 Uncomment and fix `EffectDefinition` branch in `emitExpr` — uses `topLevelName(term.name)` for the variable assignment and `normaliseEffectName(localQualifiedName(...))` for the yield key to ensure matching with handler keys.

## 4. Handle Emission

- [x] 4.1 Uncomment and fix `Handle` branch in `emitExpr` — uses `normaliseEffectName(localQualifiedName(...))` for handler keys, 1-based arg indexing, simplified `resume(x) = return x`, and body emitted WITHOUT `insideFunction` to preserve PackageSymbol variable resolution.

## 5. Tests (positive)

- [x] 5.1 Update failing tests from step 1 — changed from expecting `NotImplementedError` to asserting successful Lua emission with expected patterns.
- [x] 5.2 Add test: end-to-end effect invocation and handling — `TestEnv.eval` verifies `myEffect(42)` with handler `resume(x + 1)` returns 43.
- [x] 5.3 Add test: handle with multiple handler cases — two effects (`add`, `double`) with sequential invocation, verifies correct result (30).

## 6. Verification

- [x] 6.1 Run full test suite (`./gradlew test`) — 312 tests, 0 failures, no regressions.
