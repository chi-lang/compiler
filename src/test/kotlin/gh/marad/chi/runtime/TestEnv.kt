package gh.marad.chi.runtime

object TestEnv {
    fun eval(code: String, extEnv: LuaEnv? = null): Any? {
        val env = extEnv ?: LuaEnv()
        return if (env.eval(code, dontEvalOnlyShowLuaCode = false, emitModule = false)) {
            env.lua.get().toJavaObject()
        } else {
            null
        }
    }
}