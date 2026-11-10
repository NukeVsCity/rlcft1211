package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 HippogryphAIAirTarget */
public class HippogryphAIAirTarget extends EntityAIBase {
   private final EntityHippogryph hippogryph;

   public HippogryphAIAirTarget(EntityHippogryph dragon) {
      this.hippogryph = dragon;
   }

   @Override
   public boolean shouldExecute() {
      if (this.hippogryph != null) {
         if (!this.hippogryph.isFlying() && !this.hippogryph.isHovering()) {
            return false;
         } else if (this.hippogryph.isSitting()) {
            return false;
         } else if (this.hippogryph.isBaby()) {
            return false;
         } else if (this.hippogryph.getOwner() != null && this.hippogryph.getPassengers().contains(this.hippogryph.getOwner())) {
            return false;
         } else {
            if (this.hippogryph.airTarget != null && this.hippogryph.getDistanceSquared(new Vec3((double)this.hippogryph.airTarget.getX(),
               this.hippogryph.getY(), (double)this.hippogryph.airTarget.getZ())) > 3.0F) {
               this.hippogryph.airTarget = null;
            }
            if (this.hippogryph.airTarget != null) {
               return false;
            } else {
               Vec3 vec = this.findAirTarget();
               if (vec == null) {
                  return false;
               } else {
                  this.hippogryph.airTarget = BlockPos.containing(vec.x, vec.y, vec.z);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   /** Named continueExecuting in 1.7.1 (the pre-1.12 name), so 1.12 never called it: shouldContinueExecuting is shouldExecute. */
   public boolean continueExecuting() {
      if (!this.hippogryph.isFlying() && !this.hippogryph.isHovering()) {
         return false;
      } else if (this.hippogryph.isSitting()) {
         return false;
      } else if (this.hippogryph.isBaby()) {
         return false;
      } else {
         return AirWaterUtils.isStone(this.hippogryph) ? false : this.hippogryph.airTarget != null;
      }
   }

   public Vec3 findAirTarget() {
      return Vec3.atLowerCornerOf(this.getNearbyAirTarget());
   }

   public BlockPos getNearbyAirTarget() {
      if (this.hippogryph.getTarget() == null) {
         for (int i = 0; i < 10; i++) {
            BlockPos pos = AirWaterUtils.getBlockInViewHippogryph(this.hippogryph);
            if (pos != null && this.hippogryph.level().getBlockState(pos).isAir()) {
               return pos;
            }
         }
      } else {
         BlockPos pos = new BlockPos((int)this.hippogryph.getTarget().getX(), (int)this.hippogryph.getTarget().getY(), (int)this.hippogryph.getTarget().getZ());
         if (this.hippogryph.level().getBlockState(pos).isAir()) {
            return pos;
         }
      }
      return AirWaterUtils.pos(this.hippogryph);
   }
}
