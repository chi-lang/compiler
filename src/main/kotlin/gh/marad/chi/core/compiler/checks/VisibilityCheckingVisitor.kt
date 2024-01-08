package gh.marad.chi.core.compiler.checks

import gh.marad.chi.core.Expression
import gh.marad.chi.core.FieldAccess
import gh.marad.chi.core.FieldAssignment
import gh.marad.chi.core.analyzer.CannotAccessInternalName
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeLookupTable

class VisibilityCheckingVisitor(
    private val currentModule: String,
    private val typeLookupTable: TypeLookupTable,
) : DefaultExpressionVisitor {
    private var messages = mutableListOf<Message>()

    fun check(exprs: List<Expression>, messages: MutableList<Message>) {
        this.messages = messages
        exprs.forEach(this::visit)
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