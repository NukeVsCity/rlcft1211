package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntitySiren;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 SirenAIFindWaterTarget */
public class SirenAIFindWaterTarget extends EntityAIBase {
   private final EntitySiren mob;

   public SirenAIFindWaterTarget(EntitySiren mob) {
      this.mob = mob;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.mob.isInWater()) {
         return false;
      } else {
         if (this.mob.getRandom().nextFloat() < 0.5F) {
            Path path = this.mob.getNavigation().getPath();
            if (path != null && path.getEndNode() != null || !this.mob.getNavigation().isDone() && path != null && path.getEndNode() != null
               && !this.mob.isDirectPathBetweenPoints(this.mob.position(), new Vec3(path.getEndNode().x, path.getEndNode().y, path.getEndNode().z))) {
               this.mob.getNavigation().stop();
            }
            if (this.mob.getNavigation().isDone()) {
               Vec3 vec3 = this.findWaterTarget();
               if (vec3 != null) {
                  this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0);
                  return true;
               }
            }
         }
         return false;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return false;
   }

   public Vec3 findWaterTarget() {
      if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) {
         BlockPos blockpos1 = this.mob.getTarget().blockPosition();
         return new Vec3(blockpos1.getX(), blockpos1.getY(), blockpos1.getZ());
      } else {
         List<Vec3> water = new ArrayList<>();
         List<Vec3> singTargets = new ArrayList<>();
         for (int x = (int)this.mob.getX() - 5; x < (int)this.mob.getX() + 5; x++) {
            for (int y = (int)this.mob.getY() - 5; y < (int)this.mob.getY() + 5; y++) {
               for (int z = (int)this.mob.getZ() - 5; z < (int)this.mob.getZ() + 5; z++) {
                  BlockPos pos = new BlockPos(x, y, z);
                  if (this.mob.wantsToSing() && this.mob.level().getBlockState(pos).isSolid() && this.mob.level().isEmptyBlock(pos.above())
                     && this.mob.isDirectPathBetweenPoints(this.mob.position(), new Vec3(x, y + 1, z))) {
                     singTargets.add(new Vec3(x, y + 1, z));
                  }
                  if (Legacy112.isWaterMaterial(this.mob.level().getBlockState(pos)) && this.mob.isDirectPathBetweenPoints(this.mob.position(), new Vec3(x, y, z))) {
                     water.add(new Vec3(x, y, z));
                  }
               }
            }
         }
         if (!singTargets.isEmpty()) {
            return singTargets.get(this.mob.getRandom().nextInt(singTargets.size()));
         } else {
            return !water.isEmpty() ? water.get(this.mob.getRandom().nextInt(water.size())) : null;
         }
      }
   }
}
