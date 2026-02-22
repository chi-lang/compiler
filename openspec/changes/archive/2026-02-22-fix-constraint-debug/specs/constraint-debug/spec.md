## ADDED Requirements

### Requirement: Debug stack trace capture is disabled by default

The `Constraint` class debug flag SHALL be set to `false` by default so that constraint construction does not capture JVM stack traces during normal compilation.

#### Scenario: Normal compilation without debug overhead
- **WHEN** the compiler performs type inference and creates `Constraint` instances
- **THEN** no `RuntimeException` SHALL be allocated and `Constraint.from` SHALL be `null`

#### Scenario: Debug flag can be re-enabled
- **WHEN** a developer changes `debug` to `true` and recompiles
- **THEN** each `Constraint` construction SHALL capture the creating stack frame in `Constraint.from`
