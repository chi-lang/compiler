package gh.marad.chi.core

import gh.marad.chi.core.expressionast.ExpressionVisitor
import gh.marad.chi.core.namespace.CompilationScope
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.types.Type

sealed interface Expression {
    val sourceSection: ChiSource.Section?
    val type: OldType
    var newType: Type?

    fun accept(visitor: ExpressionVisitor)
}

data class Program(
    val packageDefinition: Package?,
    val imports: List<Import>,
    val expressions: List<Expression>,
    val sourceSection: ChiSource.Section? = null)

data class Package(val moduleName: String, val packageName: String, val sourceSection: ChiSource.Section?)

data class ImportEntry(
    val name: String,
    val alias: String?,
    val isTypeImport: Boolean,
    val isPublic: Boolean?,
    val sourceSection: ChiSource.Section?
)

data class Import(
    val moduleName: String,
    val packageName: String,
    val packageAlias: String?,
    val entries: List<ImportEntry>,
    val withinSameModule: Boolean,
    val sourceSection: ChiSource.Section?
)

data class DefineVariantType(
    val baseVariantType: VariantType,
    val constructors: List<VariantTypeConstructor>,
    override val sourceSection: ChiSource.Section?,
) : Expression {
    override val type: OldType = OldType.unit
    val name get() = baseVariantType.simpleName
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitDefineVariantType(this)
}

data class VariantTypeConstructor(
    val public: Boolean,
    val name: String,
    val fields: List<VariantTypeField>,
    val sourceSection: ChiSource.Section?
) {
    fun toVariant() = VariantType.Variant(public, name, fields.map { it.toVariantField() })
}

data class VariantTypeField(
    val public: Boolean,
    val name: String,
    val type: OldType,
    val sourceSection: ChiSource.Section?
) {
    fun toVariantField() = VariantType.VariantField(public, name, type)
}

data class Atom(val value: String, override val type: OldType, override val sourceSection: ChiSource.Section?) :
    Expression {
    companion object {
        fun unit(sourceSection: ChiSource.Section? = null) = Atom("()", OldType.unit, sourceSection)
        fun int(value: Long, sourceSection: ChiSource.Section?) = Atom("$value", OldType.intType, sourceSection)
        fun float(value: Float, sourceSection: ChiSource.Section?) = Atom("$value", OldType.floatType, sourceSection)
        fun bool(b: Boolean, sourceSection: ChiSource.Section?) = if (b) t(sourceSection) else f(sourceSection)
        fun t(sourceSection: ChiSource.Section?) = Atom("true", OldType.bool, sourceSection)
        fun f(sourceSection: ChiSource.Section?) = Atom("false", OldType.bool, sourceSection)
        fun string(value: String, sourceSection: ChiSource.Section?) = Atom(value, OldType.string, sourceSection)
    }

    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitAtom(this)

    override fun toString(): String = "Atom($value: $type)"
}

data class InterpolatedString(val parts: List<Expression>, override val sourceSection: ChiSource.Section?) :
    Expression {
    override val type: OldType = OldType.string
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitInterpolatedString(this)
}

data class VariableAccess(
    val moduleName: String,
    val packageName: String,
    val definitionScope: CompilationScope,
    val name: String,
    val isModuleLocal: Boolean,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType
        get() = definitionScope.getSymbolType(name) ?: OldType.undefined
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitVariableAccess(this)
}

data class FieldAccess(
    val receiver: Expression,
    val fieldName: String,
    val typeIsModuleLocal: Boolean,
    override val sourceSection: ChiSource.Section?,
    val memberSection: ChiSource.Section?,
) : Expression {
    override val type: OldType
        get() {
            val recvType = receiver.type
            return if (recvType is CompositeType) recvType.memberType(fieldName) ?: OldType.undefined
            else OldType.undefined
        }
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFieldAccess(this)
}

data class FieldAssignment(
    val receiver: Expression,
    val fieldName: String,
    val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType
        get() = (receiver.type as CompositeType).memberType(fieldName) ?: OldType.undefined

    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFieldAssignment(this)
}

data class Assignment(
    val definitionScope: CompilationScope, val name: String, val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType get() = value.type
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitAssignment(this)
}

data class NameDeclaration(
    val public: Boolean,
    val enclosingScope: CompilationScope,
    val name: String,
    val value: Expression,
    val mutable: Boolean,
    val expectedType: OldType?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType get() = expectedType ?: value.type
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitNameDeclaration(this)
}

