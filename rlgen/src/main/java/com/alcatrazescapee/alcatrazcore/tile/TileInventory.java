package com.alcatrazescapee.alcatrazcore.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * alcatrazcore 1.12 TileInventory (+ TileCore): an ItemStackHandler saved as "inventory", the whole tile sent as update tag, the
 * contents dropped when the block is broken (onBreakBlock, called from the blocks' onRemove).
 */
public abstract class TileInventory extends BlockEntity {
   protected final ItemStackHandler inventory;

   public TileInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, int numSlots) {
      super(type, pos, state);
      this.inventory = new ItemStackHandler(numSlots) {
         @Override
         protected void onContentsChanged(int slot) {
            TileInventory.this.setChanged();
         }
      };
   }

   public IItemHandler getInventory() {
      return this.inventory;
   }

   public void setAndUpdateSlots(int slot) {
      this.setChanged();
   }

   public int getSlotLimit(int slot) {
      return 64;
   }

   public boolean isItemValid(int slot, ItemStack stack) {
      return true;
   }

   @Override
   protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
      super.loadAdditional(nbt, registries);
      this.inventory.deserializeNBT(registries, nbt.getCompound("inventory"));
   }

   @Override
   protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
      super.saveAdditional(nbt, registries);
      nbt.put("inventory", this.inventory.serializeNBT(registries));
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   /** TileInventory.onBreakBlock: CoreHelpers.dropItemInWorld for every slot */
   public void onBreakBlock() {
      if (this.level != null) {
         for (int i = 0; i < this.inventory.getSlots(); i++) {
            ItemStack stack = this.inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
               Containers.dropItemStack(this.level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack.copy());
            }
         }
      }
   }
}
