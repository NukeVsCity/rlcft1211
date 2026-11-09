package com.github.alexthe666.iceandfire.entity.ai.legacy;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/** 1.12.2 EntityAIWander */
public class EntityAIWander extends EntityAIBase {
   protected final PathfinderMob entity;
   protected double x;
   protected double y;
   protected double z;
   protected final double speed;
   protected int executionChance;
   protected boolean mustUpdate;

   public EntityAIWander(PathfinderMob creature, double speed) {
      this(creature, speed, 120);
   }

   public EntityAIWander(PathfinderMob creature, double speed, int chance) {
      this.entity = creature;
      this.speed = speed;
      this.executionChance = chance;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.mustUpdate) {
         if (this.entity.getNoActionTime() >= 100) {
            return false;
         }
         if (this.entity.getRandom().nextInt(this.executionChance) != 0) {
            return false;
         }
      }

      Vec3 vec = this.getPosition();
      if (vec == null) {
         return false;
      } else {
         this.x = vec.x;
         this.y = vec.y;
         this.z = vec.z;
         this.mustUpdate = false;
         return true;
      }
   }

   @Nullable
   protected Vec3 getPosition() {
      return RandomPositionGenerator.findRandomTarget(this.entity, 10, 7);
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.entity.getNavigation().isDone();
   }

   @Override
   public void startExecuting() {
      this.entity.getNavigation().moveTo(this.x, this.y, this.z, this.speed);
   }

   public void makeUpdate() {
      this.mustUpdate = true;
   }

   public void setExecutionChance(int chance) {
      this.executionChance = chance;
   }
}
