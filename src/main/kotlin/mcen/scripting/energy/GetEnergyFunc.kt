package mcen.scripting.energy

import mcen.scripting.BlockTarget
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class GetEnergyFunc(private val level: Level) : OneArgFunction() {
    override fun call(arg: LuaValue?): LuaValue {
        if (arg == null) return NIL
        val target = BlockTarget.from(arg)
        val entity = level.getBlockEntity(target.blockPos) ?: return NIL
        val cap = entity.getCapability(ForgeCapabilities.ENERGY, target.direction)
        if (!cap.isPresent) return NIL
        return CoerceJavaToLua.coerce(cap.resolve().get())
    }
}