package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Ice and Fire 1.7.1 EntitySeaSerpentBubbles, a 1.12 EntityFireball: it homes on the serpent's target, deals 1 damage and pops out of
 * water. Its onUpdate ran EntityFireball.onUpdate and then moved it a second time, so it moves and accelerates twice per tick (kept).
 */
public class EntitySeaSerpentBubbles extends Projectile {
   public double accelerationX;
   public double accelerationY;
   public double accelerationZ;
   private int ticksInAir;

   public EntitySeaSerpentBubbles(EntityType<? extends EntitySeaSerpentBubbles> type, Level level) {
      super(type, level);
   }

   public EntitySeaSerpentBubbles(Level level, EntitySeaSerpent shooter, double accelX, double accelY, double accelZ) {
      super(AirWaterEntities.SEA_SERPENT_BUBBLES.get(), level);
      this.setOwner(shooter);
      this.moveTo(shooter.getX(), shooter.getY(), shooter.getZ(), shooter.getYRot(), shooter.getXRot());
      this.setDeltaMovement(Vec3.ZERO);
      // EntityFireball(world, shooter, ...) drew three gaussians for its noisy acceleration, which this constructor then overwrote
      this.random.nextGaussian();
      this.random.nextGaussian();
      this.random.nextGaussian();
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.1;
      this.accelerationY = accelY / d0 * 0.1;
      this.accelerationZ = accelZ / d0 * 0.1;
   }

   @Override
   protected void defineSynchedData(SynchedEntityData.Builder builder) {
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   /** isInsideOfMaterial(WATER) */
   @Override
   public boolean isInWater() {
      return this.isEyeInFluid(FluidTags.WATER);
   }

   @Override
   protected boolean canHitEntity(Entity target) {
      return super.canHitEntity(target) && (this.ticksInAir >= 25 || !target.is(this.getOwner() == null ? this : this.getOwner()));
   }

   private void raycastAndMove() {
      HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
      if (hit.getType() != HitResult.Type.MISS && !net.neoforged.neoforge.event.EventHooks.onProjectileImpact(this, hit)) {
         this.onHit(hit);
      }
      Vec3 m = this.getDeltaMovement();
      this.setPos(this.getX() + m.x, this.getY() + m.y, this.getZ() + m.z);
      ProjectileUtil.rotateTowardsMovement(this, 0.2F);
   }

   @Override
   public void tick() {
      Entity owner = this.getOwner();
      if (this.level().isClientSide || (owner == null || owner.isAlive()) && this.level().hasChunkAt(this.blockPosition())) {
         this.autoTarget();
         // EntityFireball.onUpdate
         this.baseTick();
         this.ticksInAir++;
         this.raycastAndMove();
         float f = 0.95F;
         if (this.isInWater()) {
            f = 0.8F;
         }
         this.accelerate(f);
         if (this.isRemoved()) {
            return;
         }
         // EntitySeaSerpentBubbles.onUpdate
         this.raycastAndMove();
         if (this.isInWater()) {
            for (int i = 0; i < 6; i++) {
               AirWaterEntities.spawnParticle("serpent_bubble", this.level(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
            }
         } else {
            this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, this.random.nextFloat());
            this.discard();
         }
         this.accelerate(0.95F);
      } else {
         this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, this.random.nextFloat());
         this.discard();
      }
   }

   private void accelerate(float f) {
      Vec3 m = this.getDeltaMovement();
      this.setDeltaMovement((m.x + this.accelerationX) * f, (m.y + this.accelerationY) * f, (m.z + this.accelerationZ) * f);
   }

   public void autoTarget() {
      if (this.getOwner() instanceof EntitySeaSerpent serpent && serpent.getTarget() != null) {
         Entity target = serpent.getTarget();
         double d2 = target.getX() - this.getX();
         double d3 = target.getY() - this.getY();
         double d4 = target.getZ() - this.getZ();
         double d0 = Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
         this.accelerationX = d2 / d0 * 0.1;
         this.accelerationY = d3 / d0 * 0.1;
         this.accelerationZ = d4 / d0 * 0.1;
      }
   }

   @Override
   protected void onHitEntity(EntityHitResult result) {
      Entity owner = this.getOwner();
      if (!result.getEntity().is(owner == null ? this : owner)) {
         if (!this.level().isClientSide && owner instanceof net.minecraft.world.entity.LivingEntity living) {
            result.getEntity().hurt(this.damageSources().mobAttack(living), 1.0F);
         }
         this.playSound(SoundEvents.ITEM_PICKUP, 1.0F, this.random.nextFloat());
         this.discard();
      }
   }

   @Override
   protected void onHitBlock(net.minecraft.world.phys.BlockHitResult result) {
   }

   @Override
   protected void addAdditionalSaveData(CompoundTag tag) {
      super.addAdditionalSaveData(tag);
   }

   @Override
   protected void readAdditionalSaveData(CompoundTag tag) {
      super.readAdditionalSaveData(tag);
   }

   @Override
   public boolean shouldBeSaved() {
      return false;
   }
}
