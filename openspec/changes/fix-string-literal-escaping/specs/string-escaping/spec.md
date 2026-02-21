## ADDED Requirements

### Requirement: Lua emitter SHALL escape special characters in string literals
When emitting a Chi string literal as a Lua single-quoted string, the emitter SHALL escape all characters that would break Lua string syntax or produce incorrect values. At minimum: backslash, single quote, newline, carriage return, tab, and null byte.

#### Scenario: String containing a newline
- **WHEN** a Chi string literal contains `\n` (e.g., `"hello\nworld"`)
- **THEN** the emitted Lua code SHALL contain `\n` as a two-character escape sequence inside the string literal, not a raw newline byte

#### Scenario: String containing a backslash
- **WHEN** a Chi string literal contains `\\` (e.g., `"path\\to\\file"`)
- **THEN** the emitted Lua code SHALL contain `\\` as escaped backslashes, not raw backslash characters that Lua would interpret as escape prefixes

#### Scenario: String containing a single quote
- **WHEN** a Chi string value contains a literal single quote (e.g., from `'` in source text)
- **THEN** the emitted Lua code SHALL escape it as `\'` so the single-quoted Lua string remains valid

#### Scenario: String containing a tab
- **WHEN** a Chi string literal contains `\t`
- **THEN** the emitted Lua code SHALL contain `\t` as a two-character escape sequence, not a raw tab byte

#### Scenario: String containing a carriage return
- **WHEN** a Chi string literal contains `\r`
- **THEN** the emitted Lua code SHALL contain `\r` as a two-character escape sequence, not a raw CR byte

#### Scenario: String containing a null byte
- **WHEN** a Chi string value contains a null byte
- **THEN** the emitted Lua code SHALL contain `\0` as an escape sequence

#### Scenario: Plain string without special characters
- **WHEN** a Chi string literal contains only printable ASCII characters with no special characters
- **THEN** the emitted Lua code SHALL pass the string through unchanged

### Requirement: Unrecognized string parts SHALL produce a compiler error
When the parser encounters a string part that does not match any known token type, it SHALL throw a `CompilerMessage` error rather than silently dropping the part.

#### Scenario: Unknown escape sequence in string
- **WHEN** a string part does not match any recognized token type (TEXT, ESCAPED_*, ID_INTERP, ENTER_EXPR)
- **THEN** the parser SHALL throw a `CompilerMessage` with a descriptive error message including the unrecognized part
