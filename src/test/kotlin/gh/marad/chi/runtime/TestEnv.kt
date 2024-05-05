package gh.marad.chi.runtime

object TestEnv {
    fun eval(code: String, extEnv: LuaEnv? = null, showLuaCode: Boolean = false): Any? {
        val env = extEnv ?: LuaEnv()
        return if (env.eval(code, showLuaCode = showLuaCode, emitModule = false)) {
            env.lua.get().toJavaObject()
        } else {
            null
        }
    }
}