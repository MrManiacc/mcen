package mcen.scripting.items

import mcen.content.ControllerTile
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ZeroArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class GetOwnItemsFunc(private val world: Level, private val blockPos: BlockPos) : ZeroArgFunction() {
    override fun call(): LuaValue {
        val entity = world.getBlockEntity(blockPos)
        if (entity is ControllerTile)
            return CoerceJavaToLua.coerce(entity.inventory)
        return LuaValue.NIL
    }

}
