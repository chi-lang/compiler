## 1. Write Failing Tests

- [ ] 1.1 Add test: generic type alias (`Option[T]`) used in a generic function signature compiles without error (reproduces the bug)
- [ ] 1.2 Add test: generic type alias with multiple type parameters (`Either[A, B]`) resolves correctly
- [ ] 1.3 Add test: non-generic type alias (`type Name = string`) is unaffected (regression guard)
- [ ] 1.4 Add test: self-application `val f = { x -> x(x) }` still produces infinite type error (BUG-02 regression guard)
- [ ] 1.5 Run tests — confirm 1.1 and 1.2 fail, 1.3 and 1.4 pass

## 2. Implement Fix

- [ ] 2.1 Add `findTypeVariables(type: Type, paramNames: List<String>): List<Variable>` helper in `Compiler.kt` that walks the type tree and collects the first `Variable` matching each param name
- [ ] 2.2 Replace `typeParamNames.map { Variable(it, level) }.zip(params)` with `findTypeVariables(base, typeParamNames).zip(params)` in the `TypeConstructorRef` branch of `resolveType`

## 3. Verify

- [ ] 3.1 Run all tests — confirm all new tests pass and no existing tests regress
- [ ] 3.2 Run full build (`./gradlew build`) to verify clean compilation
