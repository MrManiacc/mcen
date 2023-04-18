package mcen.scripting.redstone

import mcen.scripting.BlockTarget
import net.minecraft.core.Direction
import net.minecraft.world.level.Level
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction

class GetRedstoneFunc(private val world: Level) : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        if (arg == null) return NIL
        val target = BlockTarget.from(arg)
        val state = world.getBlockState(
            target.blockPos
        ) ?: return NIL
        val level = state.getSignal(world, target.blockPos, target.direction ?: Direction.UP)
        return LuaValue.valueOf(level)
    }
}