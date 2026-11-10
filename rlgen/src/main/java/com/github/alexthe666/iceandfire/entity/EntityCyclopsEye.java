package com.github.alexthe666.iceandfire.entity;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;

/**
 * 1.12 EntityCyclopsEye. Ice and Fire 1.7.1 created it but never added it to the world and the cyclops has no getParts(), so
 * nothing could ever hit it: onHitEye (blinding) never happened in RLCraft. Kept the same way.
 */
public class EntityCyclopsEye extends LandPart {
   public EntityCyclopsEye(Mob parent, float radius, float angleYaw, float offsetY, float sizeX, float sizeY, float damageMultiplier) {
      super(parent, radius, angleYaw, offsetY, sizeX, sizeY, damageMultiplier);
   }

   @Override
   public boolean attackEntityFrom(DamageSource source, float damage) {
      if (this.parent instanceof EntityCyclops && source.is(DamageTypeTags.IS_PROJECTILE)) {
         ((EntityCyclops)this.parent).onHitEye(source, damage);
         return true;
      } else {
         return this.parent.hurt(source, damage);
      }
   }
}
