package com.github.alexthe666.iceandfire.entity;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Ice and Fire 1.7.1 EntityDragonSkull: the placed skull trophy. Like 1.12 its update does nothing but zero the body/head
 * rotation (it never moves); any hit turns it back into the skull item. 1.12 getType/setType are getDragonType/setDragonType.
 */
public class EntityDragonSkull extends Animal implements IBlacklistedFromStatues, IDeadMob {
   private static final EntityDataAccessor<Integer> DRAGON_TYPE = SynchedEntityData.defineId(EntityDragonSkull.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DRAGON_AGE = SynchedEntityData.defineId(EntityDragonSkull.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Integer> DRAGON_STAGE = SynchedEntityData.defineId(EntityDragonSkull.class, EntityDataSerializers.INT);
   private static final EntityDataAccessor<Float> DRAGON_DIRECTION = SynchedEntityData.defineId(EntityDragonSkull.class, EntityDataSerializers.FLOAT);
   public final float minSize = 0.3F;
   public final float maxSize = 8.58F;

   public EntityDragonSkull(EntityType<? extends EntityDragonSkull> type, Level worldIn) {
      super(type, worldIn);
      this.noCulling = true;
   }

   public static AttributeSupplier.Builder createAttributes() {
      return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.MAX_HEALTH, 10.0);
   }

   @Override
   public boolean isInvulnerableTo(DamageSource source) {
      return source.getEntity() != null || super.isInvulnerableTo(source);
   }

   @Override
   public boolean isNoAi() {
      return true;
   }

   public boolean isOnWall() {
      return this.level().isEmptyBlock(this.blockPosition().below());
   }

   @Override
   public void tick() {
      this.yBodyRotO = 0.0F;
      this.yHeadRotO = 0.0F;
      this.yBodyRot = 0.0F;
      this.yHeadRot = 0.0F;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
      super.defineSynchedData(builder);
      builder.define(DRAGON_TYPE, 0);
      builder.define(DRAGON_AGE, 0);
      builder.define(DRAGON_STAGE, 0);
      builder.define(DRAGON_DIRECTION, 0.0F);
   }

   public float getDragonYaw() {
      return this.entityData.get(DRAGON_DIRECTION);
   }

   public void setDragonYaw(float yaw) {
      this.entityData.set(DRAGON_DIRECTION, yaw);
   }

   public int getDragonType() {
      return this.entityData.get(DRAGON_TYPE);
   }

   public void setDragonType(int type) {
      this.entityData.set(DRAGON_TYPE, type);
   }

   public int getStage() {
      return this.entityData.get(DRAGON_STAGE);
   }

   public void setStage(int stage) {
      this.entityData.set(DRAGON_STAGE, stage);
   }

   public int getDragonAge() {
      return this.entityData.get(DRAGON_AGE);
   }

   public void setDragonAge(int age) {
      this.entityData.set(DRAGON_AGE, age);
   }

   @Override
   public SoundEvent getHurtSound(DamageSource damageSourceIn) {
      return null;
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      this.turnIntoItem();
      return super.hurt(source, amount);
   }

   public void turnIntoItem() {
      if (!this.isRemoved()) {
         this.discard();
         ItemStack stack = DragonCompat.dragonSkull(this.getDragonType(), this.getStage(), this.getDragonAge());
         if (!this.level().isClientSide) {
            this.spawnAtLocation(stack, 0.0F);
         }
      }
   }

   @Override
   public InteractionResult mobInteract(Player player, InteractionHand hand) {
      if (player.isShiftKeyDown()) {
         this.setDragonYaw(player.getYRot());
      }

      return super.mobInteract(player, hand);
   }

   @Override
   public void readAdditionalSaveData(CompoundTag compound) {
      this.setDragonType(compound.getInt("Type"));
      this.setStage(compound.getInt("Stage"));
      this.setDragonAge(compound.getInt("DragonAge"));
      this.setDragonYaw(compound.getFloat("DragonYaw"));
      super.readAdditionalSaveData(compound);
   }

   @Override
   public void addAdditionalSaveData(CompoundTag compound) {
      compound.putInt("Type", this.getDragonType());
      compound.putInt("Stage", this.getStage());
      compound.putInt("DragonAge", this.getDragonAge());
      compound.putFloat("DragonYaw", this.getDragonYaw());
      super.addAdditionalSaveData(compound);
   }

   public float getDragonSize() {
      float step = -0.06624F;
      return this.getDragonAge() > 125 ? 0.3F + step * 125.0F : 0.3F + step * (float)this.getDragonAge();
   }

   @Override
   public boolean removeWhenFarAway(double distance) {
      return false;
   }

   @Override
   public boolean isPushable() {
      return false;
   }

   @Override
   protected void doPush(Entity entity) {
   }

   @Nullable
   @Override
   public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob ageable) {
      return null;
   }

   @Override
   public boolean isFood(ItemStack stack) {
      return false;
   }

   @Override
   public boolean canBeTurnedToStone() {
      return false;
   }

   @Override
   public boolean isMobDead() {
      return true;
   }
}
