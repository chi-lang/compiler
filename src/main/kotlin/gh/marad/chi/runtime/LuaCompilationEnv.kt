package gh.marad.chi.runtime

import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.namespace.CompilationEnv
import gh.marad.chi.core.namespace.PackageDescriptor
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.core.types.Type
import gh.marad.chi.core.types.TypeId

class LuaCompilationEnv(
    val luaEnv: LuaEnv,
    val imports: Set<Import> = emptySet()
) : CompilationEnv {
    private val cache = mutableMapOf<String, Map<String, Symbol>>()
    override fun getPreludeImports(): List<Import> = luaEnv.prelude + imports

    override fun getOrCreatePackage(moduleName: String, packageName: String): PackageDescriptor {
        return LuaPackageDescriptor(moduleName, packageName, luaEnv)
    }

    override fun getOrCreatePackage(pkg: Package): PackageDescriptor {
        return getOrCreatePackage(pkg.moduleName, pkg.packageName)
    }

    private fun getSymbols(moduleName: String, packageName: String): Map<String, Symbol>? {
        val packageQualifier = "$moduleName/$packageName"
        return cache.getOrPut(packageQualifier) {
//            println("Loading $moduleName/$packageName")
            val infoPath = "package.loaded['$packageQualifier']._package"
            val map = luaEnv.lua.execute("return $infoPath")
                ?.get(0)?.toJavaObject()
                ?: return null
            map as Map<String, *>
            map.mapValues { (symbolName, properties) ->
                properties as Map<String, Any>
                Symbol(
                    moduleName,
                    packageName,
                    symbolName,
                    type = (properties["type"] as String?)?.let { TypeWriter.decodeType(it) },
                    public = properties["public"] as Boolean,
                    mutable = properties["mutable"] as Boolean,
                )
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    override fun getSymbol(moduleName: String, packageName: String, symbolName: String): Symbol? {
        return getSymbols(moduleName, packageName)?.get(symbolName)

//        println("Loading $moduleName/$packageName: $symbolName")
//        val path = "package.loaded['$moduleName/$packageName']._package.$symbolName"
//        val map = luaEnv.lua.execute("require('$moduleName/$packageName'); return $path")?.get(0)?.toJavaObject() ?: return null
//        map as Map<String, Any>
//        return Symbol(
//            moduleName,
//            packageName,
//            symbolName,
//            type = (map["type"] as String?)?.let { TypeWriter.decodeType(it) },
//            public = map["public"] as Boolean,
//            mutable = map["mutable"] as Boolean
//        )
    }

    override fun getSymbol(target: PackageSymbol): Symbol? {
        return getSymbol(target.moduleName, target.packageName, target.name)
    }

    override fun getTypeAlias(moduleName: String, packageName: String, typeAliasName: String): TypeAlias? {
        return getOrCreatePackage(moduleName, packageName).getTypeAlias(typeAliasName)
    }


    class LuaPackageDescriptor(
        override val moduleName: String,
        override val packageName: String,
        private val luaEnv: LuaEnv,
    ) : PackageDescriptor {
        val luaPkgPath = "package.loaded['$moduleName/$packageName']"

        override fun getTypeAlias(name: String): TypeAlias? {
            val path = "$luaPkgPath._types.$name"
            val type = luaEnv.lua.execute("return $path")?.get(0)?.toJavaObject() ?: return null
            type as String
            return TypeAlias(
                TypeId(moduleName, packageName, name),
                TypeWriter.decodeType(type) as Type
            )
        }

    }
}