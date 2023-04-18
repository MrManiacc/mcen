package mcen.content

import mcen.content.internal.Registry
import mcen.scripting.ScriptEngine
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.energy.EnergyStorage
import net.minecraftforge.items.ItemStackHandler


class ControllerTile(pos: BlockPos, state: BlockState) : BlockEntity(Registry.Tiles.Controller, pos, state) {
    private val scriptEngine = ScriptEngine()
    var scriptSource: String = ""
    private val inventoryOptional = LazyOptional.of { inventory }
    private val energyOptional = LazyOptional.of { energy }
    val inventory: ItemStackHandler = object : ItemStackHandler(5000) {
        override fun onContentsChanged(slot: Int) {
            super.onContentsChanged(slot)
            update()
        }
    }

    val energy: EnergyStorage = object : EnergyStorage(Int.MAX_VALUE) {
        override fun receiveEnergy(maxReceive: Int, simulate: Boolean): Int {
            val received = super.receiveEnergy(maxReceive, simulate)
            update()
            return received
        }

        override fun extractEnergy(maxExtract: Int, simulate: Boolean): Int {
            val extracted = super.extractEnergy(maxExtract, simulate)
            update()
            return extracted
        }
    }


    override fun <T> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return if (cap === ForgeCapabilities.ITEM_HANDLER) {
            inventoryOptional.cast()
        } else if (cap == ForgeCapabilities.ENERGY)
            energyOptional.cast()
        else super.getCapability(cap, side)
    }

//    init {
//        Registry.Net.CompileSource.serverListener { compileSource, _ ->
//            if (compileSource.blockPos == this.blockPos && compileSource.level.location() == level?.dimensionTypeId()?.location()) {
//                compileSource(compileSource.source)
//                false
//            } else false
//        }
//    }

    fun compileSource(luaSource: String) {
        this.scriptSource = luaSource
        update()
        level?.let { scriptEngine.compile(blockPos, it, luaSource) }
    }

    fun update() {
        requestModelDataUpdate()
        setChanged()
        if (level != null) {
            level!!.setBlockAndUpdate(worldPosition, blockState)
            level!!.sendBlockUpdated(worldPosition, blockState, blockState, 3)
        }
    }

    override fun onLoad() {
        super.onLoad()
    }

    override fun onChunkUnloaded() {
        super.onChunkUnloaded()
    }

    fun tick() {
        level?.let { scriptEngine.update(blockPos, it) }
    }

    override fun saveAdditional(tag: CompoundTag) {
        super.saveAdditional(tag)
        tag.putString("script", scriptSource)
        tag.put("inventory", inventory.serializeNBT())
        tag.put("energy", energy.serializeNBT())
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(tag: CompoundTag) {
        super.load(tag)
        scriptSource = tag.getString("script")
        inventory.deserializeNBT(tag.getCompound("inventory"))
        energy.deserializeNBT(tag.get("energy"))
        level?.let { if (!it.isClientSide) scriptEngine.compile(blockPos, it, scriptSource) }
    }

    override fun getUpdatePacket(): Packet<ClientGamePacketListener>? {
        return ClientboundBlockEntityDataPacket.create(this)
    }

    override fun getUpdateTag(): CompoundTag {
        return serializeNBT()
    }

    override fun handleUpdateTag(tag: CompoundTag) {
        super.handleUpdateTag(tag)
        load(tag)
    }


    override fun onDataPacket(net: Connection, pkt: ClientboundBlockEntityDataPacket) {
        super.onDataPacket(net, pkt)
        handleUpdateTag(pkt.tag!!)
    }


}