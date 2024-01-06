package gh.marad.chi.core.analyzer

import gh.marad.chi.ast
import gh.marad.chi.core.OldType
import gh.marad.chi.core.compiler.Symbol
import gh.marad.chi.core.compiler.SymbolKind
import gh.marad.chi.core.namespace.CompilationScope
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.ScopeType
import gh.marad.chi.core.namespace.SymbolType
import gh.marad.chi.core.types.FunctionType
import gh.marad.chi.core.types.TypeVariable
import gh.marad.chi.core.types.Types
import gh.marad.chi.messages
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

class FnCallTypeCheckingSpec : FunSpec() {

    init {
        val ns = GlobalCompilationNamespace()
        ns.getDefaultPackage().symbols.apply {
            add(Symbol("user", "default", "x", SymbolKind.Local, Types.int, 0, true, true))
            add(
                Symbol(
                    "user",
                    "default",
                    "test",
                    SymbolKind.Local,
                    Types.fn(Types.int, Types.fn(Types.unit), Types.int),
                    0,
                    true,
                    true
                )
            )
        }

        test("should check that parameter argument types match") {
            messages("test(10, {})", ns).shouldBeEmpty()
            messages("test(10, 20)", ns).should {
                it.shouldHaveSize(1)
                it.first().shouldBeTypeOf<TypeInferenceFailed>()

                it[0].shouldBeTypeOf<TypeMismatch>().should { error ->
                    error.expected shouldBe OldType.fn(OldType.unit)
                    error.actual shouldBe OldType.int
                }
            }
        }

        test("should check function arity") {
            analyze(ast("test(1)", ns, ignoreCompilationErrors = true)).should {
                it.shouldHaveSize(1)
                it[0].shouldBeTypeOf<FunctionArityError>().should { error ->
                    error.expectedCount shouldBe 2
                    error.actualCount shouldBe 1
                }
            }
        }

        test("should check that only functions are called") {
            analyze(ast("x()", ns, ignoreCompilationErrors = true)).should {
                it.shouldHaveSize(1)
                it[0].shouldBeTypeOf<NotAFunction>()
            }
        }

        test("should resolve generic return type for complex case") {
            // given
            val ns = GlobalCompilationNamespace()
            val T = TypeVariable("T")
            val R = TypeVariable("R")
            ns.getDefaultPackage().symbols.apply {
                add(
                    Symbol(
                        "user", "default", "map", SymbolKind.Local,
                        FunctionType(
                            listOf(Types.array(T), Types.fn(T, R), Types.array(T)),
                            typeSchemeVariables = listOf(T, R)
                        ),
                        0, true, true
                    )
                )

                add(
                    Symbol(
                        "user",
                        "default",
                        "operation",
                        SymbolKind.Local,
                        Types.fn(Types.int, Types.string),
                        0,
                        true,
                        true
                    )
                )
                add(Symbol("user", "default", "arr", SymbolKind.Local, Types.array(Types.int), 0, true, true))
            }
//            localScope.addSymbol("operation", OldType.fn(string, intType), SymbolType.Local)
//            localScope.addSymbol("arr", array(intType), SymbolType.Local)

            // when
            val result = ast("map(arr, operation)", ns)

            // then
            result.newType shouldBe Types.array(Types.string)
        }

        test("should check explicitly specified call type params") {
            // given
            val localScope = CompilationScope(ScopeType.Package)
            localScope.addSymbol(
                "map", OldType.genericFn(
                    listOf(OldType.typeParameter("T"), OldType.typeParameter("R")),
                    OldType.array(OldType.typeParameter("R")),
                    OldType.array(OldType.typeParameter("T")),
                    OldType.fn(OldType.typeParameter("R"), OldType.typeParameter("T"))
                ),
                SymbolType.Local
            )
            localScope.addSymbol("operation", OldType.fn(OldType.unit, OldType.int), SymbolType.Local)
            localScope.addSymbol("arr", OldType.array(OldType.int), SymbolType.Local)

            // when
            val messages =
                analyze(ast("map[int, string](arr, operation)", ns, ignoreCompilationErrors = true))

            // then
            messages shouldHaveSize 1
            messages[0].shouldBeTypeOf<TypeInferenceFailed>()
        }

        test("should check explicitly specified type parameter when it's only used as return value") {
            val ns = GlobalCompilationNamespace()
            val T = TypeVariable("T")
            ns.getDefaultPackage().symbols.apply {
                add(
                    Symbol(
                        "user", "default", "unsafeArray", SymbolKind.Local,
                        FunctionType(
                            listOf(Types.int, Types.array(T)),
                            typeSchemeVariables = listOf(T)
                        ), 0, true, true
                    )
                )
            }

            val result = ast("unsafeArray[int](10)", ns)

            result.newType shouldBe Types.array(Types.int)
        }

        test("constructing recurring generic data type should work") {
            ast(
                """
                    data List[T] = Node(head: T, tail: List[T]) | Nil
                    Node(10, Node(20, Nil))
                """.trimIndent()
            )
        }


        test("typechecking should work for generic parameter types in type constructors") {
            val result = analyze(
                ast(
                    """
                    data List[T] = Node(head: T, tail: List[T]) | Nil
                    Node(10, Node("string", Nil))
                """.trimIndent(), ignoreCompilationErrors = true
                )
            )

            result.shouldNotBeEmpty()
            result[0].shouldBeTypeOf<TypeMismatch>() should {
                it.expected shouldBe OldType.string
                it.actual shouldBe OldType.int
            }
        }

        test("should check types for chain function calls") {
            val result = analyze(
                ast(
                    """
                        val foo = { a: int ->
                        	{ 42 }
                        }
                        foo()()
                    """.trimIndent(), ignoreCompilationErrors = true
                )
            )

            result shouldHaveSize 1
            result[0].shouldBeTypeOf<FunctionArityError>() should {
                it.expectedCount shouldBe 1
                it.actualCount shouldBe 0
            }
        }

        test("should accept function returning non-unit value when unit-returning function is expected as an argument") {
            val code = """
                fn forEach(f: (string) -> unit) { }    
                forEach({ it: string -> it })
            """.trimIndent()

            val result = analyze(ast(code))

            result.shouldBeEmpty()
        }
    }
}