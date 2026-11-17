package com.alcatrazescapee.alcatrazcore.inventory.slot;

import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** alcatrazcore 1.12 SlotTileCore: the tile decides isItemValid / the slot limit and is told when the slot changes. */
public class SlotTileCore extends SlotItemHandler {
   private final TileInventory tile;

   public SlotTileCore(IItemHandler inventory, int idx, int x, int y, TileInventory tile) {
      super(inventory, idx, x, y);
      this.tile = tile;
   }

   @Override
   public boolean mayPlace(ItemStack stack) {
      return this.tile.isItemValid(this.getSlotIndex(), stack) && super.mayPlace(stack);
   }

   @Override
   public int getMaxStackSize() {
      return this.tile.getSlotLimit(this.getSlotIndex());
   }

   @Override
   public void setChanged() {
      this.tile.setAndUpdateSlots(this.getSlotIndex());
      super.setChanged();
   }
}
