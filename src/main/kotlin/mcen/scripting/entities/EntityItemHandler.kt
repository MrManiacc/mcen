package mcen.scripting.entities

import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraftforge.items.IItemHandler

class EntityItemHandler(private val entities: List<ItemEntity>) : IItemHandler {
    /**
     * Returns the number of slots available
     *
     * @return The number of slots available
     */
    override fun getSlots(): Int {
        return entities.size
    }

    /**
     * Returns the ItemStack in a given slot.
     *
     * The result's stack size may be greater than the itemstack's max size.
     *
     * If the result is empty, then the slot is empty.
     *
     *
     *
     * **IMPORTANT:** This ItemStack *MUST NOT* be modified. This method is not for
     * altering an inventory's contents. Any implementers who are able to detect
     * modification through this method should throw an exception.
     *
     *
     *
     * ***SERIOUSLY: DO NOT MODIFY THE RETURNED ITEMSTACK***
     *
     *
     * @param slot Slot to query
     * @return ItemStack in given slot. Empty Itemstack if the slot is empty.
     */
    override fun getStackInSlot(slot: Int): ItemStack {
        if (slot < 0 || slot >= entities.size) return ItemStack.EMPTY
        val entity = entities[slot]
        return entity.item.copy()
    }

    /**
     *
     *
     * Inserts an ItemStack into the given slot and return the remainder.
     * The ItemStack *should not* be modified in this function!
     *
     * Note: This behaviour is subtly different from [IFluidHandler.fill]
     *
     * @param slot     Slot to insert into.
     * @param stack    ItemStack to insert. This must not be modified by the item handler.
     * @param simulate If true, the insertion is only simulated
     * @return The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
     * May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
     * The returned ItemStack can be safely modified after.
     */
    override fun insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack {
        //NOOP, just return. We cannot go from an ItemStack to an item-entity in this manner.
        return stack
    }

    /**
     * Extracts an ItemStack from the given slot.
     *
     *
     * The returned value must be empty if nothing is extracted,
     * otherwise its stack size must be less than or equal to `amount` and [ItemStack.getMaxStackSize].
     *
     *
     * @param slot     Slot to extract from.
     * @param amount   Amount to extract (may be greater than the current stack's max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemStack extracted from the slot, must be empty if nothing can be extracted.
     * The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
     */
    override fun extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack {
        if (slot < 0 || slot >= entities.size) return ItemStack.EMPTY
        val entity = entities[slot]
        val copy = entity.item.copy()
        if (copy.isEmpty) {
            entity.discard()
            return ItemStack.EMPTY
        }
        return if (simulate) {
            if (copy.count < amount)
                copy
            else {
                copy.count = amount
                copy
            }
        } else {

            if (copy.count < amount) {
                entity.item = ItemStack.EMPTY
                entity.discard()
                return copy
            } else {
                copy.count = amount
                entity.item.shrink(amount)
                if (entity.item.count <= 0) {
                    entity.discard()
                }
                return copy
            }
        }
    }

    /**
     * Retrieves the maximum stack size allowed to exist in the given slot.
     *
     * @param slot Slot to query.
     * @return     The maximum stack size allowed in the slot.
     */
    override fun getSlotLimit(slot: Int): Int {
        if (slot < 0 || slot >= entities.size) return 0
        return entities[slot].item.maxStackSize
    }

    /**
     *
     *
     * This function re-implements the vanilla function [Container.canPlaceItem].
     * It should be used instead of simulated insertions in cases where the contents and state of the inventory are
     * irrelevant, mainly for the purpose of automation and logic (for instance, testing if a minecart can wait
     * to deposit its items into a full inventory, or if the items in the minecart can never be placed into the
     * inventory and should move on).
     *
     *
     *  * isItemValid is false when insertion of the item is never valid.
     *  * When isItemValid is true, no assumptions can be made and insertion must be simulated case-by-case.
     *  * The actual items in the inventory, its fullness, or any other state are **not** considered by isItemValid.
     *
     * @param slot    Slot to query for validity
     * @param stack   Stack to test with for validity
     *
     * @return true if the slot can insert the ItemStack, not considering the current state of the inventory.
     * false if the slot can never insert the ItemStack in any situation.
     */
    override fun isItemValid(slot: Int, stack: ItemStack): Boolean {
        //No insertion allowed at all, only extraction allowed.
        return false
    }

}
