package com.lycanitesmobs.core.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in for the 1.12 BaseProjectileEntity until lyc/server pushes the real one.
public abstract class BaseProjectileEntity extends ThrowableProjectile {
   public int animationFrame = 0;
   public int animationFrameMax = 0;
   public float textureScale = 1.0F;
   public float textureOffsetY = 0.0F;

   protected BaseProjectileEntity(EntityType<? extends ThrowableProjectile> type, Level level) {
      super(type, level);
   }

   public float getProjectileScale() {
      return 1.0F;
   }

   public float getTextureOffsetY() {
      return this.textureOffsetY;
   }

   public ResourceLocation getTexture() {
      return null;
   }
}
