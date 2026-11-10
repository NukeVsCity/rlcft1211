package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;

/** 1.12.2 EntityAIMate (the baby is made by 1.21 Animal.spawnChildFromBreeding, which does what 1.12 spawnBaby did). */
public class EntityAIMate extends EntityAIBase {
   private final Animal animal;
   private final Class<? extends Animal> mateClass;
   Level world;
   private Animal targetMate;
   int spawnBabyDelay;
   double moveSpeed;

   public EntityAIMate(Animal animal, double speed) {
      this(animal, speed, animal.getClass());
   }

   public EntityAIMate(Animal animal, double speed, Class<? extends Animal> mateClass) {
      this.animal = animal;
      this.world = animal.level();
      this.mateClass = mateClass;
      this.moveSpeed = speed;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.animal.isInLove()) {
         return false;
      } else {
         this.targetMate = this.getNearbyMate();
         return this.targetMate != null;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.targetMate.isAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
   }

   @Override
   public void resetTask() {
      this.targetMate = null;
      this.spawnBabyDelay = 0;
   }

   @Override
   public void updateTask() {
      this.animal.getLookControl().setLookAt(this.targetMate, 10.0F, Legacy112.verticalFaceSpeed(this.animal));
      this.animal.getNavigation().moveTo(this.targetMate, this.moveSpeed);
      this.spawnBabyDelay++;
      if (this.spawnBabyDelay >= 60 && this.animal.distanceToSqr(this.targetMate) < 9.0) {
         if (this.world instanceof ServerLevel server) {
            this.animal.spawnChildFromBreeding(server, this.targetMate);
         }
      }
   }

   private Animal getNearbyMate() {
      List<? extends Animal> list = this.world.getEntitiesOfClass(this.mateClass, this.animal.getBoundingBox().inflate(8.0));
      double d0 = Double.MAX_VALUE;
      Animal found = null;
      for (Animal other : list) {
         if (this.animal.canMate(other) && this.animal.distanceToSqr(other) < d0) {
            found = other;
            d0 = this.animal.distanceToSqr(other);
         }
      }
      return found;
   }
}
