package mcen.scripting.energy

import mcen.content.ControllerTile
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class GetOwnEnergyFunc(private val level: Level, private val blockPos: BlockPos) : ZeroArgFunction() {
    override fun call(): LuaValue {
        val entity = level.getBlockEntity(blockPos) ?: return NIL
        if (entity is ControllerTile)
            return CoerceJavaToLua.coerce(entity.energy)
        return NIL
    }

}

