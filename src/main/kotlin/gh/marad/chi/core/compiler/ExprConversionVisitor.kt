package gh.marad.chi.core.compiler

import gh.marad.chi.core.*
import gh.marad.chi.core.Target
import gh.marad.chi.core.compiler.Compiler.resolveType
import gh.marad.chi.core.namespace.FnSymbol
import gh.marad.chi.core.namespace.FnSymbolTable
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.parser.ChiSource
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.parser.visitor.ParseAstVisitor
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Variable
import gh.marad.chi.core.utils.DefaultArguments
import java.util.UUID

class ExprConversionVisitor(
    private val pkg: Package,
    private val tables: CompileTables,
) : ParseAstVisitor<Expression> {

    class TempVarGenerator {
        private var nextTempVarId = 0
        fun nextName() = "tempVar$${nextTempVarId++}"
    }

    class WeaveContext {
        private var weaveInput: Expression? = null
        fun <T> withWeaveInput(expression: Expression, f: () -> T): T {
            val prevWeaveInput = weaveInput
            weaveInput = expression
            return f().also { weaveInput = prevWeaveInput }
        }
        fun currentInput() = weaveInput!!
    }

    private val typeTable = tables.localTypeTable
    private var currentFnSymbolTable: FnSymbolTable? = null
    private var currentTypeSchemeVariables = emptyList<String>()
    private var tempVarGenerator = TempVarGenerator()
    private var weaveContext = WeaveContext()
    private var currentTypeLevel = 0


    override fun visit(node: ParseAst): Expression = node.accept(this)

    override fun visitUnit(unitValue: UnitValue): Expression = Atom.unit(unitValue.section)

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
        Cast(parseInterpolation.value.accept(this), targetType = Type.string, parseInterpolation.section)


    override fun visitInterpolatedStringText(stringText: StringText): Expression =
        Atom.string(stringText.text, stringText.section)

    override fun visitLambda(parseLambda: ParseLambda): Expression {
        val fnSymbolTable = FnSymbolTable(currentFnSymbolTable)
        val defaultArgs = mutableMapOf<String, Expression>()
        val (params, body) = withFnSymbolTable(fnSymbolTable) {
            val params = parseLambda.formalArguments.map { argument ->
                val type = argument.typeRef?.let { resolveType(typeTable, currentTypeSchemeVariables, it, currentTypeLevel) }
                argument.defaultValue?.let { defaultArgs.put(argument.name, visit(it)) }
                fnSymbolTable.addArgument(argument.name, type)
                FnParam(argument.name, type, argument.section)
            }

            val body = withFnSymbolTable(fnSymbolTable) {
                parseLambda.body.map { it.accept(this) }
            }

            params to body
        }

        val type = Function(
            params.mapIndexed { index, param ->
                param.type ?: Variable(UUID.randomUUID().toString(), 1)
            } + Variable(UUID.randomUUID().toString(), 1),
            defaultArgs = defaultArgs.size
        )

        return Fn(
            parameters = params,
            defaultValues = defaultArgs,
            body = Block(body, parseLambda.section),
            sourceSection = parseLambda.section
        ).also {
            it.type = type
        }
    }

    override fun visitFuncWithName(parseFuncWithName: ParseFuncWithName): Expression {
        val fnSymbolTable = FnSymbolTable(currentFnSymbolTable)

        val prevTypeSchemeVariables = currentTypeSchemeVariables
        currentTypeSchemeVariables = parseFuncWithName.typeParameters.map { it.name }

        val defaultArgs = mutableMapOf<String, Expression>()

        val (params, body) = withFnSymbolTable(fnSymbolTable) {
            val params = parseFuncWithName.formalArguments.map { argument ->
                val type = resolveType(typeTable, currentTypeSchemeVariables, argument.typeRef!!, currentTypeLevel)
                argument.defaultValue?.let { defaultArgs.put(argument.name, visit(it)) }
                fnSymbolTable.addArgument(argument.name, type)
                FnParam(argument.name, type, argument.section)
            }
            currentTypeLevel += 1
            val body = parseFuncWithName.body.accept(this) as Block
            currentTypeLevel -= 1
            params to body
        }

        val function = Fn(
            parameters = params,
            defaultValues = defaultArgs,
            body = body,
            parseFuncWithName.body.section
        )

        val returnType = parseFuncWithName.returnTypeRef
            ?.let { resolveType(typeTable, currentTypeSchemeVariables, it, currentTypeLevel) }
            ?: Type.unit

        val funcTypes = params.map { it.type!! } + returnType

        val funcType = Function(types = funcTypes, defaultArgs = defaultArgs.size)

        currentTypeSchemeVariables = prevTypeSchemeVariables

        return NameDeclaration(
            public = parseFuncWithName.public,
            name = parseFuncWithName.name,
            value = function,
            mutable = false,
            expectedType = funcType,
            sourceSection = parseFuncWithName.section
        ).also {
            addLocalSymbol(it.name, it.mutable, it.public, funcType)
        }
    }

    override fun visitFnCall(parseFnCall: ParseFnCall): Expression {
        val arguments = parseFnCall.arguments.map { it.accept(this) }.toMutableList()
        val symbol = tables.getLocalSymbol(parseFnCall.name)
        DefaultArguments.fill(arguments, symbol)
        return FnCall(
            parseFnCall.function.accept(this),
            arguments,
            parseFnCall.section
        )
    }

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

    override fun visitNameDeclaration(parseNameDeclaration: ParseNameDeclaration): Expression {
        val value = parseNameDeclaration.value.accept(this)
        currentTypeLevel += 1
        return NameDeclaration(
            public = parseNameDeclaration.public,
            mutable = parseNameDeclaration.mutable,
            name = parseNameDeclaration.symbol.name,
            value = value,
            sourceSection = parseNameDeclaration.section,
            expectedType = parseNameDeclaration.typeRef?.let { resolveType(typeTable, currentTypeSchemeVariables, it, currentTypeLevel) }
        ).also {
            val type = if (value is Fn) value.type else null
            addLocalSymbol(it.name, it.mutable, it.public, type)
            currentTypeLevel -= 1
        }
    }

    override fun visitFieldAccess(parseFieldAccess: ParseFieldAccess): Expression {
        val pkg = tables.getAliasedPackage(parseFieldAccess.receiverName)
        val pkgSymbol = pkg?.let {
            tables.ns.getSymbol(it.moduleName, it.packageName, parseFieldAccess.memberName)
        }
        if (pkg != null && pkgSymbol != null) {
            return VariableAccess(
                PackageSymbol(pkgSymbol.moduleName, pkgSymbol.packageName, parseFieldAccess.memberName),
                parseFieldAccess.receiver.section)
        }

        return FieldAccess(
            receiver = parseFieldAccess.receiver.accept(this),
            fieldName = parseFieldAccess.memberName,
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

    override fun visitEffectDefinition(parseEffectDefinition: ParseEffectDefinition): Expression {
        val prevTypeSchemeVariables = currentTypeSchemeVariables
        currentTypeSchemeVariables = parseEffectDefinition.typeParameters.map { it.name }

        val params = parseEffectDefinition.formalArguments.map {
            FnParam(
                it.name,
                resolveType(typeTable, currentTypeSchemeVariables, it.typeRef!!, currentTypeLevel),
                it.section
            )
        }



        val types = params.map { it.type!! } + resolveType(typeTable, currentTypeSchemeVariables, parseEffectDefinition.returnTypeRef, currentTypeLevel)
        val type = Function(types)

        currentTypeSchemeVariables = prevTypeSchemeVariables

        return EffectDefinition(
            moduleName = pkg.moduleName,
            packageName = pkg.packageName,
            name = parseEffectDefinition.name,
            public = parseEffectDefinition.public,
            parameters = params,
            sourceSection = parseEffectDefinition.section
        ).also {
            it.type = type
            addLocalSymbol(it.name, isMutable = false, it.public, type = type)
        }
    }

    override fun visitHandle(parseHandle: ParseHandle): Expression =
        Handle(
            body = parseHandle.body.accept(this) as Block,
            cases = parseHandle.cases.map {
                val virtualSymbolTable = FnSymbolTable(currentFnSymbolTable)
                withFnSymbolTable(virtualSymbolTable) {
                    val info = getSymbol(it.effectName, parseHandle.section) as PackageSymbol
                    addLocalSymbol("resume", isMutable = false, isPublic = true)
                    it.argumentNames.forEach {
                        addLocalSymbol(it, isMutable = false, isPublic = true)
                    }

                    HandleCase(
                        moduleName = info.moduleName,
                        packageName = info.packageName,
                        effectName = it.effectName,
                        argumentNames = it.argumentNames,
                        body = it.body.accept(this),
                        sourceSection = it.section
                    ).also {
                        removeLocalSymbol("resume")
                        it.argumentNames.forEach {
                            removeLocalSymbol(it)
                        }
                    }
                }
            },
            sourceSection = parseHandle.section
        )

    override fun visitNot(parseNot: ParseNot): Expression =
        PrefixOp("!", parseNot.value.accept(this), parseNot.section)

    override fun visitBinaryOperator(parseBinaryOp: ParseBinaryOp): Expression =
        InfixOp(parseBinaryOp.op, parseBinaryOp.left.accept(this), parseBinaryOp.right.accept(this), parseBinaryOp.section)

    override fun visitBlock(parseBlock: ParseBlock): Expression {
        currentTypeLevel += 1
        return Block(
            body = parseBlock.body.map {
                it.accept(this)
            },
            sourceSection = parseBlock.section
        ).also {
            currentTypeLevel -= 1
        }
    }

    override fun visitCast(parseCast: ParseCast): Expression {
        val targetType = resolveType(typeTable, currentTypeSchemeVariables, parseCast.typeRef, currentTypeLevel)
        return Cast(parseCast.value.accept(this), targetType, parseCast.section)
    }

    override fun visitGroup(parseGroup: ParseGroup): Expression =
        parseGroup.value.accept(this)

    override fun visitIs(parseIs: ParseIs): Expression =
        Is(parseIs.value.accept(this), resolveType(typeTable, currentTypeSchemeVariables, parseIs.typeRef, currentTypeLevel), parseIs.section)

    override fun visitIfElse(parseIfElse: ParseIfElse): Expression =
        IfElse(
            condition = parseIfElse.condition.accept(this),
            thenBranch = parseIfElse.thenBody.accept(this),
            elseBranch = parseIfElse.elseBody?.accept(this),
            sourceSection = parseIfElse.section
        )

    override fun visitWhile(parseWhile: ParseWhile): Expression =
        WhileLoop(
            condition = parseWhile.condition.accept(this),
            loop = parseWhile.body.accept(this),
            sourceSection = parseWhile.section
        )

    override fun visitBreak(parseBreak: ParseBreak): Expression = Break(parseBreak.section)

    override fun visitContinue(parseContinue: ParseContinue): Expression = Continue(parseContinue.section)

    override fun visitWhen(parseWhen: ParseWhen): Expression {
        if (parseWhen.cases.isEmpty() && parseWhen.elseCase == null) {
            return Atom.unit(parseWhen.section)
        }
        if (parseWhen.cases.isEmpty() && parseWhen.elseCase != null) {
            return parseWhen.elseCase.body.accept(this)
        }
        val lastCase = parseWhen.cases.last()
        val last = if (parseWhen.elseCase != null) {
            IfElse(lastCase.condition.accept(this), lastCase.body.accept(this), parseWhen.elseCase.body.accept(this), lastCase.section)
        } else {
            IfElse(lastCase.condition.accept(this), lastCase.body.accept(this), null, lastCase.section)
        }
        return parseWhen.cases.dropLast(1).foldRight(last) { case, prev ->
            IfElse(case.condition.accept(this), case.body.accept(this), prev, case.section)
        }
    }

    override fun visitWeave(parseWeave: ParseWeave): Expression {
        val inputValue = parseWeave.value.accept(this)
        val tempVarName = tempVarGenerator.nextName()
        val tempVariableDeclaration = NameDeclaration(
            public = false,
            name = tempVarName,
            value = inputValue,
            mutable = false,
            expectedType = null,
            sourceSection = parseWeave.value.section
        ).also {
            addLocalSymbol(it.name, it.mutable, it.public)
        }

        val readVariable = VariableAccess(
            target = LocalSymbol(tempVariableDeclaration.name),
            sourceSection = parseWeave.value.section
        )

        val filledTemplate = weaveContext.withWeaveInput(readVariable) {
            parseWeave.opTemplate.accept(this)
        }

        return Block(
            listOf(tempVariableDeclaration, filledTemplate),
            parseWeave.section
        )
    }

    override fun visitPlaceholder(parseWeavePlaceholder: ParseWeavePlaceholder): Expression =
        weaveContext.currentInput()

    override fun visitReturn(parseReturn: ParseReturn): Expression =
        Return(parseReturn.value?.accept(this), parseReturn.section)

    override fun visitCreateRecord(parseCreateRecord: ParseCreateRecord): Expression =
        CreateRecord(
            parseCreateRecord.fields.map {
                CreateRecord.Field(it.name, it.value.accept(this))
            },
            parseCreateRecord.section
        )

    override fun visitCreateArray(parseCreateArray: ParseCreateArray): Expression =
        CreateArray(
            parseCreateArray.values.map { it.accept(this) },
            parseCreateArray.section
        )

    override fun visitFor(parseFor: ParseFor): Expression {
        val  fnSymbolTable = FnSymbolTable(currentFnSymbolTable)
        parseFor.vars.forEach {
            fnSymbolTable.addLocal(it, null, false)
        }
        return ForLoop(
            vars = parseFor.vars,
            iterable = visit(parseFor.iterable),
            state = parseFor.state?.let(::visit),
            init = parseFor.init?.let(::visit),
            withFnSymbolTable(fnSymbolTable) { visitBlock(parseFor.body) as Block },
            varSections = parseFor.varSections,
            iterableSection = parseFor.iterableSection,
            stateSection = parseFor.stateSection,
            initSection = parseFor.initSection,
            bodySection = parseFor.bodySection,
            parseFor.section
        )
    }

    private fun <T> withFnSymbolTable(fnSymbolTable: FnSymbolTable, f: () -> T): T {
        val prevFnSymbolTable = currentFnSymbolTable
        currentFnSymbolTable = fnSymbolTable
        return f().also {
            currentFnSymbolTable = prevFnSymbolTable
        }
    }

    private fun getSymbol(name: String, sourceSection: ChiSource.Section?): Target {
        val fnSymbolTable = currentFnSymbolTable
        return fnSymbolTable?.get(name)?.toLocalSymbol()
            ?: tables.getLocalSymbol(name)?.toPackageSymbol()
            ?: throw ExprConversionException("Tried to get local symbol '$name'", sourceSection)
    }

    private fun FnSymbol.toLocalSymbol() = LocalSymbol(name)
    private fun Symbol.toPackageSymbol() = PackageSymbol(moduleName, packageName, name)

    private fun addLocalSymbol(name: String, isMutable: Boolean, isPublic: Boolean, type: Type? = null) {
        val fnSymbolTable = currentFnSymbolTable
        if (fnSymbolTable != null) {
            // we are inside a function so we declare simple local
            fnSymbolTable.addLocal(name, null, isMutable)
        } else {
            tables.defineSymbol(
                Symbol(
                    moduleName = pkg.moduleName,
                    packageName = pkg.packageName,
                    name = name,
                    type = type,
                    isPublic,
                    isMutable,
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