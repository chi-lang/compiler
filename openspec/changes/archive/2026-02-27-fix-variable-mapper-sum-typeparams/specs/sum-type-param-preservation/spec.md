## ADDED Requirements

### Requirement: Sum type variable mapping SHALL preserve typeParams
When `VariableMapper.visitSum` transforms a `Sum` type by applying variable substitutions to its `lhs` and `rhs` subtypes, the resulting `Sum` type MUST retain the original `typeParams` list. This ensures parametric sum types (e.g., `Option[T]`) do not lose their type parameter names during polymorphic instantiation via `FreshenAboveVisitor` or any other `VariableMapper` subclass.

#### Scenario: Parametric sum type retains typeParams after variable mapping
- **WHEN** a `Sum` type with `typeParams = ["T"]` is processed by a `VariableMapper` subclass
- **THEN** the resulting `Sum` type MUST have `typeParams = ["T"]`

#### Scenario: FreshenAboveVisitor preserves typeParams during instantiation
- **WHEN** a polymorphic type containing a `Sum` with `typeParams = ["T"]` is instantiated via `PolyType.instantiate`
- **THEN** the freshened `Sum` type MUST retain `typeParams = ["T"]` with only type variables refreshed

#### Scenario: Sum type with empty typeParams is unaffected
- **WHEN** a `Sum` type with `typeParams = emptyList()` is processed by a `VariableMapper` subclass
- **THEN** the resulting `Sum` type MUST have `typeParams = emptyList()` (no change in behavior)
