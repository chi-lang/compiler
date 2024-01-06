package gh.marad.chi

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Program
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.compiler.*
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.types.SimpleType
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.Types

data class ErrorMessagesException(val errors: List<Message>) : AssertionError("Chi compilation errors")

fun compile(
    code: String,
    namespace: GlobalCompilationNamespace = GlobalCompilationNamespace(),
    ignoreCompilationErrors: Boolean = false
): Program {
    val (program, messages) = Compiler2.compile(code, namespace)

    if (!ignoreCompilationErrors) {
        messages.forEach { msg ->
            System.err.println(Compiler2.formatCompilationMessage(code, msg))
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
    val (_, messages) = Compiler2.compile(code, ns)
    return messages
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
            SymbolKind.Local,
            type,
            slot = slot,
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
            SymbolKind.Local,
            type,
            slot = slot,
            public = public,
            mutable = mutable
        )
    )
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
            typeName,
            type,
            isPublic = true,
            isVariantConstructor = true,
            parent = null,
            fields = fields
        )
    )
}