package gh.marad.chi

import gh.marad.chi.core.*
import gh.marad.chi.core.analyzer.Level
import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.compiler.Compiler
import gh.marad.chi.core.namespace.CompilationEnv
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.namespace.TestCompilationEnv
import gh.marad.chi.core.namespace.TestPackageDescriptor
import gh.marad.chi.core.types.HasTypeId
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeScheme

data class ErrorMessagesException(val errors: List<Message>) : AssertionError("Chi compilation errors")

fun compile(
    code: String,
    namespace: TestCompilationEnv = TestCompilationEnv(),
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

    // Update the test compilation env with defined types and symbols
    for (typeAlias in program.typeAliases) {
        namespace.addTypeDefinition(typeAlias)
    }

    for (nameDeclaration in program.expressions.filterIsInstance<NameDeclaration>()) {
        namespace.addSymbol(program.packageDefinition.moduleName, program.packageDefinition.packageName,
            nameDeclaration.name, nameDeclaration.type, nameDeclaration.public, nameDeclaration.mutable)
    }

    for (effectDefinition in program.expressions.filterIsInstance<EffectDefinition>()) {
        namespace.addSymbol(program.packageDefinition.moduleName, program.packageDefinition.packageName,
            effectDefinition.name, effectDefinition.type, effectDefinition.public, mutable = false)
    }

    return program
}

fun asts(code: String, ns: TestCompilationEnv = TestCompilationEnv(), ignoreCompilationErrors: Boolean = false): List<Expression> {
    return compile(code, ns, ignoreCompilationErrors).expressions
}

fun messages(code: String, ns: CompilationEnv = TestCompilationEnv()): List<Message> {
    val result = Compiler.compile(code, ns)
    return result.messages
}


fun ast(
    code: String,
    ns: TestCompilationEnv = TestCompilationEnv(),
    ignoreCompilationErrors: Boolean = false
): Expression = asts(code, ns, ignoreCompilationErrors).last()

fun TestCompilationEnv.addSymbolInDefaultPackage(name: String, type: TypeScheme? = null, public: Boolean = false,
                                             mutable: Boolean = false, @Suppress("UNUSED_PARAMETER") slot: Int = 0) {
    addSymbol(
        Symbol(
            CompilationDefaults.defaultModule, CompilationDefaults.defaultPacakge,
            name,
            type,
            public = public,
            mutable = mutable,
        )
    )
}

fun TestCompilationEnv.addSymbol(moduleName: String, packageName: String, name: String, type: TypeScheme? = null,
                             public: Boolean = false, mutable: Boolean = false, @Suppress("UNUSED_PARAMETER") slot: Int = 0) {
    addSymbol(
        Symbol(
            moduleName, packageName,
            name,
            type,
            public = public,
            mutable = mutable,
        )
    )
}

fun TestCompilationEnv.addTypeDefinition(type: Type) {
    if (type is HasTypeId && type.getTypeIds().isNotEmpty()) {
        val id = type.getTypeIds().first()
        addTypeDefinition(TypeAlias(id, type))
    } else {
        throw RuntimeException("Type $type doesn't have an ID")
    }
}

fun TestCompilationEnv.addTypeDefinition(alias: TypeAlias) {
    val id = alias.typeId
    val pkg = getOrCreatePackage(id.moduleName, id.packageName) as TestPackageDescriptor
    pkg.types.add(alias)
}
