package com.alcatrazescapee.alcatrazcore.inventory.slot;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** alcatrazcore 1.12 SlotOutput: take only. */
public class SlotOutput extends SlotItemHandler {
   public SlotOutput(IItemHandler inventory, int idx, int x, int y) {
      super(inventory, idx, x, y);
   }

   @Override
   public boolean mayPlace(ItemStack stack) {
      return false;
   }
}
