// Generated from ChiParser.g4 by ANTLR 4.12.0
package gh.marad.chi.core.antlr;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link ChiParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface ChiParserVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link ChiParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(ChiParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#package_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_definition(ChiParser.Package_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#import_definition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_definition(ChiParser.Import_definitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#package_import_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_import_alias(ChiParser.Package_import_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#import_entry}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_entry(ChiParser.Import_entryContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#import_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImport_name(ChiParser.Import_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#name_import_alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName_import_alias(ChiParser.Name_import_aliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#module_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitModule_name(ChiParser.Module_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#package_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPackage_name(ChiParser.Package_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#variantTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariantTypeDefinition(ChiParser.VariantTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#fullVariantTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFullVariantTypeDefinition(ChiParser.FullVariantTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#simplifiedVariantTypeDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSimplifiedVariantTypeDefinition(ChiParser.SimplifiedVariantTypeDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#variantTypeConstructors}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariantTypeConstructors(ChiParser.VariantTypeConstructorsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#variantTypeConstructor}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariantTypeConstructor(ChiParser.VariantTypeConstructorContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#variantFields}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariantFields(ChiParser.VariantFieldsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#variantField}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariantField(ChiParser.VariantFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#whenExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenExpression(ChiParser.WhenExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#whenConditionCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenConditionCase(ChiParser.WhenConditionCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#whenElseCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenElseCase(ChiParser.WhenElseCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#whenCaseBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenCaseBody(ChiParser.WhenCaseBodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#lambda}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambda(ChiParser.LambdaContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#block}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlock(ChiParser.BlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#effectDefinition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEffectDefinition(ChiParser.EffectDefinitionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#handleExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandleExpression(ChiParser.HandleExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#handleCase}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandleCase(ChiParser.HandleCaseContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#handleCaseEffectParam}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandleCaseEffectParam(ChiParser.HandleCaseEffectParamContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#handleCaseBody}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandleCaseBody(ChiParser.HandleCaseBodyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Cast}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCast(ChiParser.CastContext ctx);
	/**
	 * Visit a parse tree produced by the {@code StringExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringExpr(ChiParser.StringExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BoolExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolExpr(ChiParser.BoolExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IfExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfExpr(ChiParser.IfExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ContinueExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueExpr(ChiParser.ContinueExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code MethodInvocation}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMethodInvocation(ChiParser.MethodInvocationContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NotOp}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNotOp(ChiParser.NotOpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code PlaceholderExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlaceholderExpr(ChiParser.PlaceholderExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OpEqualExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpEqualExpr(ChiParser.OpEqualExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NumberExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNumberExpr(ChiParser.NumberExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FuncWithName}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncWithName(ChiParser.FuncWithNameContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IndexedAssignment}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexedAssignment(ChiParser.IndexedAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WeaveExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWeaveExpr(ChiParser.WeaveExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IsExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIsExpr(ChiParser.IsExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldAccessExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAccessExpr(ChiParser.FieldAccessExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code HandleExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitHandleExpr(ChiParser.HandleExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IdExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdExpr(ChiParser.IdExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code LambdaExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLambdaExpr(ChiParser.LambdaExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FieldAssignment}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFieldAssignment(ChiParser.FieldAssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code AssignmentExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentExpr(ChiParser.AssignmentExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code IndexOperator}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIndexOperator(ChiParser.IndexOperatorContext ctx);
	/**
	 * Visit a parse tree produced by the {@code GroupExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroupExpr(ChiParser.GroupExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EffectDef}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEffectDef(ChiParser.EffectDefContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NameDeclarationExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNameDeclarationExpr(ChiParser.NameDeclarationExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WhileLoopExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileLoopExpr(ChiParser.WhileLoopExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code WhenExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenExpr(ChiParser.WhenExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BinOp}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBinOp(ChiParser.BinOpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code FnCallExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFnCallExpr(ChiParser.FnCallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NegationExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNegationExpr(ChiParser.NegationExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code BreakExpr}
	 * labeled alternative in {@link ChiParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakExpr(ChiParser.BreakExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#divMul}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDivMul(ChiParser.DivMulContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#plusMinus}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlusMinus(ChiParser.PlusMinusContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#opEqual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOpEqual(ChiParser.OpEqualContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#and}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAnd(ChiParser.AndContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#or}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOr(ChiParser.OrContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#callGenericParameters}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallGenericParameters(ChiParser.CallGenericParametersContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#expr_comma_list}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr_comma_list(ChiParser.Expr_comma_listContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#assignment}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(ChiParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitType(ChiParser.TypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#typeNameRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeNameRef(ChiParser.TypeNameRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#functionTypeRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionTypeRef(ChiParser.FunctionTypeRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#typeConstructorRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeConstructorRef(ChiParser.TypeConstructorRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#name_declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitName_declaration(ChiParser.Name_declarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#func_with_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_with_name(ChiParser.Func_with_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#generic_type_definitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGeneric_type_definitions(ChiParser.Generic_type_definitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#func_argument_definitions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_argument_definitions(ChiParser.Func_argument_definitionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#argumentsWithTypes}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentsWithTypes(ChiParser.ArgumentsWithTypesContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#argumentWithType}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentWithType(ChiParser.ArgumentWithTypeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#func_body}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_body(ChiParser.Func_bodyContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#func_return_type}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunc_return_type(ChiParser.Func_return_typeContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#string}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitString(ChiParser.StringContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#stringPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringPart(ChiParser.StringPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#if_expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIf_expr(ChiParser.If_exprContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#bool}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBool(ChiParser.BoolContext ctx);
	/**
	 * Visit a parse tree produced by {@link ChiParser#ws}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWs(ChiParser.WsContext ctx);
}