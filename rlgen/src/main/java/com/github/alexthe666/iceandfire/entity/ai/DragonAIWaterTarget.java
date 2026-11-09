package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.DragonCompat;
import com.github.alexthe666.iceandfire.entity.DragonUtils;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 DragonAIWaterTarget (its continueExecuting() was dead code in 1.12, as in DragonAIAirTarget). */
public class DragonAIWaterTarget extends EntityAIBase {
   private EntityIceDragon dragon;

   public DragonAIWaterTarget(EntityIceDragon dragon) {
      this.dragon = dragon;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (this.dragon != null) {
         if (this.dragon.isDragonSleeping()) {
            return false;
         } else if (!this.dragon.isInWater()) {
            return false;
         } else if (this.dragon.getOwner() != null && this.dragon.getPassengers().contains(this.dragon.getOwner())) {
            return false;
         } else {
            if (this.dragon.waterTarget != null && this.dragon.isTargetBlocked(Vec3.atLowerCornerOf(this.dragon.waterTarget))) {
               this.dragon.waterTarget = null;
            }

            if (this.dragon.waterTarget != null) {
               return false;
            } else {
               Vec3 vec = this.findWaterTarget();
               if (vec == null) {
                  return false;
               } else {
                  this.dragon.waterTarget = BlockPos.containing(vec.x, vec.y, vec.z);
                  return true;
               }
            }
         }
      } else {
         return false;
      }
   }

   public Vec3 findWaterTarget() {
      return Vec3.atLowerCornerOf(this.getNearbyWaterTarget());
   }

   public BlockPos getNearbyWaterTarget() {
      if (this.dragon.getTarget() == null) {
         BlockPos pos = DragonUtils.getWaterBlockInView(this.dragon);
         return pos != null && DragonCompat.material(this.dragon.level(), pos) == DragonCompat.Material.WATER ? pos : this.dragon.blockPosition();
      } else {
         return new BlockPos((int)this.dragon.getTarget().getX(), (int)this.dragon.getTarget().getY(), (int)this.dragon.getTarget().getZ());
      }
   }
}
