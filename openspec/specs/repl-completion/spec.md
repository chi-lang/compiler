## ADDED Requirements

### Requirement: Tab completes dot-commands
The REPL SHALL provide tab completion for dot-commands. When the user types `.` followed by a partial command name and presses Tab, the REPL SHALL offer matching dot-commands as completion candidates. The completable dot-commands SHALL include: `.exit`, `.help`, `.toggleLuaCode`, `.imports`, `.clearImports`, `.vi`, `.emacs`.

#### Scenario: Partial dot-command is completed
- **WHEN** the user types `.ex` and presses Tab
- **THEN** the input is completed to `.exit`

#### Scenario: Ambiguous dot-command shows candidates
- **WHEN** the user types `.` and presses Tab
- **THEN** all available dot-commands are shown as completion candidates

#### Scenario: Dot-command with unique prefix completes immediately
- **WHEN** the user types `.tog` and presses Tab
- **THEN** the input is completed to `.toggleLuaCode`

### Requirement: Tab completes Chi keywords
The REPL SHALL provide tab completion for Chi language keywords. When the user types a partial keyword and presses Tab, the REPL SHALL offer matching keywords as completion candidates. The completable keywords SHALL be: `pub`, `val`, `var`, `fn`, `if`, `else`, `as`, `while`, `for`, `in`, `package`, `import`, `data`, `when`, `match`, `is`, `break`, `continue`, `effect`, `handle`, `with`, `return`, `trait`, `type`, `unit`, `true`, `false`.

#### Scenario: Partial keyword is completed
- **WHEN** the user types `whi` and presses Tab
- **THEN** the input is completed to `while`

#### Scenario: Ambiguous keyword prefix shows candidates
- **WHEN** the user types `co` and presses Tab
- **THEN** the REPL shows `continue` as a completion candidate

#### Scenario: Single-character prefix with multiple matches
- **WHEN** the user types `f` and presses Tab
- **THEN** the REPL shows `fn`, `for`, `false` as completion candidates

### Requirement: Completion is context-free
The REPL completer SHALL operate without inspecting compilation state. Completion SHALL be based solely on the current word being typed, matching against the static lists of dot-commands and keywords. User-defined symbols (variables, functions) SHALL NOT be completed.

#### Scenario: User-defined variable is not completed
- **WHEN** the user has defined `val myVariable = 42` and later types `myV` and presses Tab
- **THEN** no completion is offered (user-defined symbols are not in the completion set)

### Requirement: Completion only applies to current word
The REPL completer SHALL only complete the word at the current cursor position. Words earlier in the line SHALL NOT affect completion candidates.

#### Scenario: Keyword completion mid-expression
- **WHEN** the user types `val x = ` and then types `tr` and presses Tab
- **THEN** the REPL shows `true` and `trait` as completion candidates (based on the current word `tr`, not the whole line)
