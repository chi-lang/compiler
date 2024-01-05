package gh.marad.chi.core.analyzer

import gh.marad.chi.core.*
import gh.marad.chi.core.parser.ChiSource
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

fun checkModuleAndPackageNames(pkg: Package, messages: MutableList<Message>) {
    if (pkg.moduleName.isEmpty()) {
        messages.add(InvalidModuleName(pkg.moduleName, pkg.sourceSection.toCodePoint()))
    }
    if (pkg.packageName.isEmpty()) {
        messages.add(InvalidPackageName(pkg.packageName, pkg.sourceSection.toCodePoint()))
    }
}

fun checkImports(import: Import, messages: MutableList<Message>) {
    if (import.moduleName.isEmpty()) {
        messages.add(InvalidModuleName(import.moduleName, import.sourceSection.toCodePoint()))
    }
    if (import.packageName.isEmpty()) {
        messages.add(InvalidPackageName(import.packageName, import.sourceSection.toCodePoint()))
    }
    if (!import.withinSameModule) {
        import.entries.forEach {
            if (it.isPublic == false && !it.isTypeImport) {
                messages.add(ImportInternal(it.name, it.sourceSection.toCodePoint()))
            }
        }
    }
}

fun checkThatTypesContainAccessedFieldsAndFieldIsAccessible(expr: Expression, messages: MutableList<Message>) {
    if (expr is FieldAccess && expr.receiver.type.isCompositeType()) {
        val type = expr.receiver.type as CompositeType
        val hasMember = type.hasMember(expr.fieldName)
        if (!hasMember) {
            messages.add(MemberDoesNotExist(expr.receiver.type, expr.fieldName, expr.memberSection.toCodePoint()))
        }

        if (!expr.typeIsModuleLocal && !type.isPublic(expr.fieldName)) {
            messages.add(CannotAccessInternalName(expr.fieldName, expr.memberSection.toCodePoint()))
        }
    }
}

fun checkThatVariableIsDefinedAndAccessible(expr: Expression, messages: MutableList<Message>) {
    if (expr is VariableAccess) {
        val symbolInfo = expr.definitionScope.getSymbol(expr.name)
        if (symbolInfo != null) {
            if (!expr.isModuleLocal && !symbolInfo.public) {
                messages.add(CannotAccessInternalName(expr.name, expr.sourceSection.toCodePoint()))
            }
        } else {
            messages.add(UnrecognizedName(expr.name, expr.sourceSection.toCodePoint()))
        }
    }
}

fun checkThatAssignmentDoesNotChangeImmutableValue(expr: Expression, messages: MutableList<Message>) {
    if (expr is Assignment) {
        val symbol = expr.definitionScope.getSymbol(expr.name)
        if (symbol?.mutable == false) {
            messages.add(CannotChangeImmutableVariable(expr.sourceSection.toCodePoint()))
        }
    }
}


fun checkThatFunctionHasAReturnValue(expr: Expression, messages: MutableList<Message>) {
    if (expr is Fn) {
        val expected = expr.returnType
        if (expr.body.body.isEmpty() && expected != OldType.unit) {
            messages.add(MissingReturnValue(expected, expr.body.sourceSection.toCodePoint()))
        }
    }
}

fun checkThatFunctionCallsReceiveAppropriateCountOfArguments(expr: Expression, messages: MutableList<Message>) {
    if (expr is FnCall) {
        val valueType = expr.function.type

        if (valueType is FnType &&
            valueType.paramTypes.count() != expr.parameters.count()
        ) {
            messages.add(
                FunctionArityError(
                    valueType.paramTypes.count(),
                    expr.parameters.count(),
                    expr.sourceSection.toCodePoint()
                )
            )
        }
    }
}

fun checkForOverloadedFunctionCallCandidate(expr: Expression, messages: MutableList<Message>) {
    if (expr is FnCall) {
        val valueType = expr.function.type

        if (valueType is OverloadedFnType) {
            val argumentTypes = expr.parameters.map { it.type }
            if (valueType.getType(argumentTypes) == null) {
                messages.add(
                    NoCandidatesForFunction(
                        argumentTypes,
                        valueType.types.map { it.fnType }.toSet(),
                        expr.sourceSection.toCodePoint()
                    )
                )
            }
        }
    }
}

fun checkThatFunctionCallsActuallyCallFunctions(expr: Expression, messages: MutableList<Message>) {
    if (expr is FnCall) {
        val valueType = expr.function.type

        if (valueType !is FnType && valueType !is OverloadedFnType) {
            messages.add(NotAFunction(expr.sourceSection.toCodePoint()))
        }
    }
}

