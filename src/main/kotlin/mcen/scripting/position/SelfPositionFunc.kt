package mcen.scripting.position

import net.minecraft.core.BlockPos
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction

class SelfPositionFunc(private val pos: BlockPos) : ZeroArgFunction() {
    override fun call(): LuaValue {
        val result = LuaTable()
        result.set(0, LuaValue.valueOf(4))
        result.set(1, LuaValue.valueOf(pos.x))
        result.set(2, LuaValue.valueOf(pos.y))
        result.set(3, LuaValue.valueOf(pos.z))

        return result
    }
}