package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Ice and Fire 1.7.1 EntityDragonIceCharge: the ice ball (snow and dragon ice spikes instead of fire). Quirk kept: its onUpdate runs the whole 1.12 EntityFireball
 * update and then its own copy of it, so it moves (and accelerates) twice per tick.
 */
public class EntityDragonIceCharge extends LegacyFireball implements IDragonProjectile {
   public int ticksInAir;

   public EntityDragonIceCharge(EntityType<? extends EntityDragonIceCharge> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityDragonIceCharge(Level worldIn, double posX, double posY, double posZ, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_ICE_CHARGE.get(), worldIn, posX, posY, posZ, accelX, accelY, accelZ);
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.07;
      this.accelerationY = accelY / d0 * 0.07;
      this.accelerationZ = accelZ / d0 * 0.07;
   }

   public EntityDragonIceCharge(Level worldIn, EntityDragonBase shooter, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_ICE_CHARGE.get(), worldIn, shooter, accelX, accelY, accelZ);
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.07;
      this.accelerationY = accelY / d0 * 0.07;
      this.accelerationZ = accelZ / d0 * 0.07;
   }

   public void setSizes(float width, float height) {
      this.setSize(width, height);
   }

   @Override
   protected boolean isFireballFiery() {
      return false;
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public void onUpdate() {
      for (int i = 0; i < 14; i++) {
         DragonParticles.spawnParticle(
            "snowflake",
            this.level(),
            this.getX() + this.random.nextDouble() * 1.0 * (double)(this.random.nextBoolean() ? -1 : 1),
            this.getY() + this.random.nextDouble() * 1.0 * (double)(this.random.nextBoolean() ? -1 : 1),
            this.getZ() + this.random.nextDouble() * 1.0 * (double)(this.random.nextBoolean() ? -1 : 1),
            0.0,
            0.0,
            0.0
         );
      }

      this.resolveShooter();
      if (this.level().isClientSide || !this.shooterDead() && this.level().hasChunkAt(this.blockPosition())) {
         super.onUpdate();
         if (this.isFireballFiery()) {
            this.igniteForSeconds(1.0F);
         }

         this.ticksInAir++;
         RayTraceResult raytraceresult = this.forwardsRaycast(false, this.ticksInAir >= 25, this.shootingEntity);
         if (raytraceresult != null) {
            this.onImpact(raytraceresult);
         }

         this.moveAlong();
         this.level().addParticle(this.getParticleType(), this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
         this.setPos(this.getX(), this.getY(), this.getZ());
      } else {
         this.discard();
      }
   }

   @Override
   protected void onImpact(RayTraceResult movingObject) {
      boolean flag = EntityDragonFire.mobGriefing(this.level());
      if (!this.level().isClientSide) {
         Entity hit = movingObject.entityHit;
         if (hit != null && hit instanceof IDragonProjectile) {
            return;
         }

         if (hit != null
            && this.shootingEntity != null
            && this.shootingEntity instanceof EntityDragonBase
            && ((EntityDragonBase)this.shootingEntity).isTame()
            && hit instanceof Player
            && ((EntityDragonBase)this.shootingEntity).isOwner((Player)hit)) {
            return;
         }

         if (hit != null && !(hit instanceof IDragonProjectile) && hit != this.shootingEntity || hit == null) {
            if (this.shootingEntity != null
               && (
                  hit == this.shootingEntity
                     || this.shootingEntity instanceof EntityDragonBase & hit instanceof TamableAnimal
                        && ((EntityDragonBase)this.shootingEntity).isOwner(((EntityDragonBase)this.shootingEntity).getOwner())
               )) {
               return;
            }

            if (this.shootingEntity != null && IceAndFire.CONFIG.dragonGriefing != 2) {
               int explodeSize = 2;
               if (this.shootingEntity instanceof EntityDragonBase) {
                  explodeSize = 2 + ((EntityDragonBase)this.shootingEntity).getDragonStage();
               }

               FireExplosion explosion = new FireExplosion(this.level(), this.shootingEntity, this.getX(), this.getY(), this.getZ(), (float)explodeSize, flag);
               explosion.doExplosionA();
               explosion.doExplosionB(true);
               FireChargeExplosion explosion2 = new FireChargeExplosion(
                  this.level(),
                  this.shootingEntity,
                  this.getX(),
                  this.getY(),
                  this.getZ(),
                  (float)(2 + ((EntityDragonBase)this.shootingEntity).getDragonStage()),
                  false,
                  flag
               );
               explosion2.doExplosionA();
               explosion2.doExplosionB(true);
            }

            this.discard();
         }

         if (hit != null && !(hit instanceof IDragonProjectile) && !hit.is(this.shootingEntity)) {
            if (this.shootingEntity != null
               && (
                  !hit.is(this.shootingEntity) // 1.12 quirk: negated, so any other entity returns here
                     || this.shootingEntity instanceof EntityDragonBase & hit instanceof TamableAnimal
                        && ((EntityDragonBase)this.shootingEntity).getOwner() == ((TamableAnimal)hit).getOwner()
               )) {
               return;
            }

            if (this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && !hit.is(this.shootingEntity)) {
               hit.hurt(DragonCompat.dragonIce(this.level()), 10.0F);
               if (hit instanceof LivingEntity) {
                  DragonCompat.setFrozenFor(hit, 200);
               }

               if (hit instanceof LivingEntity && ((LivingEntity)hit).getHealth() == 0.0F) {
                  ((EntityDragonBase)this.shootingEntity).attackDecision = true;
               }
            }

            EntityDragonFire.applyEnchantments(this.shootingEntity, hit);
            FireExplosion explosion = new FireExplosion(this.level(), null, this.getX(), this.getY(), this.getZ(), 2.0F, flag);
            if (this.shootingEntity != null) {
               explosion = new FireExplosion(this.level(), this.shootingEntity, this.getX(), this.getY(), this.getZ(), 2.0F, flag);
            }

            explosion.doExplosionA();
            explosion.doExplosionB(true);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, Level.ExplosionInteraction.BLOCK);
            this.discard();
         }
      }

      this.discard();
   }

   @Override
   public boolean hurt(DamageSource source, float amount) {
      return false;
   }

   @Override
   public float getPickRadius() {
      return 0.0F;
   }
}
