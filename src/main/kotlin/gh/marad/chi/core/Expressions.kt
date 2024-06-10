package gh.marad.chi.core

import gh.marad.chi.core.expressionast.ExpressionVisitor
import gh.marad.chi.core.namespace.SymbolTable
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId

sealed interface Expression {
    val sourceSection: ChiSource.Section?
    var type: Type?
    var used: Boolean

    fun <T> accept(visitor: ExpressionVisitor<T>): T
    fun children(): List<Expression>
}

data class Program(
    val packageDefinition: Package,
    val imports: List<Import>,
    val typeAliases: List<TypeAlias>,
    val expressions: List<Expression>,
    val symbolTable: SymbolTable,
    val sourceSection: ChiSource.Section? = null)

data class Package(val moduleName: String, val packageName: String)

data class TypeAlias(
    val typeId: TypeId,
    val type: Type
)

data class Atom(val value: String,
                override var type: Type?,
                override val sourceSection: ChiSource.Section?
) : Expression {
    override var used: Boolean = false
    companion object {
        fun unit(sourceSection: ChiSource.Section? = null) = Atom("()", Type.unit, sourceSection)
        fun int(value: Long, sourceSection: ChiSource.Section?) = Atom("$value", Type.int, sourceSection)
        fun float(value: Float, sourceSection: ChiSource.Section?) = Atom("$value", Type.float, sourceSection)
        fun bool(b: Boolean, sourceSection: ChiSource.Section?) = if (b) t(sourceSection) else f(sourceSection)
        fun t(sourceSection: ChiSource.Section?) = Atom("true", Type.bool, sourceSection)
        fun f(sourceSection: ChiSource.Section?) = Atom("false", Type.bool, sourceSection)
        fun string(value: String, sourceSection: ChiSource.Section?) = Atom(value, Type.string, sourceSection)
        fun defaultArg(type: Type) = Atom("@", type, null)
    }

    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitAtom(this)
    override fun children(): List<Expression> = listOf()

    override fun toString(): String = "Atom($value: $type)"
}

data class InterpolatedString(val parts: List<Expression>, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitInterpolatedString(this)
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

    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitCreateRecord(this)
    override fun children(): List<Expression> = fields.map { it.value }
}

data class CreateArray(val values: List<Expression>, override val sourceSection: ChiSource.Section?) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitCreateArray(this)
    override fun children(): List<Expression> = values
}

data class VariableAccess(
    val target: Target,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitVariableAccess(this)
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
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitFieldAccess(this)
    override fun children(): List<Expression> = listOf(receiver)
}

data class FieldAssignment(
    val receiver: Expression,
    val fieldName: String,
    val value: Expression,
    val memberSection: ChiSource.Section?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitFieldAssignment(this)
    override fun children(): List<Expression> = listOf(receiver, value)
}

data class Assignment(
    val target: Target,
    val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitAssignment(this)
    override fun children(): List<Expression> = listOf(value)
}

data class NameDeclaration(
    val public: Boolean,
    val name: String,
    val value: Expression,
    val mutable: Boolean,
    val expectedType: Type?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitNameDeclaration(this)
    override fun children(): List<Expression> = listOf(value)
}

data class FnParam(val name: String,
                   var type: Type?,
                   val sourceSection: ChiSource.Section?)
data class Fn(
    val parameters: List<FnParam>,
    val defaultValues: Map<String, Expression>,
    val body: Block,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitFn(this)
    override fun children(): List<Expression> = listOf(body)
}

data class Block(val body: List<Expression>, override val sourceSection: ChiSource.Section?) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitBlock(this)
    override fun children(): List<Expression> = body
}

data class FnCall(
    var function: Expression,
    val parameters: MutableList<Expression>,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitFnCall(this)
    override fun children(): List<Expression> = listOf(function) + parameters
}

data class IfElse(
    val condition: Expression,
    val thenBranch: Expression,
    val elseBranch: Expression?,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitIfElse(this)
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
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitInfixOp(this)
    override fun children(): List<Expression> = listOf(left, right)
}

data class PrefixOp(val op: String, val expr: Expression, override val sourceSection: ChiSource.Section?) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitPrefixOp(this)
    override fun children(): List<Expression> = listOf(expr)
}

data class Cast(val expression: Expression, val targetType: Type, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitCast(this)
    override fun children(): List<Expression> = listOf(expression)
}

data class WhileLoop(val condition: Expression, val loop: Expression, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitWhileLoop(this)
    override fun children(): List<Expression> = listOf(condition, loop)
}

data class ForLoop(val vars: List<String>, val iterable: Expression, val state: Expression?, val init: Expression?,
                   val body: Block,
                   val varSections: List<ChiSource.Section>,
                   val iterableSection: ChiSource.Section,
                   val stateSection: ChiSource.Section?,
                   val initSection: ChiSource.Section?,
                   val bodySection: ChiSource.Section,
                   override val sourceSection: ChiSource.Section?) : Expression {
    override var type: Type? = null
    override var used: Boolean = false

    override fun <T> accept(visitor: ExpressionVisitor<T>): T =
        visitor.visitForLoop(this)

    override fun children(): List<Expression> =
        listOf(iterable, body)
}

data class Break(override val sourceSection: ChiSource.Section?) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitBreak(this)
    override fun children(): List<Expression> = listOf()
}

data class Continue(override val sourceSection: ChiSource.Section?) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitContinue(this)
    override fun children(): List<Expression> = listOf()
}

data class IndexOperator(
    val variable: Expression,
    val index: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitIndexOperator(this)
    override fun children(): List<Expression> = listOf(variable, index)
}

data class IndexedAssignment(
    val variable: Expression,
    val index: Expression,
    val value: Expression,
    override val sourceSection: ChiSource.Section?
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitIndexedAssignment(this)
    override fun children(): List<Expression> = listOf(variable, index, value)
}

data class Is(val value: Expression, val checkedType: Type, override val sourceSection: ChiSource.Section?) :
    Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitIs(this)
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
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitEffectDefinition(this)
    override fun children(): List<Expression> = listOf()
}

data class Handle(
    val body: Block,
    val cases: List<HandleCase>,
    override val sourceSection: ChiSource.Section?,
) : Expression {
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitHandle(this)
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
    override var type: Type? = null
    override var used: Boolean = false
    override fun <T> accept(visitor: ExpressionVisitor<T>): T = visitor.visitReturn(this)
    override fun children(): List<Expression> = if (value != null) {
        listOf(value)
    } else {
        emptyList()
    }
}