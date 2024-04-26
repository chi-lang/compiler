parser grammar ChiParser;

options { tokenVocab=ChiLexer; }

program : (package_definition ws)? (ws import_definition)* ws (typealias | expression | variantTypeDefinition | traitDefinition)? (newline (typealias | expression | variantTypeDefinition))* ws EOF ;

newline : NEWLINE+;

// ====================================================================================================
// Package and import definitions
// ====================================================================================================
package_definition : 'package' moduleName? '/' packageName?;
import_definition : 'import' moduleName '/' packageName ('as' package_import_alias)? (LBRACE (import_entry ','?)+ RBRACE)?;

package_import_alias : ID;
import_entry : import_name ('as' name_import_alias)?;
import_name : ID;
name_import_alias : ID;

moduleName : ID ('.' ID)*;
packageName : ID ('.' ID)*;

// ====================================================================================================
// Variant type definitions
// ====================================================================================================
variantTypeDefinition : fullVariantTypeDefinition | simplifiedVariantTypeDefinition;
fullVariantTypeDefinition: 'data' name=ID generic_type_definitions? '=' ws variantTypeConstructors;
simplifiedVariantTypeDefinition : 'data' PUB? name=ID generic_type_definitions? ('(' variantFields? ')')?;

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
// Traits
// ====================================================================================================

traitDefinition : 'trait' name=ID generic_type_definitions? '{' ws traitFunctionDefinition* '}';
traitFunctionDefinition : FN funcName=ID arguments=func_argument_definitions (COLON func_return_type)? ws;

// ====================================================================================================
// Types
// ====================================================================================================
typealias : TYPE name=ID generic_type_definitions? '=' type;

type
    : typeName '[' type (',' type)* ']'                     #TypeConstructorRef
    | '(' type? (COMMA type)* ')' ARROW func_return_type    #FunctionTypeRef
    | '{' recordField? (',' recordField)* '}'               #RecordType
    | type '|' type                                         #SumType
    | typeName                                              #TypeNameRef
    | UNIT                                                  #UnitTypeRef
    ;

typeName: simpleName | qualifiedName;
recordField : ws name=ID ws ':' ws type ws;

simpleName: name=ID;
qualifiedName: moduleName qualifierSeparator packageName qualifierSeparator name=ID;
qualifierSeparator: ':' ':';

// ====================================================================================================
// Expressions
// ====================================================================================================
expression
    : expression AS type # Cast
    | '{' (ws ID ws ':' ws expression)? ws (','? | (',' ws ID ':' ws expression ws)* ','?) ws '}' # CreateRecord
    | '[' ws expression? ws (',' ws expression ws)* ']' # CreateArray
    | expression IS type  # IsExpr
    | effectDefinition # EffectDef
    | handleExpression # HandleExpr
    | 'while' expression block # WhileLoopExpr
    | whenExpression # WhenExpr
    | '(' expression ')' # GroupExpr
    | expression callGenericParameters? '(' expr_comma_list ')' lambda? # FnCallExpr
    | receiver=expression ws PERIOD memberName=ID # FieldAccessExpr
    | variable=expression '[' index=expression ']' # IndexOperator
    | func_with_name # FuncWithName
    | name_declaration #NameDeclarationExpr
    | string # StringExpr
    | MINUS expression # NegationExpr
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
    | 'return' expression? # ReturnExpr
    | input=expression ws WEAVE ws template=expression ws # WeaveExpr
    | variable=ID opEqual value=expression # OpEqualExpr
    | NUMBER # NumberExpr
    | bool # BoolExpr
    | ID # IdExpr
    | UNIT # UnitValue
    | PLACEHOLDER # PlaceholderExpr
    | BREAK # BreakExpr
    | CONTINUE # ContinueExpr
    ;

lambda: LBRACE ws (argumentsWithOptionalTypes '->')? ws (expression ws)* RBRACE;
argumentsWithOptionalTypes : argumentWithOptionalType ws (',' ws argumentWithOptionalType ws)*;
argumentWithOptionalType : ID (':' type)?;
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
