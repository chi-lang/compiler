package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.CannotAccessInternalName
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
        verifyFieldAccessible(
            fieldAccess.receiver.newType!!,
            fieldAccess.fieldName,
            fieldAccess.memberSection
        )
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
            ?: TODO("Type $receiverType not found in type table!")

        val field = info.fields.firstOrNull { it.name == fieldName }
            ?: TODO("Field $fieldName not found in type $receiverType!")


        if (!field.public && info.moduleName != currentModule) {
            messages.add(CannotAccessInternalName(fieldName, sourceSection.toCodePoint()))
        }
    }

}