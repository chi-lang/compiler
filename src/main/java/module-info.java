module gh.marad.chi.compiler {
    requires java.base;
    requires transitive kotlin.stdlib;
    requires org.jgrapht.core;
    exports gh.marad.chi.core;
    exports gh.marad.chi.core.parser;
    exports gh.marad.chi.core.analyzer;
    exports gh.marad.chi.core.expressionast;
    exports gh.marad.chi.core.namespace;
}