package com.github.alexthe666.iceandfire.entity.tile;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModEntityHooks;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 TileEntityEggInIce: an ice dragon egg frozen in water hatches after dragonEggTime ticks into a tamed ice dragon (variant
 * type.ordinal() - 4, random gender, hunger 50). "Age" is saved as a byte like 1.12 (so it wraps when the chunk reloads).
 */
public class TileEntityEggInIce extends BlockEntity {
   public EnumDragonEgg type;
   public int age;
   public int ticksExisted;
   @Nullable
   public UUID ownerUUID;

   public TileEntityEggInIce(BlockPos pos, BlockState state) {
      super(ModBlocks.EGG_IN_ICE_TILE.get(), pos, state);
   }

   @Override
   protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.saveAdditional(tag, registries);
      tag.putByte("Color", (byte)(this.type != null ? this.type.ordinal() : 0));
      tag.putByte("Age", (byte)this.age);
      tag.putString("OwnerUUID", this.ownerUUID == null ? "" : this.ownerUUID.toString());
   }

   @Override
   protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
      super.loadAdditional(tag, registries);
      this.type = EnumDragonEgg.values()[tag.getByte("Color")];
      this.age = tag.getByte("Age");
      String s = tag.getString("OwnerUUID");
      if (!s.isEmpty()) {
         this.ownerUUID = UUID.fromString(s);
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

   /** Breaking the ice gives the egg back (EntityDragonEgg at the block, owner kept). */
   public void spawnEgg() {
      if (this.type != null && ModEntityHooks.spawnEggFromIce != null) {
         ModEntityHooks.spawnEggFromIce.spawn(this.level, this.type, this.worldPosition, this.ownerUUID);
      }
   }

   public static void tick(Level level, BlockPos pos, BlockState state, TileEntityEggInIce te) {
      te.update();
   }

   public void update() {
      this.age++;
      if (this.age >= IceAndFire.CONFIG.dragonEggTime && this.type != null) {
         this.level.destroyBlock(this.worldPosition, false);
         this.level.setBlockAndUpdate(this.worldPosition, Blocks.WATER.defaultBlockState());
         if (ModEntityHooks.hatchIceDragon != null) {
            ModEntityHooks.hatchIceDragon.spawn(this.level, this.type, this.worldPosition, this.ownerUUID);
         }
      }
      this.ticksExisted++;
   }
}
