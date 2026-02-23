## Why

The Chi REPL currently uses raw `readlnOrNull()` for input, which provides no line editing, no command history, and no multi-line support. Users cannot use arrow keys to navigate or edit input, cannot recall previous commands, and must resort to a semicolon hack (`;` → newline) to enter multi-line expressions. This makes the REPL painful for any non-trivial interactive use and falls far below the experience users expect from a modern language REPL.

## What Changes

- **Add JLine 3 as a dependency** for terminal handling, line editing, and history management
- **Replace `readlnOrNull()`** input with JLine's `LineReader` providing full readline-style editing (cursor movement, kill/yank, word navigation)
- **Persistent command history** saved to `$CHI_HOME/repl_history` (or `~/.chi_repl_history` if `CHI_HOME` is not set), surviving REPL restarts, with configurable history size
- **Multi-line input** with bracket/brace/paren matching — the REPL detects incomplete expressions and prompts for continuation lines with an indented `.. ` prompt instead of `> `
- **Reverse history search** via Ctrl+R, matching standard readline behavior
- **Tab completion** for dot-commands (`.exit`, `.help`, `.toggleLuaCode`, `.imports`, `.clearImports`, `.vi`, `.emacs`) and Chi keywords
- **Editing mode toggle** — `.vi` and `.emacs` dot-commands to switch between vi and emacs keybinding modes at runtime (emacs by default)
- **Remove the semicolon-as-newline hack** — no longer needed once true multi-line input works; semicolons become regular statement separators as in source files

## Capabilities

### New Capabilities

- `repl-line-editing`: Readline-style line editing in the REPL — cursor movement, kill/yank, word navigation, Home/End, and standard keybindings via JLine 3. Supports emacs (default) and vi editing modes, switchable at runtime via `.vi` and `.emacs` commands
- `repl-history`: Persistent command history with file-based storage (`$CHI_HOME/repl_history` or `~/.chi_repl_history` fallback), up/down arrow recall, and Ctrl+R reverse search
- `repl-multiline`: Multi-line input with automatic detection of incomplete expressions (unclosed brackets, braces, parens) and continuation prompts
- `repl-completion`: Tab completion for REPL dot-commands and Chi language keywords

### Modified Capabilities

_(none — no existing specs are affected)_

## Impact

- **Code**: `Repl.kt` is substantially rewritten; `Main.kt` unchanged (Repl constructor stays the same)
- **Dependencies**: New dependency on JLine 3 (`org.jline:jline:3.x`). JLine is a mature, widely-used library (used by Scala REPL, Groovy, JShell, Clojure). It has no transitive dependencies that conflict with the current stack.
- **Build**: `build.gradle` gains one `implementation` dependency. The `shadowJar` fat JAR will include JLine. The `standardInput = System.in` wiring in the `run` task remains necessary.
- **User-visible**: REPL UX improves significantly. The semicolon-as-newline convention is removed (**BREAKING** for users relying on `;` to enter multi-line code, but multi-line input replaces this naturally).
- **Files on disk**: New history file at `$CHI_HOME/repl_history` (or `~/.chi_repl_history` if `CHI_HOME` is not set), created on first REPL use.
- **Tests**: No existing tests break (there are no REPL tests). New integration-level tests should verify history persistence and multi-line detection.
