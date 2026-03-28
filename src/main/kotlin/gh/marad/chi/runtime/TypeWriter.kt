package gh.marad.chi.runtime

import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Array
import gh.marad.chi.core.types.Function
import java.io.*
import java.util.*

object TypeWriter {

    fun encodeType(type: TypeScheme): String {
        val sb = StringBuilder()
        writeLuaType(type, sb)
        return sb.toString()
    }

    fun decodeType(spec: String): TypeScheme {
        val trimmed = spec.trim()
        return if (trimmed.startsWith("{")) {
            val parsed = LuaLiteralParser(trimmed).parseValue()
            luaValueToType(parsed!!)
        } else {
            // Legacy Base64 format fallback
            val byteArray = Base64.getDecoder().decode(spec)
            readTypeScheme(DataInputStream(ByteArrayInputStream(byteArray)))
        }
    }

    // --- Lua literal encoder ---

    private fun writeLuaType(type: TypeScheme, sb: StringBuilder) {
        when (type) {
            is Primitive -> {
                val tag = primitiveTag(type) ?: throw RuntimeException("Unsupported primitive type: $type")
                sb.append("{tag=\"").append(tag).append("\"")
                val extraIds = type.ids.drop(1)
                if (extraIds.isNotEmpty()) {
                    sb.append(",ids=")
                    writeTypeIdList(extraIds, sb)
                }
                sb.append("}")
            }
            is Function -> {
                sb.append("{tag=\"fn\",types=")
                writeTypeList(type.types, sb)
                sb.append(",typeParams=")
                writeStringList(type.typeParams, sb)
                sb.append(",defaults=").append(type.defaultArgs)
                sb.append("}")
            }
            is Record -> {
                sb.append("{tag=\"record\",ids=")
                writeTypeIdList(type.ids, sb)
                sb.append(",fields=")
                writeFieldList(type.fields, sb)
                sb.append(",typeParams=")
                writeStringList(type.typeParams, sb)
                sb.append("}")
            }
            is Sum -> {
                sb.append("{tag=\"sum\",ids=")
                writeTypeIdList(type.ids, sb)
                sb.append(",lhs=")
                writeLuaType(type.lhs, sb)
                sb.append(",rhs=")
                writeLuaType(type.rhs, sb)
                sb.append(",typeParams=")
                writeStringList(type.typeParams, sb)
                sb.append("}")
            }
            is Variable -> {
                sb.append("{tag=\"var\",name=\"").append(escapeLua(type.name))
                sb.append("\",level=").append(type.level).append("}")
            }
            is Array -> {
                sb.append("{tag=\"array\",elem=")
                writeLuaType(type.elementType, sb)
                sb.append(",typeParams=")
                writeStringList(type.typeParams(), sb)
                sb.append(",ids=")
                writeTypeIdList(type.getTypeIds(), sb)
                sb.append("}")
            }
            is PolyType -> {
                sb.append("{tag=\"scheme\",level=").append(type.level).append(",body=")
                writeLuaType(type.body, sb)
                sb.append("}")
            }
            is Recursive -> {
                sb.append("{tag=\"rec\",var=")
                writeLuaType(type.variable, sb)
                sb.append(",type=")
                writeLuaType(type.type, sb)
                sb.append("}")
            }
            else -> throw RuntimeException("Unsupported type: $type")
        }
    }

    private fun primitiveTag(type: Primitive): String? {
        val id = type.getPrincipalTypeId()
        return when (id) {
            Type.anyTypeId -> "any"
            Type.boolTypeId -> "bool"
            Type.floatTypeId -> "float"
            Type.intTypeId -> "int"
            Type.stringTypeId -> "string"
            Type.unitTypeId -> "unit"
            else -> null
        }
    }

    private fun writeTypeIdList(ids: List<TypeId>, sb: StringBuilder) {
        sb.append("{")
        ids.forEachIndexed { i, id ->
            if (i > 0) sb.append(",")
            sb.append("{\"").append(escapeLua(id.moduleName))
            sb.append("\",\"").append(escapeLua(id.packageName))
            sb.append("\",\"").append(escapeLua(id.name)).append("\"}")
        }
        sb.append("}")
    }

    private fun writeTypeList(types: List<Type>, sb: StringBuilder) {
        sb.append("{")
        types.forEachIndexed { i, t ->
            if (i > 0) sb.append(",")
            writeLuaType(t, sb)
        }
        sb.append("}")
    }

    private fun writeStringList(strings: List<String>, sb: StringBuilder) {
        sb.append("{")
        strings.forEachIndexed { i, s ->
            if (i > 0) sb.append(",")
            sb.append("\"").append(escapeLua(s)).append("\"")
        }
        sb.append("}")
    }

