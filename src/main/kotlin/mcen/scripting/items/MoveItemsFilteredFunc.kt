package mcen.scripting.items

import mcen.scripting.BlockTarget
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.items.IItemHandler
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import org.luaj.vm2.Varargs
import org.luaj.vm2.lib.VarArgFunction
import org.luaj.vm2.lib.jse.CoerceJavaToLua

class MoveItemsFilteredFunc(private val world: Level) : VarArgFunction() {

    override fun invoke(args: Varargs?): Varargs {
        if (args == null || args.narg() < 3) return NIL
        val arg1 = args.arg1()
        val arg2 = args.arg(2)
        val filter = args.arg(3)
        var count = if (args.narg() == 4) args.arg(4).toint() else 64
        count = if (count > 64) 64 else count
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
                    val extracted = input.extractItem(slot, count, true)
                    for (out in 0 until output.slots) {
                        val inserted = output.insertItem(out, extracted, true)
                        val filterData = LuaTable()
                        filterData.set("from_slot", LuaValue.valueOf(slot))
                        //The extracted itemstack
                        filterData.set("extracted", CoerceJavaToLua.coerce(extracted))
                        filterData.set("to_slot", LuaValue.valueOf(out))
                        //The extracted itemstack
                        filterData.set("inserted", CoerceJavaToLua.coerce(output.getStackInSlot(out)))
                        val result = filter?.call(filterData)
                        if (result != null && result.isboolean() && result.toboolean()) output.insertItem(
                            out, input.extractItem(slot, extracted.count - inserted.count, false), false
                        )
                    }
                }
            }
            return TRUE
        } else if (arg1?.isuserdata() == true && arg2?.isuserdata() == true) {
            val input = arg1.touserdata(IItemHandler::class.java)
            val output = arg2.touserdata(IItemHandler::class.java)
            if (input is IItemHandler && output is IItemHandler) {
                for (slot in 0 until input.slots) {
                    val extracted = input.extractItem(slot, count, true)
                    for (out in 0 until output.slots) {
                        val inserted = output.insertItem(out, extracted, true)
                        val filterData = LuaTable()
                        filterData.set("from_slot", LuaValue.valueOf(slot))
                        //The extracted itemstack
                        filterData.set("extracted", CoerceJavaToLua.coerce(extracted))
                        filterData.set("to_slot", LuaValue.valueOf(out))
                        //The extracted itemstack
                        filterData.set("inserted", CoerceJavaToLua.coerce(inserted))
                        val result = filter?.call(filterData)
                        if (result != null && result.isboolean() && result.toboolean()) output.insertItem(
                            out, input.extractItem(slot, extracted.count - inserted.count, false), false
                        )
                    }
                }
            }
            return TRUE
        }
        return FALSE
    }

}