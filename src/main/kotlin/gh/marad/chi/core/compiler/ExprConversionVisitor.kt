package gh.marad.chi.core.compiler

import gh.marad.chi.core.*
import gh.marad.chi.core.Target
import gh.marad.chi.core.compiler.Compiler2.resolveType
import gh.marad.chi.core.namespace.CompilationScope
import gh.marad.chi.core.namespace.FnSymbolTable
import gh.marad.chi.core.namespace.ScopeType
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.parser.visitor.ParseAstVisitor
import gh.marad.chi.core.types.FunctionType
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeVariable
import gh.marad.chi.core.types.Types

class ExprConversionVisitor(
    private val pkg: Package,
    private val tables: CompileTables,
) : ParseAstVisitor<Expression> {

    private val typeTable = tables.localTypeTable
    private var currentFnSymbolTable: FnSymbolTable? = null
    private var currentTypeSchemeVariables = emptyList<String>()

    override fun visit(node: ParseAst): Expression = node.accept(this)

    override fun visitProgram(program: ParseProgram): Expression =
        Atom.unit(program.section)

    override fun visitPackageDefinition(parsePackageDefinition: ParsePackageDefinition): Expression =
        Atom.unit(parsePackageDefinition.section)

    override fun visitImportDefinition(parseImportDefinition: ParseImportDefinition): Expression =
        Atom.unit(parseImportDefinition.section)

    override fun visitVariantTypeDefinition(parseVariantTypeDefinition: ParseVariantTypeDefinition): Expression {
        TODO("Not yet implemented")
    }

    override fun visitTraitDefinition(parseTraitDefinition: ParseTraitDefinition): Expression {
        TODO("Not yet implemented")
    }

    override fun visitTraitFunctionDefinition(parseTraitFunctionDefinition: ParseTraitFunctionDefinition): Expression {
        TODO("Not yet implemented")
    }

    override fun visitLong(longValue: LongValue): Expression = Atom.int(longValue.value, longValue.section)

    override fun visitFloat(floatValue: FloatValue): Expression = Atom.float(floatValue.value, floatValue.section)

    override fun visitBool(boolValue: BoolValue): Expression = Atom.bool(boolValue.value, boolValue.section)

    override fun visitString(stringValue: StringValue): Expression = Atom.string(stringValue.value, stringValue.section)

    override fun visitInterpolatedString(parseInterpolatedString: ParseInterpolatedString): Expression =
        InterpolatedString(
            parts = parseInterpolatedString.parts.map { it.accept(this) },
            sourceSection = parseInterpolatedString.section
        )

    override fun visitStringInterpolation(parseInterpolation: ParseInterpolation): Expression =
        Cast(parseInterpolation.value.accept(this), targetType = Types.string, parseInterpolation.section)


    override fun visitInterpolatedStringText(stringText: StringText): Expression =
        Atom.string(stringText.text, stringText.section)

    override fun visitLambda(parseLambda: ParseLambda): Expression {
        val fnSymbolTable = FnSymbolTable()
        val params = parseLambda.formalArguments.map {
            val type = resolveType(typeTable, currentTypeSchemeVariables, it.typeRef)
            fnSymbolTable.addArgument(it.name, type)
            FnParam(it.name, type, it.section)
        }

        val body = withFnSymbolTable(fnSymbolTable) {
            parseLambda.body.map { it.accept(this) }
        }

        return Fn(
            fnScope = CompilationScope(ScopeType.Package),
            typeVariables = emptyList(),
            parameters = params,
            body = Block(body, parseLambda.section),
            sourceSection = parseLambda.section
        )
    }

    override fun visitFuncWithName(ast: ParseFuncWithName): Expression {
        val fnSymbolTable = FnSymbolTable()

        val prevTypeSchemeVariables = currentTypeSchemeVariables
        currentTypeSchemeVariables = ast.typeParameters.map { it.name }

        val params = ast.formalArguments.map {
            val type = resolveType(typeTable, currentTypeSchemeVariables, it.typeRef)
            fnSymbolTable.addArgument(it.name, type)
            FnParam(it.name, type, it.section)
        }

        val function = Fn(
            fnScope = CompilationScope(ScopeType.Package), // TODO remove
            typeVariables = ast.typeParameters.map { TypeVariable(it.name) },
            parameters = params,
            body = withFnSymbolTable(fnSymbolTable) {
                ast.body.accept(this) as Block
            },
            ast.body.section
        )

        val returnType = ast.returnTypeRef
            ?.let { resolveType(typeTable, currentTypeSchemeVariables, it) }
            ?: Types.unit

        val funcTypes = params.map { it.type!! } + returnType

        val funcType = FunctionType(
            types = funcTypes,
            typeSchemeVariables = funcTypes.flatMap { it.findTypeVariables() }
                .filter { it.name in currentTypeSchemeVariables }.distinct()
        )

        currentTypeSchemeVariables = prevTypeSchemeVariables

        return NameDeclaration(
            public = ast.public,
            enclosingScope = CompilationScope(ScopeType.Package), // TODO remove
            name = ast.name,
            value = function,
            mutable = false,
            expectedType = funcType,
            sourceSection = ast.section
        ).also {
            addLocalSymbol(it.name, type = null, it.mutable, it.public)
        }
    }

    override fun visitFnCall(parseFnCall: ParseFnCall): Expression =
        FnCall(
            parseFnCall.function.accept(this),
            parseFnCall.concreteTypeParameters.map {
                resolveType(typeTable, currentTypeSchemeVariables, it)
            },
            parseFnCall.arguments.map { it.accept(this) },
            parseFnCall.section
        )

    override fun visitAssignment(parseAssignment: ParseAssignment): Expression =
        Assignment(
            target = getSymbol(parseAssignment.variableName, parseAssignment.section),
            value = parseAssignment.value.accept(this),
            sourceSection = parseAssignment.section
        )

    override fun visitIndexedAssignment(parseIndexedAssignment: ParseIndexedAssignment): Expression =
        IndexedAssignment(
            variable = parseIndexedAssignment.variable.accept(this),
            index = parseIndexedAssignment.index.accept(this),
            value = parseIndexedAssignment.value.accept(this),
            sourceSection = parseIndexedAssignment.section
        )

    override fun visitVariableRead(parseVariableRead: ParseVariableRead): Expression =
        VariableAccess(
            target = getSymbol(parseVariableRead.variableName, sourceSection = parseVariableRead.section),
            sourceSection = parseVariableRead.section
        )

    override fun visitIndexOperator(parseIndexOperator: ParseIndexOperator): Expression =
        IndexOperator(
            variable = parseIndexOperator.variable.accept(this),
            index = parseIndexOperator.index.accept(this),
            parseIndexOperator.section
        )

    override fun visitNameDeclaration(parseNameDeclaration: ParseNameDeclaration): Expression =
        NameDeclaration(
            public = parseNameDeclaration.public,
            mutable = parseNameDeclaration.mutable,
            name = parseNameDeclaration.symbol.name,
            value = parseNameDeclaration.value.accept(this),
            sourceSection = parseNameDeclaration.section,
            expectedType = parseNameDeclaration.typeRef?.let { resolveType(typeTable, currentTypeSchemeVariables, it) },
            enclosingScope = CompilationScope(ScopeType.Package), // TODO to remove
        ).also {
            addLocalSymbol(it.name, type = null, it.mutable, it.public)
        }

    override fun visitFieldAccess(parseFieldAccess: ParseFieldAccess): Expression {
        return FieldAccess(
            receiver = parseFieldAccess.receiver.accept(this),
            fieldName = parseFieldAccess.memberName,
            typeIsModuleLocal = false, // TODO to remove, checking imports should be before conversion
            sourceSection = parseFieldAccess.section,
            memberSection = parseFieldAccess.memberSection
        )
    }

    override fun visitFieldAssignment(parseFieldAssignment: ParseFieldAssignment): Expression {
        return FieldAssignment(
            receiver = parseFieldAssignment.receiver.accept(this),
            fieldName = parseFieldAssignment.memberName,
            value = parseFieldAssignment.value.accept(this),
            sourceSection = parseFieldAssignment.section,
            memberSection = parseFieldAssignment.memberSection
        )
    }

    override fun visitMethodInvocation(parseMethodInvocation: ParseMethodInvocation): Expression {
        TODO()
    }

    override fun visitEffectDefinition(ast: ParseEffectDefinition): Expression {
        val prevTypeSchemeVariables = currentTypeSchemeVariables
        currentTypeSchemeVariables = ast.typeParameters.map { it.name }

        val params = ast.formalArguments.map {
            FnParam(
                it.name,
                resolveType(typeTable, currentTypeSchemeVariables, it.typeRef),
                it.section
            )
        }

        val types = params.map { it.type!! } + resolveType(typeTable, currentTypeSchemeVariables, ast.returnTypeRef)

        val type = FunctionType(
            types = types,
            typeSchemeVariables = ast.typeParameters.map {
                resolveType(typeTable, currentTypeSchemeVariables, it)  as TypeVariable
            }
        ).also { it.sourceSection = ast.section }

        currentTypeSchemeVariables = prevTypeSchemeVariables

        return EffectDefinition(
            moduleName = pkg.moduleName,
            packageName = pkg.packageName,
            name = ast.name,
            public = ast.public,
            typeVariables = ast.typeParameters.map { TypeVariable(it.name) },
            parameters = params,
            sourceSection = ast.section
        ).also {
            it.newType = type
            addLocalSymbol(it.name, type = type, isMutable = false, it.public)
        }
    }

    override fun visitHandle(ast: ParseHandle): Expression =
        Handle(
            body = ast.body.accept(this) as Block,
            cases = ast.cases.map {
                val info = getSymbol(it.effectName, ast.section) as PackageSymbol
                addLocalSymbol("resume", type = null, isMutable = false, isPublic = true)

                HandleCase(
                    moduleName = info.symbol.moduleName,
                    packageName = info.symbol.packageName,
                    effectName = it.effectName,
                    argumentNames = it.argumentNames,
                    body = it.body.accept(this),
                    scope = CompilationScope(ScopeType.Package), // TODO remove
                    sourceSection = it.section
                ).also {
                    removeLocalSymbol("resume")
                }
            },
            sourceSection = ast.section
        )

    override fun visitNot(parseNot: ParseNot): Expression =
        PrefixOp("!", parseNot.value.accept(this), parseNot.section)

    override fun visitBinaryOperator(parseBinaryOp: ParseBinaryOp): Expression =
        InfixOp(parseBinaryOp.op, parseBinaryOp.left.accept(this), parseBinaryOp.right.accept(this), parseBinaryOp.section)

    override fun visitBlock(parseBlock: ParseBlock): Expression =
        Block(
            body = parseBlock.body.map { it.accept(this) },
            sourceSection = parseBlock.section
        )

    override fun visitCast(parseCast: ParseCast): Expression {
        val targetType = resolveType(typeTable, currentTypeSchemeVariables, parseCast.typeRef)
        return Cast(parseCast.value.accept(this), targetType, parseCast.section)
    }

    override fun visitGroup(parseGroup: ParseGroup): Expression =
        parseGroup.value.accept(this)

    override fun visitIs(parseIs: ParseIs): Expression =
        Is(parseIs.value.accept(this), parseIs.typeName, parseIs.section)

    override fun visitIfElse(ast: ParseIfElse): Expression =
        IfElse(
            condition = ast.condition.accept(this),
            thenBranch = ast.thenBody.accept(this),
            elseBranch = ast.elseBody?.accept(this),
            sourceSection = ast.section
        )

    override fun visitWhile(ast: ParseWhile): Expression =
        WhileLoop(
            condition = ast.condition.accept(this),
            loop = ast.body.accept(this),
            sourceSection = ast.section
        )

    override fun visitBreak(parseBreak: ParseBreak): Expression = Break(parseBreak.section)

    override fun visitContinue(parseContinue: ParseContinue): Expression = Continue(parseContinue.section)

    override fun visitWhen(ast: ParseWhen): Expression {
        TODO("Not yet implemented")
    }

    override fun visitWeave(parseWeave: ParseWeave): Expression {
        TODO("Not yet implemented")
    }

    override fun visitPlaceholder(parseWeavePlaceholder: ParseWeavePlaceholder): Expression {
        TODO("Not yet implemented")
    }

    override fun visitReturn(parseReturn: ParseReturn): Expression =
        Return(parseReturn.value?.accept(this), parseReturn.section)

    private fun <T> withFnSymbolTable(fnSymbolTable: FnSymbolTable, f: () -> T): T {
        val prevFnSymbolTable = currentFnSymbolTable
        currentFnSymbolTable = fnSymbolTable
        return f().also { currentFnSymbolTable = prevFnSymbolTable }
    }

    private fun getSymbol(name: String, sourceSection: ChiSource.Section?): Target {
        val fnSymbolTable = currentFnSymbolTable
        return if (fnSymbolTable != null) {
            fnSymbolTable.get(name)?.let { LocalSymbol(it) }
                ?: throw ExprConversionException("Tried to get local symbol '$name'", sourceSection)
        } else {
            tables.localSymbolTable.get(name)?.let { PackageSymbol(it) }
                ?: throw ExprConversionException("Tried to get symbol '$name'", sourceSection)
        }
    }

    private fun addLocalSymbol(name: String, type: Type?, isMutable: Boolean, isPublic: Boolean) {
        val fnSymbolTable = currentFnSymbolTable
        if (fnSymbolTable != null) {
            // we are inside a function so we declare simple local
            fnSymbolTable.addLocal(name, type, isMutable)
        } else {
            tables.defineSymbol(
                Symbol(
                    moduleName = pkg.moduleName,
                    packageName = pkg.packageName,
                    name = name,
                    type = type,
                    isPublic, isMutable
                )
            )
        }
    }

    private fun removeLocalSymbol(name: String) {
        val fnSymbolTable = currentFnSymbolTable
        if (fnSymbolTable != null) {
            fnSymbolTable.remove(name)
        } else {
            tables.removeSymbol(name)
        }
    }
}