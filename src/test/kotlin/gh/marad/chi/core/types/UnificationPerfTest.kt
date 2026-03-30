package gh.marad.chi.core.types

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Tag
import io.kotest.matchers.shouldBe
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.types.shouldBeTypeOf

@Tag("perf")
class UnificationPerfTest {

    /**
     * Generate N constraints of the form: Variable("a$i", 0) = Type.int
     * This is the simplest case — each variable solution triggers a queue rebuild in the current implementation.
     * With N variables, unify() does O(N²) work because each solution rebuilds the entire remaining queue.
     */
    private fun generateLinearConstraints(n: Int): List<Constraint> {
        return (1..n).map { i ->
            Constraint(Variable("a$i", 0), Type.int, null, emptyList())
        }
    }

    /**
     * Generate a chain where each variable depends on the next:
     * a1 = a2, a2 = a3, ..., a(n-1) = an, an = int
     * This is worst-case for queue rebuilding since each substitution must propagate.
     */
    private fun generateChainConstraints(n: Int): List<Constraint> {
        val constraints = mutableListOf<Constraint>()
        for (i in 1 until n) {
            constraints.add(Constraint(Variable("a$i", 0), Variable("a${i + 1}", 0), null, emptyList()))
        }
        constraints.add(Constraint(Variable("a$n", 0), Type.int, null, emptyList()))
        return constraints
    }

    /**
     * Generate N constraints involving sum types to test backtracking performance:
     * Sum(Type.string, Type.int) = Type.int  (must try rhs first, succeed)
     * Sum(Type.string, Type.int) = Type.string  (must try rhs first, fail, then try lhs)
     */
    private fun generateSumConstraints(n: Int): List<Constraint> {
        val sumType = Sum(emptyList(), Type.string, Type.int)
        return (1..n).map { i ->
            if (i % 2 == 0) {
                Constraint(sumType, Type.int, null, emptyList())
            } else {
                Constraint(sumType, Type.string, null, emptyList())
            }
        }
    }

    @Test
    fun `baseline - unify 100 linear constraints`() {
        val constraints = generateLinearConstraints(100)
        val start = System.nanoTime()
        val solutions = unify(constraints)
        val elapsed = (System.nanoTime() - start) / 1_000_000
        solutions.size shouldBe 100
        // Just record time, no assertion yet
        println("100 linear constraints: ${elapsed}ms")
    }

    @Test
    fun `baseline - unify 500 linear constraints`() {
        val constraints = generateLinearConstraints(500)
        val start = System.nanoTime()
        val solutions = unify(constraints)
        val elapsed = (System.nanoTime() - start) / 1_000_000
        solutions.size shouldBe 500
        println("500 linear constraints: ${elapsed}ms")
    }

    @Test
    fun `baseline - unify 100 chain constraints`() {
        val constraints = generateChainConstraints(100)
        val start = System.nanoTime()
        val solutions = unify(constraints)
        val elapsed = (System.nanoTime() - start) / 1_000_000
        println("100 chain constraints: ${elapsed}ms, solutions: ${solutions.size}")
    }

    @Test
    fun `baseline - unify 100 sum type constraints`() {
        val constraints = generateSumConstraints(100)
        val start = System.nanoTime()
        val solutions = unify(constraints)
        val elapsed = (System.nanoTime() - start) / 1_000_000
        println("100 sum constraints: ${elapsed}ms")
    }

    @Test
    fun `mapType with many solutions should complete quickly`() {
        // Create a deeply nested function type
        val vars = (1..200).map { Variable("v$it", 0) }
        val fnType = Function(vars + Type.int)

        // Create solutions mapping each variable to Type.int
        val solutions = vars.map { it to Type.int as Type }

        val start = System.nanoTime()
        val result = mapType(fnType, solutions)
        val elapsed = (System.nanoTime() - start) / 1_000_000

        result.shouldBeTypeOf<Function>()
        (result as Function).types.forEach {
            it shouldBe Type.int
        }
        println("mapType with 200 solutions: ${elapsed}ms")
    }
}
