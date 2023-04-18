package mcen.content

import mcen.content.internal.Registry
import mcen.scripting.ScriptEngine
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class ControllerItem : BlockItem(Registry.Blocks.Controller, Properties().stacksTo(1).fireResistant()) {
    private val scriptEngine = ScriptEngine()

    /**
     * Here we implement a tick callback for the controller item in the inventory it's self.
     */
    override fun inventoryTick(pStack: ItemStack, pLevel: Level, pEntity: Entity, pSlotId: Int, pIsSelected: Boolean) {

    }
}