package gh.marad.chi.core

fun resolveGenericType(
    fnType: FnType,
    callTypeParameters: List<OldType>,
    callParameters: List<Expression>,
): OldType {
    val typeByParameterName = typesByTypeParameterName(fnType, callTypeParameters, callParameters)
    return if (fnType.returnType.isTypeConstructor()) {
        fnType.returnType.construct(typeByParameterName)
    } else {
        fnType.returnType
    }
}


fun typesByTypeParameterName(
    fnType: FnType,
    callTypeParameters: List<OldType>,
    callParameters: List<Expression>
): Map<GenericTypeParameter, OldType> {
    val namesFromTypeParameters = matchTypeParameters(fnType.genericTypeParameters, callTypeParameters)
    val namesFromCallParameters = matchCallTypes(fnType.paramTypes, callParameters.map { it.type })
    val result = mutableMapOf<GenericTypeParameter, OldType>()
    result.putAll(namesFromCallParameters)
    result.putAll(namesFromTypeParameters)
    return result
}

fun matchTypeParameters(
    definedTypeParameters: List<GenericTypeParameter>,
    callTypeParameters: List<OldType>
): Map<GenericTypeParameter, OldType> {
    return definedTypeParameters.zip(callTypeParameters).toMap()
}

fun matchCallTypes(definedParameters: List<OldType>, callParameters: List<OldType>): Map<GenericTypeParameter, OldType> {
    val result = mutableMapOf<GenericTypeParameter, OldType>()
    definedParameters.zip(callParameters)
        .forEach { (definedParam, callParam) ->
            result.putAll(matchCallTypes(definedParam, callParam))
        }
    return result
}

fun matchCallTypes(definedParam: OldType, callParam: OldType): Map<GenericTypeParameter, OldType> {
    return if (definedParam is GenericTypeParameter) {
        mapOf(definedParam to callParam)
    } else if (definedParam.isTypeConstructor()) {
        matchCallTypes(
            definedParameters = definedParam.getAllSubtypes(),
            callParameters = callParam.getAllSubtypes()
        )
    } else emptyMap()
}
