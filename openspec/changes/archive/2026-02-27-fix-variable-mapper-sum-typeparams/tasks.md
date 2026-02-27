## 1. Reproduce the bug with a failing test

- [x] 1.1 Add test verifying `VariableMapper` subclass preserves `typeParams` on `Sum` types after mapping — this test MUST fail before the fix is applied, confirming the bug exists
- [x] 1.2 Run the test and confirm it fails (typeParams are dropped, returning empty list)

## 2. Fix VariableMapper.visitSum

- [x] 2.1 In `src/main/kotlin/gh/marad/chi/core/types/TypeVisitor.kt`, update `VariableMapper.visitSum` to pass `typeParams = sum.typeParams` to `Sum.create()`
- [x] 2.2 Re-run the failing test from 1.1 and confirm it now passes

## 3. Additional tests

- [x] 3.1 Add test verifying `FreshenAboveVisitor` preserves `typeParams` on `Sum` types during instantiation
- [x] 3.2 Add test verifying `Sum` with empty `typeParams` is unaffected by variable mapping

## 4. Verification

- [x] 4.1 Run full test suite (`./gradlew test`) and confirm no regressions