    private fun writeFieldList(fields: List<Record.Field>, sb: StringBuilder) {
        sb.append("{")
        fields.forEachIndexed { i, f ->
            if (i > 0) sb.append(",")
            sb.append("{\"").append(escapeLua(f.name)).append("\",")
            writeLuaType(f.type, sb)
            sb.append("}")
        }
        sb.append("}")
    }

    private fun escapeLua(s: String): String =
        s.replace("\\", "\\\\").replace("\"", "\\\"")

    // --- Lua literal decoder ---

    @Suppress("UNCHECKED_CAST")
    private fun luaValueToType(data: Any): TypeScheme {
        val map = data as Map<String, Any?>
        return when (val tag = map["tag"] as String) {
            "int" -> Type.int.maybeAddExtraIds(map)
            "float" -> Type.float.maybeAddExtraIds(map)
            "bool" -> Type.bool.maybeAddExtraIds(map)
            "string" -> Type.string.maybeAddExtraIds(map)
            "unit" -> Type.unit.maybeAddExtraIds(map)
            "any" -> Type.any.maybeAddExtraIds(map)
            "fn" -> {
                val types = (map["types"] as List<*>).map { luaValueToType(it!!) as Type }
                val typeParams = (map["typeParams"] as? List<*>)?.map { it as String } ?: emptyList()
                val defaults = (map["defaults"] as Number).toInt()
                Function(types, typeParams, defaults)
            }
            "record" -> {
                val ids = extractTypeIds(map["ids"])
                val fields = (map["fields"] as List<*>).map { entry ->
                    val pair = entry as List<*>
                    Record.Field(pair[0] as String, luaValueToType(pair[1]!!) as Type)
                }
                val typeParams = (map["typeParams"] as? List<*>)?.map { it as String } ?: emptyList()
                Record(ids, fields, typeParams)
            }
            "sum" -> {
                val ids = extractTypeIds(map["ids"])
                val lhs = luaValueToType(map["lhs"]!!) as Type
                val rhs = luaValueToType(map["rhs"]!!) as Type
                val typeParams = (map["typeParams"] as? List<*>)?.map { it as String } ?: emptyList()
                Sum(ids, lhs, rhs, typeParams)
            }
            "var" -> Variable(map["name"] as String, (map["level"] as Number).toInt())
            "array" -> {
                val elem = luaValueToType(map["elem"]!!) as Type
                val typeParams = (map["typeParams"] as? List<*>)?.map { it as String } ?: emptyList()
                val ids = extractTypeIds(map["ids"])
                val arr = Array(elem, typeParams)
                if (ids.isNotEmpty()) arr.withAddedTypeIds(ids) as Type else arr
            }
            "scheme" -> {
                val level = (map["level"] as Number).toInt()
                val body = luaValueToType(map["body"]!!) as Type
                PolyType(level, body)
            }
            "rec" -> {
                val variable = luaValueToType(map["var"]!!) as Variable
                val innerType = luaValueToType(map["type"]!!) as Type
                Recursive(variable, innerType)
            }
            else -> throw RuntimeException("Unknown type tag: $tag")
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun Primitive.maybeAddExtraIds(map: Map<String, Any?>): Type {
        val idsList = map["ids"] as? List<*> ?: return this
        if (idsList.isEmpty()) return this
        val typeIds = idsList.map { entry ->
            val parts = entry as List<*>
            TypeId(parts[0] as String, parts[1] as String, parts[2] as String)
        }
        return withAddedTypeIds(typeIds) as Type
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractTypeIds(data: Any?): List<TypeId> {
        val list = data as? List<*> ?: return emptyList()
        return list.map { entry ->
            val parts = entry as List<*>
            TypeId(parts[0] as String, parts[1] as String, parts[2] as String)
        }
    }

    // --- Lua literal parser ---

    private class LuaLiteralParser(private val input: String) {
        private var pos = 0

        fun parseValue(): Any? {
            skipWhitespace()
            if (pos >= input.length) return null
            return when {
                input[pos] == '{' -> parseTable()
                input[pos] == '"' -> parseString()
                input[pos] == '-' || input[pos].isDigit() -> parseNumber()
                input.startsWith("true", pos) -> { pos += 4; true }
                input.startsWith("false", pos) -> { pos += 5; false }
                input.startsWith("nil", pos) -> { pos += 3; null }
                else -> throw RuntimeException("Unexpected character at pos $pos: '${input[pos]}'")
            }
        }

        private fun parseTable(): Any {
            expect('{')
            skipWhitespace()
            if (pos < input.length && input[pos] == '}') { pos++; return emptyList<Any?>() }

            val savedPos = pos
            val isRecord = tryLookAheadForKey()
            pos = savedPos

            return if (isRecord) parseRecordTable() else parseArrayTable()
        }

        private fun tryLookAheadForKey(): Boolean {
            if (pos >= input.length || (!input[pos].isLetter() && input[pos] != '_')) return false
            while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
            skipWhitespace()
            return pos < input.length && input[pos] == '='
        }

        private fun parseRecordTable(): Map<String, Any?> {
            val map = mutableMapOf<String, Any?>()
            while (true) {
                skipWhitespace()
                if (pos < input.length && input[pos] == '}') { pos++; return map }
                val key = parseIdentifier()
                skipWhitespace()
                expect('=')
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                if (pos < input.length && input[pos] == ',') pos++
            }
        }

        private fun parseArrayTable(): List<Any?> {
            val list = mutableListOf<Any?>()
            while (true) {
                skipWhitespace()
                if (pos < input.length && input[pos] == '}') { pos++; return list }
                list.add(parseValue())
                skipWhitespace()
                if (pos < input.length && input[pos] == ',') pos++
            }
        }

        private fun parseString(): String {
            expect('"')
            val sb = StringBuilder()
            while (pos < input.length && input[pos] != '"') {
                if (input[pos] == '\\') {
                    pos++
                    when (input[pos]) {
                        '"' -> sb.append('"')
                        '\\' -> sb.append('\\')
                        'n' -> sb.append('\n')
                        't' -> sb.append('\t')
                        else -> { sb.append('\\'); sb.append(input[pos]) }
                    }
                } else {
                    sb.append(input[pos])
                }
                pos++
            }
            expect('"')
            return sb.toString()
        }

        private fun parseNumber(): Number {
            val start = pos
            if (input[pos] == '-') pos++
            while (pos < input.length && input[pos].isDigit()) pos++
            if (pos < input.length && input[pos] == '.') {
                pos++
                while (pos < input.length && input[pos].isDigit()) pos++
                return input.substring(start, pos).toDouble()
            }
            return input.substring(start, pos).toInt()
        }

        private fun parseIdentifier(): String {
            val start = pos
            while (pos < input.length && (input[pos].isLetterOrDigit() || input[pos] == '_')) pos++
            check(pos > start) { "Expected identifier at pos $start" }
            return input.substring(start, pos)
        }

        private fun skipWhitespace() {
            while (pos < input.length && input[pos].isWhitespace()) pos++
        }

        private fun expect(c: Char) {
            check(pos < input.length && input[pos] == c) { "Expected '$c' at pos $pos, got '${if (pos < input.length) input[pos] else "EOF"}'" }
            pos++
        }
    }

    // --- Legacy Base64 binary decoder (kept for backward compatibility) ---

    @Throws(IOException::class)
    private fun readTypeScheme(stream: DataInputStream): TypeScheme {
        val typeId = BinaryTypeId.fromId(stream.readByte().toInt())
        return when (typeId) {
            BinaryTypeId.Any -> Type.any.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Bool -> Type.bool.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Float -> Type.float.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Int -> Type.int.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.String -> Type.string.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Unit -> Type.unit.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Record -> Record(readTypeIds(stream), readFields(stream), readStrings(stream))
            BinaryTypeId.Sum -> Sum(readTypeIds(stream), readType(stream), readType(stream), readStrings(stream))
            BinaryTypeId.Fn -> Function(readTypes(stream), readStrings(stream), defaultArgs = stream.readShort().toInt())
            BinaryTypeId.Array -> Array(readType(stream), readStrings(stream)).withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.TypeVariable -> Variable(stream.readUTF(), stream.readShort().toInt())
            BinaryTypeId.TypeScheme -> PolyType(stream.readShort().toInt(), readType(stream))
        }
    }

    @Throws(IOException::class)
    private fun readType(stream: DataInputStream): Type = readTypeScheme(stream) as Type

    @Throws(IOException::class)
    private fun readTypes(stream: DataInputStream): List<Type> {
        val count = stream.readByte()
        return (0 until count).map { readTypeScheme(stream) as Type }
    }

    @Throws(IOException::class)
    private fun readTypeIds(stream: DataInputStream): List<TypeId> {
        val count = stream.readByte().toInt()
        return (0 until count).map { TypeId(stream.readUTF(), stream.readUTF(), stream.readUTF()) }
    }

    @Throws(IOException::class)
    private fun readFields(stream: DataInputStream): List<Record.Field> {
        val count = stream.readShort().toInt()
        return (0 until count).map { Record.Field(stream.readUTF(), readType(stream)) }
    }

    @Throws(IOException::class)
    private fun readStrings(stream: DataInputStream): List<String> {
        val count = stream.readShort().toInt()
        return (0 until count).map { stream.readUTF() }
    }
}
