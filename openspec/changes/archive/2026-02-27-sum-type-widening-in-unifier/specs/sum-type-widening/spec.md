## ADDED Requirements

### Requirement: Unifier accepts sum-type widening when variable is a direct branch
When unifying a type variable with a sum type, the unifier SHALL accept the binding without triggering an infinite type error if the variable appears only as a direct (top-level) branch of the sum type. The variable SHALL be bound to the full sum type. This applies symmetrically regardless of which side (expected or actual) is the variable.

#### Scenario: Variable unified with sum containing it as direct branch
- **WHEN** unification encounters a constraint `'T = Sum('T, unit)`
- **THEN** the unifier SHALL bind `'T` to `Sum('T, unit)` without error, treating this as valid widening

#### Scenario: asOption function compiles successfully
- **WHEN** the user compiles `fn asOption[T](value: T): T | unit { value }`
- **THEN** the compiler SHALL produce no errors, recognizing that returning `T` where `T | unit` is expected is valid widening

#### Scenario: map over Option compiles successfully
- **WHEN** the user compiles a function `fn map[T,R](opt: T | unit, f: (T) -> R): R | unit { if opt is unit { unit } else { opt is T; f(opt) } }`
- **THEN** the compiler SHALL produce no errors

#### Scenario: ifPresent over Option compiles successfully
- **WHEN** the user compiles a function `fn ifPresent[T](opt: T | unit, f: (T) -> unit) { if opt is unit { unit } else { opt is T; f(opt) } }`
- **THEN** the compiler SHALL produce no errors

### Requirement: Variable nested inside a constructor within a sum is still rejected
When unifying a type variable with a sum type, the unifier SHALL reject the binding with an infinite type error if the variable appears inside a non-sum type constructor (e.g., function type, record type) within any branch of the sum, even if it also appears as a direct branch.

#### Scenario: Variable inside function type within sum is rejected
- **WHEN** unification encounters a constraint `'T = Sum(Function('T, int), unit)`
- **THEN** the unifier SHALL reject the binding with an `InfiniteType` error because `'T` occurs nested inside a function type within the sum

#### Scenario: Variable only as direct branch with no nested occurrence is accepted
- **WHEN** unification encounters a constraint `'T = Sum('T, int, unit)`
- **THEN** the unifier SHALL accept the binding because `'T` appears only as a direct branch, not nested inside any constructor in the other branches
