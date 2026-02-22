## 1. Reproduce the bug with a failing test

- [x] 1.1 Add test: `Type.union(null, Type.int, Type.int)` throws `ClassCastException` — confirms the bug exists before any fix

## 2. Fix Type.union() signature and implementation

- [x] 2.1 Change `Type.union()` return type from `Sum` to `Type` and remove the `as Sum` cast in `src/main/kotlin/gh/marad/chi/core/types/Types3.kt:48-49`
- [x] 2.2 Verify existing call sites in test code (`ObjectsSpec.kt:72`, `TypeCheckingSpec.kt:141`) compile with the widened return type — update type annotations if needed

## 3. Add test coverage

- [x] 3.1 Update the failing test from 1.1: `Type.union(null, Type.int, Type.int)` now returns `Type.int` without throwing
- [x] 3.2 Add test: `Type.union(null, Type.int, Type.string)` still returns a `Sum`
- [x] 3.3 Add test: `Type.union(null, Type.int, Type.string, Type.int)` returns a `Sum` with deduplicated types

## 4. Verify

- [x] 4.1 Run `./gradlew test` and confirm all existing tests pass
