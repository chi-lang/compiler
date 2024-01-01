package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.namespace.CompilationScope
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.ScopeType
import gh.marad.chi.core.parser.readers.ParseBlock
import gh.marad.chi.core.parser.testParse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class InferenceKtTest {
    @Test
    fun `test parametric polymorphizm type inference`() {
        // when
        val result = testInference("""
            fn id[T](i: T) { i }
            id(5)
            id(true)
        """.trimIndent())

        // then
        val a = TypeVariable("a")
        val polymorphicFunctionType = FunctionType(listOf(a, a), listOf(a))

        result.block.should { block ->
            block.newType.shouldBe(Types.bool)
            block.body[0].shouldBeTypeOf<NameDeclaration>().should {
                it.newType.shouldBe(polymorphicFunctionType)
            }
            block.body[1].shouldBeTypeOf<FnCall>().should {
                it.newType.shouldBe(Types.int)
            }
            block.body[2].shouldBeTypeOf<FnCall>().should {
                it.newType.shouldBe(Types.bool)
            }
        }
    }

    @Test
    fun `test atom type inference`() {
        // int
        testInference("5").block.body[0].shouldBeTypeOf<Atom>().should {
            it.newType.shouldBe(Types.int)
        }

        // float
        testInference("5.5").block.body[0].shouldBeTypeOf<Atom>().should {
            it.newType.shouldBe(Types.float)
        }

        // bool
        testInference("true").block.body[0].shouldBeTypeOf<Atom>().should {
            it.newType.shouldBe(Types.bool)
        }
        testInference("false").block.body[0].shouldBeTypeOf<Atom>().should {
            it.newType.shouldBe(Types.bool)
        }

        // string
        testInference("\"hello\"").block.body[0].shouldBeTypeOf<Atom>().should {
            it.newType.shouldBe(Types.string)
        }
    }

    @Test
    fun `test variable access type inference`() {
        // given
        val env = mapOf("x" to Types.int)

        // when
        val result = testInference("x", env)

        // then
        result.inferred.constraints.shouldBeEmpty()
        result.firstExpr().shouldBeTypeOf<VariableAccess>().should {
            it.newType.shouldBe(Types.int)
        }
    }

    @Test
    fun `test error when name is not defined in an environment`() {
        // given
        val env = emptyMap<String, Type>()

        // expect
        shouldThrow<TypeInferenceFailed> {
            testInference("x", env)
        }
    }

    @Test
    fun `test name declaration type inference`() {
        // given
        val nameDecl = NameDeclaration(
            public = false,
            enclosingScope = CompilationScope(ScopeType.Package),
            name = "a",
            value = Atom("5", OldType.intType, null),
            mutable = false,
            expectedType = null,
            null)

        // when
        val inferred = inferTypes(mapOf(), nameDecl)

        // then
        inferred.env["a"].shouldBe(Types.int)
        inferred.constraints.shouldBeEmpty()
        inferred.type.shouldBe(Types.int)
    }

    @Test
    fun `test effect definition type inference`() {
        // given
        val genPar = GenericTypeParameter("T")
        val effectDef = EffectDefinition(
            moduleName = "user",
            packageName = "default",
            name = "hello",
            public = false,
            genericTypeParameters = listOf(genPar),
            parameters = listOf(
                FnParam("param", type = genPar, null)
            ),
            returnType = genPar,
            sourceSection = null
        )

        // when
        val inferred = inferTypes(mapOf(), effectDef)

        // then
        val a = TypeVariable("T")
        val polyIdFuncType = FunctionType(types = listOf(a,a), typeSchemeVariables = listOf(a))
        inferred.constraints.shouldBeEmpty()
        inferred.type.shouldBe(polyIdFuncType)
        inferred.env["hello"].shouldBe(polyIdFuncType)
    }

    @Test
    fun `test assignment type inference`() {
        // given
        val assignment = Assignment(
            definitionScope = CompilationScope(ScopeType.Package),
            name = "x",
            value = Atom("5", OldType.intType, null),
            sourceSection = null
        )

        val env = mapOf("x" to Types.int)

        // when
        val inferred = inferTypes(env, assignment)

        // then
        inferred.constraints.shouldBeEmpty()
        inferred.type.shouldBe(Types.int)
    }

    @Test
    fun `test block type inference`() {
        // when
        val result = testInference("""
            val x = 5
            val y = 1
            val y = x + y
        """.trimIndent())

        // then
        result.block.body[0].shouldBeTypeOf<NameDeclaration>().should {
            it.newType.shouldBe(Types.int)
        }
        result.block.body[1].shouldBeTypeOf<NameDeclaration>().should {
            it.newType.shouldBe(Types.int)
        }
        result.block.body[2].shouldBeTypeOf<NameDeclaration>().should {
            it.newType.shouldBe(Types.int)
        }
        result.block.newType.shouldBe(Types.int)
    }

    @Test
    fun `test lambda type inference`() {
        // given
        val lambda = Fn(
            fnScope = CompilationScope(ScopeType.Function),
            genericTypeParameters = emptyList(),
            parameters = listOf(
                FnParam("param", OldType.intType, null)
            ),
            returnType = OldType.intType,
            body = Block(listOf(Atom("5", OldType.intType, null)), null),
            sourceSection = null
        )

        // when
        val result = inferTypes(mapOf(), lambda)

        // then
        result.type.shouldBe(FunctionType(listOf(Types.int, Types.int)))
        result.constraints.shouldBeEmpty()
        result.env.shouldBeEmpty()
    }

    @Test
    fun `test function call type inference`() {
        // given
        val env = mapOf("f" to FunctionType(listOf(Types.int, Types.bool)))

        // when
        val result = testInference("f(5)", env)

        // then
        result.block.newType.shouldBe(Types.bool)
        result.inferred.constraints.shouldContain(
            Constraint(
                FunctionType(listOf(Types.int, Types.bool)),
                FunctionType(listOf(Types.int, TypeVariable("t0")))
            )
        )
    }

    @Test
    fun `test if-else type inference with both branches of same type`() {
        // given
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to Types.int,
            "elseBranch" to Types.int
        )

        // when
        val result = testInference("""
            if cond { thenBranch } else { elseBranch }
        """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.newType.shouldBe(Types.int)
        }
    }

    @Test
    fun `test if-else type inference with different branch types`() {
        // given
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to Types.int,
            "elseBranch" to Types.string
        )

        // when
        val result = testInference("""
            if cond { thenBranch } else { elseBranch }
        """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.newType.shouldBe(Types.int)
        }
    }

    //FIXME @Test to wymaga, zeby było wiadomo czy expr jest używany jako wyrażenie
    fun `test if-else type inference without else branch`() {
        // given
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to Types.int,
        )

        // when
        val result = testInference("""
            if cond { thenBranch }
        """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.newType.shouldBe(Types.unit)
        }
    }


    private fun infixTestCases(): Stream<Arguments> {
        val typeShouldBeBool = true
        val typeShouldBeUnchanged = false
        return Stream.of(
            Arguments.of("+", true), // number -> number -> number
            Arguments.of("-", true),
            Arguments.of("*", true),
            Arguments.of("/", true),
            Arguments.of("%", true),
            Arguments.of("&&", true),
            Arguments.of("<", true),
            Arguments.of("<=", true),
            Arguments.of(">", true),
            Arguments.of(">=", true),
            Arguments.of("==", true),
            Arguments.of("!=", true),
            Arguments.of("||", true),
            Arguments.of("&", true),
            Arguments.of("|", true),
            Arguments.of(">>", true),
            Arguments.of("<<", true),
        )
    }

    private fun arithmeticOperators(): Stream<Arguments> = Stream.of(

    )

    @ParameterizedTest
//    @ValueSource(strings = ["+", "-", "*", "/", "%", "&&", "<", "<=", ">", ">=", "==", "!=", "||", "&", "|", ">>", "<<"])
    @MethodSource("infixTestCases")
    fun `test infix  types`() {

    }

    fun testInference(code: String, env: Map<String, Type> = mapOf()): Result {
        val ns = GlobalCompilationNamespace()
        val ctx = ConversionContext(ns)
        val ast = testParse(code)
        val expr = generateExpressionAst(ctx, ParseBlock(ast, null)) as Block
        val inferred = inferTypes(env, expr)
        val solution = unify(inferred.constraints)
        expr.accept(TypeFiller(solution))
        val finalType = applySubstitution(inferred.type, solution)
        return Result(inferred, solution, finalType, expr)
    }

    data class Result(
        val inferred: InferenceResult,
        val solution: List<Pair<TypeVariable, Type>>,
        val finalType: Type,
        val block: Block
    ) {
        fun firstExpr() = block.body.first()
    }
}