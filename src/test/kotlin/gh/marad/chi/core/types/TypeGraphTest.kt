package gh.marad.chi.core.types

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TypeGraphTest {

    @Test
    fun `should contain type after adding it`() {
        // given
        val graph = TypeGraph()

        // when
        graph.addType("int")

        // then
        assertTrue(graph.contains("int"))
    }

    @Test
    fun `should contain both types after adding an edge`() {
        // given
        val graph = TypeGraph()

        // when
        graph.addSubtype("number", "int")

        // then
        assertTrue(graph.contains("number"))
        assertTrue(graph.contains("int"))
    }

    @Test
    fun `should determine if given type is a subtype`() {
        // given type hierarchy A -> B -> C -> D
        val graph = TypeGraph()
        graph.addSubtype("A", "B")
        graph.addSubtype("B", "C")
        graph.addSubtype("C", "D")

        // expect that D is subtype of A
        assertTrue(graph.isSubtype("A", "D"))
    }

    @Test
    fun `trying to define multiple parents for a type should fail`() {
        // given
        val graph = TypeGraph()
        graph.addSubtype("parent", "child")

        // expect
        shouldThrow<TypeGraph.TypeCannotHaveMultipleParentsException> {
            graph.addSubtype("other", "child")
        }
    }

    @Test
    fun `should find type ancestors`() {
        // given type hierarchy
        val graph = TypeGraph()
        graph.addSubtype("A", "B")
        graph.addSubtype("B", "C")
        graph.addSubtype("C", "D")

        graph.addSubtype("A", "X")
        graph.addSubtype("B", "Y")
        graph.addSubtype("C", "Z")

        // when
        val result = graph.ancestors("D")

        // then
        assertIterableEquals(
            listOf("C", "B", "A"),
            result
        )
    }

    @Test
    fun `should return empty ancestor list when type has no ancestors`() {
        // given
        val graph = TypeGraph()
        graph.addType("root")

        // expect
        assertTrue(graph.ancestors("root").isEmpty())
    }

    @Test
    fun `should find common supertype of two types`() {
        val graph = TypeGraph()
        // given A -> B -> C -> D
        graph.addSubtype("A", "B")
        graph.addSubtype("B", "C")
        graph.addSubtype("C", "D")

        // and A -> B -> X -> Y
        graph.addSubtype("B", "X")
        graph.addSubtype("X", "Y")

        // when
        val supertype = graph.commonSupertype("D", "Y")

        // then
        assertEquals("B", supertype)
    }

    @Test
    fun `should not find common supertype when there is none`() {
        val graph = TypeGraph()
        // given A -> B -> C -> D
        graph.addSubtype("A", "B")
        graph.addSubtype("B", "C")
        graph.addSubtype("C", "D")

        // and X -> Y -> Z
        graph.addSubtype("X", "Y")
        graph.addSubtype("Y", "Z")

        // when
        val supertype = graph.commonSupertype("D", "Z")

        // then
        assertNull(supertype)
    }
}