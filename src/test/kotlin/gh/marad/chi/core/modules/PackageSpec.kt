package gh.marad.chi.core.modules

import gh.marad.chi.compile
import gh.marad.chi.core.analyzer.InvalidModuleName
import gh.marad.chi.core.analyzer.InvalidPackageName
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.messages
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import org.junit.jupiter.api.Test

class PackageSpec {
    @Test
    fun `should set current module and package and define name there`() {
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
        val targetSymbolTable = namespace.getOrCreatePackage("my.module", "some.system").symbols
        targetSymbolTable.hasSymbol("millis").shouldBeTrue()

        val defaultSymbolTable = namespace.getDefaultPackage().symbols
        defaultSymbolTable.hasSymbol("millis") shouldBe false
    }

    @Test
    fun `should not allow empty module name`() {
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

    @Test
    fun `should not allow empty package name`() {
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
}