package gh.marad.chi.core.types

import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.TypeTable
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
        val type = Type.record(TypeId("module", "package", "Type"), "hello" to Type.int)
        val typeTable = TypeTable()
        typeTable.addTypeAlias(type)

        // when
        val result = Compiler.resolveType(typeTable, emptyList(), TypeNameRef(null, null, "Type", null), 0)

        // then
        result shouldBe type
    }


    @Test
    fun `should resolve type variables`() {
        Compiler.resolveType(TypeTable(), listOf("T"), TypeNameRef(null, null, "T", null), 1) shouldBe Variable("T", 1)
        Compiler.resolveType(TypeTable(), listOf(), TypeParameterRef("T", null), 1) shouldBe Variable("T", 1)
    }

    @Test
    fun `should resolve type constructor to polymorphic type`() {
        // given
        val typeTable = TypeTable()
        val T = Variable("T", 1)
        val type = Type.record(TypeId("m", "p", "Type"), "foo" to T).copy(typeParams = listOf("T"))
        typeTable.addTypeAlias(type)
        var ref = TypeConstructorRef(
            TypeNameRef(null, null, "Type", null),
            typeParameters = listOf(TypeNameRef(null, null, "T", null)),
            null
        )

        // when
        var result = Compiler.resolveType(typeTable, listOf("T"), ref, 1)

        // then
        result shouldBe type
    }

    @Test
    fun `should resolve function type`() {
        // given
        val ref = FunctionTypeRef(
            argumentTypeRefs = listOf(TypeNameRef(null, null, "int", null), TypeParameterRef("T", null)),
            typeParameters = listOf(TypeParameterRef("T", null)),
            returnType = TypeNameRef(null, null, "float", null),
            section = null
        )

        // when
        val result = Compiler.resolveType(TypeTable(), emptyList(), ref, 1)

        // then
        val T = Variable("T", 1)
        result shouldBe Function(
            types = listOf(Type.int, T, Type.float),
        )
    }

    fun TypeTable.addTypeAlias(type: Record) {
        add(TypeAlias(type.getTypeIds().first(), type))
    }
}