package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.IafLand;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/** 1.12 DeathWormAIFindSandTarget: in sand, swim to the target or to a random sand block. */
public class DeathWormAIFindSandTarget extends Goal {
   private EntityDeathWorm mob;
   private int range;
   private boolean avoidAttacker;

   public DeathWormAIFindSandTarget(EntityDeathWorm mob, int range) {
      this.mob = mob;
      this.range = range;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      if (this.mob.isInSand() && !this.mob.isPassenger() && !this.mob.isVehicle()) {
         if (this.mob.getRandom().nextFloat() < 0.5F) {
            Path path = this.mob.getNavigation().getPath();
            if (path != null
               || !this.mob.getNavigation().isDone()
                  && !this.isDirectPathBetweenPoints(this.mob, this.mob.position(), new Vec3((double)path.getEndNode().x, (double)path.getEndNode().y, (double)path.getEndNode().z))) {
               this.mob.getNavigation().stop();
            }

            if (this.mob.getNavigation().isDone()) {
               BlockPos vec3 = this.findSandTarget();
               if (vec3 != null) {
                  this.mob.getNavigation().moveTo((double)vec3.getX(), (double)vec3.getY(), (double)vec3.getZ(), 1.0);
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
   public boolean canContinueToUse() {
      return false;
   }

   public BlockPos findSandTarget() {
      if (this.mob.getTarget() != null && this.mob.getTarget().isAlive()) {
         BlockPos blockpos1 = this.mob.getTarget().blockPosition();
         return new BlockPos(blockpos1.getX(), blockpos1.getY() - 1, blockpos1.getZ());
      } else {
         List<BlockPos> sand = new ArrayList<>();
         BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
         if (this.mob.isTame() && this.mob.getWormHome() != null) {
            this.range = 25;

            for (int x = this.mob.getWormHome().getX() - this.range; x < this.mob.getWormHome().getX() + this.range; x++) {
               for (int y = this.mob.getWormHome().getY() - this.range; y < this.mob.getWormHome().getY() + this.range; y++) {
                  for (int z = this.mob.getWormHome().getZ() - this.range; z < this.mob.getWormHome().getZ() + this.range; z++) {
                     if (IafLand.isSandMaterial(this.mob.level().getBlockState(p.set(x, y, z)))
                        && this.isDirectPathBetweenPoints(this.mob, this.mob.position(), new Vec3((double)x, (double)y, (double)z))) {
                        sand.add(new BlockPos(x, y, z));
                     }
                  }
               }
            }
         } else {
            for (int x = (int)this.mob.getX() - this.range; x < (int)this.mob.getX() + this.range; x++) {
               for (int y = (int)this.mob.getY() - this.range; y < (int)this.mob.getY() + this.range; y++) {
                  for (int zx = (int)this.mob.getZ() - this.range; zx < (int)this.mob.getZ() + this.range; zx++) {
                     if (IafLand.isSandMaterial(this.mob.level().getBlockState(p.set(x, y, zx)))
                        && this.isDirectPathBetweenPoints(this.mob, this.mob.position(), new Vec3((double)x, (double)y, (double)zx))) {
                        sand.add(new BlockPos(x, y, zx));
                     }
                  }
               }
            }
         }

         return !sand.isEmpty() ? sand.get(this.mob.getRandom().nextInt(sand.size())) : null;
      }
   }

   public boolean isDirectPathBetweenPoints(Entity entity, Vec3 vec1, Vec3 vec2) {
      return true;
   }
}
