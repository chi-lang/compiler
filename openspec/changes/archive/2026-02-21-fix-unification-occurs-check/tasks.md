## 1. Error Reporting

- [x] 1.1 Add `InfiniteType` data class to `src/main/kotlin/gh/marad/chi/core/analyzer/Analyzer.kt` following the existing `Message` pattern (with `variable: Variable`, `type: Type`, and `codePoint: CodePoint?` fields)

## 2. Core Fix

- [x] 2.1 Add `occursIn(variable: Variable, type: Type): Boolean` function to `src/main/kotlin/gh/marad/chi/core/types/Unification.kt` that recursively checks if a variable appears inside a type using `Type.children()`
- [x] 2.2 Add occurs check to the `expected is Variable` branch (line 26) — before adding to `solutions`, call `occursIn(expected, actual)` and throw `CompilerMessage(InfiniteType(...))` if true
- [x] 2.3 Add occurs check to the `actual is Variable` branch (line 32) — before adding to `solutions`, call `occursIn(actual, expected)` and throw `CompilerMessage(InfiniteType(...))` if true

## 3. Tests

- [x] 3.1 Add test that `val f = { x -> x(x) }` produces a compile error (infinite type via self-application)
- [x] 3.2 Add test that non-circular variable bindings still unify successfully (regression guard)
- [x] 3.3 Run full test suite (`./gradlew test`) to verify no regressions

## 4. Extended Test Coverage

- [x] 4.1 Unit tests for `occursIn` function (9 tests: function types, nested types, records, arrays, primitives, variable identity, name/level distinction)
- [x] 4.2 Direct unification tests exercising both Variable branches via `Constraint` construction
- [x] 4.3 `InfiniteType` message field verification (level=ERROR, message content)
- [x] 4.4 Nested infinite type test (self-application inside outer lambda)
- [x] 4.5 Integration test via `TestEnv.eval()` (full pipeline: parse → type → occurs check → error)
- [x] 4.6 Full test suite passes (`./gradlew test` — BUILD SUCCESSFUL)
