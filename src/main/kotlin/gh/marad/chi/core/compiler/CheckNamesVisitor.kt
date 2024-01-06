package gh.marad.chi.core.compiler

import gh.marad.chi.core.analyzer.Message
import gh.marad.chi.core.analyzer.UnrecognizedName
import gh.marad.chi.core.analyzer.toCodePoint
import gh.marad.chi.core.parser.readers.*
import gh.marad.chi.core.parser.visitor.DefaultParseAstVisitor

class CheckNamesVisitor(private val node: ParseAst, symbolTable: SymbolTable) : DefaultParseAstVisitor() {
    private var messages = mutableListOf<Message>()
    private var definedNames = mutableSetOf<String>().apply {
        symbolTable.forEach {
            add(it.name)
        }
    }

    fun check(messages: MutableList<Message>) {
        this.messages = messages
        visit(node)
    }

    override fun visitNameDeclaration(parseNameDeclaration: ParseNameDeclaration) {
        parseNameDeclaration.value.accept(this)
        definedNames.add(parseNameDeclaration.symbol.name)
    }

    override fun visitVariableRead(parseVariableRead: ParseVariableRead) {
        if (parseVariableRead.variableName !in definedNames) {
            messages.add(UnrecognizedName(parseVariableRead.variableName, parseVariableRead.section.toCodePoint()))
        }
    }

    override fun visitLambda(parseLambda: ParseLambda) {
        withNewScope {
            definedNames.addAll(parseLambda.formalArguments.map { it.name })
            super.visitLambda(parseLambda)
        }
    }

    override fun visitFuncWithName(parseFuncWithName: ParseFuncWithName) {
        definedNames.add(parseFuncWithName.name)
        withNewScope {
            definedNames.addAll(parseFuncWithName.formalArguments.map { it.name })
            super.visitFuncWithName(parseFuncWithName)
        }
    }

    override fun visitEffectDefinition(parseEffectDefinition: ParseEffectDefinition) {
        definedNames.add(parseEffectDefinition.name)
        super.visitEffectDefinition(parseEffectDefinition)
    }
    override fun visitHandle(parseHandle: ParseHandle) {
        withNewScope {
            definedNames.add("resume")
            super.visitHandle(parseHandle)
        }
    }

    private fun withNewScope(f: () -> Unit) {
        val prevScope = definedNames
        definedNames = mutableSetOf()
        definedNames.addAll(prevScope)
        f()
        definedNames = prevScope
    }
}