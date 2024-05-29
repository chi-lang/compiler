package gh.marad.chi.core.utils

import gh.marad.chi.core.Atom
import gh.marad.chi.core.Expression
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.Function
import gh.marad.chi.core.types.Variable

object DefaultArguments {
    fun fill(arguments: MutableList<Expression>, symbol: Symbol?) {
        if (symbol?.type != null && symbol.type is Function) {
            val defaultArgsPossible = symbol.type.defaultArgs
            val fnExpectedArgCount = symbol.type.types.size - 1
            val requiredArgs = fnExpectedArgCount - defaultArgsPossible
            if (arguments.size < requiredArgs) {
                // skip adding parameters - this will fail compilation in other stage
            } else {
                val missingArgs = fnExpectedArgCount - arguments.size
                repeat(missingArgs) { it ->
                    val tv = Variable("@$it", 1)
                    arguments.add(Atom.defaultArg(tv))
                }
            }
        }
    }
}