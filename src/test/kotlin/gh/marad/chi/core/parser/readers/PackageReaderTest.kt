package gh.marad.chi.core.parser.readers

import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.parser
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test


class PackageReaderTest {
    @Test
    fun `parse package definition`() {
        val source = ChiSource("package my.module/some.pkg")
        val result = PackageReader.read(source, parser(source).package_definition())
        result should {
            it.moduleName shouldBe "my.module"
            it.packageName shouldBe "some.pkg"
        }
    }

    @Test
    fun `can parse empty module`() {
        val source = ChiSource("package /some.pkg")
        val result = PackageReader.read(source, parser(source).package_definition())
        result should {
            it.moduleName shouldBe ""
            it.packageName shouldBe "some.pkg"
        }
    }

    @Test
    fun `can parse empty package`() {
        val source = ChiSource("package my.module/")
        val result = PackageReader.read(source, parser(source).package_definition())
        result should {
            it.moduleName shouldBe "my.module"
            it.packageName shouldBe ""
        }
    }
}