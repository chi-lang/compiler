## ADDED Requirements

### Requirement: Incomplete expressions trigger continuation prompt
The REPL SHALL detect incomplete expressions by counting unmatched opening brackets (`(`), braces (`{`), and square brackets (`[`). When the input has unclosed delimiters, the REPL SHALL prompt for continuation input instead of submitting the input to the compiler.

#### Scenario: Unclosed brace triggers continuation
- **WHEN** the user types `fn add(a: int, b: int) {` and presses Enter
- **THEN** the REPL displays a continuation prompt and waits for more input

#### Scenario: Unclosed parenthesis triggers continuation
- **WHEN** the user types `fn add(` and presses Enter
- **THEN** the REPL displays a continuation prompt and waits for more input

#### Scenario: Unclosed square bracket triggers continuation
- **WHEN** the user types `val arr = [1, 2,` and presses Enter
- **THEN** the REPL displays a continuation prompt and waits for more input

#### Scenario: Balanced delimiters submit immediately
- **WHEN** the user types `fn add(a: int, b: int) { a + b }` and presses Enter
- **THEN** the input is submitted to the compiler as a complete expression

### Requirement: Continuation prompt is visually distinct
The REPL SHALL display `.. ` (two dots followed by a space) as the continuation prompt, visually distinct from the primary prompt `> `.

#### Scenario: Continuation lines show secondary prompt
- **WHEN** the user types `fn foo() {` and presses Enter
- **THEN** the next line starts with the prompt `.. `

### Requirement: Brackets inside string literals are ignored
The bracket counter SHALL NOT count brackets that appear inside string literals (between unescaped `"` characters). Escaped quotes (`\"`) inside strings SHALL NOT toggle the string state.

#### Scenario: Braces inside strings do not trigger continuation
- **WHEN** the user types `val s = "{ hello }"` and presses Enter
- **THEN** the input is submitted immediately (braces inside the string are ignored)

#### Scenario: Escaped quote does not end string state
- **WHEN** the user types `val s = "escaped \" quote { still in string }"` and presses Enter
- **THEN** the input is submitted immediately (the `\"` does not end the string, so the braces are still inside the string)

### Requirement: String interpolation brackets are counted
Brackets inside string interpolation expressions (`${...}`) SHALL be counted for the purpose of multi-line detection, since they represent real code.

#### Scenario: Unclosed brace in interpolation triggers continuation
- **WHEN** the user types `val s = "value: ${` and presses Enter
- **THEN** the REPL displays a continuation prompt (the `${` opens an interpolation that is not closed)

#### Scenario: Balanced interpolation does not trigger continuation
- **WHEN** the user types `val s = "value: ${x + 1}"` and presses Enter
- **THEN** the input is submitted immediately

### Requirement: Multi-line input is submitted as a single expression
When the user completes a multi-line input (all delimiters are balanced), the REPL SHALL join all continuation lines and submit them to the compiler as a single input string.

#### Scenario: Multi-line function definition compiles correctly
- **WHEN** the user types the following across multiple lines:
  ```
  > fn add(a: int, b: int) {
  ..   a + b
  .. }
  ```
- **THEN** the complete input `fn add(a: int, b: int) {\n  a + b\n}` is submitted to the compiler

### Requirement: Negative bracket depth does not trigger continuation
If the bracket depth goes negative (more closing than opening delimiters), the REPL SHALL NOT trigger continuation. The input SHALL be submitted to the compiler, which will report the syntax error.

#### Scenario: Extra closing brace submits to compiler
- **WHEN** the user types `}` on an empty line
- **THEN** the input is submitted to the compiler (which reports a syntax error)
