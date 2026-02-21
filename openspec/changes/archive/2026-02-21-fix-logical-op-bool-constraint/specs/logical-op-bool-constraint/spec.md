## ADDED Requirements

### Requirement: Logical AND operands must be bool
The type checker SHALL constrain both operands of the `&&` operator to `Type.bool`. If either operand is not `bool`, a type error MUST be reported pointing to the offending operand.

#### Scenario: Both operands are bool
- **WHEN** a Chi program contains `true && false`
- **THEN** the expression type-checks successfully with result type `bool`

#### Scenario: Bool variables with AND
- **WHEN** a Chi program contains `val a = true` and `val b = false` and `a && b`
- **THEN** the expression type-checks successfully with result type `bool`

#### Scenario: Left operand is not bool
- **WHEN** a Chi program contains `5 && true`
- **THEN** the compiler reports a type error indicating `int` is not `bool`

#### Scenario: Right operand is not bool
- **WHEN** a Chi program contains `true && 5`
- **THEN** the compiler reports a type error indicating `int` is not `bool`

#### Scenario: Both operands are non-bool
- **WHEN** a Chi program contains `5 && 3`
- **THEN** the compiler reports type errors for the non-bool operands

### Requirement: Logical OR operands must be bool
The type checker SHALL constrain both operands of the `||` operator to `Type.bool`. If either operand is not `bool`, a type error MUST be reported pointing to the offending operand.

#### Scenario: Both operands are bool
- **WHEN** a Chi program contains `true || false`
- **THEN** the expression type-checks successfully with result type `bool`

#### Scenario: Left operand is not bool
- **WHEN** a Chi program contains `"a" || true`
- **THEN** the compiler reports a type error indicating `string` is not `bool`

#### Scenario: Right operand is not bool
- **WHEN** a Chi program contains `true || "b"`
- **THEN** the compiler reports a type error indicating `string` is not `bool`

#### Scenario: Both operands are non-bool strings
- **WHEN** a Chi program contains `"a" || "b"`
- **THEN** the compiler reports type errors for the non-bool operands

### Requirement: Comparison operators remain unchanged
The comparison operators (`<`, `<=`, `>`, `>=`, `==`, `!=`) SHALL NOT require `bool` operands. They SHALL only constrain both operands to have the same type and return `bool`.

#### Scenario: Integer comparison still works
- **WHEN** a Chi program contains `3 < 5`
- **THEN** the expression type-checks successfully with result type `bool`

#### Scenario: Equality comparison still works
- **WHEN** a Chi program contains `"a" == "b"`
- **THEN** the expression type-checks successfully with result type `bool`
