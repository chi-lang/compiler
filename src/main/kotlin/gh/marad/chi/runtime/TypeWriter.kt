package gh.marad.chi.runtime

import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type.Companion.any
import gh.marad.chi.core.types.Type.Companion.bool
import gh.marad.chi.core.types.Type.Companion.float
import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import gh.marad.chi.core.types.Type.Companion.unit
import java.io.*
import java.util.*

object TypeWriter {

    fun decodeType(spec: String): TypeScheme {
        val byteArray = Base64.getDecoder().decode(spec)
        return readType(DataInputStream(ByteArrayInputStream(byteArray)))
    }

    fun encodeType(type: TypeScheme): String {
        val baos = ByteArrayOutputStream()
        writeType(type, DataOutputStream(baos))
        return Base64.getEncoder().encodeToString(baos.toByteArray())
    }


    @Throws(IOException::class)
    fun writeTypeAlias(typeAlias: TypeAlias, stream: DataOutputStream) {
        stream.writeUTF(typeAlias.typeId.moduleName)
        stream.writeUTF(typeAlias.typeId.packageName)
        stream.writeUTF(typeAlias.typeId.name)
        writeType(typeAlias.type, stream)
    }

    @Throws(IOException::class)
    fun readTypeAlias(stream: DataInputStream): TypeAlias {
        return TypeAlias(
            TypeId(
                stream.readUTF(),
                stream.readUTF(),
                stream.readUTF()
            ),
            readType(stream)
        )
    }

    @Throws(IOException::class)
    fun writeTypes(types: List<Type>, stream: DataOutputStream) {
        stream.writeByte(types.size)
        for (type in types) {
            writeType(type, stream)
        }
    }

    @Throws(IOException::class)
    fun writeTypes(types: Array<Type>, stream: DataOutputStream) {
        stream.writeByte(types.size)
        for (type in types) {
            writeType(type, stream)
        }
    }

    @Throws(IOException::class)
    fun writeStrings(strings: List<String?>, stream: DataOutputStream) {
        stream.writeShort(strings.size)
        for (string in strings) {
            stream.writeUTF(string)
        }
    }

    @Throws(IOException::class)
    fun writeVariables(typeVars: List<Variable>, stream: DataOutputStream) {
        stream.writeByte(typeVars.size)
        for ((name, level) in typeVars) {
            stream.writeUTF(name)
            stream.writeShort(level)
        }
    }

    @Throws(IOException::class)
    fun writeType(type: TypeScheme, stream: DataOutputStream) {
        if (type is Primitive) {
            val principalTypeId = type.getPrincipalTypeId()
            if (Type.anyTypeId == principalTypeId) {
                stream.writeByte(BinaryTypeId.Any.id().toInt())
            } else if (Type.boolTypeId == principalTypeId) {
                stream.writeByte(BinaryTypeId.Bool.id().toInt())
            } else if (Type.floatTypeId == principalTypeId) {
                stream.writeByte(BinaryTypeId.Float.id().toInt())
            } else if (Type.intTypeId == principalTypeId) {
                stream.writeByte(BinaryTypeId.Int.id().toInt())
            } else if (Type.stringTypeId == principalTypeId) {
                stream.writeByte(BinaryTypeId.String.id().toInt())
            } else if (Type.unitTypeId == principalTypeId) {
                stream.writeByte(BinaryTypeId.Unit.id().toInt())
            } else {
                throw TODO("Unsupported primitive type!")
            }
            val typeIds = type.getTypeIds().drop(1)
            writeTypeIds(typeIds, stream)
        } else if (type is Record) {
            stream.writeByte(BinaryTypeId.Record.id().toInt())
            writeTypeIds(type.getTypeIds(), stream)
            stream.writeShort(type.fields.size)
            for ((name, type1) in type.fields) {
                stream.writeUTF(name)
                writeType(type1, stream)
            }
            writeStrings(type.typeParams(), stream)
        } else if (type is Sum) {
            stream.writeByte(BinaryTypeId.Sum.id().toInt())
            writeTypeIds(type.getTypeIds(), stream)
            writeType(type.lhs, stream)
            writeType(type.rhs, stream)
            writeStrings(type.typeParams(), stream)
        } else if (type is Function) {
            stream.writeByte(BinaryTypeId.Fn.id().toInt())
            writeTypes(type.types, stream)
            writeStrings(type.typeParams, stream)
            stream.writeShort(type.defaultArgs)
        } else if (type is Variable) {
            stream.writeByte(BinaryTypeId.TypeVariable.id().toInt())
            stream.writeUTF(type.name)
            stream.writeShort(type.level)
        } else if (type is gh.marad.chi.core.types.Array) {
            stream.writeByte(BinaryTypeId.Array.id().toInt())
            writeType(type.elementType, stream)
            writeStrings(type.typeParams(), stream)
            writeTypeIds(type.getTypeIds(), stream)
        } else if (type is PolyType) {
            stream.writeByte(BinaryTypeId.TypeScheme.id().toInt())
            stream.writeShort(type.level)
            writeType(type.body, stream)
        } else {
            throw TODO("Unsupported type $type")
        }
    }

