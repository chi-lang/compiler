package gh.marad.chi.core.types

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.compiler.Compiler2
import gh.marad.chi.core.compiler.Symbol
import gh.marad.chi.core.compiler.SymbolKind
import gh.marad.chi.core.compiler.TypeTable
import gh.marad.chi.core.namespace.CompilationScope
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.ScopeType
import gh.marad.chi.core.utils.printAst
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream
import kotlin.AssertionError

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
    fun `test name declaration type inference`() {
        // given
        val nameDecl = NameDeclaration(
            public = false,
            enclosingScope = CompilationScope(ScopeType.Package),
            name = "a",
            value = Atom.int(5, null),
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
        val T = TypeVariable("T")
        val effectDef = EffectDefinition(
            moduleName = "user",
            packageName = "default",
            name = "hello",
            public = false,
            typeVariables = listOf(T),
            parameters = listOf(
                FnParam("param", type = T, null)
            ),
            sourceSection = null
        )

        // when
        val inferred = inferTypes(mapOf(), effectDef)

        // then
        inferred.constraints.shouldBeEmpty()
        inferred.env["hello"].shouldBe(inferred.type)
    }

    @Test
    fun `test assignment type inference`() {
        // given
        val assignment = Assignment(
            name = "x",
            symbol = Symbol("","","",SymbolKind.Local,Types.int, 0, true, false),
            value = Atom.int(5, null),
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
            typeVariables = emptyList(),
            parameters = listOf(
                FnParam("param", Types.int, null)
            ),
            body = Block(listOf(Atom.int(5, null)), null),
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
    fun `test function definition inference`() {
        // when
        val result = testInference("""
            fn hello(a: int): int { 5 }
        """.trimIndent())

        // then
        result.firstExpr().newType shouldBe Types.fn(Types.int, Types.int)
    }

    @Test
    fun `test function call type inference`() {
        // given
        val env = mapOf("f" to FunctionType(listOf(Types.int, Types.bool)))

        // when
        val result = testInference("f(5)", env)

        // then
        printAst(result.block)
        result.block.newType.shouldBe(Types.bool)
        result.inferred.constraints shouldHaveSize 1
        result.inferred.constraints.first().should {
            it.actual shouldBe FunctionType(listOf(Types.int, Types.bool))
            it.expected shouldBe FunctionType(listOf(Types.int, TypeVariable("t0")))
            it.section.shouldNotBeNull()
        }

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
    //FIXME to wymaga, zeby było wiadomo czy expr jest używany jako wyrażenie
    //      na razie robię, że oba mają mieć ten sam typ
    fun `test if-else type inference with different branch types`() {
        // given
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to Types.int,
            "elseBranch" to Types.string
        )

        // when
        shouldThrow<TypeInferenceFailed> {
            testInference("""
                    if cond { thenBranch } else { elseBranch }
                """.trimIndent(), env, ignoreErrors = true)
        }
    }

    @Test
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


    @ParameterizedTest
    @MethodSource("booleanOperators")
    fun `test boolean operators`(op: String) {
        // given
        val env = mapOf(
            "left" to Types.bool,
            "right" to Types.bool
        )

        // when
        val result = testInference("left $op right", env)

        // then
        result.finalType shouldBe Types.bool
    }

    @ParameterizedTest
    @MethodSource("booleanOperators")
    fun `boolean operators should require both sides to be bool`(op: String) {
        // given
        val env = mapOf(
            "b" to Types.bool,
            "i" to Types.int
        )

        // expect
        assertThrows<TypeInferenceFailed> {
            println(testInference("b $op i", env, ignoreErrors = true))
        }.section.shouldNotBeNull()

        // and
        assertThrows<TypeInferenceFailed> {
            testInference("i $op b", env, ignoreErrors = true)
        }.section.shouldNotBeNull()
    }


    @ParameterizedTest
    @MethodSource("comparisonOperators")
    fun `test comparison operators`(op: String) {
        // given
        val env = mapOf(
            "left" to Types.int,
            "right" to Types.int
        )

        // when
        val result = testInference("left $op right", env)

        // then
        result.finalType shouldBe Types.bool
    }

    @ParameterizedTest
    @MethodSource("arithmeticOperators")
    fun `test arithmetic operators`(op: String) {
        // given
        val env = mapOf(
            "left" to Types.int,
            "right" to Types.int
        )

        // when
        val result = testInference("left $op right", env)

        // then
        result.finalType shouldBe Types.int
    }

    @ParameterizedTest
    @MethodSource("intOperators")
    fun `int binary operators should not accept other types`(op: String) {
        // given
        val env = mapOf(
            "f" to Types.float,
        )

        // when
        val result = shouldThrow<TypeInferenceFailed> {
            testInference("f $op f", env, ignoreErrors = true)
        }


        println(result)
    }

    @Test
    fun `test handle effect typing`() {
        // when
        val result = testInference("""
            effect eff[T](name: T): bool
            
            handle {
                eff(5)
                eff(false)
            } with {
                eff(value) -> resume(true)
            }
        """.trimIndent())

        // then
        result.block.body[1].newType shouldBe Types.bool
    }

    @Test
    fun `test prefix operator type inference`() {
        // given
        val env = mapOf("x" to Types.bool)

        // when
        val result = testInference("!x".trimIndent(), env)

        // then
        result.firstExpr().newType shouldBe Types.bool
    }

    @Test
    fun `not operator should fail with non-bool types`() {
        // given
        val env = mapOf("x" to Types.int)

        // when
        shouldThrow<TypeInferenceFailed> {
            testInference("!x".trimIndent(), env, ignoreErrors = true)
        }
    }

    @Test
    fun `test index operator type inference`() {
        // given
        val env = mapOf("x" to Types.array(Types.string))

        // when
        val result = testInference("x[0]", env)

        // then
        result.firstExpr().shouldBeTypeOf<IndexOperator>().should {
            it.newType shouldBe Types.string
            it.index.newType shouldBe Types.int
            it.variable.newType shouldBe Types.array(Types.string)
        }
    }

    @Test
    fun `test indexed assignment type inference`() {
        // given
        val env = mapOf("x" to Types.array(Types.string))

        // when
        val result = testInference("x[0] = \"hello\"", env)

        // then
        result.firstExpr().shouldBeTypeOf<IndexedAssignment>().should {
            it.newType shouldBe Types.string
            it.index.newType shouldBe Types.int
            it.variable.newType shouldBe Types.array(Types.string)
            it.value.newType shouldBe Types.string
        }
    }

    @Test
    fun `test interpolated string type inference`() {
        // given
        val env = mapOf("name" to Types.int)

        // when
        val result = testInference("\"hello \$name\"", env)

        // then
        result.firstExpr().shouldBeTypeOf<InterpolatedString>().should {
            it.newType shouldBe Types.string
            it.parts shouldHaveSize 2
            it.parts[0].newType shouldBe Types.string
            it.parts[1].shouldBeTypeOf<Cast>().should { cast ->
                cast.expression.newType shouldBe Types.int
            }
        }
    }

    @Test
    fun `test 'is' type inference`() {
        // given
        val env = mapOf("x" to Types.int)

        // when
        val result = testInference("x is bool", env)

        // then
        result.firstExpr().shouldBeTypeOf<Is>().should {
            it.newType shouldBe Types.bool
            it.value.newType shouldBe Types.int
        }
    }

    @Test
    fun `test return inference`() {
        // when
        val result = testInference("return 5")

        // then
        result.firstExpr().shouldBeTypeOf<Return>().should {
            it.newType shouldBe Types.int
            it.value?.newType shouldBe Types.int
        }
        result.inferred.constraints shouldBe emptySet()
    }

    @Test
    fun `test while loop inference`() {
        // when
        val result = testInference("while true { 5 }")

        // then
        result.firstExpr().shouldBeTypeOf<WhileLoop>().should {
            it.newType shouldBe Types.unit
            it.condition.newType shouldBe Types.bool
            it.loop.newType shouldBe Types.int
        }
    }

    @Test
    fun `while loop condition should be bool`() {
        // expect
        shouldThrow<TypeInferenceFailed> {
            testInference("while 5 { 5 }", ignoreErrors = true)
        }
    }

    @Test
    fun `variant type definitions should define constructors in env`() {
        // given
        val typeDefinition = DefineVariantType(
            baseVariantType = VariantType("user", "default", "A", emptyList(), emptyMap(), null),
            constructors = listOf(
                VariantTypeConstructor(true, "B", emptyList(), null),
                VariantTypeConstructor(true, "C", listOf(
                    VariantTypeField(true, "i", OldType.int, null)
                ), null),
            ),
            null
        )
        // when
        val result = inferTypes(emptyMap(), typeDefinition)

        // then
        val env = result.env
        env["A"] shouldBe SimpleType("user", "default", "A")
        env["B"] shouldBe SimpleType("user", "default", "B")
        env["C"] shouldBe FunctionType(types = listOf(
            Types.int,
            SimpleType("user", "default", "C")
        ))
    }

    fun testInference(code: String, env: Map<String, Type> = mapOf(), ignoreErrors: Boolean = false): Result {
        val ns = GlobalCompilationNamespace()
        val pkg = ns.getDefaultPackage()
        env.forEach { name, type ->
            pkg.symbols.add(
                Symbol(
                    moduleName = pkg.moduleName,
                    packageName = pkg.packageName,
                    name = name,
                    kind = SymbolKind.Local,
                    type = type,
                    slot = 0,
                    public = true,
                    mutable = false
                )
            )
        }

        val (program, messages) = Compiler2.compile(code, ns)

        if (messages.any { it.level == Level.ERROR } && !ignoreErrors) {
            for (message in messages) {
                println(Compiler2.formatCompilationMessage(code, message))
            }
            throw AssertionError("There were errors")
        }

        val expr = Block(program.expressions, program.sourceSection)
        val infCtx = InferenceContext(TypeGraph(), TypeTable())
        val inferred = inferTypes(infCtx, env, expr)
        val solution = unify(infCtx.typeTable, inferred.constraints)
        TypeFiller(solution).visit(expr)
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

    companion object {
        @JvmStatic
        fun booleanOperators() = Stream.of(
            Arguments.of("&&"), // bool -> bool -> bool
            Arguments.of("||"),
        )

        @JvmStatic
        fun comparisonOperators() = Stream.of(
            Arguments.of("<"), // number -> number -> bool
            Arguments.of("<="),
            Arguments.of(">"),
            Arguments.of(">="),
            Arguments.of("=="),
            Arguments.of("!="),
        )

        @JvmStatic
        fun arithmeticOperators() = Stream.of(
            Arguments.of("+"), // number -> number -> number
            Arguments.of("-"),
            Arguments.of("*"),
            Arguments.of("/"),
        )

        @JvmStatic
        fun intOperators() = Stream.of(
            Arguments.of("%"), // int -> int -> int
            Arguments.of(">>"),
            Arguments.of("<<"),
            Arguments.of("&"),
            Arguments.of("|"),
        )

    }
}