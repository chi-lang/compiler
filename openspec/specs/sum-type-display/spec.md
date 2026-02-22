### Requirement: Anonymous sum types display component types
When a `Sum` type has an empty `ids` list (anonymous sum type), `toString()` SHALL return `"$lhs | $rhs"` showing the component types separated by ` | `.

#### Scenario: Anonymous sum type with two types
- **WHEN** a `Sum` type has `ids = emptyList()`, `lhs = Primitive("string")`, `rhs = Primitive("float")`
- **THEN** `toString()` SHALL return `"string | float"`

#### Scenario: Anonymous sum type appears in error message
- **WHEN** the compiler produces a type error involving an anonymous sum type (e.g., if-else returning `string` and `float`)
- **THEN** the error message SHALL contain the readable form (e.g., `"string | float"`) instead of `"[]"`

### Requirement: Named sum types display ids and component types
When a `Sum` type has a non-empty `ids` list (named sum type, not Option), `toString()` SHALL return `"$ids[$lhs | $rhs]"` showing both the type identity and component types.

#### Scenario: Named sum type with ids
- **WHEN** a `Sum` type has non-empty `ids` and is not an Option type
- **THEN** `toString()` SHALL return a string containing both the ids and the component types in `"$ids[$lhs | $rhs]"` format

### Requirement: Option type display is unchanged
The existing Option type branch of `Sum.toString()` SHALL remain unchanged.

#### Scenario: Option type display
- **WHEN** a `Sum` type has `Type.optionTypeId` in its `ids`
- **THEN** `toString()` SHALL return the existing format: `"$ids[${subtypes.joinToString("|")}]"` with `unit` removed from the subtypes list
