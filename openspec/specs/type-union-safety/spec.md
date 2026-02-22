### Requirement: Type.union handles duplicate types without crashing
`Type.union()` SHALL return the deduplicated type when all input types collapse to a single distinct type, instead of throwing a `ClassCastException`. The return type SHALL be `Type` (not `Sum`).

#### Scenario: Union of identical primitive types
- **WHEN** `Type.union(null, Type.int, Type.int)` is called
- **THEN** the result SHALL be `Type.int` (a `Primitive`, not a `Sum`)

#### Scenario: Union of identical types with a TypeId
- **WHEN** `Type.union(someId, Type.int, Type.int)` is called
- **THEN** the result SHALL NOT throw a `ClassCastException`
- **THEN** the result SHALL be a valid `Type`

#### Scenario: Union of distinct types still produces Sum
- **WHEN** `Type.union(null, Type.int, Type.string)` is called
- **THEN** the result SHALL be a `Sum` containing both `int` and `string`

#### Scenario: Union of multiple types with some duplicates
- **WHEN** `Type.union(null, Type.int, Type.string, Type.int)` is called
- **THEN** the result SHALL be a `Sum` containing `int` and `string` (deduplicated)
