package gh.marad.chi.core

fun resolveGenericType(
    fnType: FnType,
    callTypeParameters: List<Type>,
    callParameters: List<Expression>,
): Type {
    val typeByParameterName = typesByTypeParameterName(fnType, callTypeParameters, callParameters)
    return if (fnType.returnType.isTypeConstructor()) {
        fnType.returnType.construct(typeByParameterName)
    } else {
        fnType.returnType
    }
}


fun typesByTypeParameterName(
    fnType: FnType,
    callTypeParameters: List<Type>,
    callParameters: List<Expression>
): Map<GenericTypeParameter, Type> {
    val namesFromTypeParameters = matchTypeParameters(fnType.genericTypeParameters, callTypeParameters)
    val namesFromCallParameters = matchCallTypes(fnType.paramTypes, callParameters.map { it.type })
    val result = mutableMapOf<GenericTypeParameter, Type>()
    result.putAll(namesFromCallParameters)
    result.putAll(namesFromTypeParameters)
    return result
}

fun matchTypeParameters(
    definedTypeParameters: List<GenericTypeParameter>,
    callTypeParameters: List<Type>
): Map<GenericTypeParameter, Type> {
    return definedTypeParameters.zip(callTypeParameters).toMap()
}

fun matchCallTypes(definedParameters: List<Type>, callParameters: List<Type>): Map<GenericTypeParameter, Type> {
    val result = mutableMapOf<GenericTypeParameter, Type>()
    definedParameters.zip(callParameters)
        .forEach { (definedParam, callParam) ->
            result.putAll(matchCallTypes(definedParam, callParam))
        }
    return result
}

fun matchCallTypes(definedParam: Type, callParam: Type): Map<GenericTypeParameter, Type> {
    return if (definedParam is GenericTypeParameter) {
        mapOf(definedParam to callParam)
    } else if (definedParam.isTypeConstructor()) {
        matchCallTypes(
            definedParameters = definedParam.getAllSubtypes(),
            callParameters = callParam.getAllSubtypes()
        )
    } else emptyMap()
}
