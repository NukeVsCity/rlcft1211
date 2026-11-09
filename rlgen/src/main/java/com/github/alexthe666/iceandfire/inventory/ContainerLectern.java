package com.github.alexthe666.iceandfire.inventory;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** 1.12 ContainerLectern (furnace layout: bestiary 44,35; manuscript 80,58; output 116,35). Fields synced through ContainerData. */
public class ContainerLectern extends AbstractContainerMenu {
   private final Container tileFurnace;
   public final ContainerData fields;

   public ContainerLectern(int id, Inventory playerInv) {
      this(id, playerInv, new SimpleContainer(3), new SimpleContainerData(4));
   }

   public ContainerLectern(int id, Inventory playerInv, Container furnaceInventory, ContainerData fields) {
      super(ModBlocks.LECTERN_MENU.get(), id);
      this.tileFurnace = furnaceInventory;
      this.fields = fields;
      this.addSlot(new Slot(furnaceInventory, 0, 44, 35));
      this.addSlot(new Slot(furnaceInventory, 1, 80, 58));
      this.addSlot(new SlotLectern(playerInv.player, furnaceInventory, 2, 116, 35));
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
         }
      }
      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
      }
      this.addDataSlots(fields);
   }

   @Override
   public boolean stillValid(Player playerIn) {
      return this.tileFurnace.stillValid(playerIn);
   }

   @Override
   public ItemStack quickMoveStack(Player playerIn, int index) {
      ItemStack itemstack = ItemStack.EMPTY;
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack itemstack1 = slot.getItem();
         itemstack = itemstack1.copy();
         if (index == 2) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
               return ItemStack.EMPTY;
            }
            slot.onQuickCraft(itemstack1, itemstack);
         } else if (index != 1 && index != 0) {
            if (!itemstack1.isEmpty() && itemstack1.getItem() == ModItems.bestiary) {
               if (!this.moveItemStackTo(itemstack1, 0, 1, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (!itemstack1.isEmpty() && itemstack1.getItem() == ModItems.manuscript) {
               if (!this.moveItemStackTo(itemstack1, 1, 2, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 3 && index < 30) {
               if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                  return ItemStack.EMPTY;
               }
            } else if (index >= 30 && index < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
               return ItemStack.EMPTY;
            }
         } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
            return ItemStack.EMPTY;
         }
         if (itemstack1.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
         if (itemstack1.getCount() == itemstack.getCount()) {
            return ItemStack.EMPTY;
         }
         slot.onTake(playerIn, itemstack1);
      }
      return itemstack;
   }

   /** 1.12 SlotLectern: output only. */
   public static class SlotLectern extends Slot {
      public SlotLectern(Player player, Container inv, int slotIndex, int x, int y) {
         super(inv, slotIndex, x, y);
      }

      @Override
      public boolean mayPlace(ItemStack stack) {
         return false;
      }
   }
}
