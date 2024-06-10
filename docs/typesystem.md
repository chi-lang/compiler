# Typesystem

Chi has Hindley-Millner based type inference with structural typing. 

### Available types

- primitives: `int`, `float`, `bool`, `unit`
- function types: 

### Hindley-Millner type inference

This means that you can safely omit many type declarations and the typing algorithm will figure the types out itself.
It's most notable when using lambda functions:

```chi
val id = { a -> a }
id(5)
id("hello")
```

Note that even though we didn't add any type annotations to the code the `id` function will have type `'a -> 'a`,
where `'a` is a type variable - so it accepts any type without constraints. Looking at the implementation it's pretty
clear that the type doesn't matter, but the relation between input and output types does!

Later we have `id(5)` and `id("hello")` and they will respectively have inferred types `int` and `string`.

### Structural typing

Most languages use [nominal type systems](https://en.wikipedia.org/wiki/Nominal_type_system), which means they are name
based. Oversimplifying it - two values are the same if their name the same type.

[Structural typing](https://en.wikipedia.org/wiki/Structural_type_system) on the other hand focuses more on the value
itself. So records with the same fields would have the
same structural type. Take look at this Kotlin code:

```
data class Person(val name: String)
data class PersonWithAddress(val name: String, val address: String)

fun greet(person: Person) { ... }
```

In an example above both classes represent similar data, but are unrelated. We might be tempted to
make `PersonWithAddress` a subclass of `Person`, but it is really not, and in more complex scenarios you might end up in
some convoluted type hierarchies and a lot of mapping from type to other type.

The role of the type system is to accept valid programs, and reject invalid programs. So we might want to have a bit
more freedom in how we structure our data without typesystem standing in our way. Now consider this example:

```chi
type Person = { name: string }
type PersonWithAddress = { name: string, address: string }

fn greet(person: Person) { ... }
```

This looks similar, but `Person` and `PersonWithAddress` are just type aliases for the structure they define, and the
structure is important. So what happens if we do `greet({ name: 'John', address: 'Some St. 87' })`? The value is
accepted by the type system because it's structure matches the required one.

Note that the record has more field's than needed, but the key here is that it has all the requried fields. So the type
system makes sure that we will not attempt to read field 'name' from a record that doesn't contain it. Type names are not relevant here.

