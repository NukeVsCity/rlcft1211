package com.github.alexthe666.iceandfire.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntityAmphithereArrow: 2.5 damage and a gust that throws the target back and up. */
public class EntityAmphithereArrow extends AbstractArrow {
   public EntityAmphithereArrow(EntityType<? extends EntityAmphithereArrow> type, Level level) {
      super(type, level);
      this.setBaseDamage(2.5);
   }

   public EntityAmphithereArrow(Level level, double x, double y, double z, ItemStack pickup, @Nullable ItemStack weapon) {
      super(AirWaterEntities.AMPHITHERE_ARROW.get(), x, y, z, level, pickup, weapon);
      this.setBaseDamage(2.5);
   }

   public EntityAmphithereArrow(Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
      super(AirWaterEntities.AMPHITHERE_ARROW.get(), shooter, level, pickup, weapon);
      this.setBaseDamage(2.5);
   }

   @Override
   public void tick() {
      super.tick();
      if ((this.tickCount == 1 || this.tickCount % 70 == 0) && !this.inGround && !this.onGround()) {
         this.playSound(AirWaterRefs.sound("amphithere_gust"), 1.0F, 1.0F);
      }
      if (this.level().isClientSide && !this.inGround) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         Vec3 m = this.getDeltaMovement();
         double xRatio = m.x * (double)this.getBbHeight();
         double zRatio = m.z * (double)this.getBbHeight();
         this.level().addParticle(ParticleTypes.CLOUD, this.getX() + xRatio + (double)(this.random.nextFloat() * this.getBbWidth()) - (double)this.getBbWidth() - d0 * 10.0,
            this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()) - d1 * 10.0,
            this.getZ() + zRatio + (double)(this.random.nextFloat() * this.getBbWidth()) - (double)this.getBbWidth() - d2 * 10.0, d0, d1, d2);
      }
   }

   /** EntityArrow.arrowHit */
   @Override
   protected void doPostHurtEffects(LivingEntity living) {
      super.doPostHurtEffects(living);
      living.hasImpulse = true;
      Vec3 a = this.getDeltaMovement();
      double xRatio = a.x;
      double zRatio = a.z;
      float strength = -1.4F;
      float f = (float)Math.sqrt(xRatio * xRatio + zRatio * zRatio);
      Vec3 m = living.getDeltaMovement();
      living.setDeltaMovement(m.x / 2.0 - xRatio / (double)f * (double)strength, 0.6, m.z / 2.0 - zRatio / (double)f * (double)strength);
      living.hurtMarked = true;
      this.spawnExplosionParticle();
   }

   public void spawnExplosionParticle() {
      if (this.level().isClientSide) {
         for (int height = 0; height < 1 + this.random.nextInt(2); height++) {
            for (int i = 0; i < 20; i++) {
               double d0 = this.random.nextGaussian() * 0.02;
               double d1 = this.random.nextGaussian() * 0.02;
               double d2 = this.random.nextGaussian() * 0.02;
               Vec3 m = this.getDeltaMovement();
               double xRatio = m.x * (double)height;
               double zRatio = m.z * (double)height;
               this.level().addParticle(ParticleTypes.CLOUD,
                  this.getX() + xRatio + (double)(this.random.nextFloat() * this.getBbWidth() * 5.0F) - (double)this.getBbWidth() - d0 * 10.0,
                  this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()) - d1 * 10.0 + (double)height,
                  this.getZ() + zRatio + (double)(this.random.nextFloat() * this.getBbWidth() * 5.0F) - (double)this.getBbWidth() - d2 * 10.0, d0, d1, d2);
            }
         }
      } else {
         this.level().broadcastEntityEvent(this, (byte)20);
      }
   }

   @Override
   public void handleEntityEvent(byte id) {
      if (id == 20) {
         this.spawnExplosionParticle();
      } else {
         super.handleEntityEvent(id);
      }
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(AirWaterRefs.item("amphithere_arrow"));
   }
}
