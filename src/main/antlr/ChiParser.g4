parser grammar ChiParser;

options { tokenVocab=ChiLexer; }

program : (package_definition ws)? (ws import_definition)* ws (expression | variantTypeDefinition)? (newline (expression | variantTypeDefinition))* ws EOF ;

newline : NEWLINE+;

// ====================================================================================================
// Package and import definitions
// ====================================================================================================
package_definition : 'package' module_name? '/' package_name?;
import_definition : 'import' module_name '/' package_name ('as' package_import_alias)? (LBRACE (import_entry ','?)+ RBRACE)?;

package_import_alias : ID;
import_entry : import_name ('as' name_import_alias)?;
import_name : ID;
name_import_alias : ID;

module_name : ID ('.' ID)*;
package_name : ID ('.' ID)*;

// ====================================================================================================
// Variant type definitions
// ====================================================================================================
variantTypeDefinition : fullVariantTypeDefinition | simplifiedVariantTypeDefinition;
fullVariantTypeDefinition: 'data' typeName=ID generic_type_definitions? '=' ws variantTypeConstructors;
simplifiedVariantTypeDefinition : 'data' PUB? typeName=ID generic_type_definitions? ('(' variantFields? ')')?;

variantTypeConstructors : variantTypeConstructor (ws '|' ws variantTypeConstructor)*;
variantTypeConstructor : PUB? variantName=ID ('(' variantFields? ')')? ;

variantFields : ws variantField ws (',' ws variantField ws)*;
variantField: PUB? name=ID ':' type ;

// ====================================================================================================
// When expression
// ====================================================================================================
whenExpression : WHEN LBRACE (ws whenConditionCase)+ ws whenElseCase? ws RBRACE ;
whenConditionCase: condition=expression ws '->' ws body=whenCaseBody;
whenElseCase: ELSE ws '->' ws body=whenCaseBody;
whenCaseBody : block | expression;

// ====================================================================================================
// Effect definitions
// ====================================================================================================
effectDefinition : PUB? 'effect' effectName=ID generic_type_definitions? arguments=func_argument_definitions (COLON type)?;
handleExpression : HANDLE ws block ws WITH ws LBRACE ws handleCase*  RBRACE;
handleCase : effectName=ID '(' handleCaseEffectParam (',' handleCaseEffectParam)* ')' ws '->' ws handleCaseBody ws;
handleCaseEffectParam : ID;
handleCaseBody : block | expression;

// ====================================================================================================
// Expressions
// ====================================================================================================
expression
    : expression AS type # Cast
    | effectDefinition # EffectDef
    | handleExpression # HandleExpr
    | expression IS variantName=ID  # IsExpr
    | 'while' expression block # WhileLoopExpr
    | whenExpression # WhenExpr
    | '(' expression ')' # GroupExpr
    | expression callGenericParameters? '(' expr_comma_list ')' # FnCallExpr
    | variable=expression '[' index=expression ']' # IndexOperator
    | func_with_name # FuncWithName
    | name_declaration #NameDeclarationExpr
    | string # StringExpr
    | receiver=expression ws PERIOD methodName=ID callGenericParameters? '(' arguments=expr_comma_list ')' # MethodInvocation
    | receiver=expression ws PERIOD memberName=ID # FieldAccessExpr
    | expression BIT_SHL expression # BinOp
    | expression BIT_SHR expression # BinOp
    | expression divMul expression # BinOp
    | expression MOD expression # BinOp
    | expression plusMinus expression # BinOp
    | expression COMP_OP expression # BinOp
    | NOT expression # NotOp
    | expression and expression # BinOp
    | expression or expression # BinOp
    | expression BIT_AND expression # BinOp
    | expression BIT_OR expression # BinOp
    | receiver=expression ws PERIOD memberName=ID '=' value=expression # FieldAssignment
    | assignment # AssignmentExpr
    | variable=expression '[' index=expression ']' '=' value=expression # IndexedAssignment
    | lambda # LambdaExpr
    | if_expr # IfExpr
    | input=expression ws WEAVE ws template=expression ws # WeaveExpr
    | variable=ID opEqual value=expression # OpEqualExpr
    | MINUS expression # NegationExpr
    | NUMBER # NumberExpr
    | bool # BoolExpr
    | ID # IdExpr
    | PLACEHOLDER # PlaceholderExpr
    | BREAK # BreakExpr
    | CONTINUE # ContinueExpr
    ;

lambda: LBRACE ws (argumentsWithTypes '->')? ws (expression ws)* RBRACE;
block : LBRACE ws (expression ws)* RBRACE;

divMul: DIV | MUL;
plusMinus: PLUS | MINUS;

opEqual: PLUS_EQUAL | MINUS_EQUAL | MUL_EQUAL | DIV_EQUAL;

and : BIT_AND BIT_AND;
or : BIT_OR BIT_OR;

callGenericParameters
    : '[' type (',' type)* ']'
    ;

expr_comma_list : expression? (COMMA expression)*;

assignment
    : ID EQUALS value=expression
    ;

type : typeNameRef | functionTypeRef | typeConstructorRef;
typeNameRef : (packageName=ID '.')? name=ID;
functionTypeRef : '(' type? (COMMA type)* ')' ARROW func_return_type;
typeConstructorRef : typeNameRef '[' type (',' type)* ']';

name_declaration
    : PUB? (VAL | VAR) ID (COLON type)? EQUALS expression
    ;

func_with_name
    : PUB? FN funcName=ID generic_type_definitions? arguments=func_argument_definitions (COLON func_return_type)? func_body
    ;

generic_type_definitions
    : '[' ID (COMMA ID)* ']'
    ;

func_argument_definitions : '(' ws argumentsWithTypes? ')';
argumentsWithTypes : argumentWithType ws (',' ws argumentWithType ws)*;
argumentWithType : ID ':' type;

func_body : block;

func_return_type : type ;

// ====================================================================================================
// Interpolated string
// ====================================================================================================
string : DB_QUOTE stringPart* CLOSE_STRING;
stringPart
    : TEXT
    | ESCAPED_QUOTE
    | ESCAPED_DOLLAR
    | ESCAPED_NEWLINE
    | ESCAPED_CR
    | ESCAPED_SLASH
    | ESCAPED_TAB
    | ID_INTERP
    | ENTER_EXPR expression RBRACE;

if_expr : IF condition=expression ws then_expr=block ws (ELSE else_expr=if_expr_else)? ;
if_expr_else : block | if_expr;

bool : TRUE | FALSE ;

ws : (WS | NEWLINE)* ;
