package com.alcatrazescapee.alcatrazcore.inventory.container;

import com.alcatrazescapee.alcatrazcore.tile.ITileFields;
import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * alcatrazcore 1.12 ContainerTileInventory: tile slots, then the player inventory at (8, 84) / hotbar (8, 142); ITileFields are sent
 * to the client (ContainerData = 1.12 sendWindowProperty/updateProgressBar). canInteractWith is always true, like 1.12.
 */
public abstract class ContainerTileInventory<T extends TileInventory> extends AbstractContainerMenu {
   protected final T tile;
   protected final Inventory playerInv;

   protected ContainerTileInventory(MenuType<?> type, int windowId, Inventory playerInv, T tile) {
      this(type, windowId, playerInv, tile, 0, 0);
   }

   protected ContainerTileInventory(MenuType<?> type, int windowId, Inventory playerInv, T tile, int playerSlotOffsetX, int playerSlotOffsetY) {
      super(type, windowId);
      this.tile = tile;
      this.playerInv = playerInv;
      this.addContainerSlots();
      this.addPlayerInventorySlots(playerInv, playerSlotOffsetX, playerSlotOffsetY);
      if (tile instanceof ITileFields fields) {
         this.addDataSlots(new ContainerData() {
            @Override
            public int get(int id) {
               return fields.getField(id);
            }

            @Override
            public void set(int id, int value) {
               fields.setField(id, value);
            }

            @Override
            public int getCount() {
               return fields.getFieldCount();
            }
         });
      }
   }

   public T getTile() {
      return this.tile;
   }

   @Override
   public ItemStack quickMoveStack(Player player, int index) {
      Slot slot = this.slots.get(index);
      if (slot != null && slot.hasItem()) {
         ItemStack stack = slot.getItem();
         ItemStack stackCopy = stack.copy();
         int containerSlots = this.slots.size() - player.getInventory().items.size();
         if (index < containerSlots) {
            if (!this.moveItemStackTo(stack, containerSlots, this.slots.size(), true)) {
               return ItemStack.EMPTY;
            }
            this.tile.setAndUpdateSlots(index);
         } else {
            for (int i = 0; i < containerSlots; i++) {
               if (this.slots.get(i).mayPlace(stack) && this.moveItemStackTo(stack, i, i + 1, false)) {
                  this.tile.setAndUpdateSlots(i);
               }
            }
         }
         if (stack.getCount() == 0) {
            slot.setByPlayer(ItemStack.EMPTY);
         } else {
            slot.setChanged();
         }
         if (stack.getCount() == stackCopy.getCount()) {
            return ItemStack.EMPTY;
         }
         slot.onTake(player, stack);
         return stackCopy;
      }
      return ItemStack.EMPTY;
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   protected abstract void addContainerSlots();

   protected void addPlayerInventorySlots(Inventory playerInv, int offsetX, int offsetY) {
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 9; j++) {
            this.addSlot(new Slot(playerInv, j + i * 9 + 9, offsetX + 8 + j * 18, offsetY + 84 + i * 18));
         }
      }
      for (int k = 0; k < 9; k++) {
         this.addSlot(new Slot(playerInv, k, offsetX + 8 + k * 18, offsetY + 142));
      }
   }
}
