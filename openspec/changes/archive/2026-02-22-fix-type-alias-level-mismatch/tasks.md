## 1. Write Failing Tests

- [x] 1.1 Add test: generic type alias (`Option[T]`) used in a generic function signature compiles without error (reproduces the bug)
- [x] 1.2 Add test: generic type alias with multiple type parameters (`Either[A, B]`) resolves correctly
- [x] 1.3 Add test: non-generic type alias (`type Name = string`) is unaffected (regression guard)
- [x] 1.4 Add test: self-application `val f = { x -> x(x) }` still produces infinite type error (BUG-02 regression guard)
- [x] 1.5 Run tests — confirmed 1.1 and 1.2 fail, 1.3 and 1.4 pass

## 2. Implement Fix

- [x] 2.1 Add `findTypeVariables(type: Type, paramNames: List<String>): List<Variable>` helper in `Compiler.kt` that walks the type tree and collects the first `Variable` matching each param name
- [x] 2.2 Replace `typeParamNames.map { Variable(it, level) }.zip(params)` with `findTypeVariables(base, typeParamNames).zip(params)` in the `TypeConstructorRef` branch of `resolveType`
- [x] 2.3 Add `typeSchemeLevel` field and `effectiveTypeLevel` property to `ExprConversionVisitor` to ensure type scheme variables resolve at their declaration level (not the incremented body level)
- [x] 2.4 Replace all `currentTypeLevel` usages in `resolveType` calls with `effectiveTypeLevel`
- [x] 2.5 Update existing test expectation in `FunctionConversionsKtFuncWithNameTest` (level 3 → 0, reflecting correct behavior)

## 3. Verify

- [x] 3.1 Run all tests — all 331 tests pass, no regressions
- [x] 3.2 Run full build (`./gradlew test`) — BUILD SUCCESSFUL
