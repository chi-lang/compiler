## MODIFIED Requirements

### Requirement: Occurs check rejects circular type variable bindings
The unification algorithm SHALL perform an occurs check before binding a type variable to a type. If the variable being bound appears within the target type in a way that constitutes a genuine circular reference, unification SHALL fail with an `InfiniteType` error instead of recording the substitution. However, when the target type is a sum type and the variable appears only as a direct top-level branch (not nested inside any other type constructor within the sum), the binding SHALL be accepted as valid sum-type widening — the occurs check SHALL NOT fire in this case.

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

#### Scenario: Variable as direct branch of sum is accepted (widening)
- **WHEN** unification encounters a constraint binding variable `'T` to type `'T | unit`
- **THEN** the binding SHALL be accepted as sum-type widening, not rejected as infinite type
