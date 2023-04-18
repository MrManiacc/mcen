package mcen.scripting.entities

import mcen.scripting.BlockTarget
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.structure.BoundingBox
import net.minecraft.world.phys.AABB
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.TwoArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class GetItemsFunc(private val level: Level) : TwoArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?): LuaValue {
        if (arg1?.istable() == true && arg2?.istable() == true) {
            val inputTarget = BlockTarget.from(arg1)
            val outputTarget = BlockTarget.from(arg2)
            val entities = level.getEntitiesOfClass(
                ItemEntity::class.java,
                AABB.of(BoundingBox.fromCorners(inputTarget.blockPos, outputTarget.blockPos))
            )
            return CoerceJavaToLua.coerce(EntityItemHandler((entities)))
        }
        return LuaValue.NIL
    }
}
