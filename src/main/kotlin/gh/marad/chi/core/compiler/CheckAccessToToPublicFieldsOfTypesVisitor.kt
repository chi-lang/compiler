package gh.marad.chi.core.compiler

import gh.marad.chi.core.Expression
import gh.marad.chi.core.FieldAccess
import gh.marad.chi.core.FieldAssignment
import gh.marad.chi.core.analyzer.CannotAccessInternalName
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.expressionast.DefaultExpressionVisitor
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.GenericType
import gh.marad.chi.core.types.SimpleType
import gh.marad.chi.core.types.Type

class CheckAccessToToPublicFieldsOfTypesVisitor(
    private val currentModule: String,
    private val typeTable: TypeTable
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
        val simpleType: SimpleType = if(receiverType is SimpleType) {
            receiverType
        } else if (receiverType is GenericType) {
            receiverType.types.first() as SimpleType
        } else {
            TODO("Unexpected type: $receiverType")
        }

        val info = typeTable.find(simpleType)
            ?: TODO("Type $simpleType not found in type table!")

        val field = info.fields.firstOrNull { it.name == fieldName }
            ?: TODO("Field $fieldName not found in type $simpleType!")

        if (!field.public && simpleType.moduleName != currentModule) {
            messages.add(CannotAccessInternalName(fieldName, sourceSection.toCodePoint()))
        }
    }

}