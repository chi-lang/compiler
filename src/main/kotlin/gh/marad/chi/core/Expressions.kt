package gh.marad.chi.core

import gh.marad.chi.core.expressionast.ExpressionVisitor
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types3.Type3
import gh.marad.chi.core.types3.TypeId

sealed interface Expression {
    val sourceSection: ChiSource.Section?
    var newType: Type3?
    var used: Boolean

    fun accept(visitor: ExpressionVisitor)
    fun children(): List<Expression>
}

data class Program(
    val packageDefinition: Package,
    val imports: List<Import>,
    val typeAliases: List<TypeAlias>,
    val expressions: List<Expression>,
    val sourceSection: ChiSource.Section? = null)

data class Package(val moduleName: String, val packageName: String)

data class TypeAlias(
    val typeId: TypeId,
    val newType: Type3
)

data class Atom(val value: String,
                override var newType: Type3?,
                override val sourceSection: ChiSource.Section?
) : Expression {
    override var used: Boolean = false
    companion object {
        fun unit(sourceSection: ChiSource.Section? = null) = Atom("()", Type3.unit, sourceSection)
        fun int(value: Long, sourceSection: ChiSource.Section?) = Atom("$value", Type3.int, sourceSection)
        fun float(value: Float, sourceSection: ChiSource.Section?) = Atom("$value", Type3.float, sourceSection)
        fun bool(b: Boolean, sourceSection: ChiSource.Section?) = if (b) t(sourceSection) else f(sourceSection)
        fun t(sourceSection: ChiSource.Section?) = Atom("true", Type3.bool, sourceSection)
        fun f(sourceSection: ChiSource.Section?) = Atom("false", Type3.bool, sourceSection)
        fun string(value: String, sourceSection: ChiSource.Section?) = Atom(value, Type3.string, sourceSection)
    }

    override fun accept(visitor: ExpressionVisitor) = visitor.visitAtom(this)
    override fun children(): List<Expression> = listOf()

    override fun toString(): String = "Atom($value: $newType)"
}

data class InterpolatedString(val parts: List<Expression>, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitInterpolatedString(this)
    override fun children(): List<Expression> = parts
}

sealed interface Target {
    val name : String
}

data class PackageSymbol(
    val moduleName: String,
    val packageName: String,
    override val name: String,
) : Target {
    override fun toString(): String = "$moduleName::$packageName::$name"
}


data class LocalSymbol(
    override val name: String,
) : Target {
    override fun toString(): String = name
}

data class CreateRecord(val fields: List<Field>, override val sourceSection: ChiSource.Section?) : Expression {
    data class Field(val name: String, val value: Expression)

    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitCreateRecord(this)
    override fun children(): List<Expression> = fields.map { it.value }
}

data class VariableAccess(
    val target: Target,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitVariableAccess(this)
    override fun children(): List<Expression> = listOf()
}

sealed interface DotTarget {
    object Field : DotTarget
    object LocalFunction: DotTarget
    data class PackageFunction(val moduleName: String, val packageName: String, val name: String) : DotTarget
}

data class FieldAccess(
    val receiver: Expression,
    val fieldName: String,
    override val sourceSection: ChiSource.Section?,
    val memberSection: ChiSource.Section?,
) : Expression {
    var target: DotTarget? = null
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFieldAccess(this)
    override fun children(): List<Expression> = listOf(receiver)
}

data class FieldAssignment(
    val receiver: Expression,
    val fieldName: String,
    val value: Expression,
    val memberSection: ChiSource.Section?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFieldAssignment(this)
    override fun children(): List<Expression> = listOf(receiver, value)
}

data class Assignment(
    val target: Target,
    val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitAssignment(this)
    override fun children(): List<Expression> = listOf(value)
}

data class NameDeclaration(
    val public: Boolean,
    val name: String,
    val value: Expression,
    val mutable: Boolean,
    val expectedType: Type3?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitNameDeclaration(this)
    override fun children(): List<Expression> = listOf(value)
}

data class FnParam(val name: String, var type: Type3?, val sourceSection: ChiSource.Section?)
data class Fn(
    val parameters: List<FnParam>,
    val body: Block,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFn(this)
    override fun children(): List<Expression> = listOf(body)
}

data class Block(val body: List<Expression>, override val sourceSection: ChiSource.Section?) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitBlock(this)
    override fun children(): List<Expression> = body
}

data class FnCall(
    var function: Expression,
    val parameters: MutableList<Expression>,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitFnCall(this)
    override fun children(): List<Expression> = listOf(function) + parameters
}

data class IfElse(
    val condition: Expression,
    val thenBranch: Expression,
    val elseBranch: Expression?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIfElse(this)
    override fun children(): List<Expression> = if (elseBranch != null) {
        listOf(condition, thenBranch, elseBranch)
    } else {
        listOf(condition, thenBranch)
    }
}

data class InfixOp(
    val op: String,
    val left: Expression,
    val right: Expression,
    override val sourceSection: ChiSource.Section?
) :
    Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitInfixOp(this)
    override fun children(): List<Expression> = listOf(left, right)
}

data class PrefixOp(val op: String, val expr: Expression, override val sourceSection: ChiSource.Section?) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitPrefixOp(this)
    override fun children(): List<Expression> = listOf(expr)
}

data class Cast(val expression: Expression, val targetType: Type3, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitCast(this)
    override fun children(): List<Expression> = listOf(expression)
}

data class WhileLoop(val condition: Expression, val loop: Expression, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitWhileLoop(this)
    override fun children(): List<Expression> = listOf(condition, loop)
}

data class Break(override val sourceSection: ChiSource.Section?) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitBreak(this)
    override fun children(): List<Expression> = listOf()
}

data class Continue(override val sourceSection: ChiSource.Section?) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitContinue(this)
    override fun children(): List<Expression> = listOf()
}

data class IndexOperator(
    val variable: Expression,
    val index: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIndexOperator(this)
    override fun children(): List<Expression> = listOf(variable, index)
}

data class IndexedAssignment(
    val variable: Expression,
    val index: Expression,
    val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIndexedAssignment(this)
    override fun children(): List<Expression> = listOf(variable, index, value)
}

data class Is(val value: Expression, val typeOrVariant: String, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitIs(this)
    override fun children(): List<Expression> = listOf(value)
}

data class EffectDefinition(
    val moduleName: String,
    val packageName: String,
    val name: String,
    val public: Boolean,
    val parameters: List<FnParam>,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitEffectDefinition(this)
    override fun children(): List<Expression> = listOf()
}

data class Handle(
    val body: Block,
    val cases: List<HandleCase>,
    override val sourceSection: ChiSource.Section?,
) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitHandle(this)
    override fun children(): List<Expression> = listOf(body) + cases.map { it.body }
}

data class HandleCase(
    val moduleName: String,
    val packageName: String,
    val effectName: String,
    val argumentNames: List<String>,
    val body: Expression,
    val sourceSection: ChiSource.Section?
)

data class Return(val value: Expression?,
                  override val sourceSection: ChiSource.Section?) : Expression {
    override var newType: Type3? = null
    override var used: Boolean = false
    override fun accept(visitor: ExpressionVisitor) = visitor.visitReturn(this)
    override fun children(): List<Expression> = if (value != null) {
        listOf(value)
    } else {
        emptyList()
    }
}