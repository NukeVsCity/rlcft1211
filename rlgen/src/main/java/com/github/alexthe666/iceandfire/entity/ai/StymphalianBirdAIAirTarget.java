package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityStymphalianBird;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 StymphalianBirdAIAirTarget */
public class StymphalianBirdAIAirTarget extends EntityAIBase {
   private final EntityStymphalianBird bird;

   public StymphalianBirdAIAirTarget(EntityStymphalianBird bird) {
      this.bird = bird;
   }

   @Override
   public boolean shouldExecute() {
      if (this.bird != null) {
         if (!this.bird.isFlying()) {
            return false;
         } else if (this.bird.isBaby()) {
            return false;
         } else if (this.bird.doesWantToLand()) {
            return false;
         } else {
            if (this.bird.airTarget != null && this.bird.isTargetBlocked(Vec3.atLowerCornerOf(this.bird.airTarget))) {
               this.bird.airTarget = null;
            }
            if (this.bird.airTarget != null) {
               return false;
            } else {
               Vec3 vec = this.findAirTarget();
               if (vec == null) {
                  return false;
               } else {
                  this.bird.airTarget = BlockPos.containing(vec.x, vec.y, vec.z);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   /** Named continueExecuting in 1.7.1, so 1.12 never called it: shouldContinueExecuting is shouldExecute. */
   public boolean continueExecuting() {
      if (!this.bird.isFlying()) {
         return false;
      } else if (this.bird.isBaby()) {
         return false;
      } else {
         return AirWaterUtils.isStone(this.bird) ? false : this.bird.airTarget != null;
      }
   }

   public Vec3 findAirTarget() {
      return Vec3.atLowerCornerOf(getNearbyAirTarget(this.bird));
   }

   public static BlockPos getNearbyAirTarget(EntityStymphalianBird bird) {
      if (bird.getTarget() == null) {
         BlockPos pos = AirWaterUtils.getBlockInViewStymphalian(bird);
         if (pos != null && bird.level().getBlockState(pos).isAir()) {
            return pos;
         } else {
            if (bird.flock != null && bird.flock.isLeader(bird)) {
               bird.flock.setTarget(bird.airTarget);
            }
            return AirWaterUtils.pos(bird);
         }
      } else {
         return BlockPos.containing((double)(int)bird.getTarget().getX(),
            (double)((float)(int)bird.getTarget().getY() + bird.getTarget().getEyeHeight()), (double)(int)bird.getTarget().getZ());
      }
   }
}
