package com.lycanitesmobs.core.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;

// STUB(lyc/server): stand-in until lyc/server pushes the real class.
public abstract class EntityParticle extends ThrowableProjectile {
   protected EntityParticle(EntityType<? extends ThrowableProjectile> type, Level level) {
      super(type, level);
   }

   public ResourceLocation getTexture() {
      return null;
   }
}
