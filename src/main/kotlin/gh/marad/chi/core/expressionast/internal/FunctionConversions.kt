package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Block
import gh.marad.chi.core.Fn
import gh.marad.chi.core.FnParam
import gh.marad.chi.core.NameDeclaration
import gh.marad.chi.core.expressionast.ConversionContext
import gh.marad.chi.core.expressionast.generateExpressionAst
import gh.marad.chi.core.parser.readers.ParseFuncWithName
import gh.marad.chi.core.parser.readers.ParseLambda
import gh.marad.chi.core.types.TypeVariable
import gh.marad.chi.core.types.Types

fun convertLambda(ctx: ConversionContext, ast: ParseLambda): Fn {
    return ctx.withNewFunctionScope {
        val params = ast.formalArguments.map {
            FnParam(
                it.name,
                Types.unit,
                it.section
            ).also { param ->
//                ctx.currentScope.addSymbol(param.name, param.type, SymbolType.Argument, public = false, mutable = false)
            }
        }
        val body = ast.body.map { generateExpressionAst(ctx, it) }
        Fn(
            fnScope = ctx.currentScope,
            typeVariables = emptyList(),
            parameters = params,
            body = Block(body, ast.section),
            sourceSection = ast.section,
        )
    }
}

fun convertFuncWithName(ctx: ConversionContext, ast: ParseFuncWithName): NameDeclaration {
    val typeParameterNames = ast.typeParameters.map { it.name }.toSet()
    return NameDeclaration(
        public = ast.public,
        enclosingScope = ctx.currentScope,
        name = ast.name,
        value = ctx.withNewFunctionScope {
            Fn(
                fnScope = ctx.currentScope,
//                genericTypeParameters = ast.typeParameters.map { GenericTypeParameter(it.name) },
                typeVariables = ast.typeParameters.map { TypeVariable(it.name) },
                parameters = ast.formalArguments.map {
                    FnParam(
                        it.name,
                        Types.unit,
                        it.section
                    ).also { param ->
//                        ctx.currentScope.addSymbol(
//                            param.name,
//                            param.type,
//                            SymbolType.Argument,
//                            public = false,
//                            mutable = false
//                        )
                    }
                },
                body = ctx.withTypeParameters(typeParameterNames) { generateExpressionAst(ctx, ast.body) as Block },
                sourceSection = ast.section
            )
        },
        mutable = false,
        expectedType = null,
        sourceSection = ast.section
    )
}

