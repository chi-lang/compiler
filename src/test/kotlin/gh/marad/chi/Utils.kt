package gh.marad.chi

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Program
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.expressionast.internal.defaultModule
import gh.marad.chi.core.expressionast.internal.defaultPackage
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.namespace.TypeInfo
import gh.marad.chi.core.namespace.VariantField
import gh.marad.chi.core.types.*

data class ErrorMessagesException(val errors: List<Message>) : AssertionError("Chi compilation errors")

fun compile(
    code: String,
    namespace: GlobalCompilationNamespace = GlobalCompilationNamespace(),
    ignoreCompilationErrors: Boolean = false
): Program {
    val result = Compiler.compile(code, namespace)
    val program = result.program
    val messages = result.messages

    if (!ignoreCompilationErrors) {
        messages.forEach { msg ->
            System.err.println(Compiler.formatCompilationMessage(code, msg))
            System.err.flush()
        }

        if (messages.any { it.level == Level.ERROR }) {
            throw ErrorMessagesException(messages)
        }
    }

    return program
}

fun asts(code: String, ns: GlobalCompilationNamespace, ignoreCompilationErrors: Boolean = false): List<Expression> {
    return compile(code, ns, ignoreCompilationErrors).expressions
}

fun messages(code: String, ns: GlobalCompilationNamespace = GlobalCompilationNamespace()): List<Message> {
    val result = Compiler.compile(code, ns)
    return result.messages
}


fun ast(
    code: String,
    ns: GlobalCompilationNamespace = GlobalCompilationNamespace(),
    ignoreCompilationErrors: Boolean = false
): Expression = asts(code, ns, ignoreCompilationErrors).last()

fun GlobalCompilationNamespace.addSymbolInDefaultPackage(name: String, type: Type? = null, public: Boolean = false,
                                                         mutable: Boolean = false, slot: Int = 0) {
    val pkg = getDefaultPackage()
    pkg.symbols.add(
        Symbol(
            pkg.moduleName, pkg.packageName,
            name,
            type,
            public = public,
            mutable = mutable
        )
    )
}

fun GlobalCompilationNamespace.addSymbol(moduleName: String, packageName: String, name: String, type: Type? = null,
                                         public: Boolean = false, mutable: Boolean = false, slot: Int = 0) {
    val pkg = getOrCreatePackage(moduleName, packageName)
    pkg.symbols.add(
        Symbol(
            moduleName, packageName,
            name,
            type,
            public = public,
            mutable = mutable
        )
    )
}

fun GlobalCompilationNamespace.addProductTypeInDefaultNamespace(name: String, fields: List<VariantField>, public: Boolean = true) =
    addProductType(defaultModule.name, defaultPackage.name, name, fields, public)

fun GlobalCompilationNamespace.addProductType(
    moduleName: String,
    packageName: String,
    typeName: String,
    fields: List<VariantField>,
    public: Boolean = true
): ProductType {
    val type = ProductType(
        moduleName, packageName, typeName,
        types = fields.map { it.type },
        typeParams = emptyList(),
        typeSchemeVariables = emptyList())
    val constructorType = FunctionType(
        types = fields.map { it.type } + type,
        typeSchemeVariables = emptyList()
    )
    val typeInfo = TypeInfo(
        moduleName, packageName, typeName,
        type,
        isPublic = public,
        isVariantConstructor = true,
        fields = fields
    )

    addSymbol(moduleName, packageName, typeName, constructorType, public = public)
    val pkg = getOrCreatePackage(moduleName, packageName)
    pkg.types.add(typeInfo)
    return type
}

fun GlobalCompilationNamespace.addSumTypeInDefaultPackage(name: String, subTypes: List<String>, public: Boolean = true) =
    addSumType(defaultModule.name, defaultPackage.name, name, subTypes, public)

fun GlobalCompilationNamespace.addSumType(
    moduleName: String,
    packageName: String,
    name: String,
    subTypes: List<String>,
    public: Boolean = true
): SumType {
    val type = SumType(
        moduleName, packageName, name,
        subtypes = subTypes,
        typeParams = emptyList(),
        typeSchemeVariables = emptyList()
        )
    val typeInfo = TypeInfo(
        moduleName,
        packageName,
        name,
        type,
        public,
        isVariantConstructor = false,
        fields = emptyList())

    val pkg = getOrCreatePackage(moduleName, packageName)

    pkg.types.add(typeInfo)
    return type
}



fun GlobalCompilationNamespace.addType(
    moduleName: String,
    packageName: String,
    typeName: String,
    fields: List<VariantField>,
    public: Boolean = true,
) {
    val pkg = getOrCreatePackage(moduleName, packageName)
    val type = SimpleType(moduleName, packageName, typeName)
    val constructorType = Types.fn(Types.int, Types.float, type)
    addSymbol(moduleName, packageName, typeName, constructorType, public = public)
    pkg.types.add(
        TypeInfo(
            moduleName,
            packageName,
            typeName,
            type,
            isPublic = true,
            isVariantConstructor = true,
            fields = fields
        )
    )
}