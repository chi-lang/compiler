## 1. Dependencies and build setup

- [ ] 1.1 Add `org.jline:jline:3.28.0` as `implementation` dependency in `build.gradle`
- [ ] 1.2 Verify the project compiles with `./gradlew classes` and JLine is resolved

## 2. Multi-line parser (ChiReplParser)

- [ ] 2.1 Create `ChiReplParser` class implementing JLine's `org.jline.reader.Parser` interface in `src/main/kotlin/gh/marad/chi/`
- [ ] 2.2 Implement character-by-character bracket counting for `(`, `{`, `[` with matching closers
- [ ] 2.3 Track string literal state (`"..."`) so brackets inside strings are ignored
- [ ] 2.4 Handle escape sequences inside strings (`\"`, `\\`) so escaped quotes don't toggle string state
- [ ] 2.5 Handle string interpolation (`${...}`) — exit string state on `${`, re-enter on matching `}`
- [ ] 2.6 Throw `EOFError` when total bracket depth > 0 (triggers continuation prompt)
- [ ] 2.7 Pass input through when bracket depth <= 0 (balanced or negative)
- [ ] 2.8 Write unit tests for `ChiReplParser`: balanced input, unclosed `{`, `(`, `[`, brackets inside strings, escaped quotes in strings, string interpolation with `${`, negative depth, nested delimiters

## 3. Tab completer (ChiReplCompleter)

- [ ] 3.1 Create `ChiReplCompleter` class implementing JLine's `org.jline.reader.Completer` interface in `src/main/kotlin/gh/marad/chi/`
- [ ] 3.2 Implement dot-command completion: when the current word starts with `.`, match against `.exit`, `.help`, `.toggleLuaCode`, `.imports`, `.clearImports`, `.vi`, `.emacs`
- [ ] 3.3 Implement keyword completion: when the current word does not start with `.`, match against the 27 Chi keywords
- [ ] 3.4 Ensure completion applies only to the current word at cursor position
- [ ] 3.5 Write unit tests for `ChiReplCompleter`: dot-command prefix matching, keyword prefix matching, no match returns empty, mid-line word completion

## 4. Rewrite Repl.kt to use JLine

- [ ] 4.1 Create a `historyPath()` function resolving `$CHI_HOME/repl_history` with `~/.chi_repl_history` fallback
- [ ] 4.2 Build a JLine `Terminal` via `TerminalBuilder.builder().system(true).build()`
- [ ] 4.3 Build a `LineReader` via `LineReaderBuilder` wiring in `ChiReplParser`, `ChiReplCompleter`, history file path, and history size (1000)
- [ ] 4.4 Set the secondary prompt to `".. "` for continuation lines
- [ ] 4.5 Replace the `readlnOrNull()` call with `lineReader.readLine("> ")`
- [ ] 4.6 Handle `UserInterruptException` (Ctrl+C) to cancel current input and show a new prompt
- [ ] 4.7 Handle `EndOfFileException` (Ctrl+D) to exit the REPL gracefully
- [ ] 4.8 Remove the `replace(";", "\n")` hack — pass input to the compiler as-is
- [ ] 4.9 Add `terminal.close()` cleanup on REPL exit (`.exit` command and Ctrl+D)

## 5. Editing mode dot-commands

- [ ] 5.1 Add `.vi` dot-command that switches the `LineReader` keymap to `LineReader.VIINS` and prints confirmation
- [ ] 5.2 Add `.emacs` dot-command that switches the `LineReader` keymap to `LineReader.EMACS` and prints confirmation
- [ ] 5.3 Update `.help` output to include `.vi` and `.emacs` in the listed commands

## 6. Integration testing and verification

- [ ] 6.1 Write unit test for `historyPath()`: returns `$CHI_HOME/repl_history` when env is set, `~/.chi_repl_history` otherwise
- [ ] 6.2 Manual smoke test: launch REPL, verify `> ` prompt, arrow key navigation, Ctrl+A/Ctrl+E work
- [ ] 6.3 Manual smoke test: enter multi-line expression with `{`, verify `.. ` continuation prompt, verify compiled result
- [ ] 6.4 Manual smoke test: exit and restart REPL, verify Up arrow recalls commands from prior session
- [ ] 6.5 Manual smoke test: test `.vi` / `.emacs` switching, verify Escape enters vi normal mode
- [ ] 6.6 Manual smoke test: test Tab completion for `.ex` → `.exit` and `whi` → `while`
- [ ] 6.7 Run full test suite with `./gradlew test` to verify no regressions
- [ ] 6.8 Build fat JAR with `./gradlew shadowJar` and verify it includes JLine classes
