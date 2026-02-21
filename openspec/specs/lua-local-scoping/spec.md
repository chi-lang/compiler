### Requirement: Non-top-level functions use local scoping in Lua output
The Lua code emitter SHALL emit all non-top-level function definitions (lambdas, anonymous functions, generated helpers) with the `local function` prefix so that they are scoped locally and do not pollute the Lua global namespace.

#### Scenario: Lambda assigned to a variable emits local function
- **WHEN** a Chi program contains a lambda assigned to a variable (e.g., `val f = { x: int -> x + 1 }`)
- **THEN** the emitted Lua code for the lambda body SHALL use `local function tmpN(...)` instead of `function tmpN(...)`

#### Scenario: Anonymous function passed as argument emits local function
- **WHEN** a Chi program passes an anonymous function as a function argument
- **THEN** the emitted Lua code for that anonymous function SHALL use `local function tmpN(...)` syntax

#### Scenario: Top-level named functions remain package-scoped
- **WHEN** a Chi program defines a top-level named function
- **THEN** the emitted Lua code SHALL continue to use `function __P_.name(...)` syntax (no change)

### Requirement: Cross-module isolation of temporary function names
Separately compiled Chi modules SHALL NOT have their emitted temporary function names collide at Lua runtime, because all temporary functions are locally scoped.

#### Scenario: Two modules with lambdas do not interfere
- **WHEN** two Chi modules are compiled separately and both contain lambdas (generating `tmp0`, `tmp1`, etc.)
- **AND** both modules are loaded into the same Lua runtime
- **THEN** each module's temporary functions SHALL be independently scoped and SHALL NOT overwrite each other

### Requirement: While-loop condition helpers use local scoping
Functions generated internally by the compiler for while-loop condition evaluation SHALL be locally scoped in the emitted Lua code.

#### Scenario: While loop with complex condition
- **WHEN** a Chi program contains a while loop with a complex condition that requires helper function generation
- **THEN** the emitted helper functions SHALL use `local` scoping and SHALL NOT leak into the Lua global namespace
