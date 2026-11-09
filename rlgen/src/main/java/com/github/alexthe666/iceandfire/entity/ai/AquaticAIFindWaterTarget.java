package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.Legacy112;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 AquaticAIFindWaterTarget */
public class AquaticAIFindWaterTarget extends EntityAIBase {
   private final PathfinderMob mob;
   private final int range;
   private final boolean avoidAttacker;

   public AquaticAIFindWaterTarget(PathfinderMob mob, int range, boolean avoidAttacker) {
      this.mob = mob;
      this.range = range;
      this.avoidAttacker = avoidAttacker;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (this.mob.isInWater() && !this.mob.isPassenger() && !this.mob.isVehicle()) {
         Path path = this.mob.getNavigation().getPath();
         if (this.mob.getRandom().nextFloat() < 0.15F
            || path != null && path.getEndNode() != null && this.mob.distanceToSqr(path.getEndNode().x, path.getEndNode().y, path.getEndNode().z) < 3.0) {
            if (path != null && path.getEndNode() != null) {
               this.mob.getNavigation().stop();
            }
            if (this.mob.getNavigation().isDone()) {
               BlockPos vec3 = this.findWaterTarget();
               if (vec3 != null) {
                  this.mob.getNavigation().moveTo(vec3.getX(), vec3.getY(), vec3.getZ(), 1.0);
                  return true;
               }
            }
         }
         return false;
      } else {
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return false;
   }

   public BlockPos findWaterTarget() {
      BlockPos blockpos = BlockPos.containing(this.mob.getX(), this.mob.getBoundingBox().minY, this.mob.getZ());
      if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) {
         return AirWaterUtils.pos(this.mob.getTarget());
      } else {
         for (int i = 0; i < 10; i++) {
            BlockPos blockpos1 = blockpos.offset(this.mob.getRandom().nextInt(20) - 10, this.mob.getRandom().nextInt(6) - 3, this.mob.getRandom().nextInt(20) - 10);
            if (Legacy112.isWaterMaterial(this.mob.level().getBlockState(blockpos1))) {
               return blockpos1;
            }
         }
         return null;
      }
   }

   public boolean isDirectPathBetweenPoints(PathfinderMob entity, Vec3 vec1, Vec3 vec2) {
      return AirWaterUtils.rayTraceBlocks(entity, vec1, new Vec3(vec2.x, vec2.y + (double)entity.getBbHeight() * 0.5, vec2.z), false) == null;
   }
}
