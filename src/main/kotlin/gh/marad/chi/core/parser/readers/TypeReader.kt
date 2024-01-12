package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.antlr.ChiParser
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.ParserVisitor
import gh.marad.chi.core.parser.getSection
import java.util.*

internal object TypeReader {

    fun readTypeRef(parser: ParserVisitor, source: ChiSource, ctx: ChiParser.TypeContext): TypeRef {
        return if (ctx.typeNameRef() != null) {
            readTypeName(source, ctx.typeNameRef())
        } else if (ctx.functionTypeRef() != null) {
            readFunctionType(parser, source, ctx.functionTypeRef())
        } else if (ctx.typeConstructorRef() != null) {
            readGenericType(parser, source, ctx.typeConstructorRef())
        } else {
            throw CompilerMessage.from("Unknown type", getSection(source, ctx))
        }
    }

    private fun readTypeName(source: ChiSource, ctx: ChiParser.TypeNameRefContext): TypeNameRef {
        if (ctx.packageName != null) {
            throw CompilerMessage.from(
                "Resolving type from package is not supported.",
                getSection(source, ctx))
        }
        return TypeNameRef(
            typeName = ctx.name.text,
            getSection(source, ctx)
        )
    }

    private fun readFunctionType(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: ChiParser.FunctionTypeRefContext
    ): TypeRef {
        val argTypes = ctx.type().map { readTypeRef(parser, source, it) }
        val returnType = readTypeRef(parser, source, ctx.func_return_type().type())
        return FunctionTypeRef(emptyList(), argTypes, returnType, getSection(source, ctx))
    }

    private fun readGenericType(
        parser: ParserVisitor,
        source: ChiSource,
        ctx: ChiParser.TypeConstructorRefContext
    ): TypeRef {
        val typeName = readTypeName(source, ctx.typeNameRef())
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
        val unit = TypeNameRef("unit", null)
    }
}

data class TypeParameterRef(val name: String, override val section: ChiSource.Section?) : TypeRef {
    override fun findTypeNames(): Set<String> = setOf(name)

}
data class TypeNameRef(
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

data class VariantNameRef(
    val variantType: TypeRef,
    val variantName: String,
    val variantFields: List<FormalField>,
    override val section: ChiSource.Section?
) : TypeRef {
    override fun findTypeNames(): Set<String> = variantFields.flatMap { it.typeRef.findTypeNames() }.toSet() + variantType.findTypeNames()

    override fun equals(other: Any?): Boolean =
        other != null && other is VariantNameRef
                && variantType == other.variantType
                && variantFields == other.variantFields
                && variantName == other.variantName

    override fun hashCode(): Int = Objects.hash(variantType, variantFields, variantName)
}