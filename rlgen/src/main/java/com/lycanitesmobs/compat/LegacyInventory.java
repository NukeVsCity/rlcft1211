package com.lycanitesmobs.compat;

import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * 1.12 IInventory / ISidedInventory method names on the 1.21 Container / WorldlyContainer: Lycanites inventories keep their
 * 1.12 methods and these defaults answer the 1.21 ones.
 */
public interface LegacyInventory extends Container {
   int getSizeInventory();

   ItemStack getStackInSlot(int index);

   ItemStack decrStackSize(int index, int count);

   ItemStack removeStackFromSlot(int index);

   void setInventorySlotContents(int index, ItemStack stack);

   default int getInventoryStackLimit() {
      return 64;
   }

   default void markDirty() {
   }

   default boolean isUsableByPlayer(Player player) {
      return true;
   }

   default void openInventory(Player player) {
   }

   default void closeInventory(Player player) {
   }

   default boolean isItemValidForSlot(int index, ItemStack stack) {
      return true;
   }

   default int getField(int id) {
      return 0;
   }

   default void setField(int id, int value) {
   }

   default int getFieldCount() {
      return 0;
   }

   default void clear() {
      for (int i = 0; i < this.getSizeInventory(); i++) {
         this.setInventorySlotContents(i, ItemStack.EMPTY);
      }
   }

   @Override
   default int getContainerSize() {
      return this.getSizeInventory();
   }

   @Override
   default ItemStack getItem(int index) {
      return this.getStackInSlot(index);
   }

   @Override
   default ItemStack removeItem(int index, int count) {
      return this.decrStackSize(index, count);
   }

   @Override
   default ItemStack removeItemNoUpdate(int index) {
      return this.removeStackFromSlot(index);
   }

   @Override
   default void setItem(int index, ItemStack stack) {
      this.setInventorySlotContents(index, stack);
   }

   @Override
   default int getMaxStackSize() {
      return this.getInventoryStackLimit();
   }

   @Override
   default void setChanged() {
      this.markDirty();
   }

   @Override
   default boolean stillValid(Player player) {
      return this.isUsableByPlayer(player);
   }

   @Override
   default void startOpen(Player player) {
      this.openInventory(player);
   }

   @Override
   default void stopOpen(Player player) {
      this.closeInventory(player);
   }

   @Override
   default boolean canPlaceItem(int index, ItemStack stack) {
      return this.isItemValidForSlot(index, stack);
   }

   @Override
   default void clearContent() {
      this.clear();
   }

   /** 1.12 ISidedInventory */
   interface Sided extends LegacyInventory, WorldlyContainer {
      int[] getSlotsForFace(Direction side);

      boolean canInsertItem(int index, ItemStack stack, Direction direction);

      boolean canExtractItem(int index, ItemStack stack, Direction direction);

      @Override
      default boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
         return this.canInsertItem(index, stack, direction);
      }

      @Override
      default boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
         return this.canExtractItem(index, stack, direction);
      }
   }
}
