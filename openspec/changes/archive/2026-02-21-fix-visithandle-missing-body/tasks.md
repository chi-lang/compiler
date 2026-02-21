## 1. Fix CheckNamesVisitor

- [x] 1.1 Add `parseHandle.body.accept(this)` call before the `cases.forEach` loop in `CheckNamesVisitor.visitHandle` (`src/main/kotlin/gh/marad/chi/core/compiler/checks/CheckNamesVisitor.kt:58-66`)

## 2. Tests

- [x] 2.1 Add test: undefined name in handle body produces `UnrecognizedName` error
- [x] 2.2 Add test: defined name in handle body compiles without error
- [x] 2.3 Add test: `resume` and case argument names are valid inside handler cases (existing behavior preserved)

## 3. Verification

- [x] 3.1 Run full test suite (`./gradlew test`) and confirm no regressions
