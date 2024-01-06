package gh.marad.chi.core.analyzer

import gh.marad.chi.core.*
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.Type

fun checkModuleAndPackageNames(pkg: Package, messages: MutableList<Message>) {
    if (pkg.moduleName.isEmpty()) {
        messages.add(InvalidModuleName(pkg.moduleName, CodePoint(0, 1)))
    }
    if (pkg.packageName.isEmpty()) {
        messages.add(InvalidPackageName(pkg.packageName, CodePoint(0, 1)))
    }
}

fun checkImports(import: Import, messages: MutableList<Message>) {
    if (import.moduleName.isEmpty()) {
        messages.add(InvalidModuleName(import.moduleName, import.sourceSection.toCodePoint()))
    }
    if (import.packageName.isEmpty()) {
        messages.add(InvalidPackageName(import.packageName, import.sourceSection.toCodePoint()))
    }
    if (!import.withinSameModule) {
        import.entries.forEach {
            if (it.isPublic == false && !it.isTypeImport) {
                messages.add(ImportInternal(it.name, it.sourceSection.toCodePoint()))
            }
        }
    }
}

fun checkThatAssignmentDoesNotChangeImmutableValue(expr: Expression, messages: MutableList<Message>) {
    if (expr is Assignment && !expr.symbol.mutable) {
        messages.add(CannotChangeImmutableVariable(expr.sourceSection.toCodePoint()))
    }
}

fun checkTypes(expr: Expression, messages: MutableList<Message>) {

    fun checkTypeMatches(
        expected: Type,
        actual: Type,
        sourceSection: ChiSource.Section?,
    ) {
        if (expected != actual) {
            messages.add(TypeMismatch(expected, actual, sourceSection.toCodePoint()))
        }
    }

    fun checkFieldAssignment(expr: FieldAssignment) {
        val memberType = expr.receiver.newType!!
        val assignedType = expr.value.newType!!
        checkTypeMatches(expected = memberType, actual = assignedType, expr.value.sourceSection)
    }

    @Suppress("UNUSED_VARIABLE")
    val ignored: Any = when (expr) {
        is FieldAssignment -> checkFieldAssignment(expr)
        else -> {}
    }
}