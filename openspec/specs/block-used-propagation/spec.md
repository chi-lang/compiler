## ADDED Requirements

### Requirement: Block propagates used flag to last expression
The `UsageMarker.visitBlock` method SHALL set the `used` flag on the last expression in the block's body to match the block's own `used` flag value, rather than unconditionally setting it to `true`.

#### Scenario: Block result is consumed by parent
- **WHEN** a block expression is in a position where its result is consumed (e.g., assigned to a variable, passed as argument, returned from if-else branch)
- **THEN** the last expression in the block body SHALL have `used = true`

#### Scenario: Block result is discarded
- **WHEN** a block expression is in a position where its result is discarded (e.g., a statement-level block, or inside another unused block)
- **THEN** the last expression in the block body SHALL have `used = false`

#### Scenario: Empty block
- **WHEN** a block expression has an empty body
- **THEN** no expressions are modified and no error occurs

### Requirement: Is-check on type variable in unused block does not error
The compiler SHALL NOT emit an error for an `is` expression checking a type variable when the `is` expression is inside a block whose result is discarded.

#### Scenario: Is-check on type variable in discarded block compiles without error
- **WHEN** Chi source contains an `is` expression checking a type variable inside a block whose result is not consumed
- **THEN** compilation SHALL succeed without a "cannot check type variables" error

#### Scenario: Is-check on type variable in used block still errors
- **WHEN** Chi source contains an `is` expression checking a type variable inside a block whose result IS consumed
- **THEN** compilation SHALL produce the "cannot check type variables" error
