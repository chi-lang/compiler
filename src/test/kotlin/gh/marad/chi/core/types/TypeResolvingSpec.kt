package gh.marad.chi.core.types

import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.TypeInfo
import gh.marad.chi.core.namespace.TypeTable
import gh.marad.chi.core.namespace.VariantField
import gh.marad.chi.core.parser.readers.FunctionTypeRef
import gh.marad.chi.core.parser.readers.TypeConstructorRef
import gh.marad.chi.core.parser.readers.TypeNameRef
import gh.marad.chi.core.parser.readers.TypeParameterRef
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TypeResolvingSpec {

    @Test
    fun `should resolve simple type by name`() {
        // given
        val type = SimpleType("m", "p", "Type")
        val typeTable = TypeTable()
        typeTable.addType(type)

        // when
        val result = Compiler.resolveType(typeTable, emptyList(), TypeNameRef("Type", null))

        // then
        result shouldBe type
    }


    @Test
    fun `should resolve type variables`() {
        Compiler.resolveType(TypeTable(), listOf("T"), TypeNameRef("T", null)) shouldBe TypeVariable("T")
        Compiler.resolveType(TypeTable(), listOf(), TypeParameterRef("T", null)) shouldBe TypeVariable("T")
    }

    @Test
    fun `should resolve type constructor to polymorphic type`() {
        // given
        val T = TypeVariable("T")
        val type = ProductType("m", "p", "Type",
            types = listOf(T),
            typeParams = listOf(),
            typeSchemeVariables = listOf(T)
        )
        val typeTable = TypeTable()
        typeTable.addType(type, fields = listOf(
            VariantField("foo", T, true)
        ))
        var ref = TypeConstructorRef(
            TypeNameRef("Type", null),
            typeParameters = listOf(TypeNameRef("T", null)),
            null
        )

        // when
        var result = Compiler.resolveType(typeTable, listOf("T"), ref)

        // then
        result shouldBe type
    }

    @Test
    fun `should resolve function type`() {
        // given
        val ref = FunctionTypeRef(
            argumentTypeRefs = listOf(TypeNameRef("int", null), TypeParameterRef("T", null)),
            typeParameters = listOf(TypeParameterRef("T", null)),
            returnType = TypeNameRef("float", null),
            section = null
        )

        // when
        val result = Compiler.resolveType(TypeTable(), emptyList(), ref)

        // then
        val T = TypeVariable("T")
        result shouldBe FunctionType(
            types = listOf(Types.int, T, Types.float),
            typeSchemeVariables = listOf(T),
        )
    }





    fun TypeTable.addType(type: SimpleType, isPublic: Boolean = true) =
        add(TypeInfo(type.moduleName, type.packageName, type.name, type, Types.any, isPublic, emptyList()))

    fun TypeTable.addType(type: ProductType, isPublic: Boolean = true, fields: List<VariantField>) =
        add(TypeInfo(type.moduleName, type.packageName, type.name, type, Types.any, isPublic, fields))
}