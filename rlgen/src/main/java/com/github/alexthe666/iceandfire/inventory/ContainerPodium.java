package com.github.alexthe666.iceandfire.inventory;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 1.12 ContainerPodium: one slot at 80,20, player inventory from y 51. */
public class ContainerPodium extends AbstractContainerMenu {
   private final Container podium;

   public ContainerPodium(int id, Inventory playerInventory) {
      this(id, playerInventory, new SimpleContainer(1));
   }

   public ContainerPodium(int id, Inventory playerInventory, Container podiumIn) {
      super(ModBlocks.PODIUM_MENU.get(), id);
      this.podium = podiumIn;
      podiumIn.startOpen(playerInventory.player);
      int b0 = 51;
      this.addSlot(new Slot(podiumIn, 0, 80, 20));
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, i * 18 + b0));
         }
      }
      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 58 + b0));
      }
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return this.podium.stillValid(playerIn);
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < this.podium.getContainerSize()) {
            if (!this.moveItemStackTo(itemstack1, this.podium.getContainerSize(), this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.podium.getContainerSize(), false)) {
            return ItemStack.EMPTY;
         }
         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
      }
      return itemstack;
   }

   @Override
   public void removed(Player playerIn) {
      super.removed(playerIn);
      this.podium.stopOpen(playerIn);
   }
}
