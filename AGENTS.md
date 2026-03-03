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

## Bug Fix Workflow

When fixing a bug, always follow this order:

1. **Write a failing test first.** Before changing any production code, create a test that
   reproduces the incorrect behavior described in the bug report. Run it and confirm it
   fails (or exhibits the wrong behavior) — this proves the bug exists.
2. **Implement the fix.** Change the production code to correct the bug.
3. **Re-run the test.** Confirm the previously failing test now passes.
4. **Run the full test suite** (`./gradlew test`) to verify no regressions.

Bug descriptions with fix guidance are in `specs/bugs.md`.

<!-- BEGIN BEADS INTEGRATION -->
## Issue Tracking with bd (beads)

**IMPORTANT**: This project uses **bd (beads)** for ALL issue tracking. Do NOT use markdown TODOs, task lists, or other tracking methods.

### Why bd?

- Dependency-aware: Track blockers and relationships between issues
- Git-friendly: Dolt-powered version control with native sync
- Agent-optimized: JSON output, ready work detection, discovered-from links
- Prevents duplicate tracking systems and confusion

### Quick Start

**Check for ready work:**

```bash
bd ready --json
```

**Create new issues:**

```bash
bd create "Issue title" --description="Detailed context" -t bug|feature|task -p 0-4 --json
bd create "Issue title" --description="What this issue is about" -p 1 --deps discovered-from:bd-123 --json
```

**Claim and update:**

```bash
bd update <id> --claim --json
bd update bd-42 --priority 1 --json
```

**Complete work:**

```bash
bd close bd-42 --reason "Completed" --json
```

### Issue Types

- `bug` - Something broken
- `feature` - New functionality
- `task` - Work item (tests, docs, refactoring)
- `epic` - Large feature with subtasks
- `chore` - Maintenance (dependencies, tooling)

### Priorities

- `0` - Critical (security, data loss, broken builds)
- `1` - High (major features, important bugs)
- `2` - Medium (default, nice-to-have)
- `3` - Low (polish, optimization)
- `4` - Backlog (future ideas)

### Workflow for AI Agents

1. **Check ready work**: `bd ready` shows unblocked issues
2. **Claim your task atomically**: `bd update <id> --claim`
3. **Work on it**: Implement, test, document
4. **Discover new work?** Create linked issue:
   - `bd create "Found bug" --description="Details about what was found" -p 1 --deps discovered-from:<parent-id>`
5. **Complete**: `bd close <id> --reason "Done"`

### Auto-Sync

bd automatically syncs via Dolt:

- Each write auto-commits to Dolt history
- Use `bd dolt push`/`bd dolt pull` for remote sync
- No manual export/import needed!

### Important Rules

- ✅ Use bd for ALL task tracking
- ✅ Always use `--json` flag for programmatic use
- ✅ Link discovered work with `discovered-from` dependencies
- ✅ Check `bd ready` before asking "what should I work on?"
- ❌ Do NOT create markdown TODO lists
- ❌ Do NOT use external issue trackers
- ❌ Do NOT duplicate tracking systems

For more details, see README.md and docs/QUICKSTART.md.

## Landing the Plane (Session Completion)

**When ending a work session**, you MUST complete ALL steps below. Work is NOT complete until `git push` succeeds.

**MANDATORY WORKFLOW:**

1. **File issues for remaining work** - Create issues for anything that needs follow-up
2. **Run quality gates** (if code changed) - Tests, linters, builds
3. **Update issue status** - Close finished work, update in-progress items
4. **PUSH TO REMOTE** - This is MANDATORY:
   ```bash
   git pull --rebase
   bd sync
   git push
   git status  # MUST show "up to date with origin"
   ```
5. **Clean up** - Clear stashes, prune remote branches
6. **Verify** - All changes committed AND pushed
7. **Hand off** - Provide context for next session

**CRITICAL RULES:**
- Work is NOT complete until `git push` succeeds
- NEVER stop before pushing - that leaves work stranded locally
- NEVER say "ready to push when you are" - YOU must push
- If push fails, resolve and retry until it succeeds

<!-- END BEADS INTEGRATION -->
