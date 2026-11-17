package com.lycanitesmobs.core.container;

import com.lycanitesmobs.compat.LegacyInventory;
import com.lycanitesmobs.core.inventory.InventoryCreature;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

/** 1.12 BaseSlot, with the 1.12 Slot method names (isItemValid, getSlotStackLimit, getStack, putStack...) bridged to 1.21. */
public class BaseSlot extends Slot {
   public final Container inventory;

   public BaseSlot(Container inventory, int slotIndex, int x, int y) {
      super(inventory, slotIndex, x, y);
      this.inventory = inventory;
   }

   public boolean isItemValid(ItemStack itemStack) {
      return this.inventory == null ? true : this.inventory.canPlaceItem(this.getSlotIndex(), itemStack);
   }

   @Override
   public boolean mayPlace(ItemStack stack) {
      return this.isItemValid(stack);
   }

   public int getSlotStackLimit() {
      return this.inventory instanceof InventoryCreature && ((InventoryCreature)this.inventory).getTypeFromSlot(this.getSlotIndex()) != null
         ? 1
         : this.inventory.getMaxStackSize();
   }

   @Override
   public int getMaxStackSize() {
      return this.getSlotStackLimit();
   }

   public boolean canTakeStack(Player player) {
      return true;
   }

   @Override
   public boolean mayPickup(Player player) {
      return this.canTakeStack(player);
   }

   public int getSlotIndex() {
      return this.getContainerSlot();
   }

   public ItemStack getStack() {
      return this.getItem();
   }

   public boolean getHasStack() {
      return this.hasItem();
   }

   public void putStack(ItemStack stack) {
      this.set(stack);
   }

   public void onSlotChanged() {
      this.setChanged();
   }

   public ItemStack decrStackSize(int amount) {
      return this.remove(amount);
   }
}
