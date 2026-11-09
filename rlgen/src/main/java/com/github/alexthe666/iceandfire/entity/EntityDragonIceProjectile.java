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
 * Ice and Fire 1.7.1 EntityDragonIceProjectile: one puff of the ice breath stream. Its impact is an {@link IceExplosion};
 * an entity hit directly also takes 3 dragon ice damage and is frozen for 200 ticks. Unlike fire it survives water.
 */
public class EntityDragonIceProjectile extends LegacyFireball implements IDragonProjectile {
   public EntityDragonIceProjectile(EntityType<? extends EntityDragonIceProjectile> type, Level worldIn) {
      super(type, worldIn);
   }

   public EntityDragonIceProjectile(Level worldIn, double posX, double posY, double posZ, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_ICE.get(), worldIn, posX, posY, posZ, accelX, accelY, accelZ);
   }

   public EntityDragonIceProjectile(Level worldIn, EntityDragonBase shooter, double accelX, double accelY, double accelZ) {
      super(IafDragonEntities.DRAGON_ICE.get(), worldIn, shooter, accelX, accelY, accelZ);
      this.setSize(0.5F, 0.5F);
      double d0 = Math.sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ);
      this.accelerationX = accelX / d0 * 0.1 * (double)(shooter.isFlying() ? 4 * shooter.getDragonStage() : 1);
      this.accelerationY = accelY / d0 * 0.1 * (double)(shooter.isFlying() ? 4 * shooter.getDragonStage() : 1);
      this.accelerationZ = accelZ / d0 * 0.1 * (double)(shooter.isFlying() ? 4 * shooter.getDragonStage() : 1);
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
      super.onUpdate();

      for (int i = 0; i < 6; i++) {
         DragonParticles.spawnParticle("dragonice", this.level(), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
      }

      if (this.tickCount > 160) {
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

         if (hit != null && !(hit instanceof IDragonProjectile) && this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && hit != this.shootingEntity
            || hit == null) {
            if (this.shootingEntity != null
               && (
                  hit == this.shootingEntity
                     || this.shootingEntity instanceof EntityDragonBase & hit instanceof TamableAnimal
                        && ((EntityDragonBase)this.shootingEntity).getOwner() == ((TamableAnimal)hit).getOwner()
               )) {
               return;
            }

            if (this.shootingEntity != null && this.shootingEntity instanceof EntityDragonBase && IceAndFire.CONFIG.dragonGriefing != 2) {
               IceExplosion explosion = new IceExplosion(
                  this.level(), this.shootingEntity, this.getX(), this.getY(), this.getZ(), (float)((EntityDragonBase)this.shootingEntity).getDragonStage() * 2.5F, flag
               );
               explosion.doExplosionA();
               explosion.doExplosionB(true);
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

            if (this.shootingEntity != null
               && this.shootingEntity instanceof EntityDragonBase
               && hit instanceof LivingEntity
               && ((LivingEntity)hit).getHealth() == 0.0F) {
               ((EntityDragonBase)this.shootingEntity).attackDecision = true;
            }

            EntityDragonFire.applyEnchantments(this.shootingEntity, hit);
            hit.hurt(DragonCompat.dragonIce(this.level()), 3.0F);
            if (hit instanceof LivingEntity) {
               DragonCompat.setFrozenFor(hit, 200);
            }
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
      return 1.0F;
   }
}
