package gh.marad.chi.core.modules

import gh.marad.chi.compile
import gh.marad.chi.core.analyzer.InvalidModuleName
import gh.marad.chi.core.analyzer.InvalidPackageName
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.messages
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

@Suppress("unused")
class PackageSpec : FunSpec({
    test("should set current module and package and define name there") {
        // when
        val namespace = GlobalCompilationNamespace()
        compile(
            """
                package my.module/some.system
                val millis = {}
            """.trimIndent(),
            namespace
        )

        // then
        val targetScope = namespace.getOrCreatePackage("my.module", "some.system").scope
        targetScope.containsSymbol("millis") shouldBe true

        val defaultScope = namespace.getDefaultPackage().scope
        defaultScope.containsSymbol("millis") shouldBe false
    }

    test("should not allow empty module name") {
        // when
        val messages = messages(
            """
                package /some.system
            """.trimIndent()
        )


        // then
        messages shouldHaveSize 1
        messages[0].shouldBeTypeOf<InvalidModuleName>()
            .should { it.moduleName shouldBe "" }
    }

    test("should not allow empty package name") {
        // when
        val messages = messages(
            """
                package some.module/
            """.trimIndent()
        )

        // then
        messages shouldHaveSize 1
        messages[0].shouldBeTypeOf<InvalidPackageName>()
            .should { it.packageName shouldBe "" }
    }
})