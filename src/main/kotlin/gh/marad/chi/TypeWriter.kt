package gh.marad.chi

import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.types.*
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type.Companion.any
import gh.marad.chi.core.types.Type.Companion.bool
import gh.marad.chi.core.types.Type.Companion.float
import gh.marad.chi.core.types.Type.Companion.int
import gh.marad.chi.core.types.Type.Companion.string
import gh.marad.chi.core.types.Type.Companion.unit
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.util.*

object TypeWriter {
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
            if (any == type) {
                stream.writeByte(BinaryTypeId.Any.id().toInt())
            } else if (bool == type) {
                stream.writeByte(BinaryTypeId.Bool.id().toInt())
            } else if (float == type) {
                stream.writeByte(BinaryTypeId.Float.id().toInt())
            } else if (int == type) {
                stream.writeByte(BinaryTypeId.Int.id().toInt())
            } else if (string == type) {
                stream.writeByte(BinaryTypeId.String.id().toInt())
            } else if (unit == type) {
                stream.writeByte(BinaryTypeId.Unit.id().toInt())
            }
        } else if (type is Record) {
            stream.writeByte(BinaryTypeId.Record.id().toInt())
            writeTypeId(type.getTypeId(), stream)
            stream.writeShort(type.fields.size)
            for ((name, type1) in type.fields) {
                stream.writeUTF(name)
                writeType(type1, stream)
            }
            writeStrings(type.typeParams(), stream)
        } else if (type is Sum) {
            stream.writeByte(BinaryTypeId.Sum.id().toInt())
            writeTypeId(type.getTypeId(), stream)
            writeType(type.lhs, stream)
            writeType(type.rhs, stream)
            writeStrings(type.typeParams(), stream)
        } else if (type is Function) {
            stream.writeByte(BinaryTypeId.Fn.id().toInt())
            writeTypes(type.types, stream)
            writeStrings(type.typeParams, stream)
        } else if (type is Variable) {
            stream.writeByte(BinaryTypeId.TypeVariable.id().toInt())
            stream.writeUTF(type.name)
            stream.writeShort(type.level)
        } else if (type is gh.marad.chi.core.types.Array) {
            stream.writeByte(BinaryTypeId.Array.id().toInt())
            writeType(type.elementType, stream)
            writeStrings(type.typeParams(), stream)
        } else if (type is PolyType) {
            stream.writeByte(BinaryTypeId.TypeScheme.id().toInt())
            stream.writeShort(type.level)
            writeType(type.body, stream)
        } else {
            throw TODO("Unsupported type $type")
        }
    }

    @Throws(IOException::class)
    fun writeTypeId(id: TypeId?, stream: DataOutputStream) {
        val hasId = id != null
        stream.writeBoolean(hasId)

        if (id != null) {
            stream.writeUTF(id.moduleName)
            stream.writeUTF(id.packageName)
            stream.writeUTF(id.name)
        }
    }

    @Throws(IOException::class)
    fun readTypeId(stream: DataInputStream): TypeId? {
        return if (stream.readBoolean()) {
            TypeId(
                stream.readUTF(),
                stream.readUTF(),
                stream.readUTF()
            )
        } else {
            null
        }
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
            BinaryTypeId.Any -> any
            BinaryTypeId.Bool -> bool
            BinaryTypeId.Float -> float
            BinaryTypeId.Int -> int
            BinaryTypeId.String -> string
            BinaryTypeId.Unit -> unit
            BinaryTypeId.Record -> Record(readTypeId(stream), readFields(stream), readStrings(stream))
            BinaryTypeId.Sum -> Sum(
                readTypeId(stream),
                readType(stream),
                readType(stream),
                readStrings(stream)
            )
            BinaryTypeId.Fn -> Function(
                readTypes(stream),
                readStrings(stream)
            )
            BinaryTypeId.Array -> Array(readType(stream), readStrings(stream))
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
