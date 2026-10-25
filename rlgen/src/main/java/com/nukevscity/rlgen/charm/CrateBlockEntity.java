package com.nukevscity.rlgen.charm;

import com.nukevscity.rlgen.RLGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrateBlockEntity extends RandomizableContainerBlockEntity {
   private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

   public CrateBlockEntity(BlockPos pos, BlockState state) {
      super(RLGen.CRATE_BE.get(), pos, state);
   }

   @Override
   public int getContainerSize() {
      return 9;
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> items) {
      this.items = items;
   }

   @Override
   protected Component getDefaultName() {
      return Component.translatable("block.rlgen.crate");
   }

   @Override
   protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
      return new DispenserMenu(id, inventory, this);
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
      super.loadAdditional(tag, provider);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(tag)) {
         ContainerHelper.loadAllItems(tag, this.items, provider);
      }
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
      super.saveAdditional(tag, provider);
      if (!this.trySaveLootTable(tag)) {
         ContainerHelper.saveAllItems(tag, this.items, provider);
      }
   }
}
