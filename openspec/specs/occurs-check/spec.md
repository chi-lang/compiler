### Requirement: Occurs check rejects circular type variable bindings
The unification algorithm SHALL perform an occurs check before binding a type variable to a type. If the variable being bound appears anywhere within the target type, unification SHALL fail with an `InfiniteType` error instead of recording the substitution.

#### Scenario: Self-application produces infinite type error
- **WHEN** the user compiles code containing `val f = { x -> x(x) }`
- **THEN** the compiler SHALL produce an error indicating an infinite/circular type was detected

#### Scenario: Direct circular constraint is rejected
- **WHEN** unification encounters a constraint binding variable `'a` to type `'a -> int`
- **THEN** the compiler SHALL reject the binding and report an infinite type error with the relevant source location

#### Scenario: Nested circular constraint is rejected
- **WHEN** unification encounters a constraint binding variable `'a` to type `int -> ('a -> bool)`
- **THEN** the compiler SHALL reject the binding because `'a` occurs nested within the target type

#### Scenario: Non-circular variable binding succeeds
- **WHEN** unification encounters a constraint binding variable `'a` to type `int -> bool`
- **THEN** the binding SHALL be accepted normally since `'a` does not occur in `int -> bool`

#### Scenario: Variable bound to itself is allowed
- **WHEN** unification encounters a constraint `'a = 'a`
- **THEN** the constraint SHALL be treated as trivially satisfied (handled by the `expected == actual` equality check before the variable branches)

### Requirement: Infinite type error message is clear and locatable
The `InfiniteType` error message SHALL include the variable and the type it was being bound to, and SHALL include the source code location (CodePoint) where the issue was detected.

#### Scenario: Error message content
- **WHEN** an infinite type is detected for variable `'a` in type `'a -> int`
- **THEN** the error message SHALL indicate that an infinite type was constructed and include the source position
