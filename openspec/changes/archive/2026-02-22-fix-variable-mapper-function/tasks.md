## 1. Fix VariableMapper.visitFunction

- [x] 1.1 Change `VariableMapper.visitFunction` in `src/main/kotlin/gh/marad/chi/core/types/TypeVisitor.kt` line 16-17 to use `function.copy(types = function.types.map { it.accept(this) })` instead of `Function(function.types.map { it.accept(this) })`

## 2. Tests

- [x] 2.1 Add unit test verifying `FreshenAboveVisitor` preserves `defaultArgs` on `Function` types
- [x] 2.2 Add unit test verifying `FreshenAboveVisitor` preserves `typeParams` on `Function` types
- [x] 2.3 Run full test suite (`./gradlew test`) and confirm all tests pass
