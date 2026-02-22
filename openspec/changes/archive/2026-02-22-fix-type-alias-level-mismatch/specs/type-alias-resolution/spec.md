## ADDED Requirements

### Requirement: Type alias substitution matches variables by definition-site level
When resolving a `TypeConstructorRef` that refers to a type alias, the compiler SHALL construct replacement source variables at the level stored in the alias body (definition-site level), not the call-site level. This ensures `VariableReplacer` matches and replaces the alias's type parameters correctly.

#### Scenario: Generic type alias used in a generic function signature compiles without error
- **WHEN** the user compiles code containing a type alias `type Option[T] = T | unit` and a generic function `pub fn valueOr[T](opt: Option[T], alternative: T): T { if opt is unit { alternative } else { opt as T } }`
- **THEN** the compiler SHALL produce no errors and the resolved type of parameter `opt` SHALL have its type variable replaced with the call-site variable (no stale definition-site variables remain)

#### Scenario: Generic type alias with multiple type parameters resolves correctly
- **WHEN** the user compiles code containing a type alias `type Either[A, B] = A | B` and a function `fn first[A, B](e: Either[A, B]): A | B { e }`
- **THEN** the compiler SHALL produce no errors and both type parameters SHALL be correctly substituted in the resolved type

#### Scenario: Non-generic type alias is unaffected
- **WHEN** the user compiles code containing a type alias `type Name = string` and a function `fn greet(n: Name): string { n }`
- **THEN** the compiler SHALL produce no errors (no regression for aliases without type parameters)

### Requirement: Occurs check continues to reject genuinely infinite types
The occurs check added by BUG-02 SHALL remain active and continue to reject truly circular type variable bindings. The type alias fix SHALL NOT weaken or bypass the occurs check.

#### Scenario: Self-application still produces infinite type error
- **WHEN** the user compiles code containing `val f = { x -> x(x) }`
- **THEN** the compiler SHALL produce an "Infinite type" error
