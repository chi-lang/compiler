## 1. Fix emitFn local scoping

- [ ] 1.1 In `LuaEmitter.emitFn`, change the non-top-level branch (line ~264) from `emitCode("function $tmpName(")` to `emitCode("local function $tmpName(")`

## 2. Verify while-loop helper path

- [ ] 2.1 Confirm that `emitWhile`'s `foo()` helper generates `NameDeclaration`-wrapped `Fn` nodes that flow through `emitNameDeclaration` → `emitFn` and are correctly scoped after the fix in task 1.1

## 3. Testing

- [ ] 3.1 Add a test that compiles a Chi program with a lambda and verifies the emitted Lua contains `local function` (not bare `function tmpN`)
- [ ] 3.2 Add a test that compiles a Chi program with a while loop containing a complex condition and verifies emitted helpers use `local` scoping
- [ ] 3.3 Run the full test suite (`./gradlew test`) to verify no regressions
