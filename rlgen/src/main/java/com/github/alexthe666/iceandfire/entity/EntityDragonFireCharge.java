package com.github.alexthe666.iceandfire.entity;

import com.github.alexthe666.iceandfire.IceAndFire;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Ice and Fire 1.7.1 EntityDragonFireCharge: the fire ball. Quirk kept: its onUpdate runs the whole 1.12 EntityFireball
 * update and then its own copy of it, so it moves (and accelerates) twice per tick.
 */
public class EntityDragonFireCharge extends LegacyFireball implements IDragonProjectile {
   public int ticksInAir;

   public EntityDragonFireCharge(EntityType<? extends EntityDragonFireCharge> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityDragonFireCharge(Level worldIn, double posX, double posY, double posZ, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_FIRE_CHARGE.get(), worldIn, posX, posY, posZ, accelX, accelY, accelZ);
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.07;
      this.accelerationY = accelY / d0 * 0.07;
      this.accelerationZ = accelZ / d0 * 0.07;
   }

   public EntityDragonFireCharge(Level worldIn, EntityDragonBase shooter, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_FIRE_CHARGE.get(), worldIn, shooter, accelX, accelY, accelZ);
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.07;
      this.accelerationY = accelY / d0 * 0.07;
      this.accelerationZ = accelZ / d0 * 0.07;
   }

   public void setSizes(float width, float height) {
      this.setSize(width, height);
   }

   @Override
   public boolean isPickable() {
      return false;
   }

   @Override
   public void onUpdate() {
      for (int i = 0; i < 4; i++) {
         this.level()
            .addParticle(
               ParticleTypes.FLAME,
               this.getX() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
               this.getY() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
               this.getZ() + (this.random.nextDouble() - 0.5) * (double)this.getBbWidth(),
               0.0,
               0.0,
               0.0
            );
      }

      if (this.isInWater()) {
         this.discard();
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
                  true,
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
                  hit.is(this.shootingEntity)
                     || this.shootingEntity instanceof EntityDragonBase & hit instanceof TamableAnimal
                        && ((EntityDragonBase)this.shootingEntity).getOwner() == ((TamableAnimal)hit).getOwner()
               )) {
               return;
            }

            if (this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase) {
               hit.hurt(DragonCompat.dragonFire(this.level()), 10.0F);
               if (hit instanceof LivingEntity && ((LivingEntity)hit).getHealth() == 0.0F) {
                  ((EntityDragonBase)this.shootingEntity).attackDecision = true;
               }
            }

            hit.igniteForSeconds(5.0F);
            EntityDragonFire.applyEnchantments(this.shootingEntity, hit);
            FireExplosion explosion = new FireExplosion(this.level(), null, this.getX(), this.getY(), this.getZ(), 2.0F, flag);
            if (this.shootingEntity != null) {
               explosion = new FireExplosion(this.level(), this.shootingEntity, this.getX(), this.getY(), this.getZ(), 2.0F, flag);
            }

            explosion.doExplosionA();
            explosion.doExplosionB(true);
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 4.0F, flag ? Level.ExplosionInteraction.MOB : Level.ExplosionInteraction.NONE);
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
