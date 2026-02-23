## ADDED Requirements

### Requirement: REPL uses JLine LineReader for input
The REPL SHALL use JLine 3's `LineReader` for all user input instead of raw `readlnOrNull()`. The `LineReader` SHALL provide standard readline-style editing capabilities including cursor movement (left/right arrow), word navigation (Ctrl+Left, Ctrl+Right, Alt+B, Alt+F), Home/End, kill/yank (Ctrl+K, Ctrl+Y), and line clearing (Ctrl+U).

#### Scenario: User navigates within a line using arrow keys
- **WHEN** the user types `val x = 42` and presses Left arrow 3 times
- **THEN** the cursor moves to the position before `42`, allowing in-place editing

#### Scenario: User deletes to end of line
- **WHEN** the user types `val x = hello world` and moves cursor after `hello` and presses Ctrl+K
- **THEN** the text ` world` is killed (removed and stored in kill ring) and the line reads `val x = hello`

#### Scenario: User yanks previously killed text
- **WHEN** the user presses Ctrl+Y after a previous Ctrl+K operation
- **THEN** the previously killed text is inserted at the cursor position

### Requirement: REPL defaults to emacs editing mode
The REPL SHALL start in emacs editing mode by default. JLine's standard emacs keybindings SHALL be active on startup.

#### Scenario: REPL starts in emacs mode
- **WHEN** the user launches the REPL without any prior configuration
- **THEN** emacs keybindings are active (Ctrl+A moves to beginning of line, Ctrl+E to end, Ctrl+K kills to end)

### Requirement: User can switch to vi editing mode
The REPL SHALL provide a `.vi` dot-command that switches the editing mode to vi. In vi mode, the user starts in insert mode and can press Escape to enter normal/command mode with standard vi navigation.

#### Scenario: User switches to vi mode
- **WHEN** the user types `.vi` and presses Enter
- **THEN** the REPL prints a confirmation message and subsequent input uses vi keybindings

#### Scenario: Vi mode navigation works
- **WHEN** the user is in vi mode and presses Escape, then `0`
- **THEN** the cursor moves to the beginning of the line (vi normal mode behavior)

### Requirement: User can switch back to emacs editing mode
The REPL SHALL provide a `.emacs` dot-command that switches the editing mode back to emacs.

#### Scenario: User switches from vi back to emacs
- **WHEN** the user types `.emacs` and presses Enter while in vi mode
- **THEN** the REPL prints a confirmation message and subsequent input uses emacs keybindings

### Requirement: REPL displays a primary prompt
The REPL SHALL display `> ` (greater-than followed by a space) as the primary input prompt.

#### Scenario: Primary prompt is displayed
- **WHEN** the REPL is ready for new input
- **THEN** the prompt `> ` is displayed at the beginning of the line

### Requirement: Semicolon-as-newline hack is removed
The REPL SHALL NOT replace semicolons with newlines in user input. Input SHALL be passed to the compiler as-is.

#### Scenario: Semicolons are not translated to newlines
- **WHEN** the user types `val x = 1; val y = 2`
- **THEN** the input `val x = 1; val y = 2` is passed to the compiler without modification (the compiler will report `;` as an unrecognized token)
