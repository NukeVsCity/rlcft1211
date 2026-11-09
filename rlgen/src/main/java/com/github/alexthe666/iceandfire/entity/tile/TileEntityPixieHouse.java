package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 TileEntityPixieHouse: house type 0..5 (brown/red mushroom, birch, oak, dark oak, spruce), a resident pixie that leaves with a 1
 * in 100 chance every tick. Quirk kept: the owner UUID is read from the "TicksExisted" key, so it is lost on reload.
 */
public class TileEntityPixieHouse extends BlockEntity {
   private static final float PARTICLE_WIDTH = 0.3F;
   private static final float PARTICLE_HEIGHT = 0.6F;
   public int houseType;
   public boolean hasPixie;
   public boolean tamedPixie;
   public UUID pixieOwnerUUID;
   public int pixieType;
   public int ticksExisted;
   public NonNullList<ItemStack> pixieItems = NonNullList.withSize(1, ItemStack.EMPTY);
   private Random rand = new Random();

   public TileEntityPixieHouse(BlockPos pos, BlockState state) {
      super(ModBlocks.PIXIE_HOUSE_TILE.get(), pos, state);
   }

   @Override
   protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.saveAdditional(compound, registries);
      compound.putInt("HouseType", this.houseType);
      compound.putBoolean("HasPixie", this.hasPixie);
      compound.putInt("PixieType", this.pixieType);
      compound.putBoolean("TamedPixie", this.tamedPixie);
      if (this.pixieOwnerUUID != null) {
         compound.putUUID("PixieOwnerUUID", this.pixieOwnerUUID);
      }
      ContainerHelper.saveAllItems(compound, this.pixieItems, registries);
   }

   @Override
   protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.loadAdditional(compound, registries);
      this.houseType = compound.getInt("HouseType");
      this.hasPixie = compound.getBoolean("HasPixie");
      this.pixieType = compound.getInt("PixieType");
      this.tamedPixie = compound.getBoolean("TamedPixie");
      this.pixieOwnerUUID = compound.hasUUID("TicksExisted") ? compound.getUUID("TicksExisted") : null;
      this.pixieItems = NonNullList.withSize(1, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(compound, this.pixieItems, registries);
   }

   @Override
   public Packet<ClientGamePacketListener> getUpdatePacket() {
      return ClientboundBlockEntityDataPacket.create(this);
   }

   @Override
   public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
      return this.saveWithoutMetadata(registries);
   }

   public void sync() {
      this.setChanged();
      if (this.level != null && !this.level.isClientSide) {
         this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, TileEntityPixieHouse te) {
      te.update();
   }

   public void update() {
      this.ticksExisted++;
      if (!this.level.isClientSide && this.hasPixie && new Random().nextInt(100) == 0) {
         this.releasePixie();
      }
      if (this.hasPixie && this.level.isClientSide) {
         float[] rgb = ModEntityHooks.PIXIE_PARTICLE_RGB[Math.max(0, Math.min(4, this.pixieType))];
         ModEntityHooks.spawnPixieParticle(this.level,
            (double)((float)this.worldPosition.getX() + 0.5F) + (double)(this.rand.nextFloat() * 0.3F * 2.0F) - 0.3F,
            (double)this.worldPosition.getY() + (double)(this.rand.nextFloat() * 0.6F),
            (double)((float)this.worldPosition.getZ() + 0.5F) + (double)(this.rand.nextFloat() * 0.3F * 2.0F) - 0.3F, rgb[0], rgb[1], rgb[2]);
      }
   }

   public void releasePixie() {
      if (!this.level.isClientSide && ModEntityHooks.releasePixie != null) {
         ModEntityHooks.releasePixie.release(this.level, (double)((float)this.worldPosition.getX() + 0.5F), (double)((float)this.worldPosition.getY() + 1.0F),
            (double)((float)this.worldPosition.getZ() + 0.5F), this.pixieItems.get(0), this.pixieType, this.tamedPixie, this.pixieOwnerUUID);
      }
      this.hasPixie = false;
      this.pixieType = 0;
      this.sync();
   }
}
