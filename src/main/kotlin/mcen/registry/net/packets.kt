package mcen.registry.net

import com.github.sieves.registry.internal.net.Packet
import mcen.scripting.console.LogLevel
import net.minecraft.core.BlockPos
import net.minecraft.core.Registry
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

/**
 * Sent to the client to open a specific inventory class provided the position and level of the block, so we can retrieve the tile entity.
 */
class OpenInventory(
    var inventoryClass: String = "",
    var blockPos: BlockPos = BlockPos.ZERO,
    var level: ResourceKey<Level> = Level.OVERWORLD
) : Packet() {

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeUtf(inventoryClass)
        buffer.writeBlockPos(blockPos)
        buffer.writeResourceLocation(level.location())
    }

    override fun read(buffer: FriendlyByteBuf) {
        inventoryClass = buffer.readUtf()
        blockPos = buffer.readBlockPos()
        level = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation())
    }
}

/**
 * Sent to the client to open a specific inventory class provided the position and level of the block, so we can retrieve the tile entity.
 */
class ConsoleMessagePacket(
    var message: String = "",
    var blockPos: BlockPos = BlockPos.ZERO,
    var world: ResourceKey<Level> = Level.OVERWORLD,
    var logLevel: LogLevel = LogLevel.Info
) : Packet() {

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeUtf(message)
        buffer.writeBlockPos(blockPos)
        buffer.writeResourceLocation(world.location())
        buffer.writeEnum(logLevel)
    }

    override fun read(buffer: FriendlyByteBuf) {
        message = buffer.readUtf()
        blockPos = buffer.readBlockPos()
        world = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation())
        logLevel = buffer.readEnum(LogLevel::class.java)
    }
}

/**
 * Sent to the client to open a specific inventory class provided the position and level of the block, so we can retrieve the tile entity.
 */
class ConsoleErrorPacket(
    var message: String = "",
    var blockPos: BlockPos = BlockPos.ZERO,
    var world: ResourceKey<Level> = Level.OVERWORLD,
    var line: Int = -1
) : Packet() {

    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeUtf(message)
        buffer.writeBlockPos(blockPos)
        buffer.writeResourceLocation(world.location())
        buffer.writeInt(line)
    }

    override fun read(buffer: FriendlyByteBuf) {
        message = buffer.readUtf()
        blockPos = buffer.readBlockPos()
        world = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation())
        line = buffer.readInt()
    }
}


class CompileSource(
    var blockPos: BlockPos = BlockPos.ZERO,
    var level: ResourceKey<Level> = Level.OVERWORLD,
    var source: String = ""
) : Packet() {
    override fun write(buffer: FriendlyByteBuf) {
        buffer.writeUtf(source)
        buffer.writeBlockPos(blockPos)
        buffer.writeResourceLocation(level.location())
    }

    override fun read(buffer: FriendlyByteBuf) {
        source = buffer.readUtf()
        blockPos = buffer.readBlockPos()
        level = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation())
    }
}