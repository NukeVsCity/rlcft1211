package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import com.github.alexthe666.iceandfire.entity.ai.legacy.RandomPositionGenerator;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 EntitySheepAIFollowCyclops: sheep walk to the nearest cyclops (the shepherd). */
public class EntitySheepAIFollowCyclops extends Goal {
   Animal childAnimal;
   EntityCyclops cyclops;
   double moveSpeed;
   private int delayCounter;

   public EntitySheepAIFollowCyclops(Animal animal, double speed) {
      this.childAnimal = animal;
      this.moveSpeed = speed;
   }

   @Override
   public boolean canUse() {
      List<EntityCyclops> list = this.childAnimal.level().getEntitiesOfClass(EntityCyclops.class, this.childAnimal.getBoundingBox().inflate(16.0, 8.0, 16.0));
      EntityCyclops cyclops = null;
      double d0 = Double.MAX_VALUE;

      for (EntityCyclops cyclops1 : list) {
         double d1 = this.childAnimal.distanceToSqr(cyclops1);
         if (d1 <= d0) {
            d0 = d1;
            cyclops = cyclops1;
         }
      }

      if (cyclops == null) {
         return false;
      } else if (d0 < 10.0) {
         return false;
      } else {
         this.cyclops = cyclops;
         return true;
      }
   }

   @Override
   public boolean canContinueToUse() {
      if (!this.cyclops.isAlive()) {
         return false;
      } else {
         double d0 = this.childAnimal.distanceToSqr(this.cyclops);
         return d0 >= 9.0 && d0 <= 256.0;
      }
   }

   @Override
   public void start() {
      this.delayCounter = 0;
   }

   @Override
   public void stop() {
      this.cyclops = null;
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      if (--this.delayCounter <= 0) {
         this.delayCounter = 10;
         Path path = this.getPathToEntityLiving(this.childAnimal, this.cyclops);
         if (path != null) {
            this.childAnimal.getNavigation().moveTo(path, this.moveSpeed);
         }
      }
   }

   public Path getPathToEntityLiving(Animal entityIn, EntityCyclops cyclops) {
      PathNavigation navi = entityIn.getNavigation();
      // 1.12 RandomPositionGenerator.findRandomTargetBlockTowards(entity, 2, 7, cyclops pos)
      Vec3 vec3d = RandomPositionGenerator.findRandomTargetBlockTowards(entityIn, 2, 7, new Vec3(cyclops.getX(), cyclops.getY(), cyclops.getZ()));
      if (vec3d != null) {
         BlockPos blockpos = BlockPos.containing(vec3d);
         return navi.createPath(blockpos, 0);
      } else {
         return null;
      }
   }
}
