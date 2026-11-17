package com.alcatrazescapee.alcatrazcore.inventory.container;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * alcatrazcore 1.12 ContainerItemStack: a container backed by the held item. itemIndex is the slot of the held stack (main hand
 * only, else -100); it cannot be picked up, swapped or shift-clicked while the container is open.
 */
public abstract class ContainerItemStack extends AbstractContainerMenu {
   protected final ItemStack stack;
   protected final Player player;
   protected int itemIndex;

   protected ContainerItemStack(MenuType<?> type, int windowId, Inventory playerInv, ItemStack stack, int itemIndexOffset) {
      super(type, windowId);
      this.player = playerInv.player;
      this.stack = stack;
      if (stack == this.player.getMainHandItem()) {
         this.itemIndex = playerInv.selected + 27 + itemIndexOffset;
      } else {
         this.itemIndex = -100;
      }
      this.addContainerSlots();
      this.addPlayerInventorySlots(playerInv);
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      Slot slot = this.slots.get(index);
      if (slot == null || !slot.hasItem() || index == this.itemIndex) {
         return ItemStack.EMPTY;
      }
      ItemStack itemstack1 = slot.getItem();
      ItemStack itemstack = itemstack1.copy();
      int containerSlots = this.slots.size() - player.getInventory().items.size();
      if (index < containerSlots) {
         if (!this.moveItemStackTo(itemstack1, containerSlots, this.slots.size(), true)) {
            return ItemStack.EMPTY;
         }
      } else if (!this.moveItemStackTo(itemstack1, 0, containerSlots, false)) {
         return ItemStack.EMPTY;
      }
      if (itemstack1.getCount() == 0) {
         slot.setByPlayer(ItemStack.EMPTY);
      } else {
         slot.setChanged();
      }
      if (itemstack1.getCount() == itemstack.getCount()) {
         return ItemStack.EMPTY;
      }
      slot.onTake(player, itemstack1);
      return itemstack;
   }

   @Override
   public void clicked(int slotId, int dragType, ClickType clickType, Player player) {
      if ((clickType == ClickType.QUICK_MOVE || clickType == ClickType.PICKUP || clickType == ClickType.SWAP) && slotId == this.itemIndex) {
         return;
      }
      super.clicked(slotId, dragType, clickType, player);
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   protected abstract void addContainerSlots();

   protected void addPlayerInventorySlots(Inventory playerInv) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }
      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
      }
   }
}
