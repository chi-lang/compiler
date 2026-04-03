package gh.marad.chi.core.types

import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class UnionFindTest {

    @Test
    fun `find on unbound variable returns the variable itself`() {
        val uf = UnionFind()
        val v = Variable("a", 0)
        uf.find(v) shouldBe v
    }

    @Test
    fun `bind variable then find returns the bound type`() {
        val uf = UnionFind()
        val v = Variable("a", 0)
        uf.bind(v, Type.int)
        uf.find(v) shouldBe Type.int
    }

    @Test
    fun `chained bindings resolve transitively`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        val b = Variable("b", 0)
        uf.bind(a, b)
        uf.bind(b, Type.string)
        uf.find(a) shouldBe Type.string
    }

    @Test
    fun `find resolves variables inside function types`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        uf.bind(a, Type.int)
        val fnType = Function(listOf(a, Type.bool))
        val resolved = uf.resolve(fnType)
        resolved.shouldBeTypeOf<Function>()
        (resolved as Function).types shouldBe listOf(Type.int, Type.bool)
    }

    @Test
    fun `find resolves variables inside record types`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        uf.bind(a, Type.string)
        val recType = Record(emptyList(), listOf(Record.Field("x", a)))
        val resolved = uf.resolve(recType)
        resolved.shouldBeTypeOf<Record>()
        (resolved as Record).fields[0].type shouldBe Type.string
    }

    @Test
    fun `find resolves variables inside sum types`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        uf.bind(a, Type.int)
        val sumType = Sum(emptyList(), a, Type.unit)
        val resolved = uf.resolve(sumType)
        resolved.shouldBeTypeOf<Sum>()
        val resolvedSum = resolved as Sum
        Sum.listTypes(resolvedSum).contains(Type.int) shouldBe true
        Sum.listTypes(resolvedSum).contains(Type.unit) shouldBe true
    }

    @Test
    fun `find resolves variables inside array types`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        uf.bind(a, Type.float)
        val arrType = Array(a)
        val resolved = uf.resolve(arrType)
        resolved.shouldBeTypeOf<Array>()
        (resolved as Array).elementType shouldBe Type.float
    }

    @Test
    fun `resolve preserves unbound variables`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        val b = Variable("b", 0)
        uf.bind(a, Type.int)
        val fnType = Function(listOf(a, b))
        val resolved = uf.resolve(fnType)
        resolved.shouldBeTypeOf<Function>()
        (resolved as Function).types shouldBe listOf(Type.int, b)
    }

    @Test
    fun `resolve on primitive returns primitive unchanged`() {
        val uf = UnionFind()
        uf.resolve(Type.int) shouldBe Type.int
    }

    @Test
    fun `resolve handles recursive type`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        uf.bind(a, Type.int)
        val recVar = Variable("rec", -1)
        val recursive = Recursive(recVar, Function(listOf(a, Type.bool)))
        val resolved = uf.resolve(recursive)
        resolved.shouldBeTypeOf<Recursive>()
        val inner = (resolved as Recursive).type
        inner.shouldBeTypeOf<Function>()
        (inner as Function).types shouldBe listOf(Type.int, Type.bool)
    }

    @Test
    fun `multiple bindings are independent`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        val b = Variable("b", 0)
        uf.bind(a, Type.int)
        uf.bind(b, Type.string)
        uf.find(a) shouldBe Type.int
        uf.find(b) shouldBe Type.string
    }

    @Test
    fun `allBindings returns all variable-to-type solutions`() {
        val uf = UnionFind()
        val a = Variable("a", 0)
        val b = Variable("b", 0)
        uf.bind(a, Type.int)
        uf.bind(b, Type.string)
        val bindings = uf.allBindings()
        bindings.toSet() shouldBe setOf(a to Type.int, b to Type.string)
    }
}
