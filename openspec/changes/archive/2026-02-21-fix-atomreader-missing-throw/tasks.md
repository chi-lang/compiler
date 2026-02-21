## 1. Verification

- [x] 1.1 Confirm `throw` keyword is present at `AtomReader.kt:81` in the `else` branch of string part parsing
- [x] 1.2 Verify the error message includes "Unsupported string part" and the unrecognized part's string representation

## 2. Testing

- [x] 2.1 Run existing test suite (`./gradlew test`) and confirm all tests pass
- [x] 2.2 Check if any existing tests cover the string parsing `else` branch in `AtomReader.readString`

## 3. Cleanup

- [x] 3.1 Update `specs/bugs.md` BUG-03 entry to note the fix is already present in the codebase
