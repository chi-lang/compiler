package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CannotAccessInternalName
import gh.marad.chi.core.analyzer.ErrorMessage
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeLookupTable

class VisibilityCheckingVisitor(
    private val currentModule: String,
    private val typeLookupTable: TypeLookupTable,
    private val ns: GlobalCompilationNamespace,
) : DefaultExpressionVisitor {
    private var messages = mutableListOf<Message>()

    fun check(exprs: List<Expression>, messages: MutableList<Message>) {
        this.messages = messages
        exprs.forEach(this::visit)
    }

    override fun visitVariableAccess(variableAccess: VariableAccess) {
        when(variableAccess.target) {
            is LocalSymbol -> {} // local symbols can be accessed normally
            is PackageSymbol -> {
                val symbol = ns.getOrCreatePackage(variableAccess.target.moduleName, variableAccess.target.packageName)
                    .symbols.get(variableAccess.target.name)
                if (variableAccess.target.moduleName != currentModule && symbol?.public == false) {
                    messages.add(CannotAccessInternalName(
                        variableAccess.target.toString(),
                        variableAccess.sourceSection.toCodePoint()))
                }
            }
        }
    }

    override fun visitFieldAccess(fieldAccess: FieldAccess) {
        val target = fieldAccess.target!!
        when(target) {
            DotTarget.Field -> {
                verifyFieldAccessible(
                    fieldAccess.receiver.newType!!,
                    fieldAccess.fieldName,
                    fieldAccess.memberSection
                )
            }
            DotTarget.LocalFunction -> {}
            is DotTarget.PackageFunction -> {
                val symbol = ns.getOrCreatePackage(target.moduleName, target.packageName).symbols.get(target.name)
                if (symbol?.moduleName != currentModule && symbol?.public == false) {
                    messages.add(CannotAccessInternalName(target.name, fieldAccess.memberSection.toCodePoint()))
                }
            }
        }
        super.visitFieldAccess(fieldAccess)
    }

    override fun visitFieldAssignment(fieldAssignment: FieldAssignment) {
        verifyFieldAccessible(
            fieldAssignment.receiver.newType!!,
            fieldAssignment.fieldName,
            fieldAssignment.memberSection)
        super.visitFieldAssignment(fieldAssignment)
    }

    private fun verifyFieldAccessible(receiverType: Type, fieldName: String, sourceSection: ChiSource.Section?) {

        val info = typeLookupTable.find(receiverType)
        if (info == null) {
            messages.add(ErrorMessage("Type $receiverType not found in type table!", sourceSection.toCodePoint()))
            return
        }

        val field = info.fields.firstOrNull { it.name == fieldName }
        if (field == null) {
            messages.add(ErrorMessage("Field $fieldName not found in type $receiverType!", sourceSection.toCodePoint()))
            return
        }

        if (!field.public && info.moduleName != currentModule) {
            messages.add(CannotAccessInternalName(fieldName, sourceSection.toCodePoint()))
        }
    }

}