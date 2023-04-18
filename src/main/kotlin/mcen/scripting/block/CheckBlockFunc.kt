package mcen.scripting.block

import mcen.scripting.BlockTarget
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.OneArgFunction
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

/**
 * Gets an itemStack of the block at the given position
 */
class CheckBlockFunc() : TwoArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue {
        if (arg1?.isuserdata() == true && arg2?.isstring() == true) {
            val targetTag = arg2.toString()
            val namespace = targetTag.split(":").getOrNull(0) ?: return FALSE
            val path = targetTag.split(":").getOrNull(1) ?: return FALSE
            val data = arg1.touserdata(BlockState::class.java)
            if (data is BlockState) {
                val name = data.block.descriptionId.substringAfter(".")
                val ins = name.substringBefore(".")
                val ipath = name.substringAfter(".").replace(".", "/")
                if (namespace == ins && path == ipath) return TRUE
                for (tag in data.tags) {
                    if (tag.location.namespace == namespace && tag.location.path == path) {
                        return LuaValue.TRUE
                    }
                }
            }
        }
        return FALSE
    }

}