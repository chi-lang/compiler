// Generated from ChiParser.g4 by ANTLR 4.12.0
package gh.marad.chi.core.antlr;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class ChiParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.12.0", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		PUB=1, VAL=2, VAR=3, FN=4, IF=5, ELSE=6, AS=7, WHILE=8, FOR=9, PACKAGE=10, 
		IMPORT=11, DATA=12, WHEN=13, MATCH=14, IS=15, BREAK=16, CONTINUE=17, EFFECT=18, 
		HANDLE=19, WITH=20, ARROW=21, COLON=22, LPAREN=23, RPAREN=24, LBRACE=25, 
		RBRACE=26, LSQUARE=27, RSQUARE=28, COMMA=29, PERIOD=30, DB_QUOTE=31, PLUS_EQUAL=32, 
		MINUS_EQUAL=33, MUL_EQUAL=34, DIV_EQUAL=35, EQUALS=36, WEAVE=37, PLACEHOLDER=38, 
		PLUS=39, MINUS=40, MOD=41, MUL=42, DIV=43, NOT=44, BIT_SHL=45, BIT_SHR=46, 
		BIT_AND=47, BIT_OR=48, COMP_OP=49, IS_EQ=50, NOT_EQ=51, LT=52, LEQ=53, 
		GT=54, GEQ=55, TRUE=56, FALSE=57, NUMBER=58, ID=59, NEWLINE=60, WS=61, 
		SINGLE_LINE_COMMENT=62, MULTI_LINE_COMMENT=63, STRING_ESCAPE=64, ENTER_EXPR=65, 
		ID_INTERP=66, ESCAPED_DOLLAR=67, ESCAPED_QUOTE=68, ESCAPED_NEWLINE=69, 
		ESCAPED_CR=70, ESCAPED_SLASH=71, ESCAPED_TAB=72, TEXT=73, CLOSE_STRING=74, 
		ANY=75;
	public static final int
		RULE_program = 0, RULE_package_definition = 1, RULE_import_definition = 2, 
		RULE_package_import_alias = 3, RULE_import_entry = 4, RULE_import_name = 5, 
		RULE_name_import_alias = 6, RULE_module_name = 7, RULE_package_name = 8, 
		RULE_variantTypeDefinition = 9, RULE_fullVariantTypeDefinition = 10, RULE_simplifiedVariantTypeDefinition = 11, 
		RULE_variantTypeConstructors = 12, RULE_variantTypeConstructor = 13, RULE_variantFields = 14, 
		RULE_variantField = 15, RULE_whenExpression = 16, RULE_whenConditionCase = 17, 
		RULE_whenElseCase = 18, RULE_whenCaseBody = 19, RULE_lambda = 20, RULE_block = 21, 
		RULE_effectDefinition = 22, RULE_handleExpression = 23, RULE_handleCase = 24, 
		RULE_handleCaseEffectParam = 25, RULE_handleCaseBody = 26, RULE_expression = 27, 
		RULE_divMul = 28, RULE_plusMinus = 29, RULE_opEqual = 30, RULE_and = 31, 
		RULE_or = 32, RULE_callGenericParameters = 33, RULE_expr_comma_list = 34, 
		RULE_assignment = 35, RULE_type = 36, RULE_typeNameRef = 37, RULE_functionTypeRef = 38, 
		RULE_typeConstructorRef = 39, RULE_name_declaration = 40, RULE_func_with_name = 41, 
		RULE_generic_type_definitions = 42, RULE_func_argument_definitions = 43, 
		RULE_argumentsWithTypes = 44, RULE_argumentWithType = 45, RULE_func_body = 46, 
		RULE_func_return_type = 47, RULE_string = 48, RULE_stringPart = 49, RULE_if_expr = 50, 
		RULE_then_expr = 51, RULE_else_expr = 52, RULE_bool = 53, RULE_ws = 54;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "package_definition", "import_definition", "package_import_alias", 
			"import_entry", "import_name", "name_import_alias", "module_name", "package_name", 
			"variantTypeDefinition", "fullVariantTypeDefinition", "simplifiedVariantTypeDefinition", 
			"variantTypeConstructors", "variantTypeConstructor", "variantFields", 
			"variantField", "whenExpression", "whenConditionCase", "whenElseCase", 
			"whenCaseBody", "lambda", "block", "effectDefinition", "handleExpression", 
			"handleCase", "handleCaseEffectParam", "handleCaseBody", "expression", 
			"divMul", "plusMinus", "opEqual", "and", "or", "callGenericParameters", 
			"expr_comma_list", "assignment", "type", "typeNameRef", "functionTypeRef", 
			"typeConstructorRef", "name_declaration", "func_with_name", "generic_type_definitions", 
			"func_argument_definitions", "argumentsWithTypes", "argumentWithType", 
			"func_body", "func_return_type", "string", "stringPart", "if_expr", "then_expr", 
			"else_expr", "bool", "ws"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'pub'", "'val'", "'var'", "'fn'", "'if'", "'else'", "'as'", "'while'", 
			"'FOR'", "'package'", "'import'", "'data'", "'when'", "'match'", "'is'", 
			"'break'", "'continue'", "'effect'", "'handle'", "'with'", "'->'", "':'", 
			"'('", "')'", "'{'", "'}'", "'['", "']'", "','", "'.'", null, "'+='", 
			"'-='", "'*='", "'/='", "'='", "'~>'", "'_'", "'+'", "'-'", "'%'", "'*'", 
			"'/'", "'!'", "'<<'", "'>>'", "'&'", "'|'", null, "'=='", "'!='", "'<'", 
			"'<='", "'>'", "'>='", "'true'", "'false'", null, null, null, null, null, 
			null, null, "'${'", null, "'\\$'", "'\\\"'", "'\\n'", "'\\r'", "'\\\\'", 
			"'\\t'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "PUB", "VAL", "VAR", "FN", "IF", "ELSE", "AS", "WHILE", "FOR", 
			"PACKAGE", "IMPORT", "DATA", "WHEN", "MATCH", "IS", "BREAK", "CONTINUE", 
			"EFFECT", "HANDLE", "WITH", "ARROW", "COLON", "LPAREN", "RPAREN", "LBRACE", 
			"RBRACE", "LSQUARE", "RSQUARE", "COMMA", "PERIOD", "DB_QUOTE", "PLUS_EQUAL", 
			"MINUS_EQUAL", "MUL_EQUAL", "DIV_EQUAL", "EQUALS", "WEAVE", "PLACEHOLDER", 
			"PLUS", "MINUS", "MOD", "MUL", "DIV", "NOT", "BIT_SHL", "BIT_SHR", "BIT_AND", 
			"BIT_OR", "COMP_OP", "IS_EQ", "NOT_EQ", "LT", "LEQ", "GT", "GEQ", "TRUE", 
			"FALSE", "NUMBER", "ID", "NEWLINE", "WS", "SINGLE_LINE_COMMENT", "MULTI_LINE_COMMENT", 
			"STRING_ESCAPE", "ENTER_EXPR", "ID_INTERP", "ESCAPED_DOLLAR", "ESCAPED_QUOTE", 
			"ESCAPED_NEWLINE", "ESCAPED_CR", "ESCAPED_SLASH", "ESCAPED_TAB", "TEXT", 
			"CLOSE_STRING", "ANY"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "ChiParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ChiParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode EOF() { return getToken(ChiParser.EOF, 0); }
		public Package_definitionContext package_definition() {
			return getRuleContext(Package_definitionContext.class,0);
		}
		public List<Import_definitionContext> import_definition() {
			return getRuleContexts(Import_definitionContext.class);
		}
		public Import_definitionContext import_definition(int i) {
			return getRuleContext(Import_definitionContext.class,i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<VariantTypeDefinitionContext> variantTypeDefinition() {
			return getRuleContexts(VariantTypeDefinitionContext.class);
		}
		public VariantTypeDefinitionContext variantTypeDefinition(int i) {
			return getRuleContext(VariantTypeDefinitionContext.class,i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			ws();
			setState(112);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PACKAGE) {
				{
				setState(111);
				package_definition();
				}
			}

			setState(114);
			ws();
			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IMPORT) {
				{
				{
				setState(115);
				import_definition();
				}
				}
				setState(120);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(121);
			ws();
			setState(130);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1080882879334920510L) != 0)) {
				{
				{
				setState(124);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case PUB:
				case VAL:
				case VAR:
				case FN:
				case IF:
				case WHILE:
				case WHEN:
				case BREAK:
				case CONTINUE:
				case EFFECT:
				case HANDLE:
				case LPAREN:
				case LBRACE:
				case DB_QUOTE:
				case PLACEHOLDER:
				case MINUS:
				case NOT:
				case TRUE:
				case FALSE:
				case NUMBER:
				case ID:
					{
					setState(122);
					expression(0);
					}
					break;
				case DATA:
					{
					setState(123);
					variantTypeDefinition();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(126);
				ws();
				}
				}
				setState(132);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(133);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Package_definitionContext extends ParserRuleContext {
		public TerminalNode PACKAGE() { return getToken(ChiParser.PACKAGE, 0); }
		public TerminalNode DIV() { return getToken(ChiParser.DIV, 0); }
		public Module_nameContext module_name() {
			return getRuleContext(Module_nameContext.class,0);
		}
		public Package_nameContext package_name() {
			return getRuleContext(Package_nameContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(ChiParser.NEWLINE, 0); }
		public Package_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_package_definition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitPackage_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Package_definitionContext package_definition() throws RecognitionException {
		Package_definitionContext _localctx = new Package_definitionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_package_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			match(PACKAGE);
			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(136);
				module_name();
				}
			}

			setState(139);
			match(DIV);
			setState(141);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(140);
				package_name();
				}
				break;
			}
			setState(144);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				{
				setState(143);
				match(NEWLINE);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Import_definitionContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(ChiParser.IMPORT, 0); }
		public Module_nameContext module_name() {
			return getRuleContext(Module_nameContext.class,0);
		}
		public TerminalNode DIV() { return getToken(ChiParser.DIV, 0); }
		public Package_nameContext package_name() {
			return getRuleContext(Package_nameContext.class,0);
		}
		public TerminalNode AS() { return getToken(ChiParser.AS, 0); }
		public Package_import_aliasContext package_import_alias() {
			return getRuleContext(Package_import_aliasContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(ChiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(ChiParser.RBRACE, 0); }
		public TerminalNode NEWLINE() { return getToken(ChiParser.NEWLINE, 0); }
		public List<Import_entryContext> import_entry() {
			return getRuleContexts(Import_entryContext.class);
		}
		public Import_entryContext import_entry(int i) {
			return getRuleContext(Import_entryContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public Import_definitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_import_definition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitImport_definition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Import_definitionContext import_definition() throws RecognitionException {
		Import_definitionContext _localctx = new Import_definitionContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_import_definition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(IMPORT);
			setState(147);
			module_name();
			setState(148);
			match(DIV);
			setState(149);
			package_name();
			setState(152);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(150);
				match(AS);
				setState(151);
				package_import_alias();
				}
			}

			setState(165);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(154);
				match(LBRACE);
				setState(159); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(155);
					import_entry();
					setState(157);
					_errHandler.sync(this);
					_la = _input.LA(1);
					if (_la==COMMA) {
						{
						setState(156);
						match(COMMA);
						}
					}

					}
					}
					setState(161); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( _la==ID );
				setState(163);
				match(RBRACE);
				}
				break;
			}
			setState(168);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(167);
				match(NEWLINE);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Package_import_aliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Package_import_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_package_import_alias; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitPackage_import_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Package_import_aliasContext package_import_alias() throws RecognitionException {
		Package_import_aliasContext _localctx = new Package_import_aliasContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_package_import_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(170);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Import_entryContext extends ParserRuleContext {
		public Import_nameContext import_name() {
			return getRuleContext(Import_nameContext.class,0);
		}
		public TerminalNode AS() { return getToken(ChiParser.AS, 0); }
		public Name_import_aliasContext name_import_alias() {
			return getRuleContext(Name_import_aliasContext.class,0);
		}
		public Import_entryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_import_entry; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitImport_entry(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Import_entryContext import_entry() throws RecognitionException {
		Import_entryContext _localctx = new Import_entryContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_import_entry);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			import_name();
			setState(175);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(173);
				match(AS);
				setState(174);
				name_import_alias();
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Import_nameContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Import_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_import_name; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitImport_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Import_nameContext import_name() throws RecognitionException {
		Import_nameContext _localctx = new Import_nameContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_import_name);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(177);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Name_import_aliasContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Name_import_aliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name_import_alias; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitName_import_alias(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Name_import_aliasContext name_import_alias() throws RecognitionException {
		Name_import_aliasContext _localctx = new Name_import_aliasContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_name_import_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Module_nameContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(ChiParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ChiParser.ID, i);
		}
		public List<TerminalNode> PERIOD() { return getTokens(ChiParser.PERIOD); }
		public TerminalNode PERIOD(int i) {
			return getToken(ChiParser.PERIOD, i);
		}
		public Module_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_module_name; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitModule_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Module_nameContext module_name() throws RecognitionException {
		Module_nameContext _localctx = new Module_nameContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_module_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(181);
			match(ID);
			setState(186);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERIOD) {
				{
				{
				setState(182);
				match(PERIOD);
				setState(183);
				match(ID);
				}
				}
				setState(188);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Package_nameContext extends ParserRuleContext {
		public List<TerminalNode> ID() { return getTokens(ChiParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ChiParser.ID, i);
		}
		public List<TerminalNode> PERIOD() { return getTokens(ChiParser.PERIOD); }
		public TerminalNode PERIOD(int i) {
			return getToken(ChiParser.PERIOD, i);
		}
		public Package_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_package_name; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitPackage_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Package_nameContext package_name() throws RecognitionException {
		Package_nameContext _localctx = new Package_nameContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_package_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(189);
			match(ID);
			setState(194);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==PERIOD) {
				{
				{
				setState(190);
				match(PERIOD);
				setState(191);
				match(ID);
				}
				}
				setState(196);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariantTypeDefinitionContext extends ParserRuleContext {
		public FullVariantTypeDefinitionContext fullVariantTypeDefinition() {
			return getRuleContext(FullVariantTypeDefinitionContext.class,0);
		}
		public SimplifiedVariantTypeDefinitionContext simplifiedVariantTypeDefinition() {
			return getRuleContext(SimplifiedVariantTypeDefinitionContext.class,0);
		}
		public VariantTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variantTypeDefinition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitVariantTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariantTypeDefinitionContext variantTypeDefinition() throws RecognitionException {
		VariantTypeDefinitionContext _localctx = new VariantTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_variantTypeDefinition);
		try {
			setState(199);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(197);
				fullVariantTypeDefinition();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(198);
				simplifiedVariantTypeDefinition();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FullVariantTypeDefinitionContext extends ParserRuleContext {
		public Token typeName;
		public TerminalNode DATA() { return getToken(ChiParser.DATA, 0); }
		public TerminalNode EQUALS() { return getToken(ChiParser.EQUALS, 0); }
		public VariantTypeConstructorsContext variantTypeConstructors() {
			return getRuleContext(VariantTypeConstructorsContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Generic_type_definitionsContext generic_type_definitions() {
			return getRuleContext(Generic_type_definitionsContext.class,0);
		}
		public List<TerminalNode> WS() { return getTokens(ChiParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(ChiParser.WS, i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(ChiParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ChiParser.NEWLINE, i);
		}
		public FullVariantTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fullVariantTypeDefinition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFullVariantTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FullVariantTypeDefinitionContext fullVariantTypeDefinition() throws RecognitionException {
		FullVariantTypeDefinitionContext _localctx = new FullVariantTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_fullVariantTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
			match(DATA);
			setState(202);
			((FullVariantTypeDefinitionContext)_localctx).typeName = match(ID);
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LSQUARE) {
				{
				setState(203);
				generic_type_definitions();
				}
			}

			setState(206);
			match(EQUALS);
			setState(219);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==WS) {
					{
					{
					setState(207);
					match(WS);
					}
					}
					setState(212);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case 2:
				{
				setState(216);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==NEWLINE) {
					{
					{
					setState(213);
					match(NEWLINE);
					}
					}
					setState(218);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			}
			setState(221);
			variantTypeConstructors();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SimplifiedVariantTypeDefinitionContext extends ParserRuleContext {
		public Token typeName;
		public TerminalNode DATA() { return getToken(ChiParser.DATA, 0); }
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public TerminalNode PUB() { return getToken(ChiParser.PUB, 0); }
		public Generic_type_definitionsContext generic_type_definitions() {
			return getRuleContext(Generic_type_definitionsContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public VariantFieldsContext variantFields() {
			return getRuleContext(VariantFieldsContext.class,0);
		}
		public SimplifiedVariantTypeDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simplifiedVariantTypeDefinition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitSimplifiedVariantTypeDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final SimplifiedVariantTypeDefinitionContext simplifiedVariantTypeDefinition() throws RecognitionException {
		SimplifiedVariantTypeDefinitionContext _localctx = new SimplifiedVariantTypeDefinitionContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_simplifiedVariantTypeDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(223);
			match(DATA);
			setState(225);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUB) {
				{
				setState(224);
				match(PUB);
				}
			}

			setState(227);
			((SimplifiedVariantTypeDefinitionContext)_localctx).typeName = match(ID);
			setState(229);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LSQUARE) {
				{
				setState(228);
				generic_type_definitions();
				}
			}

			setState(236);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				setState(231);
				match(LPAREN);
				setState(233);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4035225266123964418L) != 0)) {
					{
					setState(232);
					variantFields();
					}
				}

				setState(235);
				match(RPAREN);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariantTypeConstructorsContext extends ParserRuleContext {
		public List<VariantTypeConstructorContext> variantTypeConstructor() {
			return getRuleContexts(VariantTypeConstructorContext.class);
		}
		public VariantTypeConstructorContext variantTypeConstructor(int i) {
			return getRuleContext(VariantTypeConstructorContext.class,i);
		}
		public List<TerminalNode> BIT_OR() { return getTokens(ChiParser.BIT_OR); }
		public TerminalNode BIT_OR(int i) {
			return getToken(ChiParser.BIT_OR, i);
		}
		public List<TerminalNode> WS() { return getTokens(ChiParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(ChiParser.WS, i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(ChiParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ChiParser.NEWLINE, i);
		}
		public VariantTypeConstructorsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variantTypeConstructors; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitVariantTypeConstructors(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariantTypeConstructorsContext variantTypeConstructors() throws RecognitionException {
		VariantTypeConstructorsContext _localctx = new VariantTypeConstructorsContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_variantTypeConstructors);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			variantTypeConstructor();
			setState(257);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(251);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
					case 1:
						{
						setState(242);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==WS) {
							{
							{
							setState(239);
							match(WS);
							}
							}
							setState(244);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					case 2:
						{
						setState(248);
						_errHandler.sync(this);
						_la = _input.LA(1);
						while (_la==NEWLINE) {
							{
							{
							setState(245);
							match(NEWLINE);
							}
							}
							setState(250);
							_errHandler.sync(this);
							_la = _input.LA(1);
						}
						}
						break;
					}
					setState(253);
					match(BIT_OR);
					setState(254);
					variantTypeConstructor();
					}
					} 
				}
				setState(259);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariantTypeConstructorContext extends ParserRuleContext {
		public Token variantName;
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public TerminalNode PUB() { return getToken(ChiParser.PUB, 0); }
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public VariantFieldsContext variantFields() {
			return getRuleContext(VariantFieldsContext.class,0);
		}
		public VariantTypeConstructorContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variantTypeConstructor; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitVariantTypeConstructor(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariantTypeConstructorContext variantTypeConstructor() throws RecognitionException {
		VariantTypeConstructorContext _localctx = new VariantTypeConstructorContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_variantTypeConstructor);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(261);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUB) {
				{
				setState(260);
				match(PUB);
				}
			}

			setState(263);
			((VariantTypeConstructorContext)_localctx).variantName = match(ID);
			setState(269);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
			case 1:
				{
				setState(264);
				match(LPAREN);
				setState(266);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 4035225266123964418L) != 0)) {
					{
					setState(265);
					variantFields();
					}
				}

				setState(268);
				match(RPAREN);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariantFieldsContext extends ParserRuleContext {
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<VariantFieldContext> variantField() {
			return getRuleContexts(VariantFieldContext.class);
		}
		public VariantFieldContext variantField(int i) {
			return getRuleContext(VariantFieldContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public VariantFieldsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variantFields; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitVariantFields(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariantFieldsContext variantFields() throws RecognitionException {
		VariantFieldsContext _localctx = new VariantFieldsContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_variantFields);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(271);
			ws();
			setState(272);
			variantField();
			setState(273);
			ws();
			setState(281);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(274);
				match(COMMA);
				setState(275);
				ws();
				setState(276);
				variantField();
				setState(277);
				ws();
				}
				}
				setState(283);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariantFieldContext extends ParserRuleContext {
		public Token name;
		public TerminalNode COLON() { return getToken(ChiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public TerminalNode PUB() { return getToken(ChiParser.PUB, 0); }
		public VariantFieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variantField; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitVariantField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VariantFieldContext variantField() throws RecognitionException {
		VariantFieldContext _localctx = new VariantFieldContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_variantField);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(285);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUB) {
				{
				setState(284);
				match(PUB);
				}
			}

			setState(287);
			((VariantFieldContext)_localctx).name = match(ID);
			setState(288);
			match(COLON);
			setState(289);
			type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenExpressionContext extends ParserRuleContext {
		public TerminalNode WHEN() { return getToken(ChiParser.WHEN, 0); }
		public TerminalNode LBRACE() { return getToken(ChiParser.LBRACE, 0); }
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(ChiParser.RBRACE, 0); }
		public List<WhenConditionCaseContext> whenConditionCase() {
			return getRuleContexts(WhenConditionCaseContext.class);
		}
		public WhenConditionCaseContext whenConditionCase(int i) {
			return getRuleContext(WhenConditionCaseContext.class,i);
		}
		public WhenElseCaseContext whenElseCase() {
			return getRuleContext(WhenElseCaseContext.class,0);
		}
		public WhenExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWhenExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenExpressionContext whenExpression() throws RecognitionException {
		WhenExpressionContext _localctx = new WhenExpressionContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_whenExpression);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			match(WHEN);
			setState(292);
			match(LBRACE);
			setState(296); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(293);
					ws();
					setState(294);
					whenConditionCase();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(298); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,33,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
			setState(300);
			ws();
			setState(302);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ELSE) {
				{
				setState(301);
				whenElseCase();
				}
			}

			setState(304);
			ws();
			setState(305);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenConditionCaseContext extends ParserRuleContext {
		public ExpressionContext condition;
		public WhenCaseBodyContext body;
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(ChiParser.ARROW, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public WhenCaseBodyContext whenCaseBody() {
			return getRuleContext(WhenCaseBodyContext.class,0);
		}
		public WhenConditionCaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenConditionCase; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWhenConditionCase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenConditionCaseContext whenConditionCase() throws RecognitionException {
		WhenConditionCaseContext _localctx = new WhenConditionCaseContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_whenConditionCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(307);
			((WhenConditionCaseContext)_localctx).condition = expression(0);
			setState(308);
			ws();
			setState(309);
			match(ARROW);
			setState(310);
			ws();
			setState(311);
			((WhenConditionCaseContext)_localctx).body = whenCaseBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenElseCaseContext extends ParserRuleContext {
		public WhenCaseBodyContext body;
		public TerminalNode ELSE() { return getToken(ChiParser.ELSE, 0); }
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(ChiParser.ARROW, 0); }
		public WhenCaseBodyContext whenCaseBody() {
			return getRuleContext(WhenCaseBodyContext.class,0);
		}
		public WhenElseCaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenElseCase; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWhenElseCase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenElseCaseContext whenElseCase() throws RecognitionException {
		WhenElseCaseContext _localctx = new WhenElseCaseContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_whenElseCase);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			match(ELSE);
			setState(314);
			ws();
			setState(315);
			match(ARROW);
			setState(316);
			ws();
			setState(317);
			((WhenElseCaseContext)_localctx).body = whenCaseBody();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WhenCaseBodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public WhenCaseBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_whenCaseBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWhenCaseBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WhenCaseBodyContext whenCaseBody() throws RecognitionException {
		WhenCaseBodyContext _localctx = new WhenCaseBodyContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_whenCaseBody);
		try {
			setState(321);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(319);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(320);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LambdaContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(ChiParser.LBRACE, 0); }
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(ChiParser.RBRACE, 0); }
		public ArgumentsWithTypesContext argumentsWithTypes() {
			return getRuleContext(ArgumentsWithTypesContext.class,0);
		}
		public TerminalNode ARROW() { return getToken(ChiParser.ARROW, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public LambdaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambda; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitLambda(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LambdaContext lambda() throws RecognitionException {
		LambdaContext _localctx = new LambdaContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_lambda);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(323);
			match(LBRACE);
			setState(324);
			ws();
			setState(328);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
			case 1:
				{
				setState(325);
				argumentsWithTypes();
				setState(326);
				match(ARROW);
				}
				break;
			}
			setState(330);
			ws();
			setState(336);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1080882879334916414L) != 0)) {
				{
				{
				setState(331);
				expression(0);
				setState(332);
				ws();
				}
				}
				setState(338);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(339);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BlockContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(ChiParser.LBRACE, 0); }
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(ChiParser.RBRACE, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(341);
			match(LBRACE);
			setState(342);
			ws();
			setState(348);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1080882879334916414L) != 0)) {
				{
				{
				setState(343);
				expression(0);
				setState(344);
				ws();
				}
				}
				setState(350);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(351);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EffectDefinitionContext extends ParserRuleContext {
		public Token effectName;
		public Func_argument_definitionsContext arguments;
		public TerminalNode EFFECT() { return getToken(ChiParser.EFFECT, 0); }
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Func_argument_definitionsContext func_argument_definitions() {
			return getRuleContext(Func_argument_definitionsContext.class,0);
		}
		public TerminalNode PUB() { return getToken(ChiParser.PUB, 0); }
		public Generic_type_definitionsContext generic_type_definitions() {
			return getRuleContext(Generic_type_definitionsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ChiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public EffectDefinitionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_effectDefinition; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitEffectDefinition(this);
			else return visitor.visitChildren(this);
		}
	}

	public final EffectDefinitionContext effectDefinition() throws RecognitionException {
		EffectDefinitionContext _localctx = new EffectDefinitionContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_effectDefinition);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUB) {
				{
				setState(353);
				match(PUB);
				}
			}

			setState(356);
			match(EFFECT);
			setState(357);
			((EffectDefinitionContext)_localctx).effectName = match(ID);
			setState(359);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LSQUARE) {
				{
				setState(358);
				generic_type_definitions();
				}
			}

			setState(361);
			((EffectDefinitionContext)_localctx).arguments = func_argument_definitions();
			setState(364);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,41,_ctx) ) {
			case 1:
				{
				setState(362);
				match(COLON);
				setState(363);
				type();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HandleExpressionContext extends ParserRuleContext {
		public TerminalNode HANDLE() { return getToken(ChiParser.HANDLE, 0); }
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public TerminalNode WITH() { return getToken(ChiParser.WITH, 0); }
		public TerminalNode LBRACE() { return getToken(ChiParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(ChiParser.RBRACE, 0); }
		public List<HandleCaseContext> handleCase() {
			return getRuleContexts(HandleCaseContext.class);
		}
		public HandleCaseContext handleCase(int i) {
			return getRuleContext(HandleCaseContext.class,i);
		}
		public HandleExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_handleExpression; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitHandleExpression(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HandleExpressionContext handleExpression() throws RecognitionException {
		HandleExpressionContext _localctx = new HandleExpressionContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_handleExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			match(HANDLE);
			setState(367);
			ws();
			setState(368);
			block();
			setState(369);
			ws();
			setState(370);
			match(WITH);
			setState(371);
			ws();
			setState(372);
			match(LBRACE);
			setState(373);
			ws();
			setState(377);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==ID) {
				{
				{
				setState(374);
				handleCase();
				}
				}
				setState(379);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(380);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HandleCaseContext extends ParserRuleContext {
		public Token effectName;
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public List<HandleCaseEffectParamContext> handleCaseEffectParam() {
			return getRuleContexts(HandleCaseEffectParamContext.class);
		}
		public HandleCaseEffectParamContext handleCaseEffectParam(int i) {
			return getRuleContext(HandleCaseEffectParamContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode ARROW() { return getToken(ChiParser.ARROW, 0); }
		public HandleCaseBodyContext handleCaseBody() {
			return getRuleContext(HandleCaseBodyContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public HandleCaseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_handleCase; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitHandleCase(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HandleCaseContext handleCase() throws RecognitionException {
		HandleCaseContext _localctx = new HandleCaseContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_handleCase);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(382);
			((HandleCaseContext)_localctx).effectName = match(ID);
			setState(383);
			match(LPAREN);
			setState(384);
			handleCaseEffectParam();
			setState(389);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(385);
				match(COMMA);
				setState(386);
				handleCaseEffectParam();
				}
				}
				setState(391);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(392);
			match(RPAREN);
			setState(393);
			ws();
			setState(394);
			match(ARROW);
			setState(395);
			ws();
			setState(396);
			handleCaseBody();
			setState(397);
			ws();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HandleCaseEffectParamContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public HandleCaseEffectParamContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_handleCaseEffectParam; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitHandleCaseEffectParam(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HandleCaseEffectParamContext handleCaseEffectParam() throws RecognitionException {
		HandleCaseEffectParamContext _localctx = new HandleCaseEffectParamContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_handleCaseEffectParam);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399);
			match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HandleCaseBodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public HandleCaseBodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_handleCaseBody; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitHandleCaseBody(this);
			else return visitor.visitChildren(this);
		}
	}

	public final HandleCaseBodyContext handleCaseBody() throws RecognitionException {
		HandleCaseBodyContext _localctx = new HandleCaseBodyContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_handleCaseBody);
		try {
			setState(403);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,44,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(401);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(402);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(ChiParser.AS, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public CastContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitCast(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringExprContext extends ExpressionContext {
		public StringContext string() {
			return getRuleContext(StringContext.class,0);
		}
		public StringExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitStringExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BoolExprContext extends ExpressionContext {
		public BoolContext bool() {
			return getRuleContext(BoolContext.class,0);
		}
		public BoolExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitBoolExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IfExprContext extends ExpressionContext {
		public If_exprContext if_expr() {
			return getRuleContext(If_exprContext.class,0);
		}
		public IfExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitIfExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ContinueExprContext extends ExpressionContext {
		public TerminalNode CONTINUE() { return getToken(ChiParser.CONTINUE, 0); }
		public ContinueExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitContinueExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodInvocationContext extends ExpressionContext {
		public ExpressionContext receiver;
		public Token methodName;
		public Expr_comma_listContext arguments;
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public TerminalNode PERIOD() { return getToken(ChiParser.PERIOD, 0); }
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Expr_comma_listContext expr_comma_list() {
			return getRuleContext(Expr_comma_listContext.class,0);
		}
		public CallGenericParametersContext callGenericParameters() {
			return getRuleContext(CallGenericParametersContext.class,0);
		}
		public MethodInvocationContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitMethodInvocation(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NotOpContext extends ExpressionContext {
		public TerminalNode NOT() { return getToken(ChiParser.NOT, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NotOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitNotOp(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PlaceholderExprContext extends ExpressionContext {
		public TerminalNode PLACEHOLDER() { return getToken(ChiParser.PLACEHOLDER, 0); }
		public PlaceholderExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitPlaceholderExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class OpEqualExprContext extends ExpressionContext {
		public Token variable;
		public ExpressionContext value;
		public OpEqualContext opEqual() {
			return getRuleContext(OpEqualContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public OpEqualExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitOpEqualExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NumberExprContext extends ExpressionContext {
		public TerminalNode NUMBER() { return getToken(ChiParser.NUMBER, 0); }
		public NumberExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitNumberExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FuncWithNameContext extends ExpressionContext {
		public Func_with_nameContext func_with_name() {
			return getRuleContext(Func_with_nameContext.class,0);
		}
		public FuncWithNameContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFuncWithName(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IndexedAssignmentContext extends ExpressionContext {
		public ExpressionContext variable;
		public ExpressionContext index;
		public ExpressionContext value;
		public TerminalNode LSQUARE() { return getToken(ChiParser.LSQUARE, 0); }
		public TerminalNode RSQUARE() { return getToken(ChiParser.RSQUARE, 0); }
		public TerminalNode EQUALS() { return getToken(ChiParser.EQUALS, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public IndexedAssignmentContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitIndexedAssignment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WeaveExprContext extends ExpressionContext {
		public ExpressionContext input;
		public ExpressionContext template;
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public TerminalNode WEAVE() { return getToken(ChiParser.WEAVE, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public WeaveExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWeaveExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IsExprContext extends ExpressionContext {
		public Token variantName;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode IS() { return getToken(ChiParser.IS, 0); }
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public IsExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitIsExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldAccessExprContext extends ExpressionContext {
		public ExpressionContext receiver;
		public Token memberName;
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public TerminalNode PERIOD() { return getToken(ChiParser.PERIOD, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public FieldAccessExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFieldAccessExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class HandleExprContext extends ExpressionContext {
		public HandleExpressionContext handleExpression() {
			return getRuleContext(HandleExpressionContext.class,0);
		}
		public HandleExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitHandleExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IdExprContext extends ExpressionContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public IdExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitIdExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class LambdaExprContext extends ExpressionContext {
		public LambdaContext lambda() {
			return getRuleContext(LambdaContext.class,0);
		}
		public LambdaExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitLambdaExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FieldAssignmentContext extends ExpressionContext {
		public ExpressionContext receiver;
		public Token memberName;
		public ExpressionContext value;
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public TerminalNode PERIOD() { return getToken(ChiParser.PERIOD, 0); }
		public TerminalNode EQUALS() { return getToken(ChiParser.EQUALS, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public FieldAssignmentContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFieldAssignment(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentExprContext extends ExpressionContext {
		public AssignmentContext assignment() {
			return getRuleContext(AssignmentContext.class,0);
		}
		public AssignmentExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitAssignmentExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class IndexOperatorContext extends ExpressionContext {
		public ExpressionContext variable;
		public ExpressionContext index;
		public TerminalNode LSQUARE() { return getToken(ChiParser.LSQUARE, 0); }
		public TerminalNode RSQUARE() { return getToken(ChiParser.RSQUARE, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public IndexOperatorContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitIndexOperator(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GroupExprContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public GroupExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitGroupExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EffectDefContext extends ExpressionContext {
		public EffectDefinitionContext effectDefinition() {
			return getRuleContext(EffectDefinitionContext.class,0);
		}
		public EffectDefContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitEffectDef(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NameDeclarationExprContext extends ExpressionContext {
		public Name_declarationContext name_declaration() {
			return getRuleContext(Name_declarationContext.class,0);
		}
		public NameDeclarationExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitNameDeclarationExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WhileLoopExprContext extends ExpressionContext {
		public TerminalNode WHILE() { return getToken(ChiParser.WHILE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public WhileLoopExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWhileLoopExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WhenExprContext extends ExpressionContext {
		public WhenExpressionContext whenExpression() {
			return getRuleContext(WhenExpressionContext.class,0);
		}
		public WhenExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWhenExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BinOpContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode BIT_SHL() { return getToken(ChiParser.BIT_SHL, 0); }
		public TerminalNode BIT_SHR() { return getToken(ChiParser.BIT_SHR, 0); }
		public DivMulContext divMul() {
			return getRuleContext(DivMulContext.class,0);
		}
		public TerminalNode MOD() { return getToken(ChiParser.MOD, 0); }
		public PlusMinusContext plusMinus() {
			return getRuleContext(PlusMinusContext.class,0);
		}
		public TerminalNode COMP_OP() { return getToken(ChiParser.COMP_OP, 0); }
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public TerminalNode BIT_AND() { return getToken(ChiParser.BIT_AND, 0); }
		public TerminalNode BIT_OR() { return getToken(ChiParser.BIT_OR, 0); }
		public BinOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitBinOp(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FnCallExprContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public Expr_comma_listContext expr_comma_list() {
			return getRuleContext(Expr_comma_listContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public CallGenericParametersContext callGenericParameters() {
			return getRuleContext(CallGenericParametersContext.class,0);
		}
		public FnCallExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFnCallExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class NegationExprContext extends ExpressionContext {
		public TerminalNode MINUS() { return getToken(ChiParser.MINUS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NegationExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitNegationExpr(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BreakExprContext extends ExpressionContext {
		public TerminalNode BREAK() { return getToken(ChiParser.BREAK, 0); }
		public BreakExprContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitBreakExpr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 54;
		enterRecursionRule(_localctx, 54, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(437);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,45,_ctx) ) {
			case 1:
				{
				_localctx = new EffectDefContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(406);
				effectDefinition();
				}
				break;
			case 2:
				{
				_localctx = new HandleExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(407);
				handleExpression();
				}
				break;
			case 3:
				{
				_localctx = new WhileLoopExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(408);
				match(WHILE);
				setState(409);
				expression(0);
				setState(410);
				block();
				}
				break;
			case 4:
				{
				_localctx = new WhenExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(412);
				whenExpression();
				}
				break;
			case 5:
				{
				_localctx = new GroupExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(413);
				match(LPAREN);
				setState(414);
				expression(0);
				setState(415);
				match(RPAREN);
				}
				break;
			case 6:
				{
				_localctx = new AssignmentExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(417);
				assignment();
				}
				break;
			case 7:
				{
				_localctx = new FuncWithNameContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(418);
				func_with_name();
				}
				break;
			case 8:
				{
				_localctx = new NameDeclarationExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(419);
				name_declaration();
				}
				break;
			case 9:
				{
				_localctx = new StringExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(420);
				string();
				}
				break;
			case 10:
				{
				_localctx = new NotOpContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(421);
				match(NOT);
				setState(422);
				expression(16);
				}
				break;
			case 11:
				{
				_localctx = new LambdaExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(423);
				lambda();
				}
				break;
			case 12:
				{
				_localctx = new IfExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(424);
				if_expr();
				}
				break;
			case 13:
				{
				_localctx = new OpEqualExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(425);
				((OpEqualExprContext)_localctx).variable = match(ID);
				setState(426);
				opEqual();
				setState(427);
				((OpEqualExprContext)_localctx).value = expression(8);
				}
				break;
			case 14:
				{
				_localctx = new NegationExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(429);
				match(MINUS);
				setState(430);
				expression(7);
				}
				break;
			case 15:
				{
				_localctx = new NumberExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(431);
				match(NUMBER);
				}
				break;
			case 16:
				{
				_localctx = new BoolExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(432);
				bool();
				}
				break;
			case 17:
				{
				_localctx = new IdExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(433);
				match(ID);
				}
				break;
			case 18:
				{
				_localctx = new PlaceholderExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(434);
				match(PLACEHOLDER);
				}
				break;
			case 19:
				{
				_localctx = new BreakExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(435);
				match(BREAK);
				}
				break;
			case 20:
				{
				_localctx = new ContinueExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(436);
				match(CONTINUE);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(531);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(529);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
					case 1:
						{
						_localctx = new FieldAssignmentContext(new ExpressionContext(_parentctx, _parentState));
						((FieldAssignmentContext)_localctx).receiver = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(439);
						if (!(precpred(_ctx, 37))) throw new FailedPredicateException(this, "precpred(_ctx, 37)");
						setState(440);
						ws();
						setState(441);
						match(PERIOD);
						setState(442);
						((FieldAssignmentContext)_localctx).memberName = match(ID);
						setState(443);
						match(EQUALS);
						setState(444);
						((FieldAssignmentContext)_localctx).value = expression(38);
						}
						break;
					case 2:
						{
						_localctx = new IndexedAssignmentContext(new ExpressionContext(_parentctx, _parentState));
						((IndexedAssignmentContext)_localctx).variable = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(446);
						if (!(precpred(_ctx, 28))) throw new FailedPredicateException(this, "precpred(_ctx, 28)");
						setState(447);
						match(LSQUARE);
						setState(448);
						((IndexedAssignmentContext)_localctx).index = expression(0);
						setState(449);
						match(RSQUARE);
						setState(450);
						match(EQUALS);
						setState(451);
						((IndexedAssignmentContext)_localctx).value = expression(29);
						}
						break;
					case 3:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(453);
						if (!(precpred(_ctx, 22))) throw new FailedPredicateException(this, "precpred(_ctx, 22)");
						setState(454);
						match(BIT_SHL);
						setState(455);
						expression(23);
						}
						break;
					case 4:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(456);
						if (!(precpred(_ctx, 21))) throw new FailedPredicateException(this, "precpred(_ctx, 21)");
						setState(457);
						match(BIT_SHR);
						setState(458);
						expression(22);
						}
						break;
					case 5:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(459);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(460);
						divMul();
						setState(461);
						expression(21);
						}
						break;
					case 6:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(463);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(464);
						match(MOD);
						setState(465);
						expression(20);
						}
						break;
					case 7:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(466);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(467);
						plusMinus();
						setState(468);
						expression(19);
						}
						break;
					case 8:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(470);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(471);
						match(COMP_OP);
						setState(472);
						expression(18);
						}
						break;
					case 9:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(473);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(474);
						and();
						setState(475);
						expression(16);
						}
						break;
					case 10:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(477);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(478);
						or();
						setState(479);
						expression(15);
						}
						break;
					case 11:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(481);
						if (!(precpred(_ctx, 13))) throw new FailedPredicateException(this, "precpred(_ctx, 13)");
						setState(482);
						match(BIT_AND);
						setState(483);
						expression(14);
						}
						break;
					case 12:
						{
						_localctx = new BinOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(484);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(485);
						match(BIT_OR);
						setState(486);
						expression(13);
						}
						break;
					case 13:
						{
						_localctx = new CastContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(487);
						if (!(precpred(_ctx, 39))) throw new FailedPredicateException(this, "precpred(_ctx, 39)");
						setState(488);
						match(AS);
						setState(489);
						type();
						}
						break;
					case 14:
						{
						_localctx = new MethodInvocationContext(new ExpressionContext(_parentctx, _parentState));
						((MethodInvocationContext)_localctx).receiver = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(490);
						if (!(precpred(_ctx, 38))) throw new FailedPredicateException(this, "precpred(_ctx, 38)");
						setState(491);
						ws();
						setState(492);
						match(PERIOD);
						setState(493);
						((MethodInvocationContext)_localctx).methodName = match(ID);
						setState(495);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LSQUARE) {
							{
							setState(494);
							callGenericParameters();
							}
						}

						setState(497);
						match(LPAREN);
						setState(498);
						((MethodInvocationContext)_localctx).arguments = expr_comma_list();
						setState(499);
						match(RPAREN);
						}
						break;
					case 15:
						{
						_localctx = new FieldAccessExprContext(new ExpressionContext(_parentctx, _parentState));
						((FieldAccessExprContext)_localctx).receiver = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(501);
						if (!(precpred(_ctx, 36))) throw new FailedPredicateException(this, "precpred(_ctx, 36)");
						setState(502);
						ws();
						setState(503);
						match(PERIOD);
						setState(504);
						((FieldAccessExprContext)_localctx).memberName = match(ID);
						}
						break;
					case 16:
						{
						_localctx = new IsExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(506);
						if (!(precpred(_ctx, 33))) throw new FailedPredicateException(this, "precpred(_ctx, 33)");
						setState(507);
						match(IS);
						setState(508);
						((IsExprContext)_localctx).variantName = match(ID);
						}
						break;
					case 17:
						{
						_localctx = new FnCallExprContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(509);
						if (!(precpred(_ctx, 29))) throw new FailedPredicateException(this, "precpred(_ctx, 29)");
						setState(511);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==LSQUARE) {
							{
							setState(510);
							callGenericParameters();
							}
						}

						setState(513);
						match(LPAREN);
						setState(514);
						expr_comma_list();
						setState(515);
						match(RPAREN);
						}
						break;
					case 18:
						{
						_localctx = new IndexOperatorContext(new ExpressionContext(_parentctx, _parentState));
						((IndexOperatorContext)_localctx).variable = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(517);
						if (!(precpred(_ctx, 27))) throw new FailedPredicateException(this, "precpred(_ctx, 27)");
						setState(518);
						match(LSQUARE);
						setState(519);
						((IndexOperatorContext)_localctx).index = expression(0);
						setState(520);
						match(RSQUARE);
						}
						break;
					case 19:
						{
						_localctx = new WeaveExprContext(new ExpressionContext(_parentctx, _parentState));
						((WeaveExprContext)_localctx).input = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(522);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(523);
						ws();
						setState(524);
						match(WEAVE);
						setState(525);
						ws();
						setState(526);
						((WeaveExprContext)_localctx).template = expression(0);
						setState(527);
						ws();
						}
						break;
					}
					} 
				}
				setState(533);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,49,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DivMulContext extends ParserRuleContext {
		public TerminalNode DIV() { return getToken(ChiParser.DIV, 0); }
		public TerminalNode MUL() { return getToken(ChiParser.MUL, 0); }
		public DivMulContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_divMul; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitDivMul(this);
			else return visitor.visitChildren(this);
		}
	}

	public final DivMulContext divMul() throws RecognitionException {
		DivMulContext _localctx = new DivMulContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_divMul);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(534);
			_la = _input.LA(1);
			if ( !(_la==MUL || _la==DIV) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PlusMinusContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(ChiParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(ChiParser.MINUS, 0); }
		public PlusMinusContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_plusMinus; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitPlusMinus(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PlusMinusContext plusMinus() throws RecognitionException {
		PlusMinusContext _localctx = new PlusMinusContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_plusMinus);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(536);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OpEqualContext extends ParserRuleContext {
		public TerminalNode PLUS_EQUAL() { return getToken(ChiParser.PLUS_EQUAL, 0); }
		public TerminalNode MINUS_EQUAL() { return getToken(ChiParser.MINUS_EQUAL, 0); }
		public TerminalNode MUL_EQUAL() { return getToken(ChiParser.MUL_EQUAL, 0); }
		public TerminalNode DIV_EQUAL() { return getToken(ChiParser.DIV_EQUAL, 0); }
		public OpEqualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_opEqual; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitOpEqual(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OpEqualContext opEqual() throws RecognitionException {
		OpEqualContext _localctx = new OpEqualContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_opEqual);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(538);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 64424509440L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AndContext extends ParserRuleContext {
		public List<TerminalNode> BIT_AND() { return getTokens(ChiParser.BIT_AND); }
		public TerminalNode BIT_AND(int i) {
			return getToken(ChiParser.BIT_AND, i);
		}
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_and);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(540);
			match(BIT_AND);
			setState(541);
			match(BIT_AND);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrContext extends ParserRuleContext {
		public List<TerminalNode> BIT_OR() { return getTokens(ChiParser.BIT_OR); }
		public TerminalNode BIT_OR(int i) {
			return getToken(ChiParser.BIT_OR, i);
		}
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitOr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_or);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(543);
			match(BIT_OR);
			setState(544);
			match(BIT_OR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CallGenericParametersContext extends ParserRuleContext {
		public TerminalNode LSQUARE() { return getToken(ChiParser.LSQUARE, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public TerminalNode RSQUARE() { return getToken(ChiParser.RSQUARE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public CallGenericParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_callGenericParameters; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitCallGenericParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CallGenericParametersContext callGenericParameters() throws RecognitionException {
		CallGenericParametersContext _localctx = new CallGenericParametersContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_callGenericParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(546);
			match(LSQUARE);
			setState(547);
			type();
			setState(552);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(548);
				match(COMMA);
				setState(549);
				type();
				}
				}
				setState(554);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(555);
			match(RSQUARE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Expr_comma_listContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public Expr_comma_listContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr_comma_list; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitExpr_comma_list(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Expr_comma_listContext expr_comma_list() throws RecognitionException {
		Expr_comma_listContext _localctx = new Expr_comma_listContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_expr_comma_list);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(558);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 1080882879334916414L) != 0)) {
				{
				setState(557);
				expression(0);
				}
			}

			setState(564);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(560);
				match(COMMA);
				setState(561);
				expression(0);
				}
				}
				setState(566);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentContext extends ParserRuleContext {
		public ExpressionContext value;
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public TerminalNode EQUALS() { return getToken(ChiParser.EQUALS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssignmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assignment; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitAssignment(this);
			else return visitor.visitChildren(this);
		}
	}

	public final AssignmentContext assignment() throws RecognitionException {
		AssignmentContext _localctx = new AssignmentContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_assignment);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(567);
			match(ID);
			setState(568);
			match(EQUALS);
			setState(569);
			((AssignmentContext)_localctx).value = expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public TypeNameRefContext typeNameRef() {
			return getRuleContext(TypeNameRefContext.class,0);
		}
		public FunctionTypeRefContext functionTypeRef() {
			return getRuleContext(FunctionTypeRefContext.class,0);
		}
		public TypeConstructorRefContext typeConstructorRef() {
			return getRuleContext(TypeConstructorRefContext.class,0);
		}
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_type);
		try {
			setState(574);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,53,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(571);
				typeNameRef();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(572);
				functionTypeRef();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(573);
				typeConstructorRef();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeNameRefContext extends ParserRuleContext {
		public Token packageName;
		public Token name;
		public List<TerminalNode> ID() { return getTokens(ChiParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ChiParser.ID, i);
		}
		public TerminalNode PERIOD() { return getToken(ChiParser.PERIOD, 0); }
		public TypeNameRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeNameRef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitTypeNameRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeNameRefContext typeNameRef() throws RecognitionException {
		TypeNameRefContext _localctx = new TypeNameRefContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_typeNameRef);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(578);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
			case 1:
				{
				setState(576);
				((TypeNameRefContext)_localctx).packageName = match(ID);
				setState(577);
				match(PERIOD);
				}
				break;
			}
			setState(580);
			((TypeNameRefContext)_localctx).name = match(ID);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionTypeRefContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public TerminalNode ARROW() { return getToken(ChiParser.ARROW, 0); }
		public Func_return_typeContext func_return_type() {
			return getRuleContext(Func_return_typeContext.class,0);
		}
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public FunctionTypeRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionTypeRef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFunctionTypeRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionTypeRefContext functionTypeRef() throws RecognitionException {
		FunctionTypeRefContext _localctx = new FunctionTypeRefContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_functionTypeRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(582);
			match(LPAREN);
			setState(584);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN || _la==ID) {
				{
				setState(583);
				type();
				}
			}

			setState(590);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(586);
				match(COMMA);
				setState(587);
				type();
				}
				}
				setState(592);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(593);
			match(RPAREN);
			setState(594);
			match(ARROW);
			setState(595);
			func_return_type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeConstructorRefContext extends ParserRuleContext {
		public TypeNameRefContext typeNameRef() {
			return getRuleContext(TypeNameRefContext.class,0);
		}
		public TerminalNode LSQUARE() { return getToken(ChiParser.LSQUARE, 0); }
		public List<TypeContext> type() {
			return getRuleContexts(TypeContext.class);
		}
		public TypeContext type(int i) {
			return getRuleContext(TypeContext.class,i);
		}
		public TerminalNode RSQUARE() { return getToken(ChiParser.RSQUARE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public TypeConstructorRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_typeConstructorRef; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitTypeConstructorRef(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeConstructorRefContext typeConstructorRef() throws RecognitionException {
		TypeConstructorRefContext _localctx = new TypeConstructorRefContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_typeConstructorRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			typeNameRef();
			setState(598);
			match(LSQUARE);
			setState(599);
			type();
			setState(604);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(600);
				match(COMMA);
				setState(601);
				type();
				}
				}
				setState(606);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(607);
			match(RSQUARE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Name_declarationContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public TerminalNode EQUALS() { return getToken(ChiParser.EQUALS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode VAL() { return getToken(ChiParser.VAL, 0); }
		public TerminalNode VAR() { return getToken(ChiParser.VAR, 0); }
		public TerminalNode PUB() { return getToken(ChiParser.PUB, 0); }
		public TerminalNode COLON() { return getToken(ChiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public Name_declarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_name_declaration; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitName_declaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Name_declarationContext name_declaration() throws RecognitionException {
		Name_declarationContext _localctx = new Name_declarationContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_name_declaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(610);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUB) {
				{
				setState(609);
				match(PUB);
				}
			}

			setState(612);
			_la = _input.LA(1);
			if ( !(_la==VAL || _la==VAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			setState(613);
			match(ID);
			setState(616);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(614);
				match(COLON);
				setState(615);
				type();
				}
			}

			setState(618);
			match(EQUALS);
			setState(619);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Func_with_nameContext extends ParserRuleContext {
		public Token funcName;
		public Func_argument_definitionsContext arguments;
		public TerminalNode FN() { return getToken(ChiParser.FN, 0); }
		public Func_bodyContext func_body() {
			return getRuleContext(Func_bodyContext.class,0);
		}
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public Func_argument_definitionsContext func_argument_definitions() {
			return getRuleContext(Func_argument_definitionsContext.class,0);
		}
		public TerminalNode PUB() { return getToken(ChiParser.PUB, 0); }
		public Generic_type_definitionsContext generic_type_definitions() {
			return getRuleContext(Generic_type_definitionsContext.class,0);
		}
		public TerminalNode COLON() { return getToken(ChiParser.COLON, 0); }
		public Func_return_typeContext func_return_type() {
			return getRuleContext(Func_return_typeContext.class,0);
		}
		public Func_with_nameContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_with_name; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFunc_with_name(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Func_with_nameContext func_with_name() throws RecognitionException {
		Func_with_nameContext _localctx = new Func_with_nameContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_func_with_name);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(622);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PUB) {
				{
				setState(621);
				match(PUB);
				}
			}

			setState(624);
			match(FN);
			setState(625);
			((Func_with_nameContext)_localctx).funcName = match(ID);
			setState(627);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LSQUARE) {
				{
				setState(626);
				generic_type_definitions();
				}
			}

			setState(629);
			((Func_with_nameContext)_localctx).arguments = func_argument_definitions();
			setState(632);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COLON) {
				{
				setState(630);
				match(COLON);
				setState(631);
				func_return_type();
				}
			}

			setState(634);
			func_body();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Generic_type_definitionsContext extends ParserRuleContext {
		public TerminalNode LSQUARE() { return getToken(ChiParser.LSQUARE, 0); }
		public List<TerminalNode> ID() { return getTokens(ChiParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(ChiParser.ID, i);
		}
		public TerminalNode RSQUARE() { return getToken(ChiParser.RSQUARE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public Generic_type_definitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_generic_type_definitions; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitGeneric_type_definitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Generic_type_definitionsContext generic_type_definitions() throws RecognitionException {
		Generic_type_definitionsContext _localctx = new Generic_type_definitionsContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_generic_type_definitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(636);
			match(LSQUARE);
			setState(637);
			match(ID);
			setState(642);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(638);
				match(COMMA);
				setState(639);
				match(ID);
				}
				}
				setState(644);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(645);
			match(RSQUARE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Func_argument_definitionsContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public WsContext ws() {
			return getRuleContext(WsContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public ArgumentsWithTypesContext argumentsWithTypes() {
			return getRuleContext(ArgumentsWithTypesContext.class,0);
		}
		public Func_argument_definitionsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_argument_definitions; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFunc_argument_definitions(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Func_argument_definitionsContext func_argument_definitions() throws RecognitionException {
		Func_argument_definitionsContext _localctx = new Func_argument_definitionsContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_func_argument_definitions);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(647);
			match(LPAREN);
			setState(648);
			ws();
			setState(650);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(649);
				argumentsWithTypes();
				}
			}

			setState(652);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentsWithTypesContext extends ParserRuleContext {
		public List<ArgumentWithTypeContext> argumentWithType() {
			return getRuleContexts(ArgumentWithTypeContext.class);
		}
		public ArgumentWithTypeContext argumentWithType(int i) {
			return getRuleContext(ArgumentWithTypeContext.class,i);
		}
		public List<WsContext> ws() {
			return getRuleContexts(WsContext.class);
		}
		public WsContext ws(int i) {
			return getRuleContext(WsContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(ChiParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(ChiParser.COMMA, i);
		}
		public ArgumentsWithTypesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentsWithTypes; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitArgumentsWithTypes(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentsWithTypesContext argumentsWithTypes() throws RecognitionException {
		ArgumentsWithTypesContext _localctx = new ArgumentsWithTypesContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_argumentsWithTypes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(654);
			argumentWithType();
			setState(655);
			ws();
			setState(662);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(656);
				match(COMMA);
				setState(657);
				argumentWithType();
				setState(658);
				ws();
				}
				}
				setState(664);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArgumentWithTypeContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(ChiParser.ID, 0); }
		public TerminalNode COLON() { return getToken(ChiParser.COLON, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public ArgumentWithTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_argumentWithType; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitArgumentWithType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArgumentWithTypeContext argumentWithType() throws RecognitionException {
		ArgumentWithTypeContext _localctx = new ArgumentWithTypeContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_argumentWithType);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(665);
			match(ID);
			setState(666);
			match(COLON);
			setState(667);
			type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Func_bodyContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public Func_bodyContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_body; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFunc_body(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Func_bodyContext func_body() throws RecognitionException {
		Func_bodyContext _localctx = new Func_bodyContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_func_body);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(669);
			block();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Func_return_typeContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public Func_return_typeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_func_return_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitFunc_return_type(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Func_return_typeContext func_return_type() throws RecognitionException {
		Func_return_typeContext _localctx = new Func_return_typeContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_func_return_type);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(671);
			type();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringContext extends ParserRuleContext {
		public TerminalNode DB_QUOTE() { return getToken(ChiParser.DB_QUOTE, 0); }
		public TerminalNode CLOSE_STRING() { return getToken(ChiParser.CLOSE_STRING, 0); }
		public List<StringPartContext> stringPart() {
			return getRuleContexts(StringPartContext.class);
		}
		public StringPartContext stringPart(int i) {
			return getRuleContext(StringPartContext.class,i);
		}
		public StringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_string; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitString(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringContext string() throws RecognitionException {
		StringContext _localctx = new StringContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_string);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(673);
			match(DB_QUOTE);
			setState(677);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((((_la - 65)) & ~0x3f) == 0 && ((1L << (_la - 65)) & 511L) != 0)) {
				{
				{
				setState(674);
				stringPart();
				}
				}
				setState(679);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(680);
			match(CLOSE_STRING);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StringPartContext extends ParserRuleContext {
		public TerminalNode TEXT() { return getToken(ChiParser.TEXT, 0); }
		public TerminalNode ESCAPED_QUOTE() { return getToken(ChiParser.ESCAPED_QUOTE, 0); }
		public TerminalNode ESCAPED_DOLLAR() { return getToken(ChiParser.ESCAPED_DOLLAR, 0); }
		public TerminalNode ESCAPED_NEWLINE() { return getToken(ChiParser.ESCAPED_NEWLINE, 0); }
		public TerminalNode ESCAPED_CR() { return getToken(ChiParser.ESCAPED_CR, 0); }
		public TerminalNode ESCAPED_SLASH() { return getToken(ChiParser.ESCAPED_SLASH, 0); }
		public TerminalNode ESCAPED_TAB() { return getToken(ChiParser.ESCAPED_TAB, 0); }
		public TerminalNode ID_INTERP() { return getToken(ChiParser.ID_INTERP, 0); }
		public TerminalNode ENTER_EXPR() { return getToken(ChiParser.ENTER_EXPR, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(ChiParser.RBRACE, 0); }
		public StringPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stringPart; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitStringPart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StringPartContext stringPart() throws RecognitionException {
		StringPartContext _localctx = new StringPartContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_stringPart);
		try {
			setState(694);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TEXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(682);
				match(TEXT);
				}
				break;
			case ESCAPED_QUOTE:
				enterOuterAlt(_localctx, 2);
				{
				setState(683);
				match(ESCAPED_QUOTE);
				}
				break;
			case ESCAPED_DOLLAR:
				enterOuterAlt(_localctx, 3);
				{
				setState(684);
				match(ESCAPED_DOLLAR);
				}
				break;
			case ESCAPED_NEWLINE:
				enterOuterAlt(_localctx, 4);
				{
				setState(685);
				match(ESCAPED_NEWLINE);
				}
				break;
			case ESCAPED_CR:
				enterOuterAlt(_localctx, 5);
				{
				setState(686);
				match(ESCAPED_CR);
				}
				break;
			case ESCAPED_SLASH:
				enterOuterAlt(_localctx, 6);
				{
				setState(687);
				match(ESCAPED_SLASH);
				}
				break;
			case ESCAPED_TAB:
				enterOuterAlt(_localctx, 7);
				{
				setState(688);
				match(ESCAPED_TAB);
				}
				break;
			case ID_INTERP:
				enterOuterAlt(_localctx, 8);
				{
				setState(689);
				match(ID_INTERP);
				}
				break;
			case ENTER_EXPR:
				enterOuterAlt(_localctx, 9);
				{
				setState(690);
				match(ENTER_EXPR);
				setState(691);
				expression(0);
				setState(692);
				match(RBRACE);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class If_exprContext extends ParserRuleContext {
		public ExpressionContext condition;
		public TerminalNode IF() { return getToken(ChiParser.IF, 0); }
		public TerminalNode LPAREN() { return getToken(ChiParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(ChiParser.RPAREN, 0); }
		public Then_exprContext then_expr() {
			return getRuleContext(Then_exprContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ELSE() { return getToken(ChiParser.ELSE, 0); }
		public Else_exprContext else_expr() {
			return getRuleContext(Else_exprContext.class,0);
		}
		public TerminalNode NEWLINE() { return getToken(ChiParser.NEWLINE, 0); }
		public If_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_if_expr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitIf_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final If_exprContext if_expr() throws RecognitionException {
		If_exprContext _localctx = new If_exprContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_if_expr);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(696);
			match(IF);
			setState(697);
			match(LPAREN);
			setState(698);
			((If_exprContext)_localctx).condition = expression(0);
			setState(699);
			match(RPAREN);
			setState(700);
			then_expr();
			setState(706);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,69,_ctx) ) {
			case 1:
				{
				setState(702);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==NEWLINE) {
					{
					setState(701);
					match(NEWLINE);
					}
				}

				setState(704);
				match(ELSE);
				setState(705);
				else_expr();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Then_exprContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Then_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_then_expr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitThen_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Then_exprContext then_expr() throws RecognitionException {
		Then_exprContext _localctx = new Then_exprContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_then_expr);
		try {
			setState(710);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,70,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(708);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(709);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class Else_exprContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public Else_exprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_else_expr; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitElse_expr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final Else_exprContext else_expr() throws RecognitionException {
		Else_exprContext _localctx = new Else_exprContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_else_expr);
		try {
			setState(714);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,71,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(712);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(713);
				expression(0);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BoolContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(ChiParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(ChiParser.FALSE, 0); }
		public BoolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitBool(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoolContext bool() throws RecognitionException {
		BoolContext _localctx = new BoolContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_bool);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(716);
			_la = _input.LA(1);
			if ( !(_la==TRUE || _la==FALSE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class WsContext extends ParserRuleContext {
		public List<TerminalNode> WS() { return getTokens(ChiParser.WS); }
		public TerminalNode WS(int i) {
			return getToken(ChiParser.WS, i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(ChiParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(ChiParser.NEWLINE, i);
		}
		public WsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ws; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof ChiParserVisitor ) return ((ChiParserVisitor<? extends T>)visitor).visitWs(this);
			else return visitor.visitChildren(this);
		}
	}

	public final WsContext ws() throws RecognitionException {
		WsContext _localctx = new WsContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_ws);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(721);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(718);
					_la = _input.LA(1);
					if ( !(_la==NEWLINE || _la==WS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
					} 
				}
				setState(723);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,72,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 27:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 37);
		case 1:
			return precpred(_ctx, 28);
		case 2:
			return precpred(_ctx, 22);
		case 3:
			return precpred(_ctx, 21);
		case 4:
			return precpred(_ctx, 20);
		case 5:
			return precpred(_ctx, 19);
		case 6:
			return precpred(_ctx, 18);
		case 7:
			return precpred(_ctx, 17);
		case 8:
			return precpred(_ctx, 15);
		case 9:
			return precpred(_ctx, 14);
		case 10:
			return precpred(_ctx, 13);
		case 11:
			return precpred(_ctx, 12);
		case 12:
			return precpred(_ctx, 39);
		case 13:
			return precpred(_ctx, 38);
		case 14:
			return precpred(_ctx, 36);
		case 15:
			return precpred(_ctx, 33);
		case 16:
			return precpred(_ctx, 29);
		case 17:
			return precpred(_ctx, 27);
		case 18:
			return precpred(_ctx, 9);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001K\u02d5\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u00076\u0001"+
		"\u0000\u0001\u0000\u0003\u0000q\b\u0000\u0001\u0000\u0001\u0000\u0005"+
		"\u0000u\b\u0000\n\u0000\f\u0000x\t\u0000\u0001\u0000\u0001\u0000\u0001"+
		"\u0000\u0003\u0000}\b\u0000\u0001\u0000\u0001\u0000\u0005\u0000\u0081"+
		"\b\u0000\n\u0000\f\u0000\u0084\t\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0003\u0001\u008a\b\u0001\u0001\u0001\u0001\u0001\u0003\u0001"+
		"\u008e\b\u0001\u0001\u0001\u0003\u0001\u0091\b\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u0099"+
		"\b\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0003\u0002\u009e\b\u0002"+
		"\u0004\u0002\u00a0\b\u0002\u000b\u0002\f\u0002\u00a1\u0001\u0002\u0001"+
		"\u0002\u0003\u0002\u00a6\b\u0002\u0001\u0002\u0003\u0002\u00a9\b\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"\u00b0\b\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0005\u0007\u00b9\b\u0007\n\u0007\f\u0007\u00bc"+
		"\t\u0007\u0001\b\u0001\b\u0001\b\u0005\b\u00c1\b\b\n\b\f\b\u00c4\t\b\u0001"+
		"\t\u0001\t\u0003\t\u00c8\b\t\u0001\n\u0001\n\u0001\n\u0003\n\u00cd\b\n"+
		"\u0001\n\u0001\n\u0005\n\u00d1\b\n\n\n\f\n\u00d4\t\n\u0001\n\u0005\n\u00d7"+
		"\b\n\n\n\f\n\u00da\t\n\u0003\n\u00dc\b\n\u0001\n\u0001\n\u0001\u000b\u0001"+
		"\u000b\u0003\u000b\u00e2\b\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00e6"+
		"\b\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00ea\b\u000b\u0001\u000b"+
		"\u0003\u000b\u00ed\b\u000b\u0001\f\u0001\f\u0005\f\u00f1\b\f\n\f\f\f\u00f4"+
		"\t\f\u0001\f\u0005\f\u00f7\b\f\n\f\f\f\u00fa\t\f\u0003\f\u00fc\b\f\u0001"+
		"\f\u0001\f\u0005\f\u0100\b\f\n\f\f\f\u0103\t\f\u0001\r\u0003\r\u0106\b"+
		"\r\u0001\r\u0001\r\u0001\r\u0003\r\u010b\b\r\u0001\r\u0003\r\u010e\b\r"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0005\u000e\u0118\b\u000e\n\u000e\f\u000e\u011b"+
		"\t\u000e\u0001\u000f\u0003\u000f\u011e\b\u000f\u0001\u000f\u0001\u000f"+
		"\u0001\u000f\u0001\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010"+
		"\u0001\u0010\u0004\u0010\u0129\b\u0010\u000b\u0010\f\u0010\u012a\u0001"+
		"\u0010\u0001\u0010\u0003\u0010\u012f\b\u0010\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001\u0012\u0001"+
		"\u0012\u0001\u0013\u0001\u0013\u0003\u0013\u0142\b\u0013\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0149\b\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u014f\b\u0014\n"+
		"\u0014\f\u0014\u0152\t\u0014\u0001\u0014\u0001\u0014\u0001\u0015\u0001"+
		"\u0015\u0001\u0015\u0001\u0015\u0001\u0015\u0005\u0015\u015b\b\u0015\n"+
		"\u0015\f\u0015\u015e\t\u0015\u0001\u0015\u0001\u0015\u0001\u0016\u0003"+
		"\u0016\u0163\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0168"+
		"\b\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u016d\b\u0016"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0017\u0001\u0017\u0001\u0017\u0005\u0017\u0178\b\u0017\n\u0017"+
		"\f\u0017\u017b\t\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0184\b\u0018\n\u0018"+
		"\f\u0018\u0187\t\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u001a"+
		"\u0001\u001a\u0003\u001a\u0194\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b"+
		"\u01b6\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u01f0\b\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0003\u001b\u0200\b\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0001\u001b\u0005\u001b\u0212\b\u001b\n\u001b\f\u001b\u0215\t\u001b\u0001"+
		"\u001c\u0001\u001c\u0001\u001d\u0001\u001d\u0001\u001e\u0001\u001e\u0001"+
		"\u001f\u0001\u001f\u0001\u001f\u0001 \u0001 \u0001 \u0001!\u0001!\u0001"+
		"!\u0001!\u0005!\u0227\b!\n!\f!\u022a\t!\u0001!\u0001!\u0001\"\u0003\""+
		"\u022f\b\"\u0001\"\u0001\"\u0005\"\u0233\b\"\n\"\f\"\u0236\t\"\u0001#"+
		"\u0001#\u0001#\u0001#\u0001$\u0001$\u0001$\u0003$\u023f\b$\u0001%\u0001"+
		"%\u0003%\u0243\b%\u0001%\u0001%\u0001&\u0001&\u0003&\u0249\b&\u0001&\u0001"+
		"&\u0005&\u024d\b&\n&\f&\u0250\t&\u0001&\u0001&\u0001&\u0001&\u0001\'\u0001"+
		"\'\u0001\'\u0001\'\u0001\'\u0005\'\u025b\b\'\n\'\f\'\u025e\t\'\u0001\'"+
		"\u0001\'\u0001(\u0003(\u0263\b(\u0001(\u0001(\u0001(\u0001(\u0003(\u0269"+
		"\b(\u0001(\u0001(\u0001(\u0001)\u0003)\u026f\b)\u0001)\u0001)\u0001)\u0003"+
		")\u0274\b)\u0001)\u0001)\u0001)\u0003)\u0279\b)\u0001)\u0001)\u0001*\u0001"+
		"*\u0001*\u0001*\u0005*\u0281\b*\n*\f*\u0284\t*\u0001*\u0001*\u0001+\u0001"+
		"+\u0001+\u0003+\u028b\b+\u0001+\u0001+\u0001,\u0001,\u0001,\u0001,\u0001"+
		",\u0001,\u0005,\u0295\b,\n,\f,\u0298\t,\u0001-\u0001-\u0001-\u0001-\u0001"+
		".\u0001.\u0001/\u0001/\u00010\u00010\u00050\u02a4\b0\n0\f0\u02a7\t0\u0001"+
		"0\u00010\u00011\u00011\u00011\u00011\u00011\u00011\u00011\u00011\u0001"+
		"1\u00011\u00011\u00011\u00031\u02b7\b1\u00012\u00012\u00012\u00012\u0001"+
		"2\u00012\u00032\u02bf\b2\u00012\u00012\u00032\u02c3\b2\u00013\u00013\u0003"+
		"3\u02c7\b3\u00014\u00014\u00034\u02cb\b4\u00015\u00015\u00016\u00056\u02d0"+
		"\b6\n6\f6\u02d3\t6\u00016\u0000\u000167\u0000\u0002\u0004\u0006\b\n\f"+
		"\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02468:"+
		"<>@BDFHJLNPRTVXZ\\^`bdfhjl\u0000\u0006\u0001\u0000*+\u0001\u0000\'(\u0001"+
		"\u0000 #\u0001\u0000\u0002\u0003\u0001\u000089\u0001\u0000<=\u0311\u0000"+
		"n\u0001\u0000\u0000\u0000\u0002\u0087\u0001\u0000\u0000\u0000\u0004\u0092"+
		"\u0001\u0000\u0000\u0000\u0006\u00aa\u0001\u0000\u0000\u0000\b\u00ac\u0001"+
		"\u0000\u0000\u0000\n\u00b1\u0001\u0000\u0000\u0000\f\u00b3\u0001\u0000"+
		"\u0000\u0000\u000e\u00b5\u0001\u0000\u0000\u0000\u0010\u00bd\u0001\u0000"+
		"\u0000\u0000\u0012\u00c7\u0001\u0000\u0000\u0000\u0014\u00c9\u0001\u0000"+
		"\u0000\u0000\u0016\u00df\u0001\u0000\u0000\u0000\u0018\u00ee\u0001\u0000"+
		"\u0000\u0000\u001a\u0105\u0001\u0000\u0000\u0000\u001c\u010f\u0001\u0000"+
		"\u0000\u0000\u001e\u011d\u0001\u0000\u0000\u0000 \u0123\u0001\u0000\u0000"+
		"\u0000\"\u0133\u0001\u0000\u0000\u0000$\u0139\u0001\u0000\u0000\u0000"+
		"&\u0141\u0001\u0000\u0000\u0000(\u0143\u0001\u0000\u0000\u0000*\u0155"+
		"\u0001\u0000\u0000\u0000,\u0162\u0001\u0000\u0000\u0000.\u016e\u0001\u0000"+
		"\u0000\u00000\u017e\u0001\u0000\u0000\u00002\u018f\u0001\u0000\u0000\u0000"+
		"4\u0193\u0001\u0000\u0000\u00006\u01b5\u0001\u0000\u0000\u00008\u0216"+
		"\u0001\u0000\u0000\u0000:\u0218\u0001\u0000\u0000\u0000<\u021a\u0001\u0000"+
		"\u0000\u0000>\u021c\u0001\u0000\u0000\u0000@\u021f\u0001\u0000\u0000\u0000"+
		"B\u0222\u0001\u0000\u0000\u0000D\u022e\u0001\u0000\u0000\u0000F\u0237"+
		"\u0001\u0000\u0000\u0000H\u023e\u0001\u0000\u0000\u0000J\u0242\u0001\u0000"+
		"\u0000\u0000L\u0246\u0001\u0000\u0000\u0000N\u0255\u0001\u0000\u0000\u0000"+
		"P\u0262\u0001\u0000\u0000\u0000R\u026e\u0001\u0000\u0000\u0000T\u027c"+
		"\u0001\u0000\u0000\u0000V\u0287\u0001\u0000\u0000\u0000X\u028e\u0001\u0000"+
		"\u0000\u0000Z\u0299\u0001\u0000\u0000\u0000\\\u029d\u0001\u0000\u0000"+
		"\u0000^\u029f\u0001\u0000\u0000\u0000`\u02a1\u0001\u0000\u0000\u0000b"+
		"\u02b6\u0001\u0000\u0000\u0000d\u02b8\u0001\u0000\u0000\u0000f\u02c6\u0001"+
		"\u0000\u0000\u0000h\u02ca\u0001\u0000\u0000\u0000j\u02cc\u0001\u0000\u0000"+
		"\u0000l\u02d1\u0001\u0000\u0000\u0000np\u0003l6\u0000oq\u0003\u0002\u0001"+
		"\u0000po\u0001\u0000\u0000\u0000pq\u0001\u0000\u0000\u0000qr\u0001\u0000"+
		"\u0000\u0000rv\u0003l6\u0000su\u0003\u0004\u0002\u0000ts\u0001\u0000\u0000"+
		"\u0000ux\u0001\u0000\u0000\u0000vt\u0001\u0000\u0000\u0000vw\u0001\u0000"+
		"\u0000\u0000wy\u0001\u0000\u0000\u0000xv\u0001\u0000\u0000\u0000y\u0082"+
		"\u0003l6\u0000z}\u00036\u001b\u0000{}\u0003\u0012\t\u0000|z\u0001\u0000"+
		"\u0000\u0000|{\u0001\u0000\u0000\u0000}~\u0001\u0000\u0000\u0000~\u007f"+
		"\u0003l6\u0000\u007f\u0081\u0001\u0000\u0000\u0000\u0080|\u0001\u0000"+
		"\u0000\u0000\u0081\u0084\u0001\u0000\u0000\u0000\u0082\u0080\u0001\u0000"+
		"\u0000\u0000\u0082\u0083\u0001\u0000\u0000\u0000\u0083\u0085\u0001\u0000"+
		"\u0000\u0000\u0084\u0082\u0001\u0000\u0000\u0000\u0085\u0086\u0005\u0000"+
		"\u0000\u0001\u0086\u0001\u0001\u0000\u0000\u0000\u0087\u0089\u0005\n\u0000"+
		"\u0000\u0088\u008a\u0003\u000e\u0007\u0000\u0089\u0088\u0001\u0000\u0000"+
		"\u0000\u0089\u008a\u0001\u0000\u0000\u0000\u008a\u008b\u0001\u0000\u0000"+
		"\u0000\u008b\u008d\u0005+\u0000\u0000\u008c\u008e\u0003\u0010\b\u0000"+
		"\u008d\u008c\u0001\u0000\u0000\u0000\u008d\u008e\u0001\u0000\u0000\u0000"+
		"\u008e\u0090\u0001\u0000\u0000\u0000\u008f\u0091\u0005<\u0000\u0000\u0090"+
		"\u008f\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091"+
		"\u0003\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u000b\u0000\u0000\u0093"+
		"\u0094\u0003\u000e\u0007\u0000\u0094\u0095\u0005+\u0000\u0000\u0095\u0098"+
		"\u0003\u0010\b\u0000\u0096\u0097\u0005\u0007\u0000\u0000\u0097\u0099\u0003"+
		"\u0006\u0003\u0000\u0098\u0096\u0001\u0000\u0000\u0000\u0098\u0099\u0001"+
		"\u0000\u0000\u0000\u0099\u00a5\u0001\u0000\u0000\u0000\u009a\u009f\u0005"+
		"\u0019\u0000\u0000\u009b\u009d\u0003\b\u0004\u0000\u009c\u009e\u0005\u001d"+
		"\u0000\u0000\u009d\u009c\u0001\u0000\u0000\u0000\u009d\u009e\u0001\u0000"+
		"\u0000\u0000\u009e\u00a0\u0001\u0000\u0000\u0000\u009f\u009b\u0001\u0000"+
		"\u0000\u0000\u00a0\u00a1\u0001\u0000\u0000\u0000\u00a1\u009f\u0001\u0000"+
		"\u0000\u0000\u00a1\u00a2\u0001\u0000\u0000\u0000\u00a2\u00a3\u0001\u0000"+
		"\u0000\u0000\u00a3\u00a4\u0005\u001a\u0000\u0000\u00a4\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a5\u009a\u0001\u0000\u0000\u0000\u00a5\u00a6\u0001\u0000"+
		"\u0000\u0000\u00a6\u00a8\u0001\u0000\u0000\u0000\u00a7\u00a9\u0005<\u0000"+
		"\u0000\u00a8\u00a7\u0001\u0000\u0000\u0000\u00a8\u00a9\u0001\u0000\u0000"+
		"\u0000\u00a9\u0005\u0001\u0000\u0000\u0000\u00aa\u00ab\u0005;\u0000\u0000"+
		"\u00ab\u0007\u0001\u0000\u0000\u0000\u00ac\u00af\u0003\n\u0005\u0000\u00ad"+
		"\u00ae\u0005\u0007\u0000\u0000\u00ae\u00b0\u0003\f\u0006\u0000\u00af\u00ad"+
		"\u0001\u0000\u0000\u0000\u00af\u00b0\u0001\u0000\u0000\u0000\u00b0\t\u0001"+
		"\u0000\u0000\u0000\u00b1\u00b2\u0005;\u0000\u0000\u00b2\u000b\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b4\u0005;\u0000\u0000\u00b4\r\u0001\u0000\u0000"+
		"\u0000\u00b5\u00ba\u0005;\u0000\u0000\u00b6\u00b7\u0005\u001e\u0000\u0000"+
		"\u00b7\u00b9\u0005;\u0000\u0000\u00b8\u00b6\u0001\u0000\u0000\u0000\u00b9"+
		"\u00bc\u0001\u0000\u0000\u0000\u00ba\u00b8\u0001\u0000\u0000\u0000\u00ba"+
		"\u00bb\u0001\u0000\u0000\u0000\u00bb\u000f\u0001\u0000\u0000\u0000\u00bc"+
		"\u00ba\u0001\u0000\u0000\u0000\u00bd\u00c2\u0005;\u0000\u0000\u00be\u00bf"+
		"\u0005\u001e\u0000\u0000\u00bf\u00c1\u0005;\u0000\u0000\u00c0\u00be\u0001"+
		"\u0000\u0000\u0000\u00c1\u00c4\u0001\u0000\u0000\u0000\u00c2\u00c0\u0001"+
		"\u0000\u0000\u0000\u00c2\u00c3\u0001\u0000\u0000\u0000\u00c3\u0011\u0001"+
		"\u0000\u0000\u0000\u00c4\u00c2\u0001\u0000\u0000\u0000\u00c5\u00c8\u0003"+
		"\u0014\n\u0000\u00c6\u00c8\u0003\u0016\u000b\u0000\u00c7\u00c5\u0001\u0000"+
		"\u0000\u0000\u00c7\u00c6\u0001\u0000\u0000\u0000\u00c8\u0013\u0001\u0000"+
		"\u0000\u0000\u00c9\u00ca\u0005\f\u0000\u0000\u00ca\u00cc\u0005;\u0000"+
		"\u0000\u00cb\u00cd\u0003T*\u0000\u00cc\u00cb\u0001\u0000\u0000\u0000\u00cc"+
		"\u00cd\u0001\u0000\u0000\u0000\u00cd\u00ce\u0001\u0000\u0000\u0000\u00ce"+
		"\u00db\u0005$\u0000\u0000\u00cf\u00d1\u0005=\u0000\u0000\u00d0\u00cf\u0001"+
		"\u0000\u0000\u0000\u00d1\u00d4\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001"+
		"\u0000\u0000\u0000\u00d2\u00d3\u0001\u0000\u0000\u0000\u00d3\u00dc\u0001"+
		"\u0000\u0000\u0000\u00d4\u00d2\u0001\u0000\u0000\u0000\u00d5\u00d7\u0005"+
		"<\u0000\u0000\u00d6\u00d5\u0001\u0000\u0000\u0000\u00d7\u00da\u0001\u0000"+
		"\u0000\u0000\u00d8\u00d6\u0001\u0000\u0000\u0000\u00d8\u00d9\u0001\u0000"+
		"\u0000\u0000\u00d9\u00dc\u0001\u0000\u0000\u0000\u00da\u00d8\u0001\u0000"+
		"\u0000\u0000\u00db\u00d2\u0001\u0000\u0000\u0000\u00db\u00d8\u0001\u0000"+
		"\u0000\u0000\u00dc\u00dd\u0001\u0000\u0000\u0000\u00dd\u00de\u0003\u0018"+
		"\f\u0000\u00de\u0015\u0001\u0000\u0000\u0000\u00df\u00e1\u0005\f\u0000"+
		"\u0000\u00e0\u00e2\u0005\u0001\u0000\u0000\u00e1\u00e0\u0001\u0000\u0000"+
		"\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000"+
		"\u0000\u00e3\u00e5\u0005;\u0000\u0000\u00e4\u00e6\u0003T*\u0000\u00e5"+
		"\u00e4\u0001\u0000\u0000\u0000\u00e5\u00e6\u0001\u0000\u0000\u0000\u00e6"+
		"\u00ec\u0001\u0000\u0000\u0000\u00e7\u00e9\u0005\u0017\u0000\u0000\u00e8"+
		"\u00ea\u0003\u001c\u000e\u0000\u00e9\u00e8\u0001\u0000\u0000\u0000\u00e9"+
		"\u00ea\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000\u00eb"+
		"\u00ed\u0005\u0018\u0000\u0000\u00ec\u00e7\u0001\u0000\u0000\u0000\u00ec"+
		"\u00ed\u0001\u0000\u0000\u0000\u00ed\u0017\u0001\u0000\u0000\u0000\u00ee"+
		"\u0101\u0003\u001a\r\u0000\u00ef\u00f1\u0005=\u0000\u0000\u00f0\u00ef"+
		"\u0001\u0000\u0000\u0000\u00f1\u00f4\u0001\u0000\u0000\u0000\u00f2\u00f0"+
		"\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000\u00f3\u00fc"+
		"\u0001\u0000\u0000\u0000\u00f4\u00f2\u0001\u0000\u0000\u0000\u00f5\u00f7"+
		"\u0005<\u0000\u0000\u00f6\u00f5\u0001\u0000\u0000\u0000\u00f7\u00fa\u0001"+
		"\u0000\u0000\u0000\u00f8\u00f6\u0001\u0000\u0000\u0000\u00f8\u00f9\u0001"+
		"\u0000\u0000\u0000\u00f9\u00fc\u0001\u0000\u0000\u0000\u00fa\u00f8\u0001"+
		"\u0000\u0000\u0000\u00fb\u00f2\u0001\u0000\u0000\u0000\u00fb\u00f8\u0001"+
		"\u0000\u0000\u0000\u00fc\u00fd\u0001\u0000\u0000\u0000\u00fd\u00fe\u0005"+
		"0\u0000\u0000\u00fe\u0100\u0003\u001a\r\u0000\u00ff\u00fb\u0001\u0000"+
		"\u0000\u0000\u0100\u0103\u0001\u0000\u0000\u0000\u0101\u00ff\u0001\u0000"+
		"\u0000\u0000\u0101\u0102\u0001\u0000\u0000\u0000\u0102\u0019\u0001\u0000"+
		"\u0000\u0000\u0103\u0101\u0001\u0000\u0000\u0000\u0104\u0106\u0005\u0001"+
		"\u0000\u0000\u0105\u0104\u0001\u0000\u0000\u0000\u0105\u0106\u0001\u0000"+
		"\u0000\u0000\u0106\u0107\u0001\u0000\u0000\u0000\u0107\u010d\u0005;\u0000"+
		"\u0000\u0108\u010a\u0005\u0017\u0000\u0000\u0109\u010b\u0003\u001c\u000e"+
		"\u0000\u010a\u0109\u0001\u0000\u0000\u0000\u010a\u010b\u0001\u0000\u0000"+
		"\u0000\u010b\u010c\u0001\u0000\u0000\u0000\u010c\u010e\u0005\u0018\u0000"+
		"\u0000\u010d\u0108\u0001\u0000\u0000\u0000\u010d\u010e\u0001\u0000\u0000"+
		"\u0000\u010e\u001b\u0001\u0000\u0000\u0000\u010f\u0110\u0003l6\u0000\u0110"+
		"\u0111\u0003\u001e\u000f\u0000\u0111\u0119\u0003l6\u0000\u0112\u0113\u0005"+
		"\u001d\u0000\u0000\u0113\u0114\u0003l6\u0000\u0114\u0115\u0003\u001e\u000f"+
		"\u0000\u0115\u0116\u0003l6\u0000\u0116\u0118\u0001\u0000\u0000\u0000\u0117"+
		"\u0112\u0001\u0000\u0000\u0000\u0118\u011b\u0001\u0000\u0000\u0000\u0119"+
		"\u0117\u0001\u0000\u0000\u0000\u0119\u011a\u0001\u0000\u0000\u0000\u011a"+
		"\u001d\u0001\u0000\u0000\u0000\u011b\u0119\u0001\u0000\u0000\u0000\u011c"+
		"\u011e\u0005\u0001\u0000\u0000\u011d\u011c\u0001\u0000\u0000\u0000\u011d"+
		"\u011e\u0001\u0000\u0000\u0000\u011e\u011f\u0001\u0000\u0000\u0000\u011f"+
		"\u0120\u0005;\u0000\u0000\u0120\u0121\u0005\u0016\u0000\u0000\u0121\u0122"+
		"\u0003H$\u0000\u0122\u001f\u0001\u0000\u0000\u0000\u0123\u0124\u0005\r"+
		"\u0000\u0000\u0124\u0128\u0005\u0019\u0000\u0000\u0125\u0126\u0003l6\u0000"+
		"\u0126\u0127\u0003\"\u0011\u0000\u0127\u0129\u0001\u0000\u0000\u0000\u0128"+
		"\u0125\u0001\u0000\u0000\u0000\u0129\u012a\u0001\u0000\u0000\u0000\u012a"+
		"\u0128\u0001\u0000\u0000\u0000\u012a\u012b\u0001\u0000\u0000\u0000\u012b"+
		"\u012c\u0001\u0000\u0000\u0000\u012c\u012e\u0003l6\u0000\u012d\u012f\u0003"+
		"$\u0012\u0000\u012e\u012d\u0001\u0000\u0000\u0000\u012e\u012f\u0001\u0000"+
		"\u0000\u0000\u012f\u0130\u0001\u0000\u0000\u0000\u0130\u0131\u0003l6\u0000"+
		"\u0131\u0132\u0005\u001a\u0000\u0000\u0132!\u0001\u0000\u0000\u0000\u0133"+
		"\u0134\u00036\u001b\u0000\u0134\u0135\u0003l6\u0000\u0135\u0136\u0005"+
		"\u0015\u0000\u0000\u0136\u0137\u0003l6\u0000\u0137\u0138\u0003&\u0013"+
		"\u0000\u0138#\u0001\u0000\u0000\u0000\u0139\u013a\u0005\u0006\u0000\u0000"+
		"\u013a\u013b\u0003l6\u0000\u013b\u013c\u0005\u0015\u0000\u0000\u013c\u013d"+
		"\u0003l6\u0000\u013d\u013e\u0003&\u0013\u0000\u013e%\u0001\u0000\u0000"+
		"\u0000\u013f\u0142\u0003*\u0015\u0000\u0140\u0142\u00036\u001b\u0000\u0141"+
		"\u013f\u0001\u0000\u0000\u0000\u0141\u0140\u0001\u0000\u0000\u0000\u0142"+
		"\'\u0001\u0000\u0000\u0000\u0143\u0144\u0005\u0019\u0000\u0000\u0144\u0148"+
		"\u0003l6\u0000\u0145\u0146\u0003X,\u0000\u0146\u0147\u0005\u0015\u0000"+
		"\u0000\u0147\u0149\u0001\u0000\u0000\u0000\u0148\u0145\u0001\u0000\u0000"+
		"\u0000\u0148\u0149\u0001\u0000\u0000\u0000\u0149\u014a\u0001\u0000\u0000"+
		"\u0000\u014a\u0150\u0003l6\u0000\u014b\u014c\u00036\u001b\u0000\u014c"+
		"\u014d\u0003l6\u0000\u014d\u014f\u0001\u0000\u0000\u0000\u014e\u014b\u0001"+
		"\u0000\u0000\u0000\u014f\u0152\u0001\u0000\u0000\u0000\u0150\u014e\u0001"+
		"\u0000\u0000\u0000\u0150\u0151\u0001\u0000\u0000\u0000\u0151\u0153\u0001"+
		"\u0000\u0000\u0000\u0152\u0150\u0001\u0000\u0000\u0000\u0153\u0154\u0005"+
		"\u001a\u0000\u0000\u0154)\u0001\u0000\u0000\u0000\u0155\u0156\u0005\u0019"+
		"\u0000\u0000\u0156\u015c\u0003l6\u0000\u0157\u0158\u00036\u001b\u0000"+
		"\u0158\u0159\u0003l6\u0000\u0159\u015b\u0001\u0000\u0000\u0000\u015a\u0157"+
		"\u0001\u0000\u0000\u0000\u015b\u015e\u0001\u0000\u0000\u0000\u015c\u015a"+
		"\u0001\u0000\u0000\u0000\u015c\u015d\u0001\u0000\u0000\u0000\u015d\u015f"+
		"\u0001\u0000\u0000\u0000\u015e\u015c\u0001\u0000\u0000\u0000\u015f\u0160"+
		"\u0005\u001a\u0000\u0000\u0160+\u0001\u0000\u0000\u0000\u0161\u0163\u0005"+
		"\u0001\u0000\u0000\u0162\u0161\u0001\u0000\u0000\u0000\u0162\u0163\u0001"+
		"\u0000\u0000\u0000\u0163\u0164\u0001\u0000\u0000\u0000\u0164\u0165\u0005"+
		"\u0012\u0000\u0000\u0165\u0167\u0005;\u0000\u0000\u0166\u0168\u0003T*"+
		"\u0000\u0167\u0166\u0001\u0000\u0000\u0000\u0167\u0168\u0001\u0000\u0000"+
		"\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u0169\u016c\u0003V+\u0000\u016a"+
		"\u016b\u0005\u0016\u0000\u0000\u016b\u016d\u0003H$\u0000\u016c\u016a\u0001"+
		"\u0000\u0000\u0000\u016c\u016d\u0001\u0000\u0000\u0000\u016d-\u0001\u0000"+
		"\u0000\u0000\u016e\u016f\u0005\u0013\u0000\u0000\u016f\u0170\u0003l6\u0000"+
		"\u0170\u0171\u0003*\u0015\u0000\u0171\u0172\u0003l6\u0000\u0172\u0173"+
		"\u0005\u0014\u0000\u0000\u0173\u0174\u0003l6\u0000\u0174\u0175\u0005\u0019"+
		"\u0000\u0000\u0175\u0179\u0003l6\u0000\u0176\u0178\u00030\u0018\u0000"+
		"\u0177\u0176\u0001\u0000\u0000\u0000\u0178\u017b\u0001\u0000\u0000\u0000"+
		"\u0179\u0177\u0001\u0000\u0000\u0000\u0179\u017a\u0001\u0000\u0000\u0000"+
		"\u017a\u017c\u0001\u0000\u0000\u0000\u017b\u0179\u0001\u0000\u0000\u0000"+
		"\u017c\u017d\u0005\u001a\u0000\u0000\u017d/\u0001\u0000\u0000\u0000\u017e"+
		"\u017f\u0005;\u0000\u0000\u017f\u0180\u0005\u0017\u0000\u0000\u0180\u0185"+
		"\u00032\u0019\u0000\u0181\u0182\u0005\u001d\u0000\u0000\u0182\u0184\u0003"+
		"2\u0019\u0000\u0183\u0181\u0001\u0000\u0000\u0000\u0184\u0187\u0001\u0000"+
		"\u0000\u0000\u0185\u0183\u0001\u0000\u0000\u0000\u0185\u0186\u0001\u0000"+
		"\u0000\u0000\u0186\u0188\u0001\u0000\u0000\u0000\u0187\u0185\u0001\u0000"+
		"\u0000\u0000\u0188\u0189\u0005\u0018\u0000\u0000\u0189\u018a\u0003l6\u0000"+
		"\u018a\u018b\u0005\u0015\u0000\u0000\u018b\u018c\u0003l6\u0000\u018c\u018d"+
		"\u00034\u001a\u0000\u018d\u018e\u0003l6\u0000\u018e1\u0001\u0000\u0000"+
		"\u0000\u018f\u0190\u0005;\u0000\u0000\u01903\u0001\u0000\u0000\u0000\u0191"+
		"\u0194\u0003*\u0015\u0000\u0192\u0194\u00036\u001b\u0000\u0193\u0191\u0001"+
		"\u0000\u0000\u0000\u0193\u0192\u0001\u0000\u0000\u0000\u01945\u0001\u0000"+
		"\u0000\u0000\u0195\u0196\u0006\u001b\uffff\uffff\u0000\u0196\u01b6\u0003"+
		",\u0016\u0000\u0197\u01b6\u0003.\u0017\u0000\u0198\u0199\u0005\b\u0000"+
		"\u0000\u0199\u019a\u00036\u001b\u0000\u019a\u019b\u0003*\u0015\u0000\u019b"+
		"\u01b6\u0001\u0000\u0000\u0000\u019c\u01b6\u0003 \u0010\u0000\u019d\u019e"+
		"\u0005\u0017\u0000\u0000\u019e\u019f\u00036\u001b\u0000\u019f\u01a0\u0005"+
		"\u0018\u0000\u0000\u01a0\u01b6\u0001\u0000\u0000\u0000\u01a1\u01b6\u0003"+
		"F#\u0000\u01a2\u01b6\u0003R)\u0000\u01a3\u01b6\u0003P(\u0000\u01a4\u01b6"+
		"\u0003`0\u0000\u01a5\u01a6\u0005,\u0000\u0000\u01a6\u01b6\u00036\u001b"+
		"\u0010\u01a7\u01b6\u0003(\u0014\u0000\u01a8\u01b6\u0003d2\u0000\u01a9"+
		"\u01aa\u0005;\u0000\u0000\u01aa\u01ab\u0003<\u001e\u0000\u01ab\u01ac\u0003"+
		"6\u001b\b\u01ac\u01b6\u0001\u0000\u0000\u0000\u01ad\u01ae\u0005(\u0000"+
		"\u0000\u01ae\u01b6\u00036\u001b\u0007\u01af\u01b6\u0005:\u0000\u0000\u01b0"+
		"\u01b6\u0003j5\u0000\u01b1\u01b6\u0005;\u0000\u0000\u01b2\u01b6\u0005"+
		"&\u0000\u0000\u01b3\u01b6\u0005\u0010\u0000\u0000\u01b4\u01b6\u0005\u0011"+
		"\u0000\u0000\u01b5\u0195\u0001\u0000\u0000\u0000\u01b5\u0197\u0001\u0000"+
		"\u0000\u0000\u01b5\u0198\u0001\u0000\u0000\u0000\u01b5\u019c\u0001\u0000"+
		"\u0000\u0000\u01b5\u019d\u0001\u0000\u0000\u0000\u01b5\u01a1\u0001\u0000"+
		"\u0000\u0000\u01b5\u01a2\u0001\u0000\u0000\u0000\u01b5\u01a3\u0001\u0000"+
		"\u0000\u0000\u01b5\u01a4\u0001\u0000\u0000\u0000\u01b5\u01a5\u0001\u0000"+
		"\u0000\u0000\u01b5\u01a7\u0001\u0000\u0000\u0000\u01b5\u01a8\u0001\u0000"+
		"\u0000\u0000\u01b5\u01a9\u0001\u0000\u0000\u0000\u01b5\u01ad\u0001\u0000"+
		"\u0000\u0000\u01b5\u01af\u0001\u0000\u0000\u0000\u01b5\u01b0\u0001\u0000"+
		"\u0000\u0000\u01b5\u01b1\u0001\u0000\u0000\u0000\u01b5\u01b2\u0001\u0000"+
		"\u0000\u0000\u01b5\u01b3\u0001\u0000\u0000\u0000\u01b5\u01b4\u0001\u0000"+
		"\u0000\u0000\u01b6\u0213\u0001\u0000\u0000\u0000\u01b7\u01b8\n%\u0000"+
		"\u0000\u01b8\u01b9\u0003l6\u0000\u01b9\u01ba\u0005\u001e\u0000\u0000\u01ba"+
		"\u01bb\u0005;\u0000\u0000\u01bb\u01bc\u0005$\u0000\u0000\u01bc\u01bd\u0003"+
		"6\u001b&\u01bd\u0212\u0001\u0000\u0000\u0000\u01be\u01bf\n\u001c\u0000"+
		"\u0000\u01bf\u01c0\u0005\u001b\u0000\u0000\u01c0\u01c1\u00036\u001b\u0000"+
		"\u01c1\u01c2\u0005\u001c\u0000\u0000\u01c2\u01c3\u0005$\u0000\u0000\u01c3"+
		"\u01c4\u00036\u001b\u001d\u01c4\u0212\u0001\u0000\u0000\u0000\u01c5\u01c6"+
		"\n\u0016\u0000\u0000\u01c6\u01c7\u0005-\u0000\u0000\u01c7\u0212\u0003"+
		"6\u001b\u0017\u01c8\u01c9\n\u0015\u0000\u0000\u01c9\u01ca\u0005.\u0000"+
		"\u0000\u01ca\u0212\u00036\u001b\u0016\u01cb\u01cc\n\u0014\u0000\u0000"+
		"\u01cc\u01cd\u00038\u001c\u0000\u01cd\u01ce\u00036\u001b\u0015\u01ce\u0212"+
		"\u0001\u0000\u0000\u0000\u01cf\u01d0\n\u0013\u0000\u0000\u01d0\u01d1\u0005"+
		")\u0000\u0000\u01d1\u0212\u00036\u001b\u0014\u01d2\u01d3\n\u0012\u0000"+
		"\u0000\u01d3\u01d4\u0003:\u001d\u0000\u01d4\u01d5\u00036\u001b\u0013\u01d5"+
		"\u0212\u0001\u0000\u0000\u0000\u01d6\u01d7\n\u0011\u0000\u0000\u01d7\u01d8"+
		"\u00051\u0000\u0000\u01d8\u0212\u00036\u001b\u0012\u01d9\u01da\n\u000f"+
		"\u0000\u0000\u01da\u01db\u0003>\u001f\u0000\u01db\u01dc\u00036\u001b\u0010"+
		"\u01dc\u0212\u0001\u0000\u0000\u0000\u01dd\u01de\n\u000e\u0000\u0000\u01de"+
		"\u01df\u0003@ \u0000\u01df\u01e0\u00036\u001b\u000f\u01e0\u0212\u0001"+
		"\u0000\u0000\u0000\u01e1\u01e2\n\r\u0000\u0000\u01e2\u01e3\u0005/\u0000"+
		"\u0000\u01e3\u0212\u00036\u001b\u000e\u01e4\u01e5\n\f\u0000\u0000\u01e5"+
		"\u01e6\u00050\u0000\u0000\u01e6\u0212\u00036\u001b\r\u01e7\u01e8\n\'\u0000"+
		"\u0000\u01e8\u01e9\u0005\u0007\u0000\u0000\u01e9\u0212\u0003H$\u0000\u01ea"+
		"\u01eb\n&\u0000\u0000\u01eb\u01ec\u0003l6\u0000\u01ec\u01ed\u0005\u001e"+
		"\u0000\u0000\u01ed\u01ef\u0005;\u0000\u0000\u01ee\u01f0\u0003B!\u0000"+
		"\u01ef\u01ee\u0001\u0000\u0000\u0000\u01ef\u01f0\u0001\u0000\u0000\u0000"+
		"\u01f0\u01f1\u0001\u0000\u0000\u0000\u01f1\u01f2\u0005\u0017\u0000\u0000"+
		"\u01f2\u01f3\u0003D\"\u0000\u01f3\u01f4\u0005\u0018\u0000\u0000\u01f4"+
		"\u0212\u0001\u0000\u0000\u0000\u01f5\u01f6\n$\u0000\u0000\u01f6\u01f7"+
		"\u0003l6\u0000\u01f7\u01f8\u0005\u001e\u0000\u0000\u01f8\u01f9\u0005;"+
		"\u0000\u0000\u01f9\u0212\u0001\u0000\u0000\u0000\u01fa\u01fb\n!\u0000"+
		"\u0000\u01fb\u01fc\u0005\u000f\u0000\u0000\u01fc\u0212\u0005;\u0000\u0000"+
		"\u01fd\u01ff\n\u001d\u0000\u0000\u01fe\u0200\u0003B!\u0000\u01ff\u01fe"+
		"\u0001\u0000\u0000\u0000\u01ff\u0200\u0001\u0000\u0000\u0000\u0200\u0201"+
		"\u0001\u0000\u0000\u0000\u0201\u0202\u0005\u0017\u0000\u0000\u0202\u0203"+
		"\u0003D\"\u0000\u0203\u0204\u0005\u0018\u0000\u0000\u0204\u0212\u0001"+
		"\u0000\u0000\u0000\u0205\u0206\n\u001b\u0000\u0000\u0206\u0207\u0005\u001b"+
		"\u0000\u0000\u0207\u0208\u00036\u001b\u0000\u0208\u0209\u0005\u001c\u0000"+
		"\u0000\u0209\u0212\u0001\u0000\u0000\u0000\u020a\u020b\n\t\u0000\u0000"+
		"\u020b\u020c\u0003l6\u0000\u020c\u020d\u0005%\u0000\u0000\u020d\u020e"+
		"\u0003l6\u0000\u020e\u020f\u00036\u001b\u0000\u020f\u0210\u0003l6\u0000"+
		"\u0210\u0212\u0001\u0000\u0000\u0000\u0211\u01b7\u0001\u0000\u0000\u0000"+
		"\u0211\u01be\u0001\u0000\u0000\u0000\u0211\u01c5\u0001\u0000\u0000\u0000"+
		"\u0211\u01c8\u0001\u0000\u0000\u0000\u0211\u01cb\u0001\u0000\u0000\u0000"+
		"\u0211\u01cf\u0001\u0000\u0000\u0000\u0211\u01d2\u0001\u0000\u0000\u0000"+
		"\u0211\u01d6\u0001\u0000\u0000\u0000\u0211\u01d9\u0001\u0000\u0000\u0000"+
		"\u0211\u01dd\u0001\u0000\u0000\u0000\u0211\u01e1\u0001\u0000\u0000\u0000"+
		"\u0211\u01e4\u0001\u0000\u0000\u0000\u0211\u01e7\u0001\u0000\u0000\u0000"+
		"\u0211\u01ea\u0001\u0000\u0000\u0000\u0211\u01f5\u0001\u0000\u0000\u0000"+
		"\u0211\u01fa\u0001\u0000\u0000\u0000\u0211\u01fd\u0001\u0000\u0000\u0000"+
		"\u0211\u0205\u0001\u0000\u0000\u0000\u0211\u020a\u0001\u0000\u0000\u0000"+
		"\u0212\u0215\u0001\u0000\u0000\u0000\u0213\u0211\u0001\u0000\u0000\u0000"+
		"\u0213\u0214\u0001\u0000\u0000\u0000\u02147\u0001\u0000\u0000\u0000\u0215"+
		"\u0213\u0001\u0000\u0000\u0000\u0216\u0217\u0007\u0000\u0000\u0000\u0217"+
		"9\u0001\u0000\u0000\u0000\u0218\u0219\u0007\u0001\u0000\u0000\u0219;\u0001"+
		"\u0000\u0000\u0000\u021a\u021b\u0007\u0002\u0000\u0000\u021b=\u0001\u0000"+
		"\u0000\u0000\u021c\u021d\u0005/\u0000\u0000\u021d\u021e\u0005/\u0000\u0000"+
		"\u021e?\u0001\u0000\u0000\u0000\u021f\u0220\u00050\u0000\u0000\u0220\u0221"+
		"\u00050\u0000\u0000\u0221A\u0001\u0000\u0000\u0000\u0222\u0223\u0005\u001b"+
		"\u0000\u0000\u0223\u0228\u0003H$\u0000\u0224\u0225\u0005\u001d\u0000\u0000"+
		"\u0225\u0227\u0003H$\u0000\u0226\u0224\u0001\u0000\u0000\u0000\u0227\u022a"+
		"\u0001\u0000\u0000\u0000\u0228\u0226\u0001\u0000\u0000\u0000\u0228\u0229"+
		"\u0001\u0000\u0000\u0000\u0229\u022b\u0001\u0000\u0000\u0000\u022a\u0228"+
		"\u0001\u0000\u0000\u0000\u022b\u022c\u0005\u001c\u0000\u0000\u022cC\u0001"+
		"\u0000\u0000\u0000\u022d\u022f\u00036\u001b\u0000\u022e\u022d\u0001\u0000"+
		"\u0000\u0000\u022e\u022f\u0001\u0000\u0000\u0000\u022f\u0234\u0001\u0000"+
		"\u0000\u0000\u0230\u0231\u0005\u001d\u0000\u0000\u0231\u0233\u00036\u001b"+
		"\u0000\u0232\u0230\u0001\u0000\u0000\u0000\u0233\u0236\u0001\u0000\u0000"+
		"\u0000\u0234\u0232\u0001\u0000\u0000\u0000\u0234\u0235\u0001\u0000\u0000"+
		"\u0000\u0235E\u0001\u0000\u0000\u0000\u0236\u0234\u0001\u0000\u0000\u0000"+
		"\u0237\u0238\u0005;\u0000\u0000\u0238\u0239\u0005$\u0000\u0000\u0239\u023a"+
		"\u00036\u001b\u0000\u023aG\u0001\u0000\u0000\u0000\u023b\u023f\u0003J"+
		"%\u0000\u023c\u023f\u0003L&\u0000\u023d\u023f\u0003N\'\u0000\u023e\u023b"+
		"\u0001\u0000\u0000\u0000\u023e\u023c\u0001\u0000\u0000\u0000\u023e\u023d"+
		"\u0001\u0000\u0000\u0000\u023fI\u0001\u0000\u0000\u0000\u0240\u0241\u0005"+
		";\u0000\u0000\u0241\u0243\u0005\u001e\u0000\u0000\u0242\u0240\u0001\u0000"+
		"\u0000\u0000\u0242\u0243\u0001\u0000\u0000\u0000\u0243\u0244\u0001\u0000"+
		"\u0000\u0000\u0244\u0245\u0005;\u0000\u0000\u0245K\u0001\u0000\u0000\u0000"+
		"\u0246\u0248\u0005\u0017\u0000\u0000\u0247\u0249\u0003H$\u0000\u0248\u0247"+
		"\u0001\u0000\u0000\u0000\u0248\u0249\u0001\u0000\u0000\u0000\u0249\u024e"+
		"\u0001\u0000\u0000\u0000\u024a\u024b\u0005\u001d\u0000\u0000\u024b\u024d"+
		"\u0003H$\u0000\u024c\u024a\u0001\u0000\u0000\u0000\u024d\u0250\u0001\u0000"+
		"\u0000\u0000\u024e\u024c\u0001\u0000\u0000\u0000\u024e\u024f\u0001\u0000"+
		"\u0000\u0000\u024f\u0251\u0001\u0000\u0000\u0000\u0250\u024e\u0001\u0000"+
		"\u0000\u0000\u0251\u0252\u0005\u0018\u0000\u0000\u0252\u0253\u0005\u0015"+
		"\u0000\u0000\u0253\u0254\u0003^/\u0000\u0254M\u0001\u0000\u0000\u0000"+
		"\u0255\u0256\u0003J%\u0000\u0256\u0257\u0005\u001b\u0000\u0000\u0257\u025c"+
		"\u0003H$\u0000\u0258\u0259\u0005\u001d\u0000\u0000\u0259\u025b\u0003H"+
		"$\u0000\u025a\u0258\u0001\u0000\u0000\u0000\u025b\u025e\u0001\u0000\u0000"+
		"\u0000\u025c\u025a\u0001\u0000\u0000\u0000\u025c\u025d\u0001\u0000\u0000"+
		"\u0000\u025d\u025f\u0001\u0000\u0000\u0000\u025e\u025c\u0001\u0000\u0000"+
		"\u0000\u025f\u0260\u0005\u001c\u0000\u0000\u0260O\u0001\u0000\u0000\u0000"+
		"\u0261\u0263\u0005\u0001\u0000\u0000\u0262\u0261\u0001\u0000\u0000\u0000"+
		"\u0262\u0263\u0001\u0000\u0000\u0000\u0263\u0264\u0001\u0000\u0000\u0000"+
		"\u0264\u0265\u0007\u0003\u0000\u0000\u0265\u0268\u0005;\u0000\u0000\u0266"+
		"\u0267\u0005\u0016\u0000\u0000\u0267\u0269\u0003H$\u0000\u0268\u0266\u0001"+
		"\u0000\u0000\u0000\u0268\u0269\u0001\u0000\u0000\u0000\u0269\u026a\u0001"+
		"\u0000\u0000\u0000\u026a\u026b\u0005$\u0000\u0000\u026b\u026c\u00036\u001b"+
		"\u0000\u026cQ\u0001\u0000\u0000\u0000\u026d\u026f\u0005\u0001\u0000\u0000"+
		"\u026e\u026d\u0001\u0000\u0000\u0000\u026e\u026f\u0001\u0000\u0000\u0000"+
		"\u026f\u0270\u0001\u0000\u0000\u0000\u0270\u0271\u0005\u0004\u0000\u0000"+
		"\u0271\u0273\u0005;\u0000\u0000\u0272\u0274\u0003T*\u0000\u0273\u0272"+
		"\u0001\u0000\u0000\u0000\u0273\u0274\u0001\u0000\u0000\u0000\u0274\u0275"+
		"\u0001\u0000\u0000\u0000\u0275\u0278\u0003V+\u0000\u0276\u0277\u0005\u0016"+
		"\u0000\u0000\u0277\u0279\u0003^/\u0000\u0278\u0276\u0001\u0000\u0000\u0000"+
		"\u0278\u0279\u0001\u0000\u0000\u0000\u0279\u027a\u0001\u0000\u0000\u0000"+
		"\u027a\u027b\u0003\\.\u0000\u027bS\u0001\u0000\u0000\u0000\u027c\u027d"+
		"\u0005\u001b\u0000\u0000\u027d\u0282\u0005;\u0000\u0000\u027e\u027f\u0005"+
		"\u001d\u0000\u0000\u027f\u0281\u0005;\u0000\u0000\u0280\u027e\u0001\u0000"+
		"\u0000\u0000\u0281\u0284\u0001\u0000\u0000\u0000\u0282\u0280\u0001\u0000"+
		"\u0000\u0000\u0282\u0283\u0001\u0000\u0000\u0000\u0283\u0285\u0001\u0000"+
		"\u0000\u0000\u0284\u0282\u0001\u0000\u0000\u0000\u0285\u0286\u0005\u001c"+
		"\u0000\u0000\u0286U\u0001\u0000\u0000\u0000\u0287\u0288\u0005\u0017\u0000"+
		"\u0000\u0288\u028a\u0003l6\u0000\u0289\u028b\u0003X,\u0000\u028a\u0289"+
		"\u0001\u0000\u0000\u0000\u028a\u028b\u0001\u0000\u0000\u0000\u028b\u028c"+
		"\u0001\u0000\u0000\u0000\u028c\u028d\u0005\u0018\u0000\u0000\u028dW\u0001"+
		"\u0000\u0000\u0000\u028e\u028f\u0003Z-\u0000\u028f\u0296\u0003l6\u0000"+
		"\u0290\u0291\u0005\u001d\u0000\u0000\u0291\u0292\u0003Z-\u0000\u0292\u0293"+
		"\u0003l6\u0000\u0293\u0295\u0001\u0000\u0000\u0000\u0294\u0290\u0001\u0000"+
		"\u0000\u0000\u0295\u0298\u0001\u0000\u0000\u0000\u0296\u0294\u0001\u0000"+
		"\u0000\u0000\u0296\u0297\u0001\u0000\u0000\u0000\u0297Y\u0001\u0000\u0000"+
		"\u0000\u0298\u0296\u0001\u0000\u0000\u0000\u0299\u029a\u0005;\u0000\u0000"+
		"\u029a\u029b\u0005\u0016\u0000\u0000\u029b\u029c\u0003H$\u0000\u029c["+
		"\u0001\u0000\u0000\u0000\u029d\u029e\u0003*\u0015\u0000\u029e]\u0001\u0000"+
		"\u0000\u0000\u029f\u02a0\u0003H$\u0000\u02a0_\u0001\u0000\u0000\u0000"+
		"\u02a1\u02a5\u0005\u001f\u0000\u0000\u02a2\u02a4\u0003b1\u0000\u02a3\u02a2"+
		"\u0001\u0000\u0000\u0000\u02a4\u02a7\u0001\u0000\u0000\u0000\u02a5\u02a3"+
		"\u0001\u0000\u0000\u0000\u02a5\u02a6\u0001\u0000\u0000\u0000\u02a6\u02a8"+
		"\u0001\u0000\u0000\u0000\u02a7\u02a5\u0001\u0000\u0000\u0000\u02a8\u02a9"+
		"\u0005J\u0000\u0000\u02a9a\u0001\u0000\u0000\u0000\u02aa\u02b7\u0005I"+
		"\u0000\u0000\u02ab\u02b7\u0005D\u0000\u0000\u02ac\u02b7\u0005C\u0000\u0000"+
		"\u02ad\u02b7\u0005E\u0000\u0000\u02ae\u02b7\u0005F\u0000\u0000\u02af\u02b7"+
		"\u0005G\u0000\u0000\u02b0\u02b7\u0005H\u0000\u0000\u02b1\u02b7\u0005B"+
		"\u0000\u0000\u02b2\u02b3\u0005A\u0000\u0000\u02b3\u02b4\u00036\u001b\u0000"+
		"\u02b4\u02b5\u0005\u001a\u0000\u0000\u02b5\u02b7\u0001\u0000\u0000\u0000"+
		"\u02b6\u02aa\u0001\u0000\u0000\u0000\u02b6\u02ab\u0001\u0000\u0000\u0000"+
		"\u02b6\u02ac\u0001\u0000\u0000\u0000\u02b6\u02ad\u0001\u0000\u0000\u0000"+
		"\u02b6\u02ae\u0001\u0000\u0000\u0000\u02b6\u02af\u0001\u0000\u0000\u0000"+
		"\u02b6\u02b0\u0001\u0000\u0000\u0000\u02b6\u02b1\u0001\u0000\u0000\u0000"+
		"\u02b6\u02b2\u0001\u0000\u0000\u0000\u02b7c\u0001\u0000\u0000\u0000\u02b8"+
		"\u02b9\u0005\u0005\u0000\u0000\u02b9\u02ba\u0005\u0017\u0000\u0000\u02ba"+
		"\u02bb\u00036\u001b\u0000\u02bb\u02bc\u0005\u0018\u0000\u0000\u02bc\u02c2"+
		"\u0003f3\u0000\u02bd\u02bf\u0005<\u0000\u0000\u02be\u02bd\u0001\u0000"+
		"\u0000\u0000\u02be\u02bf\u0001\u0000\u0000\u0000\u02bf\u02c0\u0001\u0000"+
		"\u0000\u0000\u02c0\u02c1\u0005\u0006\u0000\u0000\u02c1\u02c3\u0003h4\u0000"+
		"\u02c2\u02be\u0001\u0000\u0000\u0000\u02c2\u02c3\u0001\u0000\u0000\u0000"+
		"\u02c3e\u0001\u0000\u0000\u0000\u02c4\u02c7\u0003*\u0015\u0000\u02c5\u02c7"+
		"\u00036\u001b\u0000\u02c6\u02c4\u0001\u0000\u0000\u0000\u02c6\u02c5\u0001"+
		"\u0000\u0000\u0000\u02c7g\u0001\u0000\u0000\u0000\u02c8\u02cb\u0003*\u0015"+
		"\u0000\u02c9\u02cb\u00036\u001b\u0000\u02ca\u02c8\u0001\u0000\u0000\u0000"+
		"\u02ca\u02c9\u0001\u0000\u0000\u0000\u02cbi\u0001\u0000\u0000\u0000\u02cc"+
		"\u02cd\u0007\u0004\u0000\u0000\u02cdk\u0001\u0000\u0000\u0000\u02ce\u02d0"+
		"\u0007\u0005\u0000\u0000\u02cf\u02ce\u0001\u0000\u0000\u0000\u02d0\u02d3"+
		"\u0001\u0000\u0000\u0000\u02d1\u02cf\u0001\u0000\u0000\u0000\u02d1\u02d2"+
		"\u0001\u0000\u0000\u0000\u02d2m\u0001\u0000\u0000\u0000\u02d3\u02d1\u0001"+
		"\u0000\u0000\u0000Ipv|\u0082\u0089\u008d\u0090\u0098\u009d\u00a1\u00a5"+
		"\u00a8\u00af\u00ba\u00c2\u00c7\u00cc\u00d2\u00d8\u00db\u00e1\u00e5\u00e9"+
		"\u00ec\u00f2\u00f8\u00fb\u0101\u0105\u010a\u010d\u0119\u011d\u012a\u012e"+
		"\u0141\u0148\u0150\u015c\u0162\u0167\u016c\u0179\u0185\u0193\u01b5\u01ef"+
		"\u01ff\u0211\u0213\u0228\u022e\u0234\u023e\u0242\u0248\u024e\u025c\u0262"+
		"\u0268\u026e\u0273\u0278\u0282\u028a\u0296\u02a5\u02b6\u02be\u02c2\u02c6"+
		"\u02ca\u02d1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}