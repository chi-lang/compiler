package gh.marad.chi.runtime

import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function
import gh.marad.chi.runtime.TypeWriter.decodeType
import gh.marad.chi.runtime.TypeWriter.encodeType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TypeWriterTest {

    private fun roundtrip(type: TypeScheme) {
        val encoded = encodeType(type)
        val decoded = decodeType(encoded)
        assertEquals(type, decoded, "Roundtrip failed for encoded: $encoded")
    }

    // --- Primitives ---

    @Test
    fun `roundtrip int`() = roundtrip(Type.int)

    @Test
    fun `roundtrip float`() = roundtrip(Type.float)

    @Test
    fun `roundtrip bool`() = roundtrip(Type.bool)

    @Test
    fun `roundtrip string`() = roundtrip(Type.string)

    @Test
    fun `roundtrip unit`() = roundtrip(Type.unit)

    @Test
    fun `roundtrip any`() = roundtrip(Type.any)

    // --- Primitive with extra type ids ---

    @Test
    fun `roundtrip primitive with extra ids`() {
        val extraId = TypeId("mymod", "mypkg", "MyAlias")
        val aliased = Type.int.withAddedTypeId(extraId)
        roundtrip(aliased)
    }

    // --- Function types ---

    @Test
    fun `roundtrip simple function`() {
        val fn = Function(listOf(Type.int, Type.int), emptyList(), 0)
        roundtrip(fn)
    }

    @Test
    fun `roundtrip function with multiple args`() {
        val fn = Function(listOf(Type.int, Type.float, Type.string), emptyList(), 0)
        roundtrip(fn)
    }

    @Test
    fun `roundtrip function with defaults`() {
        val fn = Function(listOf(Type.int, Type.int, Type.int), emptyList(), 2)
        roundtrip(fn)
    }

    @Test
    fun `roundtrip function with type params`() {
        val fn = Function(listOf(Variable("T", 0), Variable("T", 0)), listOf("T"), 0)
        roundtrip(fn)
    }

    @Test
    fun `roundtrip higher-order function`() {
        val inner = Function(listOf(Type.int, Type.bool), emptyList(), 0)
        val outer = Function(listOf(inner, Type.string), emptyList(), 0)
        roundtrip(outer)
    }

    // --- Records ---

    @Test
    fun `roundtrip empty record`() {
        val record = Record(emptyList(), emptyList())
        roundtrip(record)
    }

    @Test
    fun `roundtrip record with fields`() {
        val record = Record(
            listOf(TypeId("std", "lang.map", "Map")),
            listOf(
                Record.Field("name", Type.string),
                Record.Field("age", Type.int)
            )
        )
        roundtrip(record)
    }

    @Test
    fun `roundtrip record with type params`() {
        val record = Record(
            listOf(TypeId("std", "lang.map", "Map")),
            listOf(
                Record.Field("class", Type.string),
                Record.Field("map", Record(emptyList(), emptyList()))
            ),
            listOf("K", "V")
        )
        roundtrip(record)
    }

    // --- Sum types ---

    @Test
    fun `roundtrip option type`() {
        val option = Type.option(Type.int)
        roundtrip(option)
    }

    @Test
    fun `roundtrip sum type`() {
        val sum = Sum.create(
            listOf(TypeId("test", "pkg", "Either")),
            Type.string,
            Type.int
        )
        roundtrip(sum)
    }

    // --- Variables ---

    @Test
    fun `roundtrip variable`() = roundtrip(Variable("T", 0))

    @Test
    fun `roundtrip variable with level`() = roundtrip(Variable("a42", 3))

    // --- Arrays ---

    @Test
    fun `roundtrip array of int`() = roundtrip(Array(Type.int))

    @Test
    fun `roundtrip array of records`() {
        val record = Record(
            listOf(TypeId("m", "p", "Point")),
            listOf(Record.Field("x", Type.float), Record.Field("y", Type.float))
        )
        roundtrip(Array(record))
    }

    @Test
    fun `roundtrip array with type id`() {
        val arr = Array(Type.int).withAddedTypeId(TypeId("std", "lang.array", "array"))
        roundtrip(arr)
    }

    // --- PolyType ---

    @Test
    fun `roundtrip polytype`() {
        val poly = PolyType(1, Function(listOf(Variable("T", 0), Variable("T", 0)), listOf("T"), 0))
        roundtrip(poly)
    }

    // --- Recursive ---

    @Test
    fun `roundtrip recursive type`() {
        val rec = Recursive(Variable("X", 0), Record(
            listOf(TypeId("test", "pkg", "List")),
            listOf(
                Record.Field("head", Type.int),
                Record.Field("tail", Variable("X", 0))
            )
        ))
        roundtrip(rec)
    }

    // --- Encoding format verification ---

    @Test
    fun `encoded format starts with curly brace`() {
        val encoded = encodeType(Type.int)
        assert(encoded.startsWith("{")) { "Expected Lua literal format, got: $encoded" }
    }

    @Test
    fun `encoded format does not contain Base64`() {
        val encoded = encodeType(Function(listOf(Type.int, Type.string), emptyList(), 0))
        assert(!encoded.contains("==")) { "Looks like Base64: $encoded" }
        assert(encoded.contains("tag=")) { "Expected Lua table format: $encoded" }
    }

    // --- Legacy Base64 backward compatibility ---

    @Test
    fun `decode legacy Base64 int type`() {
        // Type.int in old binary format
        val base64 = "AwA="
        val decoded = decodeType(base64)
        assertEquals(Type.int, decoded)
    }

    @Test
    fun `decode legacy Base64 function type`() {
        // Function(int -> int) in old binary format
        val base64 = "BgIDAAMAAAAAAA=="
        val decoded = decodeType(base64)
        val expected = Function(listOf(Type.int, Type.int), emptyList(), 0)
        assertEquals(expected, decoded)
    }
}
