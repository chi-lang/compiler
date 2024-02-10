package gh.marad.chi.core.types

import gh.marad.chi.addSymbolInDefaultPackage
import gh.marad.chi.ast
import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CompilerMessage
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.TypeMismatch
import gh.marad.chi.core.compiler.CompileTables
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.utils.printAst
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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

class InferenceKtTest {
    @Test
    fun `test parametric polymorphizm type inference`() {
        // when
        val result = testInference("""
            fn id[T](i: T): T { i }
            id(5)
            id(true)
        """.trimIndent())

        // then
        val a = TypeVariable("a")
        val polymorphicFunctionType = FunctionType(listOf(a, a), listOf(a))

        result.block.should { block ->
            block.type.shouldBe(Types.bool)
            block.body[0].shouldBeTypeOf<NameDeclaration>().should {
                it.type.shouldBe(polymorphicFunctionType)
            }
            block.body[1].shouldBeTypeOf<FnCall>().should {
                it.type.shouldBe(Types.int)
            }
            block.body[2].shouldBeTypeOf<FnCall>().should {
                it.type.shouldBe(Types.bool)
            }
        }
    }

    @Test
    fun `test atom type inference`() {
        // int
        testInference("5").block.body[0].shouldBeTypeOf<Atom>().should {
            it.type.shouldBe(Types.int)
        }

        // float
        testInference("5.5").block.body[0].shouldBeTypeOf<Atom>().should {
            it.type.shouldBe(Types.float)
        }

        // bool
        testInference("true").block.body[0].shouldBeTypeOf<Atom>().should {
            it.type.shouldBe(Types.bool)
        }
        testInference("false").block.body[0].shouldBeTypeOf<Atom>().should {
            it.type.shouldBe(Types.bool)
        }

        // string
        testInference("\"hello\"").block.body[0].shouldBeTypeOf<Atom>().should {
            it.type.shouldBe(Types.string)
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
            it.type.shouldBe(Types.int)
        }
    }

