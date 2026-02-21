## 1. Core Fix

- [x] 1.1 Split the `InfixOp` branch in `Typer.kt:346-357` to handle `&&`/`||` separately from comparison operators
- [x] 1.2 Add `Constraint(Type.bool, lhsType, ...)` and `Constraint(Type.bool, rhsType, ...)` for `&&` and `||` operators
- [x] 1.3 Keep comparison operators (`<`, `<=`, `>`, `>=`, `==`, `!=`) with existing behavior (only `lhsType = rhsType` constraint)

## 2. Tests

- [x] 2.1 Add test: `&&` with bool operands type-checks successfully
- [x] 2.2 Add test: `||` with bool operands type-checks successfully
- [x] 2.3 Add test: `&&` with non-bool operands produces type error
- [x] 2.4 Add test: `||` with non-bool operands produces type error
- [x] 2.5 Add test: comparison operators (`<`, `==`) still work on non-bool types (regression guard)

## 3. Verification

- [x] 3.1 Run full test suite (`./gradlew test`) and confirm all tests pass
