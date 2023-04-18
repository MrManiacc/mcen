package mcen.content.internal

import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level

data class WorldPos(val position: BlockPos, val world: ResourceKey<Level>) {
    companion object {
        val Identity: WorldPos = WorldPos(BlockPos.ZERO, Level.OVERWORLD)
    }
}