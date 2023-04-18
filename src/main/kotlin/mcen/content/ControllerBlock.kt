package mcen.content

import mcen.content.internal.Registry
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

class ControllerBlock(properties: Properties) : Block(properties), EntityBlock {
    override fun <T : BlockEntity?> getTicker(
        pLevel: Level,
        pState: BlockState,
        pBlockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T> = Ticker as BlockEntityTicker<T>

    override fun use(
        pState: BlockState, pLevel: Level, pPos: BlockPos, pPlayer: Player, pHand: InteractionHand, pHit: BlockHitResult
    ): InteractionResult {
        if (pLevel.isClientSide) {
            val entity = pLevel.getBlockEntity(pPos) as? ControllerTile ?: return InteractionResult.FAIL
            Minecraft.getInstance().setScreen(ControllerScreen(pLevel, pPos, entity))
        }
        return InteractionResult.SUCCESS
    }


    object Ticker : BlockEntityTicker<ControllerTile> {
        override fun tick(pLevel: Level, pPos: BlockPos, pState: BlockState, pBlockEntity: ControllerTile) {
            if (pLevel.isClientSide) return //do not tick client side
            pBlockEntity.tick()
        }

    }

    override fun newBlockEntity(pPos: BlockPos, pState: BlockState): BlockEntity? {
        return Registry.Tiles.Controller.create(pPos, pState)
    }
}