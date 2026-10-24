package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CocoonBlockEntity extends RandomizableContainerBlockEntity {
   private NonNullList<ItemStack> items = NonNullList.withSize(18, ItemStack.EMPTY);

   public CocoonBlockEntity(BlockPos pos, BlockState state) {
      super(RLGen.COCOON_BE.get(), pos, state);
   }

   @Override
   protected Component getDefaultName() {
      return Component.translatable("container.rlgen.myrmex_cocoon");
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
   public int getContainerSize() {
      return 18;
   }

   @Override
   protected AbstractContainerMenu createMenu(int id, Inventory inv) {
      return new ChestMenu(MenuType.GENERIC_9x2, id, inv, this, 2);
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(tag)) {
         ContainerHelper.loadAllItems(tag, this.items, registries);
      }
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      if (!this.trySaveLootTable(tag)) {
         ContainerHelper.saveAllItems(tag, this.items, registries);
      }
   }
}
