## ADDED Requirements

### Requirement: History is persisted to disk
The REPL SHALL persist command history to a file on disk. The history file location SHALL be resolved as:
1. `$CHI_HOME/repl_history` if the `CHI_HOME` environment variable is set
2. `~/.chi_repl_history` if `CHI_HOME` is not set

The history file SHALL be created lazily on first REPL input if it does not exist. The parent directory MUST exist — the REPL SHALL NOT create intermediate directories.

#### Scenario: History saved to CHI_HOME when set
- **WHEN** `CHI_HOME` is set to `/home/user/.chi` and the user enters `val x = 1` in the REPL
- **THEN** the command is saved to `/home/user/.chi/repl_history`

#### Scenario: History saved to home directory when CHI_HOME is not set
- **WHEN** `CHI_HOME` is not set and the user enters `val x = 1` in the REPL
- **THEN** the command is saved to `~/.chi_repl_history`

#### Scenario: History file created on first use
- **WHEN** the history file does not yet exist and the user enters their first command
- **THEN** the history file is created and the command is written to it

### Requirement: History survives REPL restarts
The REPL SHALL load previously saved history from the history file on startup. Commands from prior sessions SHALL be accessible via up/down arrow navigation.

#### Scenario: Previous session commands are available
- **WHEN** the user starts a new REPL session after previously entering `val x = 42` in a prior session
- **THEN** pressing the Up arrow recalls `val x = 42`

### Requirement: History navigation via arrow keys
The REPL SHALL support navigating through command history using the Up and Down arrow keys. Up arrow SHALL recall older commands, Down arrow SHALL recall newer commands.

#### Scenario: User recalls previous command with Up arrow
- **WHEN** the user has entered `val x = 1` and then `val y = 2` and presses Up arrow once
- **THEN** the input line shows `val y = 2`

#### Scenario: User navigates back to newer command with Down arrow
- **WHEN** the user has pressed Up arrow twice (showing `val x = 1`) and then presses Down arrow once
- **THEN** the input line shows `val y = 2`

### Requirement: Reverse history search
The REPL SHALL support reverse incremental search via Ctrl+R. As the user types a search pattern, the REPL SHALL display the most recent matching history entry.

#### Scenario: User searches history with Ctrl+R
- **WHEN** the user presses Ctrl+R and types `fn`
- **THEN** the REPL displays the most recent history entry containing `fn`

### Requirement: History size is capped
The REPL SHALL store a maximum of 1000 history entries. When the limit is reached, the oldest entries SHALL be discarded to make room for new ones.

#### Scenario: History does not grow beyond limit
- **WHEN** the user has entered 1000 commands and enters one more
- **THEN** the oldest history entry is removed and the new command is stored

### Requirement: Dot-commands are stored in history
The REPL SHALL store dot-commands (e.g., `.help`, `.imports`) in the history alongside regular Chi expressions. All input lines SHALL be recorded in history.

#### Scenario: Dot-command appears in history
- **WHEN** the user types `.imports` and later presses Up arrow
- **THEN** `.imports` appears as a recallable history entry
