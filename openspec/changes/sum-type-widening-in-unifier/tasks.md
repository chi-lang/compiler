## 1. Expose Sum.listTypes

- [ ] 1.1 Change `Sum.companion.listTypes` visibility from `private` to `internal` in `src/main/kotlin/gh/marad/chi/core/types/Types3.kt`

## 2. Add sum-aware occurs check

- [ ] 2.1 Add `occursInExcludingSumBranches(variable: Variable, type: Type): Boolean` function in `src/main/kotlin/gh/marad/chi/core/types/Unification.kt` that flattens sum branches via `Sum.listTypes`, excludes the variable from direct branches, and checks `occursIn` only on the remaining branches
- [ ] 2.2 Replace `occursIn(expected, actual)` with `occursInExcludingSumBranches(expected, actual)` in the `expected is Variable` branch (line 32)
- [ ] 2.3 Replace `occursIn(actual, expected)` with `occursInExcludingSumBranches(actual, expected)` in the `actual is Variable` branch (line 41)

## 3. Tests — widening accepted

- [ ] 3.1 Write test: unification of `'T = Sum('T, unit)` succeeds (direct branch widening)
- [ ] 3.2 Write test: `fn asOption[T](value: T): T | unit { value }` compiles without errors
- [ ] 3.3 Write test: `fn map[T,R](opt: T | unit, f: (T) -> R): R | unit { ... }` compiles without errors
- [ ] 3.4 Write test: `fn ifPresent[T](opt: T | unit, f: (T) -> unit) { ... }` compiles without errors

## 4. Tests — infinite types still rejected

- [ ] 4.1 Write test: `val f = { x -> x(x) }` still produces infinite type error
- [ ] 4.2 Write test: `'T = Sum(Function('T, int), unit)` is rejected (variable nested in function within sum)

## 5. Verify

- [ ] 5.1 Run full test suite (`./gradlew test`) and confirm no regressions
