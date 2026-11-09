package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.inventory.ContainerPodium;
import com.github.alexthe666.iceandfire.item.ItemDragonEgg;
import com.github.alexthe666.iceandfire.item.ItemMyrmexEgg;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 TileEntityPodium: one slot shown on top of the podium (dragon/myrmex eggs, or anything through the GUI). */
public class TileEntityPodium extends BlockEntity implements WorldlyContainer, MenuProvider {
   private static final int[] slotsTop = new int[]{0};
   private NonNullList<ItemStack> stacks = NonNullList.withSize(1, ItemStack.EMPTY);

   public TileEntityPodium(BlockPos pos, BlockState state) {
      super(ModBlocks.PODIUM_TILE.get(), pos, state);
   }

   @Override
   public int getContainerSize() {
      return this.stacks.size();
   }

   @Override
   public ItemStack getItem(int index) {
      return this.stacks.get(index);
   }

   public ItemStack getStackInSlot(int index) {
      return this.stacks.get(index);
   }

   @Override
   public ItemStack removeItem(int index, int count) {
      if (!this.stacks.get(index).isEmpty()) {
         if (this.stacks.get(index).getCount() <= count) {
            ItemStack itemstack = this.stacks.get(index);
            this.stacks.set(index, ItemStack.EMPTY);
            this.sync();
            return itemstack;
         }
         ItemStack itemstack = this.stacks.get(index).split(count);
         if (this.stacks.get(index).isEmpty()) {
            this.stacks.set(index, ItemStack.EMPTY);
         }
         this.sync();
         return itemstack;
      }
      return ItemStack.EMPTY;
   }

   @Override
   public ItemStack removeItemNoUpdate(int index) {
      return ItemStack.EMPTY;
   }

   @Override
   public void setItem(int index, ItemStack stack) {
      this.stacks.set(index, stack);
      if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
         stack.setCount(this.getMaxStackSize());
      }
      this.sync();
   }

   /** 1.12 sent MessageUpdatePodium to all clients; 1.21 sends the block entity update packet. */
   private void sync() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   @Override
   protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.loadAdditional(compound, registries);
      this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      ContainerHelper.loadAllItems(compound, this.stacks, registries);
   }

   @Override
   protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.saveAdditional(compound, registries);
      ContainerHelper.saveAllItems(compound, this.stacks, true, registries);
   }

   @Override
   public boolean canPlaceItemThroughFace(int index, ItemStack stack, Direction direction) {
      return index != 0 || stack.getItem() instanceof ItemDragonEgg || stack.getItem() instanceof ItemMyrmexEgg;
   }

   @Override
   public boolean stillValid(Player player) {
      return true;
   }

   @Override
   public void clearContent() {
      this.stacks.clear();
   }

   @Override
   public int[] getSlotsForFace(Direction side) {
      return slotsTop;
   }

   @Override
   public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
      return false;
   }

   /** 1.12 isItemValidForSlot returned false (only canInsertItem allows eggs through hoppers). */
   @Override
   public boolean canPlaceItem(int index, ItemStack stack) {
      return false;
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   @Override
   public Component getDisplayName() {
      return Component.translatable("tile.iceandfire.podium.name");
   }

   @Override
   public boolean isEmpty() {
      for (ItemStack s : this.stacks) {
         if (!s.isEmpty()) {
            return false;
         }
      }
      return true;
   }

   @Override
   public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
      return new ContainerPodium(id, inv, this);
   }
}
