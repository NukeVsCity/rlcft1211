package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModSounds;
import com.github.alexthe666.iceandfire.enums.EnumDragonEgg;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Ice and Fire 1.7.1 EntityDragonEgg: a placed egg. Fire eggs standing in fire count up and hatch a tamed baby fire dragon
 * after "Dragon Egg Hatch Time" (7200) ticks in fire; ice eggs in water turn into an egg-in-ice block (1 in 500 per tick).
 * Punching it gives the egg item back. 1.12 getType/setType are getEggType/setEggType (1.21 Entity.getType is the entity type).
 */
public class EntityDragonEgg extends Mob implements IBlacklistedFromStatues, IDeadMob {
   private static final EntityDataAccessor<Integer> DRAGON_TYPE = SynchedEntityData.defineId(EntityDragonEgg.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DRAGON_AGE = SynchedEntityData.defineId(EntityDragonEgg.class, EntityDataSerializers.INT);
   protected static final EntityDataAccessor<Optional<UUID>> OWNER_UNIQUE_ID = SynchedEntityData.defineId(EntityDragonEgg.class, EntityDataSerializers.OPTIONAL_UUID);

   public EntityDragonEgg(EntityType<? extends EntityDragonEgg> type, Level worldIn) {
      super(type, worldIn);
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.MAX_HEALTH, 10.0);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
      tag.putInt("Color", (byte)this.getEggType().ordinal());
      tag.putByte("DragonAge", (byte)this.getDragonAge());
      if (this.getOwnerId() == null) {
         tag.putString("OwnerUUID", "");
      } else {
         tag.putString("OwnerUUID", this.getOwnerId().toString());
      }
   }

   @Override
   public void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
      this.setEggType(EnumDragonEgg.values()[Math.max(0, Math.min(7, tag.getInt("Color")))]);
      this.setDragonAge(tag.getByte("DragonAge"));
      String s = tag.getString("OwnerUUID");
      if (!s.isEmpty()) {
         this.setOwnerId(UUID.fromString(s));
      }
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DRAGON_TYPE, 0);
      builder.define(DRAGON_AGE, 0);
      builder.define(OWNER_UNIQUE_ID, Optional.empty());
   }

   @Nullable
   public UUID getOwnerId() {
      return this.entityData.get(OWNER_UNIQUE_ID).orElse(null);
   }

   public void setOwnerId(@Nullable UUID uuid) {
      this.entityData.set(OWNER_UNIQUE_ID, Optional.ofNullable(uuid));
   }

   public EnumDragonEgg getEggType() {
      return EnumDragonEgg.values()[this.entityData.get(DRAGON_TYPE)];
   }

   public void setEggType(EnumDragonEgg newtype) {
      this.entityData.set(DRAGON_TYPE, newtype.ordinal());
   }

   /** 1.12 isEntityInvulnerable: anything with a source entity. */
   @Override
   public boolean isInvulnerableTo(DamageSource source) {
      return source.getEntity() != null || super.isInvulnerableTo(source);
   }

   public int getDragonAge() {
      return this.entityData.get(DRAGON_AGE);
   }

   public void setDragonAge(int i) {
      this.entityData.set(DRAGON_AGE, i);
   }

   @Override
   public void tick() {
      super.tick();
      this.setAirSupply(200);
      BlockPos pos = this.blockPosition();
      DragonCompat.Material material = DragonCompat.material(this.level(), pos);
      if (material == DragonCompat.Material.FIRE && this.getEggType().isFire) {
         this.setDragonAge(this.getDragonAge() + 1);
      }

      if (material == DragonCompat.Material.WATER && !this.getEggType().isFire && this.getRandom().nextInt(500) == 0) {
         this.discard();
         this.level().setBlockAndUpdate(pos, DragonCompat.state(ModBlocks.eggInIce));
         this.level().playLocalSound(this.getX(), this.getY() + (double)this.getEyeHeight(), this.getZ(), SoundEvents.GLASS_BREAK, this.getSoundSource(), 2.5F, 1.0F, false);
         DragonCompat.onEggInIce(this.level(), pos, this.getEggType(), this.getOwnerId());
      }

      if (this.getDragonAge() > IceAndFire.CONFIG.dragonEggTime && DragonCompat.material(this.level(), pos) == DragonCompat.Material.FIRE && this.getEggType().isFire) {
         this.level().removeBlock(pos, false);
         EntityFireDragon dragon = new EntityFireDragon(IafDragonEntities.FIRE_DRAGON.get(), this.level());
         dragon.setVariant(this.getEggType().ordinal());
         dragon.setGender(this.getRandom().nextBoolean());
         dragon.setPos((double)pos.getX() + 0.5, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5);
         dragon.setHunger(50);
         if (!this.level().isClientSide) {
            this.level().addFreshEntity(dragon);
         }

         dragon.setTame(true, true);
         dragon.setOwnerUUID(this.getOwnerId());
         this.level().playLocalSound(this.getX(), this.getY() + (double)this.getEyeHeight(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, this.getSoundSource(), 2.5F, 1.0F, false);
         this.level().playLocalSound(this.getX(), this.getY() + (double)this.getEyeHeight(), this.getZ(), DragonCompat.snd(ModSounds.DRAGON_HATCH), this.getSoundSource(), 2.5F, 1.0F, false);
         this.discard();
      }
   }

   @Override
   public boolean isNoAi() {
      return false;
   }

   @Override
   public SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return null;
   }

   /** 1.12 attackEntityFrom: any hit (even an invulnerable one) pops the egg back as an item. */
   @Override
   public boolean hurt(DamageSource source, float amount) {
      // 1.12 never called attackEntityFrom with fire damage on a fire-immune entity (the egg sits in fire to hatch)
      if (this.fireImmune() && source.is(DamageTypeTags.IS_FIRE)) {
         return false;
      }
      if (!this.level().isClientSide && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !this.isRemoved()) {
         this.spawnAtLocation(this.getItem(), 0.0F);
      }

      this.discard();
      return super.hurt(source, amount);
   }

   private ItemStack getItem() {
      switch (this.getEggType()) {
         case GREEN:
            return new ItemStack(ModItems.dragonegg_green);
         case BRONZE:
            return new ItemStack(ModItems.dragonegg_bronze);
         case GRAY:
            return new ItemStack(ModItems.dragonegg_gray);
         case BLUE:
            return new ItemStack(ModItems.dragonegg_blue);
         case WHITE:
            return new ItemStack(ModItems.dragonegg_white);
         case SAPPHIRE:
            return new ItemStack(ModItems.dragonegg_sapphire);
         case SILVER:
            return new ItemStack(ModItems.dragonegg_silver);
         default:
            return new ItemStack(ModItems.dragonegg_red);
      }
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Override
   public boolean canBeTurnedToStone() {
      return false;
   }

   public void onPlayerPlace(Player player) {
      this.setOwnerId(player.getUUID());
   }

   @Override
   public boolean isMobDead() {
      return true;
   }

   // 1.12 EntityLiving.canDespawn() was not overridden: an egg more than 128 blocks from every player can despawn, as in 1.12.
}