fun checkGenericTypes(expr: Expression, messages: MutableList<Message>) {
    if (expr is FnCall && expr.function.type is FnType && expr.callTypeParameters.isNotEmpty()) {
        val fnType = expr.function.type as FnType
        // check that all generic type parameters were passed
        if (fnType.genericTypeParameters.size != expr.callTypeParameters.size) {
            messages.add(
                GenericTypeArityError(
                    fnType.genericTypeParameters.size,
                    expr.callTypeParameters.size,
                    expr.function.sourceSection.toCodePoint()
                )
            )
        }

        // check that parameters passed to the function have the same type that is declared in generic type parameters
        val typeParameterNameToParamIndex =
            fnType.paramTypes.foldIndexed(mutableListOf<Pair<String, Int>>()) { paramIndex, acc, type ->
                if (type is GenericTypeParameter) {
                    acc.add(type.typeParameterName to paramIndex)
                }
                acc
            }.toMap()

        fnType.genericTypeParameters.forEachIndexed { genericTypeParameterIndex, genericTypeParameter ->
            val genericParamIndex = typeParameterNameToParamIndex[genericTypeParameter.typeParameterName]
            val genericParam = genericParamIndex?.let { expr.parameters[genericParamIndex] }
                ?: return@forEachIndexed
            val expectedType = expr.callTypeParameters[genericTypeParameterIndex]
            val actualType = genericParam.type
            if (!typesMatch(expectedType, actualType)) {
                messages.add(
                    TypeMismatch(
                        expectedType,
                        actualType,
                        expr.parameters[genericParamIndex].sourceSection.toCodePoint()
                    )
                )
            }
        }
    }
}

fun typesMatch(
    expected: OldType,
    actual: OldType,
): Boolean {
    if (expected == OldType.any) {
        // accept any type
        return true
    }
    return expected == actual || isSubType(actual, expected) || matchStructurally(
        expected,
        actual
    )
}

fun matchStructurally(expected: OldType, actual: OldType): Boolean {
    val expectedSubtypes = expected.getAllSubtypes()
    val actualSubtypes = actual.getAllSubtypes()
    return expected.javaClass == actual.javaClass &&
            expectedSubtypes.size == actualSubtypes.size &&
            expectedSubtypes.zip(actualSubtypes)
                .all { typesMatch(it.first, it.second) }
}


