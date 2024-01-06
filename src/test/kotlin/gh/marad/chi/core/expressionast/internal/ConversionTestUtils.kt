package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Package
import gh.marad.chi.core.compiler.ExprConversionVisitor
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.parser.readers.ParseAst

fun convertAst(ast: ParseAst, ns: GlobalCompilationNamespace = GlobalCompilationNamespace()): Expression {
    val pkg = ns.getDefaultPackage()
    return ExprConversionVisitor(Package(pkg.moduleName, pkg.packageName), pkg.symbols, pkg.types)
        .visit(ast)
}