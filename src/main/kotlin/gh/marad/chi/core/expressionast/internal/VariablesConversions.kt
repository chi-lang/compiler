package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.*
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.namespace.FnSymbol
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.namespace.SymbolKind
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.types.Types

fun convertVariableRead(ctx: ConversionContext, ast: ParseVariableRead): VariableAccess {
    val lookup = ctx.lookup(ast.variableName)
    return VariableAccess(
        target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
        sourceSection = ast.section
    )
}

fun convertNameDeclaration(ctx: ConversionContext, ast: ParseNameDeclaration): NameDeclaration {
    return NameDeclaration(
        enclosingScope = ctx.currentScope,
        public = ast.public,
        name = ast.symbol.name,
        value = generateExpressionAst(ctx, ast.value),
        mutable = ast.mutable,
//        expectedType = ast.typeRef?.let { ctx.resolveType(it) },
        expectedType = Types.any,
        sourceSection = ast.section
    ).also {
//        ctx.currentScope.addSymbol(it.name, it.type, SymbolType.Local, public = it.public, mutable = it.mutable)
    }
}

fun convertAssignment(ctx: ConversionContext, ast: ParseAssignment): Assignment =
    // TODO czy tutaj nie lepiej mieć zamiast `name` VariableAccess i mieć tam nazwę i pakiet?
    Assignment(
        target = PackageSymbol(Symbol("","", ast.variableName, Types.unit, false, false)),
        value = generateExpressionAst(ctx, ast.value),
        sourceSection = ast.section
    )

fun convertIndexedAssignment(ctx: ConversionContext, ast: ParseIndexedAssignment): IndexedAssignment =
    IndexedAssignment(
        variable = generateExpressionAst(ctx, ast.variable),
        index = generateExpressionAst(ctx, ast.index),
        value = generateExpressionAst(ctx, ast.value),
        sourceSection = ast.section
    )

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
            target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
            sourceSection = ast.section
        )
    }

    val receiver = generateExpressionAst(ctx, ast.receiver)
    val scope = ctx.namespace.getOrCreatePackage(/*receiver.type.moduleName*/ "module", /*receiver.type.packageName*/ "package").scope
    if (scope.containsSymbol(ast.memberName)) {
        return VariableAccess(
            target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
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
                    target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
                    sourceSection = ast.memberSection
                )
            } else null
        },
        {
            val scope = ctx.namespace.getOrCreatePackage(/*receiver.type.moduleName*/ "module", /*receiver.type.packageName*/ "package").scope
            if (scope.containsSymbol(ast.methodName)) {
                VariableAccess(
                    target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
                    sourceSection = ast.memberSection
                )
            } else null
        },
        {
            val methodLookup = ctx.lookup(ast.methodName)
            val methodPkg = ctx.namespace.getOrCreatePackage(methodLookup.moduleName, methodLookup.packageName)
            VariableAccess(
                target = LocalSymbol(FnSymbol("", SymbolKind.Local, null, false)),
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


fun convertFieldAssignment(ctx: ConversionContext, ast: ParseFieldAssignment): FieldAssignment {
    return FieldAssignment(
        receiver = generateExpressionAst(ctx, ast.receiver),
        fieldName = ast.memberName,
        value = generateExpressionAst(ctx, ast.value),
        sourceSection = ast.section,
        memberSection = ast.memberSection
    )
}