fun checkTypes(expr: Expression, messages: MutableList<Message>) {

    fun checkTypeMatches(
        expected: OldType,
        actual: OldType,
        sourceSection: ChiSource.Section?,
    ) {
        if (!typesMatch(expected, actual)) {
            messages.add(TypeMismatch(expected, actual, sourceSection.toCodePoint()))
        }
    }

    fun checkPrefixOp(op: PrefixOp) {
        when (op.op) {
            "!" -> if (op.expr.type != OldType.bool) {
                messages.add(TypeMismatch(OldType.bool, op.expr.type, op.sourceSection.toCodePoint()))
            }
            else -> TODO("Unimplemented prefix operator")
        }
    }

    fun checkAssignment(expr: Assignment) {
        val scope = expr.definitionScope

        val expectedType = scope.getSymbolType(expr.name)

        if (expectedType != null) {
            checkTypeMatches(expectedType, expr.value.type, expr.sourceSection)
        }
    }

    fun checkNameDeclaration(expr: NameDeclaration) {
        if (expr.expectedType != null) {
            checkTypeMatches(expr.expectedType, expr.value.type, expr.value.sourceSection)
        }
    }

    fun checkFn(expr: Fn) {
        val expected = expr.returnType

        forEachAst(expr) {
            if (it is Return) {
                checkTypeMatches(expected, it.type, it.sourceSection)
            }
        }

        if (expected == OldType.unit) {
            return
        }

        if (expr.body.body.isNotEmpty()) {
            val actual = expr.body.type
            val sourceSection = expr.body.body.last().sourceSection
            checkTypeMatches(expected, actual, sourceSection)
        }
    }

    fun checkFnCall(expr: FnCall) {
        val fnType = expr.function.type

        if (fnType is FnType) {
            val genericParamToTypeFromPassedParameters =
                matchCallTypes(
                    fnType.paramTypes,
                    expr.parameters.map { it.type })
            fnType.paramTypes.zip(expr.parameters) { definition, passed ->
                val expectedType = definition.construct(genericParamToTypeFromPassedParameters)
                val actualType = passed.type
                if (expectedType is FnType && actualType is FnType
                    && expectedType.returnType == OldType.unit
                    && actualType.paramTypes.size == expectedType.paramTypes.size
                ) {
                    // if types are FnType and expected FnType returns unit - then check only arguments - return value doesn't matter
                    val allArgumentsMatch =
                        expectedType.paramTypes.zip(actualType.paramTypes).all { (expected, actual) ->
                            typesMatch(expected, actual)
                        }
                    if (!allArgumentsMatch) {
                        messages.add(TypeMismatch(expectedType, actualType, passed.sourceSection.toCodePoint()))
                    }
                } else {
                    checkTypeMatches(
                        expectedType,
                        actualType,
                        passed.sourceSection
                    )
                }
            }

            if (expr.callTypeParameters.isNotEmpty()) {
                val genericParamToTypeFromDefinedParameters =
                    matchTypeParameters(fnType.genericTypeParameters, expr.callTypeParameters)
                fnType.genericTypeParameters.forEach { param ->
                    val expected = genericParamToTypeFromDefinedParameters[param]!!
                    val actual = genericParamToTypeFromPassedParameters[param]
                    if (actual != null && !typesMatch(expected, actual)) {
                        messages.add(GenericTypeMismatch(expected, actual, param, expr.sourceSection.toCodePoint()))
                    }
                }
            }
        }
    }

    fun checkFieldAssignment(expr: FieldAssignment) {
        val memberType = (expr.receiver.type as CompositeType).memberType(expr.fieldName)!!
        val assignedType = expr.value.type
        checkTypeMatches(expected = memberType, actual = assignedType, expr.value.sourceSection)
    }

    fun checkIfElseType(expr: IfElse) {
        val conditionType = expr.condition.type
        if (conditionType != OldType.bool) {
            messages.add(TypeMismatch(OldType.bool, conditionType, expr.condition.sourceSection.toCodePoint()))
        }
    }

    fun checkInfixOp(expr: InfixOp) {
        val leftType = expr.left.type
        val rightType = expr.right.type

        if (leftType != rightType) {
            messages.add(TypeMismatch(expected = leftType, rightType, expr.right.sourceSection.toCodePoint()))
        } else if (expr.op in arrayOf("|", "&", "<<", ">>") && !leftType.isNumber()) {
            messages.add(TypeMismatch(expected = OldType.intType, leftType, expr.left.sourceSection.toCodePoint()))
        } else if (expr.op in arrayOf("|", "&", "<<", ">>") && !rightType.isNumber()) {
            messages.add(TypeMismatch(expected = OldType.intType, rightType, expr.right.sourceSection.toCodePoint()))
        }
    }

    fun checkCast(expr: Cast) {
    }

    fun checkWhileLoop(expr: WhileLoop) {
        checkTypeMatches(OldType.bool, expr.condition.type, expr.sourceSection)
    }

    fun checkIndexOperator(expr: IndexOperator) {
        if (expr.variable.type.isIndexable()) {
            checkTypeMatches(expr.variable.type.expectedIndexType(), expr.index.type, expr.index.sourceSection)
        } else {
            messages.add(TypeIsNotIndexable(expr.variable.type, expr.variable.sourceSection.toCodePoint()))
        }
    }

    fun checkIndexedAssignment(expr: IndexedAssignment) {
        if (expr.variable.type.isIndexable()) {
            checkTypeMatches(expr.variable.type.expectedIndexType(), expr.index.type, expr.index.sourceSection)
            checkTypeMatches(expr.variable.type.indexedElementType(), expr.value.type, expr.value.sourceSection)
        } else {
            messages.add(TypeIsNotIndexable(expr.variable.type, expr.variable.sourceSection.toCodePoint()))
        }
    }

    fun checkIs(expr: Is) {
    }

    @Suppress("UNUSED_VARIABLE")
    val ignored: Any = when (expr) {
        is DefineVariantType -> {} // nothing to check
        is Assignment -> checkAssignment(expr)
        is NameDeclaration -> checkNameDeclaration(expr)
        is Block -> {} // nothing to check
        is Fn -> checkFn(expr)
        is FnCall -> checkFnCall(expr)
        is Atom -> {} // nothing to check
        is VariableAccess -> {} // nothing to check
        is FieldAccess -> {} // nothing to check
        is FieldAssignment -> checkFieldAssignment(expr)
        is IfElse -> checkIfElseType(expr)
        is InfixOp -> checkInfixOp(expr)
        is PrefixOp -> checkPrefixOp(expr)
        is Cast -> checkCast(expr)
        is Group -> {} // nothing to check
        is WhileLoop -> checkWhileLoop(expr)
        is IndexOperator -> checkIndexOperator(expr)
        is IndexedAssignment -> checkIndexedAssignment(expr)
        is Is -> checkIs(expr)
        is Break -> {} // nothing to check
        is Continue -> {} // nothing to check
        is EffectDefinition -> {} // TODO: maybe something?
        is Handle -> {} // TODO: check that effect branches return the same type that handle expects
        is InterpolatedString -> {} // nothing to check
        is Return -> {}
    }
}


private var typeGraph: Graph<String, DefaultEdge> =
    DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java).also {
        it.addVertex("unit")
        it.addVertex("int")
        it.addVertex("float")

        it.addEdge("int", "float")
    }

fun isSubType(subtype: OldType, supertype: OldType): Boolean {
    return if (subtype != supertype && typeGraph.containsVertex(subtype.name) && typeGraph.containsVertex(supertype.name)) {
        val dijkstraAlgo = DijkstraShortestPath(typeGraph)
        val path = dijkstraAlgo.getPath(subtype.name, supertype.name)
        path != null
    } else {
        false
    }
}

