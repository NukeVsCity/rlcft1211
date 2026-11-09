package com.github.alexthe666.iceandfire.entity;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 EntitySeaSerpentArrow: 3 damage, ignores water drag, bubble trail. */
public class EntitySeaSerpentArrow extends AbstractArrow {
   public EntitySeaSerpentArrow(EntityType<? extends EntitySeaSerpentArrow> type, Level level) {
      super(type, level);
      this.setBaseDamage(3.0);
   }

   public EntitySeaSerpentArrow(Level level, double x, double y, double z, ItemStack pickup, @Nullable ItemStack weapon) {
      super(AirWaterEntities.SEA_SERPENT_ARROW.get(), x, y, z, level, pickup, weapon);
      this.setBaseDamage(3.0);
   }

   public EntitySeaSerpentArrow(Level level, LivingEntity shooter, ItemStack pickup, @Nullable ItemStack weapon) {
      super(AirWaterEntities.SEA_SERPENT_ARROW.get(), shooter, level, pickup, weapon);
      this.setBaseDamage(3.0);
   }

   @Override
   public void tick() {
      super.tick();
      if (this.level().isClientSide && !this.inGround) {
         double d0 = this.random.nextGaussian() * 0.02;
         double d1 = this.random.nextGaussian() * 0.02;
         double d2 = this.random.nextGaussian() * 0.02;
         Vec3 m = this.getDeltaMovement();
         double xRatio = m.x * (double)this.getBbHeight();
         double zRatio = m.z * (double)this.getBbHeight();
         this.level().addParticle(ParticleTypes.BUBBLE, this.getX() + xRatio + (double)(this.random.nextFloat() * this.getBbWidth()) - (double)this.getBbWidth() - d0 * 10.0,
            this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()) - d1 * 10.0,
            this.getZ() + zRatio + (double)(this.random.nextFloat() * this.getBbWidth()) - (double)this.getBbWidth() - d2 * 10.0, d0, d1, d2);
         this.level().addParticle(ParticleTypes.SPLASH, this.getX() + xRatio + (double)(this.random.nextFloat() * this.getBbWidth()) - (double)this.getBbWidth() - d0 * 10.0,
            this.getY() + (double)(this.random.nextFloat() * this.getBbHeight()) - d1 * 10.0,
            this.getZ() + zRatio + (double)(this.random.nextFloat() * this.getBbWidth()) - (double)this.getBbWidth() - d2 * 10.0, d0, d1, d2);
      }
   }

   @Override
   protected float getWaterInertia() {
      return 0.99F;
   }

   @Override
   protected ItemStack getDefaultPickupItem() {
      return new ItemStack(AirWaterRefs.item("sea_serpent_arrow"));
   }
}