    @Throws(IOException::class)
    fun writeTypeIds(ids: List<TypeId>, stream: DataOutputStream) {
        stream.writeByte(ids.size)
        for (id in ids) {
            stream.writeUTF(id.moduleName)
            stream.writeUTF(id.packageName)
            stream.writeUTF(id.name)
        }
    }

    @Throws(IOException::class)
    fun readTypeIds(stream: DataInputStream): List<TypeId> {
        val typeIdCount = stream.readByte().toInt()
        val ids = mutableListOf<TypeId>()
        repeat(typeIdCount) {
            ids.add(
                TypeId(
                    stream.readUTF(),
                    stream.readUTF(),
                    stream.readUTF()
                ))
        }
        return ids
    }

    @Throws(IOException::class)
    fun readTypes(stream: DataInputStream): List<Type> {
        val count = stream.readByte()
        val types = ArrayList<Type>(count.toInt())
        for (i in 0 until count) {
            types.add(readTypeScheme(stream) as Type)
        }
        return types
    }

    @Throws(IOException::class)
    fun readType(stream: DataInputStream): Type {
        return readTypeScheme(stream) as Type
    }

    @Throws(IOException::class)
    fun readTypeScheme(stream: DataInputStream): TypeScheme {
        val typeId = BinaryTypeId.fromId(stream.readByte().toInt())
        return when (typeId) {
            BinaryTypeId.Any -> any.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Bool -> bool.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Float -> float.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Int -> int.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.String -> string.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Unit -> unit.withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.Record -> Record(readTypeIds(stream), readFields(stream), readStrings(stream))
            BinaryTypeId.Sum -> Sum(
                readTypeIds(stream),
                readType(stream),
                readType(stream),
                readStrings(stream)
            )
            BinaryTypeId.Fn -> Function(
                readTypes(stream),
                readStrings(stream),
                defaultArgs = stream.readShort().toInt()
            )
            BinaryTypeId.Array -> Array(readType(stream), readStrings(stream))
                .withAddedTypeIds(readTypeIds(stream))
            BinaryTypeId.TypeVariable -> Variable(stream.readUTF(), stream.readShort().toInt())
            BinaryTypeId.TypeScheme -> PolyType(stream.readShort().toInt(), readType(stream))
        }
    }

    @Throws(IOException::class)
    private fun readFields(stream: DataInputStream): List<Record.Field> {
        val fieldCount = stream.readShort()
        val fields = ArrayList<Record.Field>(fieldCount.toInt())
        for (i in 0 until fieldCount) {
            fields.add(
                Record.Field(
                    stream.readUTF(),
                    readType(stream)
                )
            )
        }
        return fields
    }

    @Throws(IOException::class)
    private fun readStrings(stream: DataInputStream): List<String> {
        val count = stream.readShort()
        val result = ArrayList<String>(count.toInt())
        for (i in 0 until count) {
            result.add(stream.readUTF())
        }
        return result
    }
}
