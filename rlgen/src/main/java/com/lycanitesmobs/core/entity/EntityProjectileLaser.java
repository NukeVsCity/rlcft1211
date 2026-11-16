package com.lycanitesmobs.core.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in until lyc/server pushes the real class.
public abstract class EntityProjectileLaser extends BaseProjectileEntity {
   public float laserWidth;

   protected EntityProjectileLaser(EntityType<? extends ThrowableProjectile> type, Level level) {
      super(type, level);
   }

   public LaserEndProjectileEntity getLaserEnd() {
      return null;
   }

   public float getLaserWidth() {
      return this.laserWidth;
   }

   public float getLength() {
      return 0.0F;
   }

   public ResourceLocation getBeamTexture() {
      return null;
   }
}
