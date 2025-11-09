# Agent Guidelines for Chi Compiler

## Build & Test
- **Build**: `./gradlew build` (runs compilation, ANTLR grammar generation, and tests)
- **Test all**: `./gradlew test`
- **Single test**: `./gradlew test --tests "ClassName.test method name"` or `./gradlew test --tests "ClassName"`
- **Clean**: `./gradlew clean`
- **Shadow JAR**: `./gradlew shadowJar` (produces `build/libs/chi-all.jar`)

## Language & Tools
- **Primary**: Kotlin (JVM target 11), Java 11+
- **Grammar**: ANTLR4 (generates parser in `src/main/java/gh/marad/chi/core/antlr/` - do not edit manually)
- **Testing**: JUnit 5 + Kotest matchers

## Code Style
- **Package structure**: `gh.marad.chi.*` namespace
- **Imports**: Group by standard library, then third-party, then project (separated by blank lines)
- **Naming**: camelCase for functions/variables, PascalCase for classes, UPPER_SNAKE for constants
- **Types**: Use Kotlin sealed interfaces for expression hierarchies; nullable types explicit (`Type?`)
- **Data classes**: Prefer for immutable models; include `sourceSection: ChiSource.Section?` for AST nodes
- **Error handling**: Use `Message` hierarchy for compiler errors; `MessageCollectingErrorListener` for ANTLR
- **Expressions**: All implement `Expression` interface with `accept()` visitor, `type`, `used`, `children()`
