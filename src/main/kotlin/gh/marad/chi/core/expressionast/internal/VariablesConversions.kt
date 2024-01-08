package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.parser.readers.ParseFieldAccess
import gh.marad.chi.core.parser.readers.ParseIndexOperator
import gh.marad.chi.core.parser.readers.ParseMethodInvocation
import gh.marad.chi.core.types.Types

fun convertIndexOperator(ctx: ConversionContext, ast: ParseIndexOperator): IndexOperator =
    IndexOperator(
        variable = generateExpressionAst(ctx, ast.variable),
        index = generateExpressionAst(ctx, ast.index),
        sourceSection = ast.section
    )

fun convertFieldAccess(ctx: ConversionContext, ast: ParseFieldAccess): Expression {
    val pkg = ctx.imports.lookupPackage(ast.receiverName)

    if (pkg != null) {
        return VariableAccess(
            target = LocalSymbol("", false),
            sourceSection = ast.section
        )
    }

    val receiver = generateExpressionAst(ctx, ast.receiver)
    val scope = ctx.namespace.getOrCreatePackage(/*receiver.type.moduleName*/ "module", /*receiver.type.packageName*/ "package").scope
    if (scope.containsSymbol(ast.memberName)) {
        return VariableAccess(
            target = LocalSymbol("", false),
            sourceSection = ast.memberSection
        )
    }

    return FieldAccess(
        receiver,
        ast.memberName,
        typeIsModuleLocal = false, //ctx.currentModule == receiver.type.moduleName,
        ast.section,
        ast.memberSection,
    )
}

fun convertMethodInvocation(ctx: ConversionContext, ast: ParseMethodInvocation): Expression {
    val receiver = generateExpressionAst(ctx, ast.receiver)
    val pkg = ctx.imports.lookupPackage(ast.receiverName)

    val function = sequenceOf(
        {
            if (pkg != null) {
                VariableAccess(
                    target = LocalSymbol("", false),
                    sourceSection = ast.memberSection
                )
            } else null
        },
        {
            val scope = ctx.namespace.getOrCreatePackage(/*receiver.type.moduleName*/ "module", /*receiver.type.packageName*/ "package").scope
            if (scope.containsSymbol(ast.methodName)) {
                VariableAccess(
                    target = LocalSymbol("", false),
                    sourceSection = ast.memberSection
                )
            } else null
        },
        {
            val methodLookup = ctx.lookup(ast.methodName)
            val methodPkg = ctx.namespace.getOrCreatePackage(methodLookup.moduleName, methodLookup.packageName)
            VariableAccess(
                target = LocalSymbol("", false),
                sourceSection = ast.memberSection
            )
        }
    ).map { it() }.filterNotNull().first()

    val convertedArguments = ast.arguments.map { generateExpressionAst(ctx, it) }

    val arguments = if (pkg != null) {
        convertedArguments
    } else {
        listOf(receiver) + convertedArguments
    }

    return FnCall(
        function = function,
        callTypeParameters = listOf(Types.unit),
        parameters = arguments,
        ast.memberSection
    )
}


