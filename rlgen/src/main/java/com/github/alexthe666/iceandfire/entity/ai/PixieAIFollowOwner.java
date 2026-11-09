package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.Legacy112;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/** Ice and Fire 1.7.1 PixieAIFollowOwner */
public class PixieAIFollowOwner extends EntityAIBase {
   private final EntityPixie tameable;
   private final double followSpeed;
   Level world;
   float maxDist;
   float minDist;
   private LivingEntity owner;
   private int timeToRecalcPath;
   private float oldWaterCost;

   public PixieAIFollowOwner(EntityPixie tameableIn, double followSpeedIn, float minDistIn, float maxDistIn) {
      this.tameable = tameableIn;
      this.world = tameableIn.level();
      this.followSpeed = followSpeedIn;
      this.minDist = minDistIn;
      this.maxDist = maxDistIn;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      LivingEntity living = this.tameable.getOwner();
      if (living == null) {
         return false;
      } else if (living instanceof Player p && p.isSpectator()) {
         return false;
      } else if (this.tameable.isSitting()) {
         return false;
      } else if (this.tameable.distanceToSqr(living) < (double)(this.minDist * this.minDist)) {
         return false;
      } else {
         this.owner = living;
         return true;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.tameable.getLegacyMoveControl().hasWanted()
         || this.tameable.distanceToSqr(this.owner) > (double)(this.maxDist * this.maxDist) && !this.tameable.isSitting();
   }

   @Override
   public void startExecuting() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.tameable.getPathfindingMalus(PathType.WATER);
      this.tameable.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   @Override
   public void resetTask() {
      this.owner = null;
      this.tameable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
      this.tameable.getLegacyMoveControl().setWait();
      this.tameable.slowSpeed = false;
   }

   private boolean isEmptyBlock(BlockPos pos) {
      BlockState state = this.world.getBlockState(pos);
      return state.isAir() ? true : !state.isCollisionShapeFullBlock(this.world, pos);
   }

   @Override
   public void updateTask() {
      this.tameable.getLookControl().setLookAt(this.owner, 10.0F, Legacy112.verticalFaceSpeed(this.tameable));
      if (!this.tameable.isSitting() && --this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         this.tameable.getMoveControl().setWantedPosition(this.owner.getX(), this.owner.getEyeY(), this.owner.getZ(), 0.25);
         this.tameable.slowSpeed = true;
         if (!this.tameable.isLeashed() && this.tameable.distanceToSqr(this.owner) >= 50.0) {
            int i = Mth.floor(this.owner.getX()) - 2;
            int j = Mth.floor(this.owner.getZ()) - 2;
            int k = Mth.floor(this.owner.getBoundingBox().minY);
            for (int l = 0; l <= 4; l++) {
               for (int i1 = 0; i1 <= 4; i1++) {
                  if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isEmptyBlock(new BlockPos(i + l, k, j + i1))
                     && this.isEmptyBlock(new BlockPos(i + l, k + 1, j + i1))) {
                     this.tameable.moveTo((float)(i + l) + 0.5F, (double)k + 1.5, (float)(j + i1) + 0.5F, this.tameable.getYRot(),
                        this.tameable.getXRot());
                     this.tameable.getLegacyMoveControl().setWait();
                     return;
                  }
               }
            }
         }
      }
   }
}
