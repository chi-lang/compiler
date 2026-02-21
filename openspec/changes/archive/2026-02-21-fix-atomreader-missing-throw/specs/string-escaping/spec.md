## MODIFIED Requirements

### Requirement: Unrecognized string parts SHALL produce a compiler error
When the parser encounters a string part that does not match any known token type, it SHALL throw a `CompilerMessage` error rather than silently dropping the part.

#### Scenario: Unknown escape sequence in string
- **WHEN** a string part does not match any recognized token type (TEXT, ESCAPED_*, ID_INTERP, ENTER_EXPR)
- **THEN** the parser SHALL throw a `CompilerMessage` with a descriptive error message including the unrecognized part

#### Scenario: Error message includes part details
- **WHEN** an unrecognized string part is encountered
- **THEN** the `CompilerMessage` SHALL include the text "Unsupported string part" and the string representation of the unrecognized part
