### Requirement: EffectDefinition Lua emission
The compiler SHALL emit valid Lua code for `EffectDefinition` AST nodes. The emitted code SHALL define a package-level function that, when called, yields the effect name and arguments via `coroutine.yield`.

#### Scenario: Simple effect definition emits callable Lua function
- **WHEN** a Chi program defines an effect with `effect myEffect(x: int): int`
- **THEN** the compiler SHALL emit Lua code that assigns a function to the package table (`__P_.myEffect`) which calls `coroutine.yield` with the normalised effect name and a table of its arguments

#### Scenario: Effect definition with multiple parameters
- **WHEN** a Chi program defines an effect with multiple parameters (e.g., `effect log(level: int, msg: string): unit`)
- **THEN** the compiler SHALL emit Lua code that passes all arguments as a table to `coroutine.yield`

### Requirement: Handle expression Lua emission
The compiler SHALL emit valid Lua code for `Handle` AST nodes. The emitted code SHALL wrap the handle body in a Lua coroutine, define a handler dispatch table, and run a dispatch loop that matches yielded effects to their handlers.

#### Scenario: Handle expression with single effect handler
- **WHEN** a Chi program contains a `handle { body } with { effectName(args) -> handlerBody }` expression
- **THEN** the compiler SHALL emit Lua code that: (1) creates a handler table mapping the normalised effect name to a handler function, (2) wraps the body in a coroutine, (3) dispatches yielded effects to the matching handler via `chi_handle_effect`

#### Scenario: Handle expression with multiple effect handlers
- **WHEN** a Chi program contains a handle expression with multiple handler cases
- **THEN** the compiler SHALL emit a handler table containing all handler entries, comma-separated

#### Scenario: Handler arguments use correct Lua 1-based indexing
- **WHEN** a handler case declares argument names (e.g., `myEffect(x, y) -> ...`)
- **THEN** the emitted Lua code SHALL unpack arguments using 1-based indices (`args[1]`, `args[2]`, etc.)

### Requirement: chi_handle_effect runtime helper
The compiler SHALL emit a `chi_handle_effect` Lua helper function in the output when the program contains `Handle` expressions. This function SHALL implement the coroutine dispatch loop: resume the body coroutine, match yielded effect names to handlers, call the handler, and resume with the handler's result.

#### Scenario: Runtime helper emitted only when needed
- **WHEN** a Chi program contains at least one `Handle` expression
- **THEN** the emitted Lua output SHALL include the `chi_handle_effect` function definition before any expression code

#### Scenario: Runtime helper not emitted when unnecessary
- **WHEN** a Chi program contains no `Handle` expressions
- **THEN** the emitted Lua output SHALL NOT include the `chi_handle_effect` function definition

### Requirement: End-to-end effects execution
A Chi program that defines an effect, invokes it inside a handle body, and provides a handler SHALL compile and execute successfully, producing the correct result.

#### Scenario: Effect invoked and handled returns handler result
- **WHEN** a Chi program defines an effect, invokes it inside a `handle` block, and the handler provides a value via `resume(value)`
- **THEN** the program SHALL execute without error and the effect invocation SHALL return the value provided by the handler

#### Scenario: Handle expression result is the body result
- **WHEN** a `handle` expression's body completes normally (without yielding an unhandled effect)
- **THEN** the result of the `handle` expression SHALL be the result of the body block
