package mcen.api.graph.node

import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ThreeArgFunction

class LuaInput : ThreeArgFunction() {
    override fun call(name: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue {


        return LuaValue.NIL
    }
}