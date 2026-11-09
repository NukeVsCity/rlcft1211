package com.github.alexthe666.iceandfire.inventory;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 1.12 ContainerMyrmexCocoon: a 2-row chest layout. */
public class ContainerMyrmexCocoon extends AbstractContainerMenu {
   private final Container lowerChestInventory;
   private final int numRows;

   public ContainerMyrmexCocoon(int id, Inventory playerInventory) {
      this(id, playerInventory, new SimpleContainer(18), playerInventory.player);
   }

   public ContainerMyrmexCocoon(int id, Inventory playerInventory, Container chestInventory, Player player) {
      super(ModBlocks.MYRMEX_COCOON_MENU.get(), id);
      this.lowerChestInventory = chestInventory;
      this.numRows = chestInventory.getContainerSize() / 9;
      chestInventory.startOpen(player);
      int i = (this.numRows - 4) * 18;
      for (int j = 0; j < this.numRows; j++) {
         for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(chestInventory, k + j * 9, 8 + k * 18, 18 + j * 18));
         }
      }
      for (int l = 0; l < 3; l++) {
         for (int j1 = 0; j1 < 9; j1++) {
            this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + i));
         }
      }
      for (int i1 = 0; i1 < 9; i1++) {
         this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + i));
      }
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return this.lowerChestInventory.stillValid(playerIn);
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index < this.numRows * 9) {
            if (!this.moveItemStackTo(itemstack1, this.numRows * 9, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 0, this.numRows * 9, false)) {
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
      this.lowerChestInventory.stopOpen(playerIn);
   }

   public Container getLowerChestInventory() {
      return this.lowerChestInventory;
   }
}
