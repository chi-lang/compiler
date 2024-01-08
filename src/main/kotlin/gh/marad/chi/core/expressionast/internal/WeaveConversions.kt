package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.namespace.FnSymbol
import gh.marad.chi.core.namespace.SymbolKind
import gh.marad.chi.core.parser.readers.ParseWeave
import gh.marad.chi.core.parser.readers.ParseWeavePlaceholder

fun convertWeave(ctx: ConversionContext, weave: ParseWeave): Expression {
    val inputValue = generateExpressionAst(ctx, weave.value)
    val tempVarName = ctx.nextTempVarName()
    val tempVariableDeclaration = NameDeclaration(
        enclosingScope = ctx.currentScope,
        public = false,
        name = tempVarName,
        value = inputValue,
        mutable = false,
        expectedType = null,
        sourceSection = weave.value.section
    )
//    ctx.currentScope.addSymbol(tempVarName, tempVariableDeclaration.type, SymbolType.Local, false)
    val readVariable =
        VariableAccess(
            target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
            weave.value.section
        )
    val filledTemplate = ctx.withWeaveInput(readVariable) {
        generateExpressionAst(ctx, weave.opTemplate)
    }
    return Block(
        listOf(tempVariableDeclaration, filledTemplate),
        weave.section
    )
}

fun convertWeavePlaceholder(ctx: ConversionContext, placeholder: ParseWeavePlaceholder): Expression {
    return ctx.currentWeaveInput ?: TODO("This should never happen!")
}