data class Group(val value: Expression, override val sourceSection: ChiSource.Section?) : Expression {
    override val type: OldType
        get() = value.type
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitGroup(this)
}

data class FnParam(val name: String, val type: OldType, val sourceSection: ChiSource.Section?)
data class Fn(
    val fnScope: CompilationScope,
    val genericTypeParameters: List<GenericTypeParameter>,
    val parameters: List<FnParam>,
    val returnType: OldType,
    val body: Block,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType get() = FnType(genericTypeParameters, parameters.map { it.type }, returnType)
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFn(this)
}

data class Block(val body: List<Expression>, override val sourceSection: ChiSource.Section?) : Expression {
    override val type: OldType get() = body.lastOrNull()?.type ?: OldType.unit
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitBlock(this)
}

data class FnCall(
    val function: Expression,
    val callTypeParameters: List<OldType>,
    val parameters: List<Expression>,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType
        get() {
            val functionType: FnType = when (val fnType = function.type) {
                is FnType -> fnType
                is OverloadedFnType -> fnType.getType(parameters.map { it.type }) ?: return OldType.undefined
                else -> return fnType
            }
            return resolveGenericType(
                functionType,
                callTypeParameters,
                parameters,
            )
        }
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFnCall(this)
}

data class IfElse(
    val condition: Expression,
    val thenBranch: Expression,
    val elseBranch: Expression?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType
        get() = if (thenBranch.type == elseBranch?.type) thenBranch.type else OldType.unit
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIfElse(this)
}

data class InfixOp(
    val op: String,
    val left: Expression,
    val right: Expression,
    override val sourceSection: ChiSource.Section?
) :
    Expression {
    // FIXME: this should probably choose broader type
    override val type: OldType
        get() = when (op) {
            in listOf("==", "!=", "<", ">", "<=", ">=", "&&", "||") -> OldType.bool
            else -> left.type
        }
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitInfixOp(this)
}

data class PrefixOp(val op: String, val expr: Expression, override val sourceSection: ChiSource.Section?) : Expression {
    override val type: OldType get() = expr.type
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitPrefixOp(this)
}

data class Cast(val expression: Expression, val targetType: OldType, override val sourceSection: ChiSource.Section?) :
    Expression {
    override val type: OldType get() = targetType
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitCast(this)
}

data class WhileLoop(val condition: Expression, val loop: Expression, override val sourceSection: ChiSource.Section?) :
    Expression {
    override val type: OldType get() = OldType.unit
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitWhileLoop(this)
}

data class Break(override val sourceSection: ChiSource.Section?) : Expression {
    override val type: OldType
        get() = OldType.unit
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitBreak(this)
}

data class Continue(override val sourceSection: ChiSource.Section?) : Expression {
    override val type: OldType
        get() = OldType.unit
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitContinue(this)
}

data class IndexOperator(
    val variable: Expression,
    val index: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType
        get() {
//            assert(variable.type.isIndexable()) { "Cannot index types other than array!" }
            return variable.type.indexedElementType()
        }
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIndexOperator(this)
}

data class IndexedAssignment(
    val variable: Expression,
    val index: Expression,
    val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType
        get() {
//            assert(variable.type.isIndexable()) { "Cannot index types other than array!" }
            return variable.type.indexedElementType()
        }
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIndexedAssignment(this)
}

data class Is(val value: Expression, val typeOrVariant: String, override val sourceSection: ChiSource.Section?) :
    Expression {
    override val type: OldType = OldType.bool
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIs(this)
}

data class EffectDefinition(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val public: Boolean,
    val genericTypeParameters: List<GenericTypeParameter>,
    val parameters: List<FnParam>,
    val returnType: OldType,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override val type: OldType get() = FnType(genericTypeParameters, parameters.map { it.type }, returnType)
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitEffectDefinition(this)
}

data class Handle(
    val body: Block,
    val cases: List<HandleCase>,
    override val sourceSection: ChiSource.Section?,
) : Expression {
    override val type: OldType get() = body.type
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitHandle(this)
}

data class HandleCase(
    val moduleName: String,
    val packageName: String,
    val effectName: String,
    val argumentNames: List<String>,
    val body: Expression,
    val scope: CompilationScope,
    val sourceSection: ChiSource.Section?
)

data class Return(val value: Expression?,
                  override val sourceSection: ChiSource.Section?) : Expression {
    override val type: OldType get() = value?.type ?: OldType.unit
    override var newType: Type? = null
    override fun accept(visitor: ExpressionVisitor) = visitor.visitReturn(this)
}