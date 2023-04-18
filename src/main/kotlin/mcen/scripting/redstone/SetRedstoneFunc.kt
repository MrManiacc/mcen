package mcen.scripting.redstone

import mcen.scripting.BlockTarget
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction

class SetRedstoneFunc(private val world: Level) : TwoArgFunction() {
    override fun call(arg: LuaValue?, arg2: LuaValue?): LuaValue {
        if (arg == null) return FALSE
        val target = BlockTarget.from(arg)
        var state = world.getBlockState(
            target.blockPos
        ) ?: return FALSE
        val powered = state.getOptionalValue(BlockStateProperties.POWERED)
        val power = state.getOptionalValue(BlockStateProperties.POWER)
        val lit = state.getOptionalValue(BlockStateProperties.LIT)
        val extended = state.getOptionalValue(BlockStateProperties.EXTENDED)
        if (arg2?.isboolean() == true) {
//            val level = state.getSignal(world, target.blockPos, target.direction ?: Direction.UP)
            val value = arg2.toboolean()
            if (powered.isPresent)
                state = state.setValue(BlockStateProperties.POWERED, value)
            if (power.isPresent)
                state = state.setValue(BlockStateProperties.POWER, if (value) 15 else 0)
            if (lit.isPresent)
                state = state.setValue(BlockStateProperties.LIT, value)

            world.setBlockAndUpdate(target.blockPos, state)

            return TRUE
        } else if (arg2?.isint() == true) {
            val value = arg2.toint()
            if (powered.isPresent)
                state = state.setValue(BlockStateProperties.POWERED, value > 0)
            if (power.isPresent)
                state = state.setValue(BlockStateProperties.POWER, value)
            if (lit.isPresent)
                state = state.setValue(BlockStateProperties.LIT, value > 0)

            world.setBlockAndUpdate(target.blockPos, state)
            return TRUE
        }
        return FALSE
    }
}