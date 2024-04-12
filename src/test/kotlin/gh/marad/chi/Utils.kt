package gh.marad.chi

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Program
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.GlobalCompilationNamespaceImpl
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.HasTypeId
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeScheme

data class ErrorMessagesException(val errors: List<Message>) : AssertionError("Chi compilation errors")

fun compile(
    code: String,
    namespace: GlobalCompilationNamespace = GlobalCompilationNamespaceImpl(),
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

fun asts(code: String, ns: GlobalCompilationNamespace = GlobalCompilationNamespaceImpl(), ignoreCompilationErrors: Boolean = false): List<Expression> {
    return compile(code, ns, ignoreCompilationErrors).expressions
}

fun messages(code: String, ns: GlobalCompilationNamespace = GlobalCompilationNamespaceImpl()): List<Message> {
    val result = Compiler.compile(code, ns)
    return result.messages
}


fun ast(
    code: String,
    ns: GlobalCompilationNamespace = GlobalCompilationNamespaceImpl(),
    ignoreCompilationErrors: Boolean = false
): Expression = asts(code, ns, ignoreCompilationErrors).last()

fun GlobalCompilationNamespace.addSymbolInDefaultPackage(name: String, type: TypeScheme? = null, public: Boolean = false,
                                                         mutable: Boolean = false, @Suppress("UNUSED_PARAMETER") slot: Int = 0) {
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

fun GlobalCompilationNamespace.addSymbol(moduleName: String, packageName: String, name: String, type: TypeScheme? = null,
                                         public: Boolean = false, mutable: Boolean = false, @Suppress("UNUSED_PARAMETER") slot: Int = 0) {
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

fun GlobalCompilationNamespace.addTypeDefinition(type: Type) {
    if (type is HasTypeId && type.getTypeId() != null) {
        val id = type.getTypeId()!!
        addTypeDefinition(TypeAlias(id, type))
    } else {
        throw RuntimeException("Type $type doesn't have an ID")
    }
}

fun GlobalCompilationNamespace.addTypeDefinition(alias: TypeAlias) {
    val id = alias.typeId
    val pkg = getOrCreatePackage(id.moduleName, id.packageName)
    pkg.types.add(alias)
}
