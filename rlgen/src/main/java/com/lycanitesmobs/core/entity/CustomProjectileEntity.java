package com.lycanitesmobs.core.entity;

import com.lycanitesmobs.core.info.projectile.ProjectileInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// STUB(lyc/server): stand-in until lyc/server pushes the real class.
public abstract class CustomProjectileEntity extends BaseProjectileEntity {
   public ProjectileInfo projectileInfo;
   public float laserWidth;

   protected CustomProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
      super(type, level);
   }

   public Vec3 getLaserEnd() {
      return null;
   }

   public ResourceLocation getBeamTexture() {
      return null;
   }
}
