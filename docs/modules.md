# Modules and Imports

Chi organises code into **modules** and **packages**. A module is a top-level namespace (like a library), and packages are sub-namespaces within a module. Together they form a two-level hierarchy: `module/package`.

## Package Declaration

Every Chi source file can declare which module and package it belongs to:

```chi
package mylib/utils
```

The module name can contain dots for sub-grouping:

```chi
package std/lang.option
```

Here `std` is the module and `lang.option` is the package.

If no package declaration is present, the file defaults to `user/default`.

## Imports

Use `import` to bring names from other packages into scope:

```chi
import std/lang { println, print }
```

### Importing specific names

You can import specific names from a package:

```chi
import mylib/utils { helper, calculate }
```

### Import with alias

You can alias individual imports:

```chi
import mylib/utils { helper as h }
```

You can also alias the entire package:

```chi
import mod/pack as pkg
pkg.foo
```

When you alias a package, you can access its public members using dot notation on the alias.

### Visibility and `pub`

Only names marked with `pub` are accessible from other packages (when the modules differ). Within the same module, all names are accessible regardless of `pub`:

```chi
// In package mylib/math
pub fn add(a: int, b: int): int { a + b }
fn internalHelper(x: int): int { x * 2 }  // not accessible outside this module

pub val pi = 3.14
```

From another module:

```chi
import mylib/math { add, pi }
add(1, 2)   // works
// internalHelper is not accessible
```

## Auto-Imported Names

The following names from `std/lang` are automatically available in every Chi program without an explicit import:

- `println` -- print a value followed by a newline
- `print` -- print a value without a trailing newline
- `eval` -- evaluate a Chi code string at runtime

If the standard library is loaded (via `CHI_HOME`), additional types like `Option` from `std/lang.option` are also available.

## Package Structure

A typical Chi project might organise packages like this:

```
package std/lang           // core language functions
package std/lang.option    // Option type
package std/lang.array     // array operations
package std/lang.any       // operations on any type
package myapp/core         // application core
package myapp/utils        // utility functions
```

## Cross-Package Type Resolution

When you call a method on a value using dot notation, Chi looks up functions not only in local scope but also in the package where the receiver's type was defined:

```chi
package std/lang.array
pub fn add[T](arr: array[T]) { ... }
```

```chi
// In another file:
val arr = [1, 2, 3]
arr.add()  // found via the array type's home package
```

This enables an extensible "method" system without requiring types to declare their methods upfront.

## Defining and Using Types Across Packages

Types defined in one package can be imported and used in another:

```chi
// Define in one package
package some/pkg
type A = int | unit
```

```chi
// Use in another package
package other/pkg
import some/pkg { A }
val a: A = 5
```

## Qualified Type Names

Types can be referenced with their fully qualified name using the `module::package::Name` syntax in type annotations:

```chi
val x: std::lang.option::Option = 5
```

This is rarely needed since imported names can be used directly.
