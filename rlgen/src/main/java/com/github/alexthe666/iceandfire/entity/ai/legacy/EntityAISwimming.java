package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;

/** 1.12.2 net.minecraft.entity.ai.EntityAISwimming. */
public class EntityAISwimming extends EntityAIBase {
   private final Mob entity;

   public EntityAISwimming(Mob entity) {
      this.entity = entity;
      this.setMutexBits(4);
      if (entity.getNavigation() instanceof GroundPathNavigation) {
         ((GroundPathNavigation)entity.getNavigation()).setCanFloat(true);
      } else if (entity.getNavigation() instanceof FlyingPathNavigation) {
         ((FlyingPathNavigation)entity.getNavigation()).setCanFloat(true);
      }
   }

   @Override
   public boolean shouldExecute() {
      return this.entity.isInWater() || this.entity.isInLava();
   }

   @Override
   public void updateTask() {
      if (this.entity.getRandom().nextFloat() < 0.8F) {
         this.entity.getJumpControl().jump();
      }
   }
}
