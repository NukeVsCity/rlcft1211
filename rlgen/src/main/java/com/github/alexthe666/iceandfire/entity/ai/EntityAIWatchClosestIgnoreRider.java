package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Ice and Fire 1.7.1 EntityAIWatchClosestIgnoreRider. Its own "entity" field shadows the parent's and is never set, so it asks
 * whether the watched entity rides or is ridden by null: never. The task rolls its chance and searches, then never runs (kept).
 */
public class EntityAIWatchClosestIgnoreRider extends EntityAIWatchClosest {
   Mob entity;

   public EntityAIWatchClosestIgnoreRider(Mob entity, Class<LivingEntity> type, float dist) {
      super(entity, type, dist);
   }

   @Override
   public boolean shouldExecute() {
      return super.shouldExecute() && this.closestEntity != null && isRidingOrBeingRiddenBy(this.closestEntity, this.entity);
   }

   /** 1.12 Entity.isRidingOrBeingRiddenBy */
   private static boolean isRidingOrBeingRiddenBy(Entity e, Entity other) {
      for (Entity passenger : e.getPassengers()) {
         if (passenger.equals(other)) {
            return true;
         }
         if (isRidingOrBeingRiddenBy(passenger, other)) {
            return true;
         }
      }
      return false;
   }
}
