package gh.marad.chi.runtime

import gh.marad.chi.core.parser.readers.Import
import gh.marad.chi.lua.formatLuaCode
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.notExists

class ModuleLoader(
    private val compiler: LuaCompiler,
    private val rootDir: Path
) {
    class Error(info: String): RuntimeException(info)

    /**
     * Compiles the code and returns the Lua code.
     * It also generates the *.lua file with compiled code.
     *
     * @return It returns the lua code for the package.
     */
    fun load(moduleName: String, packageName: String): String {
        val chiSrcPath = rootDir.resolve(moduleName).resolve("$packageName.chi")
        val luaSrcPath = rootDir.resolve(moduleName).resolve("$packageName.lua")
        if (chiSrcPath.notExists()) {
            throw Error("Module $moduleName/$packageName doesn't exist. Tried to load '$chiSrcPath'")
        }

        if (luaSrcPath.exists()) {
            val chiSrcModificationTime = Files.getLastModifiedTime(chiSrcPath, LinkOption.NOFOLLOW_LINKS).toInstant()
            val luaSrcModificationTime = Files.getLastModifiedTime(luaSrcPath, LinkOption.NOFOLLOW_LINKS).toInstant()
            if (chiSrcModificationTime.isAfter(luaSrcModificationTime)) {
                return Files.readString(luaSrcPath)
            }
        }

        val chiCode = Files.readString(chiSrcPath)
        val luaCode = compiler.compileToLua(chiCode)!!
        Files.writeString(luaSrcPath, luaCode)
        return luaCode
    }

}

fun main() {
    val imports = mutableListOf<Import>()
    imports.add(
        Import("std", "lang", packageAlias = null, entries = listOf(
        Import.Entry("println", alias = null, section = null),
        Import.Entry("eval", alias = null, section = null)
    ), null)
    )
    val env = LuaEnv(imports)
    val luaCompiler = LuaCompiler(env)
    val loader = ModuleLoader(luaCompiler, Path.of("D:/dev/chi-stdlib"))
    try {
        val code = loader.load("std", "lang.string")
        println(formatLuaCode(code))
        env.lua.run(code)
    } catch (ex: LuaCompiler.Error) {
        ex.messages.forEach {
            println(it.message)
        }
    }
}