## Context

The Chi REPL (`Repl.kt`, ~130 lines) currently reads input via `readlnOrNull()` with no line editing, history, or multi-line support. Users type one-line expressions with a `;`-as-newline hack for multi-line code. The REPL supports dot-commands (`.exit`, `.help`, `.toggleLuaCode`, `.imports`, `.clearImports`) and raw Lua passthrough (`@` prefix).

The project uses Gradle with a `shadowJar` fat JAR, JVM 25 toolchain, and has no existing terminal/readline dependencies. The REPL is launched from `Main.kt` by constructing `Repl(env)` and calling `.run()`.

## Goals / Non-Goals

**Goals:**

- Replace raw stdin reading with JLine 3's `LineReader` for full readline-style editing
- Persist command history to disk, respecting `$CHI_HOME/repl_history` with `~/.chi_repl_history` fallback
- Detect incomplete expressions (unclosed brackets/braces/parens) and prompt for continuation lines
- Provide tab completion for dot-commands and Chi keywords
- Keep the `Repl` class API unchanged — `Repl(env).run()` still works

**Non-Goals:**

- Syntax highlighting in the REPL (possible future enhancement with JLine's `Highlighter`, but not in this change)
- Completion of user-defined symbols (variables, functions) — would require querying the compilation environment at runtime, deferred to a future change
- Custom key bindings beyond what JLine provides out of the box
- Windows-specific terminal handling — JLine handles this transparently

## Decisions

### 1. JLine 3 as the terminal library

**Decision:** Use `org.jline:jline:3.28.0` (latest stable).

**Alternatives considered:**

- **Raw `System.in` + ANSI escape codes**: Would require reimplementing line editing, history, and terminal detection from scratch. Enormous effort for a solved problem.
- **JLine 2**: Legacy, no longer maintained. JLine 3 is the active version.
- **Lanterna**: Full TUI framework — overkill for a line-oriented REPL.
- **JReadline / Aesh**: Red Hat's readline library. Less widely adopted than JLine; JLine has broader ecosystem support (Scala, Groovy, JShell, Clojure all use it).

**Rationale:** JLine 3 is the de facto standard for JVM terminal interaction. Single dependency (`org.jline:jline`), no transitive conflicts, well-documented, actively maintained.

### 2. History file location strategy

**Decision:** Resolve history path as:

1. If `$CHI_HOME` is set → `$CHI_HOME/repl_history`
2. Otherwise → `~/.chi_repl_history`

Implemented as a simple function:

```kotlin
private fun historyPath(): Path {
    val chiHome = System.getenv("CHI_HOME")
    return if (chiHome != null) {
        Path.of(chiHome, "repl_history")
    } else {
        Path.of(System.getProperty("user.home"), ".chi_repl_history")
    }
}
```

History size capped at 1000 entries (JLine's `DefaultHistory` default). The file is created lazily on first REPL input.

### 3. Multi-line detection via bracket counting

**Decision:** Implement a JLine `Parser` that counts unmatched `(`, `{`, `[` delimiters. When the count is positive after a line, JLine automatically requests a continuation line (secondary prompt `.. `).

**Approach:** Scan the input character-by-character, tracking:

- Bracket depth for each pair: `()`, `{}`, `[]`
- String literal state (inside `"..."`) — brackets inside strings don't count
- Escape sequences inside strings (`\"`, `\\`) — so escaped quotes don't toggle string state
- String interpolation (`${...}`) — braces inside interpolation do count

When total bracket depth > 0 after processing all input, signal `EOFError` to JLine, which triggers the continuation prompt.

**Alternatives considered:**

- **ANTLR-based parsing for completeness detection**: Would give perfect accuracy but adds significant complexity — need to handle parse errors gracefully and distinguish "incomplete" from "invalid". The bracket-counting heuristic covers the vast majority of real cases (blocks, function definitions, lambdas, argument lists, array literals).
- **Trailing operator detection** (e.g., line ends with `+`, `&&`): Deferred — adds complexity and edge cases. Users can always use explicit brackets.

**Known limitation:** The heuristic won't detect incomplete expressions that don't involve brackets, like `val x =` on its own. This is acceptable — the user can wrap in braces or simply type the value on the same line.

### 4. Completion strategy

**Decision:** Implement a JLine `Completer` that handles two categories:

1. **Dot-commands**: When input starts with `.`, complete from the registered command map keys (`.exit`, `.help`, `.toggleLuaCode`, `.imports`, `.clearImports`, `.vi`, `.emacs`)
2. **Keywords**: When typing a bare word, complete from Chi's 27 keywords: `pub`, `val`, `var`, `fn`, `if`, `else`, `as`, `while`, `for`, `in`, `package`, `import`, `data`, `when`, `match`, `is`, `break`, `continue`, `effect`, `handle`, `with`, `return`, `trait`, `type`, `unit`, `true`, `false`

Completion is context-free — it does not inspect compilation state.

### 5. Architecture: new classes alongside Repl

**Decision:** Extract terminal concerns into focused components, keep `Repl` as the orchestrator:

- **`ChiLineReader`** (or factory function): Builds the configured JLine `LineReader` with history, parser, and completer wired up
- **`ChiReplParser`**: Implements JLine's `Parser` interface for multi-line detection
- **`ChiReplCompleter`**: Implements JLine's `Completer` interface for dot-commands and keywords

`Repl.kt` changes:

- Constructor gains no new parameters (JLine setup is internal)
- `run()` replaces `readlnOrNull()` loop with `lineReader.readLine("> ")` / `readLine(".. ")`
- The `;`-to-newline replacement is removed
- Dot-command handling and eval logic remain unchanged
- Terminal and history are cleaned up via `terminal.close()` on exit

### 6. Editing mode: emacs default with vi/emacs toggle

**Decision:** The REPL starts in emacs mode (JLine default). Two new dot-commands allow runtime switching:

- `.vi` — switches to vi editing mode
- `.emacs` — switches back to emacs editing mode

Implementation via JLine's `KeyMap` switching:

```kotlin
".vi" to { _: Array<String> ->
    lineReader.keyMaps[LineReader.VIINS]?.let { lineReader.keyMap = LineReader.VIINS }
    println("Switched to vi mode.")
},
".emacs" to { _: Array<String> ->
    lineReader.keyMaps[LineReader.EMACS]?.let { lineReader.keyMap = LineReader.EMACS }
    println("Switched to emacs mode.")
}
```

**Rationale:** JLine supports both modes natively. Exposing them as dot-commands is consistent with the existing REPL command pattern and requires no configuration files. Emacs is the default because it matches most terminal defaults and readline conventions.

### 7. Semicolon handling

**Decision:** Remove the `replace(";", "\n")` hack. Semicolons in Chi source are not meaningful to the grammar (they're not defined as tokens in the lexer). The hack was a workaround for lack of multi-line — with proper multi-line support, it's unnecessary.

After removal, typing `val x = 1; val y = 2` will be passed to the compiler as-is. Since `;` is not in the lexer, ANTLR will report it as a token recognition error. This is the correct behavior — the user should use multi-line input instead.

**Migration:** This is a breaking change for users who rely on `;`. The new multi-line support is the replacement path.

## Risks / Trade-offs

**[JLine JAR size increases fat JAR]** → JLine 3 adds ~1.5MB to the shadow JAR. Acceptable for a development tool.

**[Bracket heuristic misses edge cases]** → Expressions like `val x =` (incomplete but no unclosed bracket) won't trigger continuation. → Users can wrap in `{ }` or type on one line. Can be improved later without API changes.

**[String interpolation complicates bracket counting]** → `"text ${expr} more"` contains `{` `}` that should be counted. → The parser tracks string state and interpolation depth, handling this correctly. Tested explicitly.

**[Terminal detection in non-interactive contexts]** → If stdin is piped (not a TTY), JLine's `LineReader` may behave differently. → JLine has built-in dumb-terminal fallback. The `run` task's `standardInput = System.in` continues to work because JLine detects terminal capabilities automatically.

**[History file permissions]** → History file may contain sensitive data typed in REPL. → File is created with default user permissions (umask). No special handling needed — same as bash_history.

**[Breaking: semicolon removal]** → Users relying on `;` for multi-line will need to adapt. → Multi-line input is strictly superior. Document in release notes.
