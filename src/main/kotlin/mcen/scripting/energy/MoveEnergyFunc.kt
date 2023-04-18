package mcen.scripting.energy

import mcen.scripting.BlockTarget
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.energy.IEnergyStorage
import org.luaj.vm2.LuaValue
import org.luaj.vm2.lib.ThreeArgFunction

class MoveEnergyFunc(private val level: Level) : ThreeArgFunction() {
    override fun call(arg1: LuaValue?, arg2: LuaValue?, arg3: LuaValue?): LuaValue {
        if (arg3 == null || !arg3.isint()) return NIL
        val extractAmount = arg3.toint()
        if (arg1?.istable() == true && arg2?.istable() == true) {
            val inputTarget = BlockTarget.from(arg1)
            val outputTarget = BlockTarget.from(arg2)
            val inputCap = level.getBlockEntity(
                inputTarget.blockPos
            )?.getCapability(ForgeCapabilities.ENERGY)
            val outputCap = level.getBlockEntity(
                outputTarget.blockPos
            )?.getCapability(ForgeCapabilities.ENERGY)
            if (inputCap?.isPresent == true && outputCap?.isPresent == true) {
                val input = inputCap.resolve().get()
                val output = outputCap.resolve().get()
                val simulated = input.extractEnergy(extractAmount, true)
                val totalReceived = output.receiveEnergy(simulated, true)
                return LuaValue.valueOf(output.receiveEnergy(input.extractEnergy(totalReceived, false), false))
            }
        } else if (arg1?.isuserdata() == true && arg2?.isuserdata() == true) {
            val input = arg1.touserdata(IEnergyStorage::class.java)
            val output = arg2.touserdata(IEnergyStorage::class.java)
            if (input is IEnergyStorage && output is IEnergyStorage) {
                val simulated = input.extractEnergy(extractAmount, true)
                val totalReceived = output.receiveEnergy(simulated, true)
                return LuaValue.valueOf(output.receiveEnergy(input.extractEnergy(totalReceived, false), false))
            }
        }
        return NIL
    }
}