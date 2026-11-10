package com.github.alexthe666.iceandfire.entity.ai.legacy;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 1.12.2 net.minecraft.entity.ai.EntityAIFleeSun. */
public class EntityAIFleeSun extends EntityAIBase {
   private final PathfinderMob creature;
   private double shelterX;
   private double shelterY;
   private double shelterZ;
   private final double movementSpeed;
   private final Level world;

   public EntityAIFleeSun(PathfinderMob creature, double speed) {
      this.creature = creature;
      this.movementSpeed = speed;
      this.world = creature.level();
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.world.isDay()) {
         return false;
      } else if (!this.creature.isOnFire()) {
         return false;
      } else if (!this.world.canSeeSky(BlockPos.containing(this.creature.getX(), this.creature.getBoundingBox().minY, this.creature.getZ()))) {
         return false;
      } else if (!this.creature.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
         return false;
      } else {
         Vec3 vec3d = this.findPossibleShelter();
         if (vec3d == null) {
            return false;
         } else {
            this.shelterX = vec3d.x;
            this.shelterY = vec3d.y;
            this.shelterZ = vec3d.z;
            return true;
         }
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.creature.getNavigation().isDone();
   }

   @Override
   public void startExecuting() {
      this.creature.getNavigation().moveTo(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
   }

   @Nullable
   private Vec3 findPossibleShelter() {
      RandomSource random = this.creature.getRandom();
      BlockPos blockpos = BlockPos.containing(this.creature.getX(), this.creature.getBoundingBox().minY, this.creature.getZ());

      for (int i = 0; i < 10; i++) {
         BlockPos pos = blockpos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
         if (!this.world.canSeeSky(pos) && this.creature.getWalkTargetValue(pos) < 0.0F) {
            return new Vec3((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
         }
      }

      return null;
   }
}
