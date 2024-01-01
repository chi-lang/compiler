package gh.marad.chi.core.types

import java.lang.RuntimeException

typealias TypeName = String

class TypeGraph {
    private val parents = mutableMapOf<TypeName, TypeName?>()

    fun addType(name: TypeName) {
        parents.getOrPut(name) { null }
    }

    fun addSubtype(from: TypeName, to: TypeName) {
        addType(from)
        addType(to)
        val parent = parents[to]
        if (parent == null) {
            parents.getOrPut(to) { from }
        } else {
            throw TypeCannotHaveMultipleParentsException(
                type = to,
                currentParent = parent,
                newParent = from)
        }
    }

    fun contains(type: TypeName): Boolean =
        parents.containsKey(type)

    fun isSubtype(parent: TypeName, child: TypeName): Boolean {
        return parent in ancestors(child)
    }

    fun ancestors(type: TypeName): List<TypeName> {
        var current: TypeName? = parents[type]
        var result = mutableListOf<TypeName>()
        while (current != null) {
            result.add(current)
            current = parents[current]
        }
        return result
    }

    fun commonSupertype(a: TypeName, b: TypeName): TypeName? {
        val aAncs = ancestors(a).toSet()
        for (bAnc in ancestors(b)) {
            if (bAnc in aAncs) {
                return bAnc
            }
        }
        return null
    }

    class TypeCannotHaveMultipleParentsException(
        type: TypeName,
        currentParent: TypeName,
        newParent: TypeName
    ) : RuntimeException(
        "Type $type already has parent $currentParent. Cannot add parent $newParent!"
    )
}