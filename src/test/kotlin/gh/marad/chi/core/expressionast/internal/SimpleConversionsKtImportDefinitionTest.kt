package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.Alias
import gh.marad.chi.core.parser.readers.ParseImportDefinition
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class SimpleConversionsKtImportDefinitionTest {

    @Test
    fun `should convert module and package name`() {
        // given
        val import = sampleImport.copy(
            moduleName = otherModule,
            packageName = otherPackage
        )

        // when
        val result = convertImportDefinition(defaultContext(), import)

        // then
        result.moduleName shouldBe otherModule.name
        result.packageName shouldBe otherPackage.name
    }

    @Test
    fun `package alias is optional`() {
        convertImportDefinition(defaultContext(), sampleImport.copy(packageAlias = null))
            .packageAlias.shouldBeNull()
    }

    @Test
    fun `should read package alias`() {
        // given
        val import = sampleImport.copy(
            packageAlias = Alias("myAlias", testSection)
        )

        // when
        val alias = convertImportDefinition(defaultContext(), import).packageAlias

        // then
        alias.shouldNotBeNull() shouldBe "myAlias"
    }

    @Test
    fun `determine if import is from the same module`() {
        // given
        val ctx = defaultContext().inPackage(
            moduleName = otherModule.name,
            packageName = otherPackage.name
        )
        val import = sampleImport.copy(
            moduleName = otherModule,
            packageName = otherPackage
        )

        // then
        convertImportDefinition(ctx, import).withinSameModule.shouldBeTrue()
    }

    @Test
    fun `determine if import is from different module`() {
        // given
        val ctx = defaultContext().inPackage(
            moduleName = "yet.another.module",
            packageName = otherPackage.name
        )
        val import = sampleImport.copy(
            moduleName = otherModule,
            packageName = otherPackage
        )

        // then
        convertImportDefinition(ctx, import).withinSameModule.shouldBeFalse()
    }

    @Test
    fun `should convert entries`() {
        // given
        val import = sampleImport.copy(
            entries = listOf(
                importEntry(
                    name = "name",
                    alias = Alias("alias", sectionA),
                    section = sectionB
                )
            )
        )

        // when
        val entry = convertImportDefinition(defaultContext(), import).entries.first()

        // then
        entry.isTypeImport.shouldBeFalse()
        entry.isPublic.shouldBeNull()
        entry.name shouldBe "name"
        entry.alias.shouldNotBeNull() shouldBe "alias"
        entry.sourceSection shouldBe sectionB
    }

    @Test
    fun `should determine if symbol is public`() {
        // given
        val ctx = defaultContext().addPublicSymbol(otherModule, otherPackage, "variable")
        val import = sampleImport.copy(
            moduleName = otherModule,
            packageName = otherPackage,
            entries = listOf(importEntry("variable"))
        )

        // when
        val entry = convertImportDefinition(ctx, import).entries.first()

        // then
        entry.isPublic.shouldNotBeNull().shouldBeTrue()
    }

    @Test
    fun `should determine if imported symbol is a type`() {
        val ctx = defaultContext()
        ctx.addTypeDefinition(otherModule, otherPackage, "SomeType")
        val import = sampleImport.copy(
            moduleName = otherModule,
            packageName = otherPackage,
            entries = listOf(importEntry("SomeType"))
        )

        // when
        val entry = convertImportDefinition(ctx, import).entries.first()

        // then
        entry.isTypeImport.shouldBeTrue()
    }

    //    private val testModuleName = ModuleName("my.mod", sectionA)
//    private val testPackageName = PackageName("my.pkg", sectionB)
    private val sampleImport =
        ParseImportDefinition(
            otherModule,
            otherPackage,
            packageAlias = null,
            entries = emptyList(),
            testSection
        )

    private fun importEntry(name: String, alias: Alias? = null, section: ChiSource.Section? = null) =
        ParseImportDefinition.Entry(name, alias, section)
}