    @Test
    fun `test name declaration type inference`() {
        // given
        val nameDecl = NameDeclaration(
            public = false,
            name = "a",
            value = Atom.int(5, null),
            mutable = false,
            expectedType = null,
            null)

        val env = emptyEnv()

        // when
        val inferred = inferTypes(env, nameDecl)

        // then
        env.getType("a", null) shouldBe Types.int
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
                FnParam("param", type = T, null, null)
            ),
            sourceSection = null
        )

        val env = emptyEnv()

        // when
        val inferred = inferTypes(env, effectDef)

        // then
        inferred.constraints.shouldBeEmpty()
        env.getType("hello", null) shouldBe inferred.type
    }

    @Test
    fun `test assignment type inference`() {
        // given
        val assignment = Assignment(
            target = PackageSymbol("","",""),
            value = Atom.int(5, null),
            sourceSection = null
        )

        val env = emptyEnv()
        env.setType("x", Types.int)

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
            it.type.shouldBe(Types.int)
        }
        result.block.body[1].shouldBeTypeOf<NameDeclaration>().should {
            it.type.shouldBe(Types.int)
        }
        result.block.body[2].shouldBeTypeOf<NameDeclaration>().should {
            it.type.shouldBe(Types.int)
        }
        result.block.type.shouldBe(Types.int)
    }

    @Test
    fun `test lambda type inference`() {
        // given
        val lambda = Fn(
            typeVariables = emptyList(),
            parameters = listOf(
                FnParam("param", Types.int, null, null)
            ),
            body = Block(listOf(Atom.int(5, null)), null),
            sourceSection = null
        )

        val env = emptyEnv()

        // when
        val result = inferTypes(env, lambda)

        // then
        result.type.shouldBe(FunctionType(listOf(Types.int, Types.int)))
        result.constraints.shouldBeEmpty()
        env.getNames().shouldBeEmpty()
    }

    @Test
    fun `test function definition inference`() {
        // when
        val result = testInference("""
            fn hello(a: int): int { 5 }
        """.trimIndent())

        // then
        result.firstExpr().type shouldBe Types.fn(Types.int, Types.int)
    }

    @Test
    fun `test function call type inference`() {
        // given
        val env = mapOf("f" to FunctionType(listOf(Types.int, Types.bool)))

        // when
        val result = testInference("f(5)", env)

        // then
        printAst(result.block)
        result.block.type.shouldBe(Types.bool)
        result.inferred.constraints shouldHaveSize 1
        result.inferred.constraints.first().should {
            it.expected shouldBe FunctionType(listOf(Types.int, Types.bool))
            it.actual shouldBe FunctionType(listOf(Types.int, TypeVariable("t0")))
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
            it.type.shouldBe(Types.int)
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
            it.type shouldBe Types.any
        }
    }

    @Test
    fun `if-else should choose broader type when then is SumType and else is ProductType`() {
        // given
        val sumType = SumType("module", "package", "Sum", emptyList(), listOf("Product"), emptyList())
        val productType = ProductType("module", "package", "Product", emptyList(), emptyList(), emptyList())
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to sumType,
            "elseBranch" to productType
        )

        // when
        val result = testInference("""
                if cond { thenBranch } else { elseBranch }
            """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.type shouldBe sumType
        }
    }

    @Test
    fun `if-else should choose broader type when then is SumType and else is SimpleType`() {
        // given
        val sumType = SumType("module", "package", "Sum", emptyList(), listOf("Product"), emptyList())
        val simpleType = SimpleType("module", "package", "Product")
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to sumType,
            "elseBranch" to simpleType
        )

        // when
        val result = testInference("""
                if cond { thenBranch } else { elseBranch }
            """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.type shouldBe sumType
        }
    }


    @Test
    fun `if-else should choose broader type when then is ProductType and else is SumType`() {
        // given
        val sumType = SumType("module", "package", "Sum", emptyList(), listOf("Product"), emptyList())
        val productType = ProductType("module", "package", "Product", emptyList(), emptyList(), emptyList())
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to productType,
            "elseBranch" to sumType
        )

        // when
        val result = testInference("""
                if cond { thenBranch } else { elseBranch }
            """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.type shouldBe sumType
        }
    }

    @Test
    fun `if-else should choose broader type when then is SimpleType and else is SumType`() {
        // given
        val sumType = SumType("module", "package", "Sum", emptyList(), listOf("Product"), emptyList())
        val productType = SimpleType("module", "package", "Product")
        val env = mapOf(
            "cond" to Types.bool,
            "thenBranch" to productType,
            "elseBranch" to sumType
        )

        // when
        val result = testInference("""
                if cond { thenBranch } else { elseBranch }
            """.trimIndent(), env)

        // then
        result.firstExpr().shouldBeTypeOf<IfElse>().should {
            it.type shouldBe sumType
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
            it.type.shouldBe(Types.unit)
        }
    }

    @Test
    fun `test if else typing for polymorphic product types`() {
        // when
        val code = """
            data Option[T] = pub Just(pub value: T) | pub Nothing
            pub fn map[T,R](option: Option[T], f: (T) -> R): Option[R] {
                when {
                    option is Just -> {
                        option as Just
                        Just(f(option.value))
                    }
                    else -> Nothing
                }
            } 
            
            val opt = Just(5)
            opt.map({ it -> "hello" })
        """.trimIndent()

        val result = ast(code, GlobalCompilationNamespace(), ignoreCompilationErrors = true)

        // then
        result.shouldBeTypeOf<FnCall>()
            .type shouldBe SumType("user", "default", "Option",
            typeParams = listOf(Types.string),
            subtypes = listOf("Just", "Nothing"),
            typeSchemeVariables = listOf()
        )
    }

    @Test
    fun `foo`() {
        val code = """
            data Option[T] = pub Just(pub value: T) | pub Nothing
            pub fn or[T](opt: Option[T], other: Option[T]): Option[T] {
                when {
                    opt is Just -> opt
                    else -> other
                }
            }
        """.trimIndent()
        val result = ast(code)

        printAst(result)
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
        assertThrows<CompilerMessage> {
            println(testInference("b $op i", env, ignoreErrors = true))
        }.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.bool
            it.actual shouldBe Types.int
        }

        // and
        assertThrows<CompilerMessage> {
            testInference("i $op b", env, ignoreErrors = true)
        }.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.bool
            it.actual shouldBe Types.int
        }
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
        val result = shouldThrow<CompilerMessage> {
            testInference("f $op f", env, ignoreErrors = true)
        }

        // then
        result.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.int
            it.actual shouldBe Types.float
        }
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
                eff(value) -> {
                    value
                    resume(true)
                }
            }
        """.trimIndent())

        // then
        result.block.body[1].type shouldBe Types.bool
    }

    @Test
    fun `test prefix operator type inference`() {
        // given
        val env = mapOf("x" to Types.bool)

        // when
        val result = testInference("!x".trimIndent(), env)

        // then
        result.firstExpr().type shouldBe Types.bool
    }

    @Test
    fun `not operator should fail with non-bool types`() {
        // given
        val env = mapOf("x" to Types.int)

        // when
        val ex = shouldThrow<CompilerMessage> {
            testInference("!x".trimIndent(), env, ignoreErrors = true)
        }

        // then
        ex.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.bool
            it.actual shouldBe Types.int
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
            it.type shouldBe Types.string
            it.index.type shouldBe Types.int
            it.variable.type shouldBe Types.array(Types.string)
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
            it.type shouldBe Types.string
            it.index.type shouldBe Types.int
            it.variable.type shouldBe Types.array(Types.string)
            it.value.type shouldBe Types.string
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
            it.type shouldBe Types.string
            it.parts shouldHaveSize 2
            it.parts[0].type shouldBe Types.string
            it.parts[1].shouldBeTypeOf<Cast>().should { cast ->
                cast.expression.type shouldBe Types.int
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
            it.type shouldBe Types.bool
            it.value.type shouldBe Types.int
        }
    }

    @Test
    fun `test return inference`() {
        // when
        val result = testInference("return 5", ignoreErrors = true)

        // then
        result.firstExpr().shouldBeTypeOf<Return>().should {
            it.type shouldBe Types.int
            it.value?.type shouldBe Types.int
        }
        result.inferred.constraints shouldBe emptySet()
    }

    @Test
    fun `test while loop inference`() {
        // when
        val result = testInference("while true { 5 }")

        // then
        result.firstExpr().shouldBeTypeOf<WhileLoop>().should {
            it.type shouldBe Types.unit
            it.condition.type shouldBe Types.bool
            it.loop.type shouldBe Types.int
        }
    }

    @Test
    fun `while loop condition should be bool`() {
        // expect
        shouldThrow<CompilerMessage> {
            testInference("while 5 { 5 }", ignoreErrors = true)
        }.msg.shouldBeTypeOf<TypeMismatch>().should {
            it.expected shouldBe Types.bool
            it.actual shouldBe Types.int
        }
    }

    fun testInference(code: String, givenEnv: Map<String, Type> = mapOf(), ignoreErrors: Boolean = false, skipSecondInference: Boolean = true): Result {
        val ns = GlobalCompilationNamespace()
        givenEnv.forEach { (name, type) ->
            ns.addSymbolInDefaultPackage(name, type, public = true)
        }

        val result = Compiler.compile(code, ns)
        val program = result.program
        val messages = result.messages


        if (messages.any { it.level == Level.ERROR } && !ignoreErrors) {
            for (message in messages) {
                println(Compiler.formatCompilationMessage(code, message))
            }
            throw AssertionError("There were errors: $messages")
        }

        val expr = Block(program.expressions, program.sourceSection)
        val infCtx = InferenceContext(ns, TypeLookupTable(ns))
        val tables = CompileTables(program.packageDefinition, ns)
        val env = InferenceEnv(program.packageDefinition, tables, ns)
        val inferred = inferTypes(infCtx, env, expr)
        val solution = unify(inferred.constraints)
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
        fun emptyEnv(): InferenceEnv {
            val ns = GlobalCompilationNamespace()
            val pkg = Package(ns.getDefaultPackage().moduleName, ns.getDefaultPackage().packageName)
            return InferenceEnv(
                pkg,
                CompileTables(pkg, ns),
                ns
            )
        }

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