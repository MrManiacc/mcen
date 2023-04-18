package mcen.scripting.position

import mcen.scripting.BlockTarget
import net.minecraft.core.BlockPos
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction

class OffsetPositionFunc(private val pos: BlockPos) : OneArgFunction() {
    private val position = tableOf(arrayOf(LuaValue.valueOf(4), valueOf(pos.x), valueOf(pos.y), valueOf(pos.z)))
    override fun call(arg: LuaValue?): LuaValue {
        if (arg == null)
            return position
        val target = BlockTarget.from(arg)
        val offset = pos.offset(target.blockPos.x, target.blockPos.y, target.blockPos.z)
        val result = LuaTable()
        result.set(0, LuaValue.valueOf(4))
        result.set(1, LuaValue.valueOf(offset.x))
        result.set(2, LuaValue.valueOf(offset.y))
        result.set(3, LuaValue.valueOf(offset.z))
        return result
    }
}