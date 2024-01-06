package gh.marad.chi.core.namespace

import gh.marad.chi.core.OldType
import gh.marad.chi.core.VariantType
import gh.marad.chi.core.parser.readers.ParseVariantTypeDefinition
import gh.marad.chi.core.parser.readers.TypeRef

class TypeRegistry {
    private val types: MutableMap<String, OldType> = mutableMapOf(
        "any" to OldType.any,
        "int" to OldType.int,
        "float" to OldType.floatType,
        "unit" to OldType.unit,
        "string" to OldType.string,
        "bool" to OldType.bool,
        "array" to OldType.array(OldType.typeParameter("T"))
    )
    private val variants = mutableMapOf<String, List<VariantType.Variant>>()
    private val typeByVariantName = mutableMapOf<String, VariantType>()

    fun getTypeOrNull(name: String): OldType? = types[name]

    fun getTypeVariants(variantName: String): List<VariantType.Variant>? = variants[variantName]

    fun getTypeByVariantName(variantName: String): VariantType? = typeByVariantName[variantName]?.let {
        it.withVariant(variants[it.simpleName]?.find { variant -> variant.variantName == variantName })
    }

    @Suppress("unused")
    fun defineVariantType(variantType: VariantType, variants: List<VariantType.Variant>) {
        types[variantType.simpleName] = variantType
        this.variants[variantType.simpleName] = variants
        variants.forEach {
            typeByVariantName[it.variantName] = variantType
        }
        variantType.variant = variants.singleOrNull()
    }

    fun defineTypes(
        moduleName: String,
        packageName: String,
        typeDefs: List<ParseVariantTypeDefinition>,
        resolveTypeRef: (TypeRef, typeParameterNames: Set<String>) -> OldType
    ) {
        typeDefs.forEach { addVariantType(moduleName, packageName, it) }
        typeDefs.forEach { addVariantConstructors(it, resolveTypeRef) }
    }

    private fun addVariantType(moduleName: String, packageName: String, typeDefinition: ParseVariantTypeDefinition) {
        types[typeDefinition.typeName] = VariantType(
            moduleName,
            packageName,
            typeDefinition.typeName,
            typeDefinition.typeParameters.map { typeParam -> OldType.typeParameter(typeParam.name) },
            emptyMap(),
            null,
        )
    }

    private fun addVariantConstructors(
        typeDefinition: ParseVariantTypeDefinition,
        resolveTypeRef: (TypeRef, typeParameterNames: Set<String>) -> OldType
    ) {
        val baseType = (types[typeDefinition.typeName]
            ?: TODO("Type ${typeDefinition.typeName} is not defined here!")) as VariantType
        val variantTypeParameters = baseType.genericTypeParameters.map { it.name }.toSet()
        val variants = typeDefinition.variantConstructors.map {
            VariantType.Variant(
                public = it.public,
                variantName = it.name,
                fields = it.formalFields.map { arg ->
                    VariantType.VariantField(arg.public, arg.name, resolveTypeRef(arg.typeRef, variantTypeParameters))
                }
            )
        }

        this.variants[typeDefinition.typeName] = variants
        variants.forEach { typeByVariantName[it.variantName] = baseType }
        baseType.variant = variants.singleOrNull()
    }
}

