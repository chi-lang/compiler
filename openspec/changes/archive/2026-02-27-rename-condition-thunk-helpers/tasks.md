## 1. Rename function and parameter

- [x] 1.1 Rename `foo` to `extractConditionThunks` at definition (line 589) and all call sites (lines 592, 593, 637)
- [x] 1.2 Rename parameter `bar` to `thunkDeclarations` at definition (line 589) and usage (line 601)

## 2. Clean up dead code

- [x] 2.1 Remove commented-out `//InfixOp(term.op, left, right, term.sourceSection)` on line 594

## 3. Verify

- [x] 3.1 Run `./gradlew test` to confirm no regressions (JDK 17+ not available in environment; verified manually — pure rename, all references consistent, no old names remain)
