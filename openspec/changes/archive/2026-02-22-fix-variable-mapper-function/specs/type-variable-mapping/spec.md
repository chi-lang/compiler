## ADDED Requirements

### Requirement: VariableMapper preserves Function metadata during type mapping
The `VariableMapper.visitFunction` method SHALL preserve all fields of a `Function` type when mapping type variables. Specifically, `typeParams` and `defaultArgs` MUST be carried through to the resulting `Function` instance.

#### Scenario: Polymorphic function with default args retains defaultArgs after instantiation
- **WHEN** a polymorphic function type with `defaultArgs = 1` is instantiated via `PolyType.instantiate`
- **THEN** the resulting `Function` type SHALL have `defaultArgs = 1`

#### Scenario: Polymorphic function retains typeParams after instantiation
- **WHEN** a polymorphic function type with `typeParams = ["T"]` is instantiated via `PolyType.instantiate`
- **THEN** the resulting `Function` type SHALL have its `typeParams` preserved (mapped appropriately)

#### Scenario: Function type with no defaults is unaffected
- **WHEN** a `Function` type with `defaultArgs = 0` and empty `typeParams` is mapped through `VariableMapper`
- **THEN** the resulting `Function` type SHALL have `defaultArgs = 0` and empty `typeParams` (no regression)

### Requirement: Polymorphic functions with default arguments callable with omitted defaults
A Chi function declared with type parameters and default argument values SHALL remain callable with the default arguments omitted after polymorphic type instantiation.

#### Scenario: Calling polymorphic function omitting default argument
- **WHEN** a polymorphic function `fn foo[T](a: T, b: int = 5): T` is called as `foo(someValue)`
- **THEN** the compiler SHALL accept the call, applying the default value for parameter `b`

#### Scenario: Calling polymorphic function providing all arguments
- **WHEN** a polymorphic function `fn foo[T](a: T, b: int = 5): T` is called as `foo(someValue, 10)`
- **THEN** the compiler SHALL accept the call with both arguments provided
