## 1. Reproduce Bug with Test

- [x] 1.1 Write a test that creates a `Constraint` instance and asserts `from` field is NOT null (confirming the bug — debug is always on, stack traces are captured unconditionally)
- [x] 1.2 Run the test and confirm it passes (proving the bug exists)

## 2. Fix Debug Flag

- [x] 2.1 Change `private const val debug = true` to `private const val debug = false` in `src/main/kotlin/gh/marad/chi/core/types/Constraint.kt:5`

## 3. Verification

- [x] 3.1 Run the test from 1.1 — it should now FAIL (stack trace no longer captured, `from` is `null`), confirming the fix works
- [x] 3.2 Update the test to assert the correct behavior (`from` is `null` when debug is off)
- [x] 3.3 Run `./gradlew test` and confirm all tests pass
