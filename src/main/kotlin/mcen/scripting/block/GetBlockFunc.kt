package mcen.scripting.block

import mcen.scripting.BlockTarget
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

/**
 * Gets an itemStack of the block at the given position
 */
class GetBlockFunc(private val world: Level) : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        if (arg == null) return NIL
        val target = BlockTarget.from(arg)
        val state = world.getBlockState(target.blockPos)
        return CoerceJavaToLua.coerce(state)
    }
}