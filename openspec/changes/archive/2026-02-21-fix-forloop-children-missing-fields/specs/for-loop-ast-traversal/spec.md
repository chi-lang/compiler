## ADDED Requirements

### Requirement: ForLoop children include all sub-expressions
The `ForLoop.children()` method SHALL return all sub-expression fields: `iterable`, `state` (when non-null), `init` (when non-null), and `body`. Null fields SHALL be excluded from the returned list.

#### Scenario: For-loop with state and init
- **WHEN** a `ForLoop` node has non-null `state` and `init` fields
- **THEN** `children()` SHALL return a list containing `iterable`, `state`, `init`, and `body` in that order

#### Scenario: For-loop without state and init
- **WHEN** a `ForLoop` node has null `state` and `init` fields
- **THEN** `children()` SHALL return a list containing only `iterable` and `body`

### Requirement: ParseFor children include all sub-expressions
The `ParseFor.children()` method SHALL return all sub-expression fields: `iterable`, `state` (when non-null), `init` (when non-null), and `body`. Null fields SHALL be excluded from the returned list.

#### Scenario: ParseFor with state and init
- **WHEN** a `ParseFor` node has non-null `state` and `init` fields
- **THEN** `children()` SHALL return a list containing `iterable`, `state`, `init`, and `body` in that order

#### Scenario: ParseFor without state and init
- **WHEN** a `ParseFor` node has null `state` and `init` fields
- **THEN** `children()` SHALL return a list containing only `iterable` and `body`

### Requirement: Compiler passes visit for-loop state and init
All compiler visitor passes that use default tree traversal SHALL visit the `state` and `init` sub-expressions of for-loop nodes. This includes name checking, immutability checking, visibility checking, function call checking, usage marking, and type variable resolution.

#### Scenario: Undefined name in for-loop state expression detected
- **WHEN** a for-loop's `state` expression references an undefined variable
- **THEN** the compiler SHALL report an "unresolved name" error

#### Scenario: Undefined name in for-loop init expression detected
- **WHEN** a for-loop's `init` expression references an undefined variable
- **THEN** the compiler SHALL report an "unresolved name" error
