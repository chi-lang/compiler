## 1. Regression Test (red — confirms the bug exists)

- [x] 1.1 Add a failing test that demonstrates the bug: compile a block whose result is discarded and assert that the last expression has `used = false` (currently it will be `true` — test should fail before the fix)
- [x] 1.2 Run the test and confirm it fails, documenting the incorrect behavior

## 2. Fix Usage Propagation

- [x] 2.1 In `src/main/kotlin/gh/marad/chi/core/compiler/Usage.kt`, change `visitBlock` to propagate `block.used` to the last body expression instead of unconditionally setting `true`
- [x] 2.2 Run the regression test from 1.1 and confirm it now passes

## 3. Additional Tests

- [x] 3.1 Add test verifying that the last expression in a used block has `used = true`
- [x] 3.2 Add test verifying that an empty block does not cause an error
- [x] 3.3 Add test verifying that `is` check on a type variable inside a discarded block compiles without error
- [x] 3.4 Add test verifying that `is` check on a type variable inside a used block still produces a compile error

## 4. Verification

- [x] 4.1 Run full test suite (`./gradlew test`) and confirm all tests pass
