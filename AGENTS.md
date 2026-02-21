# AGENTS.md - Chi Compiler

## Project Overview

This is the **Chi programming language compiler**, written in **Kotlin** (1.6.21, JVM 11).
Chi is a statically-typed language with Hindley-Milner type inference and structural
typing. The compiler parses Chi source, performs type checking, and emits **Lua code**
(targeting LuaJIT) which runs via the `luajava` JVM-to-Lua bridge.

## Build Commands

```bash
# Full build (compile + test)
./gradlew build

# Compile only (no tests)
./gradlew classes

# Regenerate ANTLR parser from grammar files (.g4)
./gradlew generateGrammarSource

# Produce fat JAR (build/libs/chi-all.jar)
./gradlew shadowJar
```

## Test Commands

Tests use **JUnit 5** with **Kotest matchers** for assertions.

```bash
# Run all tests
./gradlew test

# Run a single test class (use fully qualified name)
./gradlew test --tests "gh.marad.chi.core.parser.readers.FuncReaderTest"

# Run a single test method (backtick names need quoting)
./gradlew test --tests "gh.marad.chi.core.parser.readers.FuncReaderTest.should read simple function definition"

# Run all tests in a package
./gradlew test --tests "gh.marad.chi.core.parser.readers.*"

# Run with test output visible
./gradlew test --info
```

Test source location: `src/test/kotlin/gh/marad/chi/`

Test helpers in `src/test/kotlin/gh/marad/chi/Utils.kt`:
- `compile(code)` - compiles Chi source, returns `Program`
- `ast(code)` / `asts(code)` - compile and extract expression(s)
- `messages(code)` - compile and return compiler messages

Integration test helper in `src/test/kotlin/gh/marad/chi/runtime/TestEnv.kt`:
- `TestEnv.eval(code)` - end-to-end compile Chi to Lua and evaluate, returning result

## Lint / Format

No automated linter or formatter is configured. Follow IntelliJ IDEA Kotlin defaults.
There is no `.editorconfig`, ktlint, detekt, or spotless configuration.

## Code Style Guidelines

### Formatting
- **4-space indentation** (no tabs)
- Opening brace on the same line as the declaration (K&R / Kotlin standard)
- No strict line-length limit; aim for ~120 characters
- Single-expression functions use `=` body syntax:
  ```kotlin
  fun getMessages(): List<Message> = messages
  override fun children(): List<Expression> = listOf()
  ```

### Naming Conventions
- **Classes, interfaces, sealed interfaces, data classes:** `PascalCase`
- **Functions, methods, properties, local variables:** `camelCase`
- **Packages:** all-lowercase, dot-separated (`gh.marad.chi.core.types`)
- **Companion object constants:** `camelCase` with `@JvmStatic` (not UPPER_SNAKE_CASE)
  ```kotlin
  @JvmStatic val int = Primitive("int")
  @JvmStatic val string = Primitive("string")
  ```
- **Test methods:** backtick-quoted descriptive sentences
  ```kotlin
  @Test fun `should prohibit changing immutable values`() { ... }
  ```

### Imports
- **Wildcard imports** (`*`) for own project packages
- **Explicit imports** for external libraries and standard library
- All imports in a single contiguous block (no blank-line separators between groups)
  ```kotlin
  import gh.marad.chi.core.*
  import gh.marad.chi.core.analyzer.*
  import gh.marad.chi.core.types.Function
  import io.kotest.matchers.shouldBe
  import org.junit.jupiter.api.Test
  ```

### Types
- **Always** explicitly type function parameters
- **Explicitly** declare return types on public/internal functions
- Return types on private functions may be inferred (expression-body style)
- Local variables use type inference (`val`/`var` without explicit types)
- Use Kotlin nullable types (`Type?`), not `Optional`
- Prefer `sealed interface` over `sealed class` for type hierarchies

### Error Handling
- Compiler errors use structured `Message` data classes (in `core/analyzer/`)
- Errors are accumulated into `MutableList<Message>`, not thrown immediately
- `CompilerMessage` exception wraps a `Message` for critical/unrecoverable errors
- Use `err(message, sourceSection)` utility to throw `CompilerMessage` concisely
- Check accumulated errors at phase boundaries and return early:
  ```kotlin
  if (resultMessages.isNotEmpty()) {
      return CompilationResult(refineMessages(resultMessages), ...)
  }
  ```
- Use `TODO()` for unimplemented code paths

### Comments
- Use `// title` followed by `// ====` for section dividers in long functions:
  ```kotlin
  // infer types
  // ===========
  ```
- Use `TODO:` and `FIXME:` markers for known issues
- KDoc (`/** */`) is rarely used; only add when parameter documentation is needed

## Architecture Notes

### Compiler Pipeline
1. **Parse** (ANTLR4) -> Parse AST (`core/parser/readers/`)
2. **Convert** to Expression AST (`core/expressionast/`)
3. **Type inference** via Hindley-Milner (`core/types/`)
4. **Semantic checks** (`core/compiler/checks/`)
5. **Emit Lua** (`lua/LuaEmitter.kt`)

### Key Patterns
- **Visitor pattern** with two hierarchies: `ParseAstVisitor` and `ExpressionVisitor`
- **Reader objects** in `parser/readers/`: each is an `internal object XxxReader` with a
  `read()` function, plus corresponding data classes in the same file
- **Namespace management** via `SymbolTable`, `TypeTable`, `PackageDescriptor`

### Key Files
- `core/Expressions.kt` - all expression AST node types (~30 data classes)
- `core/types/Types3.kt` - all type system data classes
- `core/compiler/Compiler.kt` - main compilation orchestrator
- `lua/LuaEmitter.kt` - Chi-to-Lua code generator (~680 lines)
- `core/parser/ChiParsers.kt` - parser entry point

### ANTLR Grammar
- Lexer: `src/main/antlr/ChiLexer.g4`
- Parser: `src/main/antlr/ChiParser.g4`
- Generated output: `src/main/java/gh/marad/chi/core/antlr/`
- Regenerate after grammar changes: `./gradlew generateGrammarSource`

### Known Quirk
- `CompilationDefaults.defaultPacakge` is a known typo ("pacakge" instead of "package")
  that is used consistently throughout the codebase. Do not "fix" it without renaming
  all references.
