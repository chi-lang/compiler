package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.antlr.ChiParser.*
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import java.util.*

internal object TypeReader {

    fun readTypeRef(parser: ParserVisitor, source: ChiSource, ctx: TypeContext): TypeRef {
        return when (ctx) {
            is TypeNameRefContext -> readTypeName(source, ctx.typeName())
            is FunctionTypeRefContext -> readFunctionType(parser, source, ctx)
            is RecordTypeContext -> TODO()
            is SumTypeContext -> TODO()
            is TypeConstructorRefContext -> readGenericType(parser, source, ctx)
            else -> throw CompilerMessage.from("Unsupported kind of type definition: $ctx", getSection(source, ctx))
        }
    }

    private fun readTypeName(source: ChiSource, typeName: TypeNameContext): TypeNameRef {
        val section = getSection(source, typeName)
        return if (typeName.simpleName() != null) {
            TypeNameRef(null, null, typeName.simpleName().name.text, section)
        } else if (typeName.qualifiedName() != null) {
            TypeNameRef(
                moduleName = typeName.qualifiedName().moduleName().text,
                packageName = typeName.qualifiedName().packageName().text,
                typeName = typeName.qualifiedName().name.text,
                section
            )
        } else {
            throw CompilerMessage.from("Unsupported kind of type name: $typeName", section)
        }
    }

    private fun readFunctionType(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: FunctionTypeRefContext
    ): TypeRef {
        val argTypes = ctx.type().map { readTypeRef(parser, source, it) }
        val returnType = readTypeRef(parser, source, ctx.func_return_type().type())
        return FunctionTypeRef(emptyList(), argTypes, returnType, getSection(source, ctx))
    }

    private fun readGenericType(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: TypeConstructorRefContext
    ): TypeRef {
        val typeName = readTypeName(source, ctx.typeName())
        val typeParameters = ctx.type().map { readTypeRef(parser, source, it) }
        return TypeConstructorRef(
            typeName, typeParameters, getSection(source, ctx)
        )
    }
}

sealed interface TypeRef {
    fun findTypeNames(): Set<String>
    val section: ChiSource.Section?
    companion object {
        val unit = TypeNameRef(null, null, "unit", null)
    }
}

data class TypeParameterRef(val name: String, override val section: ChiSource.Section?) : TypeRef {
    override fun findTypeNames(): Set<String> = setOf(name)

}
data class TypeNameRef(
    val moduleName: String?,
    val packageName: String?,
    val typeName: String,
    override val section: ChiSource.Section?
) : TypeRef {
    override fun findTypeNames(): Set<String> = setOf(typeName)
    override fun equals(other: Any?): Boolean = other != null && other is TypeNameRef && typeName == other.typeName
    override fun hashCode(): Int = Objects.hash(typeName)
}

data class FunctionTypeRef(
    val typeParameters: List<TypeRef>,
    val argumentTypeRefs: List<TypeRef>,
    val returnType: TypeRef,
    override val section: ChiSource.Section?
) : TypeRef {
    override fun findTypeNames(): Set<String> = argumentTypeRefs.flatMap { it.findTypeNames() }.toSet() + returnType.findTypeNames()

    override fun equals(other: Any?): Boolean =
        other != null && other is FunctionTypeRef
                && argumentTypeRefs == other.argumentTypeRefs
                && returnType == other.returnType

    override fun hashCode(): Int = Objects.hash(argumentTypeRefs, returnType)
}

data class TypeConstructorRef(
    val baseType: TypeRef,
    val typeParameters: List<TypeRef>,
    override val section: ChiSource.Section?
) : TypeRef {
    override fun findTypeNames(): Set<String> = baseType.findTypeNames()

    override fun equals(other: Any?): Boolean =
        other != null && other is TypeConstructorRef
                && baseType == other.baseType

    override fun hashCode(): Int = Objects.hash(baseType)
}