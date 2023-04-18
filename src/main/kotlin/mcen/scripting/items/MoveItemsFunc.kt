package mcen.scripting.items

import mcen.scripting.BlockTarget
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.IItemHandler
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ThreeArgFunction

class MoveItemsFunc(private val world: Level) : ThreeArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue {
        var moveCount = if (arg3?.isint() == true) arg3.toint() else 64
        moveCount = if (moveCount > 64) 64 else moveCount
        if (arg1?.istable() == true && arg2?.istable() == true) {
            val inputTarget = BlockTarget.from(arg1)
            val outputTarget = BlockTarget.from(arg2)
            val inputCap = world.getBlockEntity(
                inputTarget.blockPos
            )?.getCapability(ForgeCapabilities.ITEM_HANDLER)
            val outputCap = world.getBlockEntity(
                outputTarget.blockPos
            )?.getCapability(ForgeCapabilities.ITEM_HANDLER)
            if (inputCap?.isPresent == true && outputCap?.isPresent == true) {
                val input = inputCap.resolve().get()
                val output = outputCap.resolve().get()
                for (slot in 0 until input.slots) {
                    val extracted = input.extractItem(slot, moveCount, true)
                    for (out in 0 until output.slots) {
                        val inserted = output.insertItem(out, extracted, true)
                        output.insertItem(
                            out, input.extractItem(slot, extracted.count - inserted.count, false), false
                        )
                    }
                }
            }
        } else if (arg1?.isuserdata() == true && arg2?.isuserdata() == true) {
            val input = arg1.touserdata(IItemHandler::class.java)
            val output = arg2.touserdata(IItemHandler::class.java)
            if (input is IItemHandler && output is IItemHandler) {
                for (slot in 0 until input.slots) {
                    val extracted = input.extractItem(slot, moveCount, true)
                    for (out in 0 until output.slots) {
                        val inserted = output.insertItem(out, extracted, true)
                        output.insertItem(
                            out, input.extractItem(slot, extracted.count - inserted.count, false), false
                        )
                    }
                }
            }
        }
        return NIL
    }
}