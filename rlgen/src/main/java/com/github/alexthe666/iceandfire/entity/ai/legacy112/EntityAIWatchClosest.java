package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** 1.12.2 EntityAIWatchClosest */
public class EntityAIWatchClosest extends EntityAIBase {
   protected Mob entity;
   protected Entity closestEntity;
   protected float maxDistance;
   private int lookTime;
   private final float chance;
   protected Class<? extends Entity> watchedClass;

   public EntityAIWatchClosest(Mob entity, Class<? extends Entity> watchTarget, float maxDistance) {
      this(entity, watchTarget, maxDistance, 0.02F);
   }

   public EntityAIWatchClosest(Mob entity, Class<? extends Entity> watchTarget, float maxDistance, float chance) {
      this.entity = entity;
      this.watchedClass = watchTarget;
      this.maxDistance = maxDistance;
      this.chance = chance;
      this.setMutexBits(2);
   }

   /** EntitySelectors.notRiding(entity) */
   private boolean notRiding(Entity e) {
      Entity a = this.entity;
      while (a.isPassenger()) {
         a = a.getVehicle();
         if (a == e) {
            return false;
         }
      }
      return true;
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
            this.closestEntity = Legacy112.getClosestPlayer(this.entity.level(), this.entity.getX(), this.entity.getY(), this.entity.getZ(),
               this.maxDistance, e -> Legacy112.notSpectating(e) && this.notRiding(e));
         } else {
            this.closestEntity = Legacy112.findNearestEntityWithinAABB(this.watchedClass,
               this.entity.getBoundingBox().inflate(this.maxDistance, 3.0, this.maxDistance), this.entity);
         }

         return this.closestEntity != null;
      }
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
      this.entity.getLookControl().setLookAt(this.closestEntity.getX(), this.closestEntity.getEyeY(), this.closestEntity.getZ(),
         Legacy112.horizontalFaceSpeed(this.entity), Legacy112.verticalFaceSpeed(this.entity));
      this.lookTime--;
   }
}
