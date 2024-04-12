package gh.marad.chi.runtime

import gh.marad.chi.core.Package
import gh.marad.chi.core.PackageSymbol
import gh.marad.chi.core.TypeAlias
import gh.marad.chi.core.namespace.CompilationEnv
import gh.marad.chi.core.namespace.PackageDescriptor
import gh.marad.chi.core.namespace.PreludeImport
import gh.marad.chi.core.namespace.Symbol
import gh.marad.chi.core.types.TypeId

class LuaCompilationEnv(
    val luaEnv: LuaEnv
) : CompilationEnv {
    override fun getPreludeImports(): List<PreludeImport> = luaEnv.prelude

    override fun getOrCreatePackage(moduleName: String, packageName: String): PackageDescriptor {
        return LuaPackageDescriptor(moduleName, packageName, luaEnv)
    }

    override fun getOrCreatePackage(pkg: Package): PackageDescriptor {
        return getOrCreatePackage(pkg.moduleName, pkg.packageName)
    }

    @Suppress("UNCHECKED_CAST")
    override fun getSymbol(moduleName: String, packageName: String, symbolName: String): Symbol? {
        val luaPkgPath = "chi.${moduleName.replace(".", "_")}.${packageName.replace('.', '_')}"
        val path = "$luaPkgPath._package.$symbolName"
        val map = luaEnv.lua.execute("return $path")?.get(0)?.toJavaObject() ?: return null
        map as Map<String, Any>
        return Symbol(
            moduleName,
            packageName,
            symbolName,
            type = (map["type"] as String?)?.let { TypeWriter.decodeType(it) },
            public = map["public"] as Boolean,
            mutable = map["mutable"] as Boolean
        )

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
        val luaPkgPath = "chi.${moduleName.replace(".", "_")}.${packageName.replace('.', '_')}"

        override fun getTypeAlias(name: String): TypeAlias? {
            val path = "$luaPkgPath._types.$name"
            val type = luaEnv.lua.execute("return $path")?.get(0)?.toJavaObject() ?: return null
            type as String
            return TypeAlias(
                TypeId(moduleName, packageName, name),
                TypeWriter.decodeType(type)
            )
        }

    }
}