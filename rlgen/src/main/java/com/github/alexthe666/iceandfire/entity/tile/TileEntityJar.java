package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.core.ModSounds;
import java.util.Random;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 TileEntityJar: holds a pixie (type 0..4), makes pixie dust once (1.12 check {@code ticksExisted % 24000 * 2 == 0}, i.e. every
 * 24000 ticks), sparkles, chirps, and releases the pixie when broken (ModEntityHooks.releasePixie).
 */
public class TileEntityJar extends BlockEntity {
   private static final float PARTICLE_WIDTH = 0.3F;
   private static final float PARTICLE_HEIGHT = 0.6F;
   public boolean hasPixie;
   public boolean hasProduced;
   public boolean tamedPixie;
   public UUID pixieOwnerUUID;
   public int pixieType;
   public int ticksExisted;
   public NonNullList<ItemStack> pixieItems = NonNullList.withSize(1, ItemStack.EMPTY);
   public float rotationYaw;
   public float prevRotationYaw;
   private Random rand = new Random();

   public TileEntityJar(BlockPos pos, BlockState state) {
      super(ModBlocks.JAR_TILE.get(), pos, state);
      this.hasPixie = state.getBlock() != ModBlocks.jar_empty;
   }

   @Override
   protected void saveAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.saveAdditional(compound, registries);
      compound.putBoolean("HasPixie", this.hasPixie);
      compound.putInt("PixieType", this.pixieType);
      compound.putBoolean("HasProduced", this.hasProduced);
      compound.putBoolean("TamedPixie", this.tamedPixie);
      if (this.pixieOwnerUUID != null) {
         compound.putUUID("PixieOwnerUUID", this.pixieOwnerUUID);
      }
      compound.putInt("TicksExisted", this.ticksExisted);
      ContainerHelper.saveAllItems(compound, this.pixieItems, registries);
   }

   @Override
   protected void loadAdditional(CompoundTag compound, HolderLookup.Provider registries) {
      super.loadAdditional(compound, registries);
      this.hasPixie = compound.getBoolean("HasPixie");
      this.pixieType = compound.getInt("PixieType");
      this.hasProduced = compound.getBoolean("HasProduced");
      this.ticksExisted = compound.getInt("TicksExisted");
      this.tamedPixie = compound.getBoolean("TamedPixie");
      this.pixieOwnerUUID = compound.hasUUID("PixieOwnerUUID") ? compound.getUUID("PixieOwnerUUID") : null;
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

   public static void tick(Level level, BlockPos pos, BlockState state, TileEntityJar te) {
      te.update();
   }

   public void update() {
      this.ticksExisted++;
      if (this.hasPixie && this.level.isClientSide) {
         float[] rgb = ModEntityHooks.PIXIE_PARTICLE_RGB[Math.max(0, Math.min(4, this.pixieType))];
         ModEntityHooks.spawnPixieParticle(this.level,
            (double)((float)this.worldPosition.getX() + 0.5F) + (double)(this.rand.nextFloat() * 0.3F * 2.0F) - 0.3F,
            (double)this.worldPosition.getY() + (double)(this.rand.nextFloat() * 0.6F),
            (double)((float)this.worldPosition.getZ() + 0.5F) + (double)(this.rand.nextFloat() * 0.3F * 2.0F) - 0.3F, rgb[0], rgb[1], rgb[2]);
      }
      if (this.ticksExisted % 24000 * 2 == 0 && !this.hasProduced && this.hasPixie && !this.level.isClientSide) {
         this.hasProduced = true;
         this.sync();
      }
      this.prevRotationYaw = this.rotationYaw;
      if (this.rand.nextInt(30) == 0) {
         this.rotationYaw = this.rand.nextFloat() * 360.0F - 180.0F;
      }
      if (this.ticksExisted % 40 == 0 && this.rand.nextInt(6) == 0 && this.level.isClientSide) {
         this.level.playLocalSound((double)this.worldPosition.getX() + 0.5, (double)this.worldPosition.getY() + 0.5,
            (double)this.worldPosition.getZ() + 0.5, ModSounds.PIXIE_IDLE, SoundSource.NEUTRAL, 1.0F, 1.0F, false);
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
