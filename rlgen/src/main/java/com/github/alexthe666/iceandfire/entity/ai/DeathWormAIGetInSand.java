package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.IafLand;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 1.12 DeathWormAIGetInSand: out of sand, walk to a sand block nearby. */
public class DeathWormAIGetInSand extends Goal {
   private final EntityDeathWorm creature;
   private double shelterX;
   private double shelterY;
   private double shelterZ;
   private final double movementSpeed;
   private final Level world;

   public DeathWormAIGetInSand(EntityDeathWorm theCreatureIn, double movementSpeedIn) {
      this.creature = theCreatureIn;
      this.movementSpeed = movementSpeedIn;
      this.world = theCreatureIn.level();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (!this.creature.isVehicle() && !this.creature.isInSand() && (this.creature.getTarget() == null || this.creature.getTarget().isInWater())) {
         Vec3 vec3d = this.findPossibleShelter();
         if (vec3d == null) {
            return false;
         } else {
            this.shelterX = vec3d.x;
            this.shelterY = vec3d.y;
            this.shelterZ = vec3d.z;
            return true;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.creature.getNavigation().isDone();
   }

   @Override
   public void start() {
      this.creature.getNavigation().moveTo(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
   }

   @Nullable
   private Vec3 findPossibleShelter() {
      RandomSource random = this.creature.getRandom();
      BlockPos blockpos = BlockPos.containing(this.creature.getX(), this.creature.getBoundingBox().minY, this.creature.getZ());

      for (int i = 0; i < 10; i++) {
         BlockPos blockpos1 = blockpos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
         if (IafLand.isSandMaterial(this.world.getBlockState(blockpos1))) {
            return new Vec3((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
         }
      }

      return null;
   }
}
