## 1. Fix children() methods

- [x] 1.1 Change `ForLoop.children()` in `src/main/kotlin/gh/marad/chi/core/Expressions.kt:265-266` from `listOf(iterable, body)` to `listOfNotNull(iterable, state, init, body)`
- [x] 1.2 Change `ParseFor.children()` in `src/main/kotlin/gh/marad/chi/core/parser/readers/ForReader.kt:43-44` from `listOf(iterable, body)` to `listOfNotNull(iterable, state, init, body)`

## 2. Verification

- [x] 2.1 Run existing test suite to ensure no regressions
