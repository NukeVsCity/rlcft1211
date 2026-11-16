package com.lycanitesmobs.core.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in for the 1.12 BaseProjectileEntity until lyc/server pushes the real one.
public abstract class BaseProjectileEntity extends ThrowableProjectile {
   protected BaseProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
      super(type, level);
   }

   public float getProjectileScale() {
      return 1.0F;
   }
}
