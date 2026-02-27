## MODIFIED Requirements

### Requirement: While-loop condition helpers use descriptive names

The private helper function that extracts while-loop condition sub-expressions into thunk declarations SHALL use descriptive names that communicate its purpose.

#### Scenario: Function name describes extraction behavior

- **WHEN** reading the while-loop emission code in `LuaEmitter.kt`
- **THEN** the helper function SHALL be named `extractConditionThunks` (not `foo`)
- **AND** its accumulator parameter SHALL be named `thunkDeclarations` (not `bar`)

#### Scenario: No behavioral change

- **WHEN** a Chi program containing a while loop is compiled to Lua
- **THEN** the emitted Lua code SHALL be identical to what was produced before the rename
