### Requirement: Handle body name checking
The compiler's `CheckNamesVisitor` SHALL visit the body of `handle` expressions during the name-checking pass. The body SHALL be visited in the outer scope (without `resume` or handler argument names in scope).

#### Scenario: Undefined name in handle body produces compile error
- **WHEN** a Chi program contains a `handle` expression whose body references an undefined variable
- **THEN** the compiler SHALL produce an `UnrecognizedName` error pointing to the undefined reference

#### Scenario: Defined name in handle body compiles successfully
- **WHEN** a Chi program contains a `handle` expression whose body references only names that are in scope (locally defined or from the symbol table)
- **THEN** the compiler SHALL NOT produce any `UnrecognizedName` error for the handle body

#### Scenario: Handler cases retain correct scoping
- **WHEN** a Chi program contains a `handle` expression with handler cases that reference `resume` or case argument names
- **THEN** the compiler SHALL NOT produce `UnrecognizedName` errors for `resume` or case argument names within their respective handler case bodies
