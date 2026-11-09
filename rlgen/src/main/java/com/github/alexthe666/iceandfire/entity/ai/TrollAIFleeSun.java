package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityTroll;
import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** 1.12 TrollAIFleeSun (the shelter must not see the sky and must be a bright spot: getBlockPathWeight below 0, as in 1.7.1). */
public class TrollAIFleeSun extends Goal {
   private final EntityTroll troll;
   private double shelterX;
   private double shelterY;
   private double shelterZ;
   private final double movementSpeed;
   private final Level world;

   public TrollAIFleeSun(EntityTroll theCreatureIn, double movementSpeedIn) {
      this.troll = theCreatureIn;
      this.movementSpeed = movementSpeedIn;
      this.world = theCreatureIn.level();
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (!this.world.isDay()) {
         return false;
      } else if (!this.world.canSeeSky(BlockPos.containing(this.troll.getX(), this.troll.getBoundingBox().minY, this.troll.getZ()))) {
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
   public boolean canContinueToUse() {
      return !this.troll.getNavigation().isDone();
   }

   @Override
   public void start() {
      this.troll.getNavigation().moveTo(this.shelterX, this.shelterY, this.shelterZ, this.movementSpeed);
   }

   @Nullable
   private Vec3 findPossibleShelter() {
      RandomSource random = this.troll.getRandom();
      BlockPos blockpos = BlockPos.containing(this.troll.getX(), this.troll.getBoundingBox().minY, this.troll.getZ());

      for (int i = 0; i < 10; i++) {
         BlockPos blockpos1 = blockpos.offset(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
         if (!this.world.canSeeSky(blockpos1) && this.troll.getWalkTargetValue(blockpos1) < 0.0F) {
            return new Vec3((double)blockpos1.getX(), (double)blockpos1.getY(), (double)blockpos1.getZ());
         }
      }

      return null;
   }
}
