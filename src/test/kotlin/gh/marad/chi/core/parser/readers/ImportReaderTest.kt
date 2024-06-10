package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.parser
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class ImportReaderTest {
    @Test
    fun `parse import definition`() {
        val source = ChiSource("import some.module/some.pkg as pkgAlias { foo as fooAlias, bar as barAlias }")
        val import = ImportReader.read(source, parser(source).import_definition())
        import should {
            it.moduleName shouldBe "some.module"
            it.packageName shouldBe "some.pkg"
            it.packageAlias shouldBe "pkgAlias"
            it.entries shouldHaveSize 2
            it.entries[0] should { fooEntry ->
                fooEntry.name shouldBe "foo"
                fooEntry.alias shouldBe "fooAlias"
                fooEntry.section?.getCode() shouldBe "foo as fooAlias"
            }
            it.entries[1] should { barEntry ->
                barEntry.name shouldBe "bar"
                barEntry.alias shouldBe "barAlias"
                barEntry.section?.getCode() shouldBe "bar as barAlias"
            }
            it.section?.getCode() shouldBe source.code
        }
    }

}