package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.DragonCompat;
import com.github.alexthe666.iceandfire.entity.DragonUtils;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Ice and Fire 1.7.1 DragonAIAirTarget: picks the next point to fly to. Its continueExecuting() was never called in 1.12
 * (the 1.12 name is shouldContinueExecuting), so the default (shouldExecute again) applies, as here.
 */
public class DragonAIAirTarget extends EntityAIBase {
   private EntityDragonBase dragon;
   private Level theWorld;

   public DragonAIAirTarget(EntityDragonBase dragon) {
      this.dragon = dragon;
      this.theWorld = dragon.level();
   }

   @Override
   public boolean shouldExecute() {
      if (this.dragon == null) {
         return false;
      } else if ((this.dragon.isFlying() || this.dragon.isHovering()) && !this.dragon.onGround()) {
         if (this.dragon.isDragonSleeping()) {
            return false;
         } else if (this.dragon.isChild()) {
            return false;
         } else if (this.dragon.getOwner() != null && this.dragon.getPassengers().contains(this.dragon.getOwner())) {
            return false;
         } else {
            if (this.dragon.airTarget != null && this.dragon.isTargetBlocked(Vec3.atLowerCornerOf(this.dragon.airTarget))) {
               this.dragon.airTarget = null;
            }

            if (this.dragon.airTarget != null) {
               return false;
            } else {
               Vec3 vec = this.findAirTarget();
               if (vec == null) {
                  return false;
               } else {
                  this.dragon.airTarget = BlockPos.containing(vec.x, vec.y, vec.z);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public Vec3 findAirTarget() {
      return Vec3.atLowerCornerOf(this.getNearbyAirTarget());
   }

   public BlockPos getNearbyAirTarget() {
      if (this.dragon.getTarget() == null) {
         BlockPos pos = DragonUtils.getBlockInView(this.dragon);
         return pos != null && this.dragon.level().getBlockState(pos).isAir() ? pos : this.dragon.blockPosition();
      } else {
         return new BlockPos((int)this.dragon.getTarget().getX(), (int)this.dragon.getTarget().getY(), (int)this.dragon.getTarget().getZ());
      }
   }
}
