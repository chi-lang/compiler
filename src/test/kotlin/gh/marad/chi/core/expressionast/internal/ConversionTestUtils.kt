package gh.marad.chi.core.expressionast.internal

import gh.marad.chi.core.Expression
import gh.marad.chi.core.Package
import gh.marad.chi.core.compiler.CompileTables
import gh.marad.chi.core.compiler.ExprConversionVisitor
import gh.marad.chi.core.namespace.GlobalCompilationNamespace
import gh.marad.chi.core.namespace.GlobalCompilationNamespaceImpl
import gh.marad.chi.core.parser.readers.ParseAst

fun convertAst(ast: ParseAst, ns: GlobalCompilationNamespace = GlobalCompilationNamespaceImpl()): Expression {
    val pkg = ns.getDefaultPackage()
    val pkgDef = Package(pkg.moduleName, pkg.packageName)
    val compileTables = CompileTables(pkgDef, ns, emptyList())
    return ExprConversionVisitor(Package(pkg.moduleName, pkg.packageName), compileTables)
        .visit(ast)
}