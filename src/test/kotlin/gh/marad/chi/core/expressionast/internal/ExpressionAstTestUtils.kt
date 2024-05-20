package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.CompilationDefaults
import gh.marad.chi.core.Expression
import gh.marad.chi.core.VariableAccess
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.FormalArgument
import gh.marad.chi.core.parser.readers.ModuleName
import gh.marad.chi.core.parser.readers.PackageName
import gh.marad.chi.core.parser.readers.TypeNameRef
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf

val testSource = ChiSource("dummy code")
val testSection = testSource.getSection(0, 5)
val sectionA = testSource.getSection(0, 1)
val sectionB = testSource.getSection(1, 2)
val sectionC = testSource.getSection(2, 3)

val defaultModule = ModuleName(CompilationDefaults.defaultModule, null)
val defaultPackage = PackageName(CompilationDefaults.defaultPacakge, null)

val intTypeRef = TypeNameRef(null, null, "int", sectionA)

fun arg(name: String, typeName: String) = FormalArgument(name, TypeNameRef(null, null, typeName, sectionB), section = sectionA)
fun intArg(name: String) = FormalArgument(name, intTypeRef, section = sectionA)

fun Expression.shouldBeVariable(name: String, section: ChiSource.Section? = null) {
    this.shouldBeTypeOf<VariableAccess>() should {
        it.target.name shouldBe name
        if (section != null) {
            it.sourceSection shouldBe section
        }
    }
}


