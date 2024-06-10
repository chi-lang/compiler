package gh.marad.chi.runtime

import party.iroiro.luajava.Lua
import party.iroiro.luajava.Lua.LuaType

class ChiString {
    companion object {
        @JvmStatic
        fun open(L: Lua): Int {
            L.createTable(0, 1)
            L.push { l ->
                val s = l.get().toJavaObject()
                val sb = StringBuilder()
                sb.append(s)
                sb.reverse()
                l.push(sb.toString())
                1
            }
            L.setField(-2, "reverse")
            L.push { l ->
                val s = l.get().toJavaObject() as String
                val iter = s.codePoints().iterator()
                l.push { l ->
                    if (iter.hasNext()) {
                        l.push(iter.next())
                    } else {
                        l.pushNil()
                    }
                    1
                }
                1
            }
            L.setField(-2, "codePoints")
            L.push { l ->
                val s = l.get().toJavaObject() as String
                l.push(s.trimStart())
                1
            }
            L.setField(-2, "trimStart")
            L.push { l ->
                val s = l.get().toJavaObject() as String
                l.push(s.trimEnd())
                1
            }
            L.setField(-2, "trimEnd")
            L.push { l ->
                val sb = StringBuilder()
                val args = ArrayList<String>(l.top)
                repeat(l.top) {
                    val type = l.type(-1)
                    val obj = l.get().toJavaObject()
                    when {
                        type == LuaType.USERDATA || type == LuaType.STRING ->
                            args.add(obj as String)
                        else -> {
                            throw RuntimeException("chistr.concat expects argument to be a string but got $type. Object was $obj")
                        }
                    }
                }
                args.asReversed().forEach(sb::append)
                l.push(sb.toString())
                1
            }
            L.setField(-2, "concat")
            return 1
        }
    }
}
