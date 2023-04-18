package mcen.scripting

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue

data class BlockTarget(val blockPos: BlockPos, val direction: Direction? = null) {

    companion object {
        fun from(luaValue: LuaValue): BlockTarget {
            val blockPos = getPosition(luaValue)
            val direction = getDirection(luaValue)
            return BlockTarget(blockPos, direction)
        }

        /**
         * Gets the face direction of the target
         */
        private fun getDirection(luaValue: LuaValue): Direction? {
            var face = ""
            if (luaValue is LuaTable) {
                val keys = luaValue.checktable().keyCount()
                if (keys == 4) {
                    val last = luaValue.get(4)
                    face = if (last.isstring()) last.toString()
                    else luaValue.get(0).toString()
                } else if (keys == 3) return null
            }
            for (dir in Direction.values()) {
                if (dir.name.equals(face, true)) return dir
            }
            return null
        }

        private fun getPosition(luaValue: LuaValue): BlockPos {
            if (luaValue is LuaTable) {
                val keys = luaValue.checktable().keyCount()
                if (keys == 4) {
                    val first = luaValue.get(1)
                    return if (!first.isstring()) BlockPos(luaValue.get(0).toint(), luaValue.get(1).toint(), luaValue.get(2).toint())
                    else BlockPos(luaValue.get(1).toint(), luaValue.get(2).toint(), luaValue.get(3).toint())
                } else if (keys == 3) return BlockPos(luaValue.get(1).toint(), luaValue.get(2).toint(), luaValue.get(3).toint())
            }
            if (luaValue.get("x")?.isint() == true && luaValue.get("y")?.isint() == true && luaValue.get("z")
                    ?.isint() == true
            ) return BlockPos(luaValue.get("x").toint(), luaValue.get("y").toint(), luaValue.get("z").toint())
            return BlockPos.ZERO
        }
    }

}