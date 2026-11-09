package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.inventory.ContainerMyrmexCocoon;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 TileEntityMyrmexCocoon: an 18-slot loot container (myrmex food/loot chests) that squelches when opened and closed. */
public class TileEntityMyrmexCocoon extends RandomizableContainerBlockEntity {
   private NonNullList<ItemStack> chestContents = NonNullList.withSize(18, ItemStack.EMPTY);

   public TileEntityMyrmexCocoon(BlockPos pos, BlockState state) {
      super(ModBlocks.MYRMEX_COCOON_TILE.get(), pos, state);
   }

   @Override
   public int getContainerSize() {
      return 18;
   }

   @Override
   protected Component getDefaultName() {
      return Component.translatable(this.getBlockState().getBlock().getDescriptionId());
   }

   @Override
   protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.loadAdditional(compound, registries);
      this.chestContents = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
      if (!this.tryLoadLootTable(compound)) {
         ContainerHelper.loadAllItems(compound, this.chestContents, registries);
      }
   }

   @Override
   protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.saveAdditional(compound, registries);
      if (!this.trySaveLootTable(compound)) {
         ContainerHelper.saveAllItems(compound, this.chestContents, registries);
      }
   }

   @Override
   protected NonNullList<ItemStack> getItems() {
      return this.chestContents;
   }

   @Override
   protected void setItems(NonNullList<ItemStack> items) {
      this.chestContents = items;
   }

   @Override
   protected AbstractContainerMenu createMenu(int id, Inventory playerInventory) {
      return new ContainerMyrmexCocoon(id, playerInventory, this, playerInventory.player);
   }

   @Override
   public void startOpen(Player player) {
      this.unpackLootTable(null);
      if (!player.level().isClientSide) {
         player.level().playSound(null, this.worldPosition, SoundEvents.SLIME_JUMP, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   @Override
   public void stopOpen(Player player) {
      this.unpackLootTable(null);
      if (!player.level().isClientSide) {
         player.level().playSound(null, this.worldPosition, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
      }
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   public boolean isFull(ItemStack heldStack) {
      for (ItemStack itemstack : this.chestContents) {
         if (itemstack.isEmpty()
            || heldStack != null && !heldStack.isEmpty() && ItemStack.isSameItem(itemstack, heldStack)
               && itemstack.getCount() + heldStack.getCount() < itemstack.getMaxStackSize()) {
            return false;
         }
      }
      return true;
   }
}
