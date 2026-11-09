package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** 1.12.2 net.minecraft.entity.ai.EntityAIWatchClosest. Face speeds come from {@link FaceSpeeds} when the mob has its own. */
public class EntityAIWatchClosest extends EntityAIBase {
   /** 1.12 EntityLiving.getHorizontalFaceSpeed / getVerticalFaceSpeed (10 and 40 by default). */
   public interface FaceSpeeds {
      int getHorizontalFaceSpeed();

      default int getVerticalFaceSpeed() {
         return 40;
      }
   }

   protected Mob entity;
   protected Entity closestEntity;
   protected float maxDistance;
   private int lookTime;
   private final float chance;
   protected Class<? extends Entity> watchedClass;

   public EntityAIWatchClosest(Mob entity, Class<? extends Entity> watchTargetClass, float maxDistance) {
      this(entity, watchTargetClass, maxDistance, 0.02F);
   }

   public EntityAIWatchClosest(Mob entity, Class<? extends Entity> watchTargetClass, float maxDistance, float chance) {
      this.entity = entity;
      this.watchedClass = watchTargetClass;
      this.maxDistance = maxDistance;
      this.chance = chance;
      this.setMutexBits(2);
   }

   @Override
   public boolean shouldExecute() {
      if (this.entity.getRandom().nextFloat() >= this.chance) {
         return false;
      } else {
         if (this.entity.getTarget() != null) {
            this.closestEntity = this.entity.getTarget();
         }

         if (this.watchedClass == Player.class) {
            this.closestEntity = this.entity
               .level()
               .getNearestPlayer(
                  this.entity.getX(),
                  this.entity.getY(),
                  this.entity.getZ(),
                  (double)this.maxDistance,
                  e -> EntitySelector.NO_SPECTATORS.test(e) && EntitySelector.notRiding(this.entity).test(e)
               );
         } else {
            this.closestEntity = findNearest(this.entity, this.watchedClass, this.maxDistance);
         }

         return this.closestEntity != null;
      }
   }

   /** 1.12 World.findNearestEntityWithinAABB. */
   private static Entity findNearest(Mob from, Class<? extends Entity> type, float maxDistance) {
      List<? extends Entity> list = from.level().getEntitiesOfClass(type, from.getBoundingBox().inflate((double)maxDistance, 3.0, (double)maxDistance));
      Entity closest = null;
      double best = Double.MAX_VALUE;
      for (Entity e : list) {
         if (e != from && EntitySelector.NO_SPECTATORS.test(e)) {
            double d = from.distanceToSqr(e);
            if (d <= best) {
               closest = e;
               best = d;
            }
         }
      }
      return closest;
   }

   @Override
   public boolean shouldContinueExecuting() {
      if (!this.closestEntity.isAlive()) {
         return false;
      } else {
         return this.entity.distanceToSqr(this.closestEntity) > (double)(this.maxDistance * this.maxDistance) ? false : this.lookTime > 0;
      }
   }

   @Override
   public void startExecuting() {
      this.lookTime = 40 + this.entity.getRandom().nextInt(40);
   }

   @Override
   public void resetTask() {
      this.closestEntity = null;
   }

   @Override
   public void updateTask() {
      int yawSpeed = this.entity instanceof FaceSpeeds ? ((FaceSpeeds)this.entity).getHorizontalFaceSpeed() : 10;
      int pitchSpeed = this.entity instanceof FaceSpeeds ? ((FaceSpeeds)this.entity).getVerticalFaceSpeed() : 40;
      this.entity
         .getLookControl()
         .setLookAt(this.closestEntity.getX(), this.closestEntity.getY() + (double)this.closestEntity.getEyeHeight(), this.closestEntity.getZ(), (float)yawSpeed, (float)pitchSpeed);
      this.lookTime--;
   }
}
