package gh.marad.chi.core.namespace

import gh.marad.chi.core.TypeAlias

class TypeTable {
    private val aliases = mutableMapOf<String, TypeAlias>()

    fun add(typeAlias: TypeAlias) {
        aliases[typeAlias.typeId.name] = typeAlias
    }

    fun add(alias: String, typeAlias: TypeAlias) {
        aliases[alias] = typeAlias
    }

    fun getAlias(name: String): TypeAlias? = aliases[name]


    fun add(table: TypeTable) {
        aliases.putAll(table.aliases)
    }

}
