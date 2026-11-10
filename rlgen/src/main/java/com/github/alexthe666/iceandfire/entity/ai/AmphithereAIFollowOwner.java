package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityAmphithere;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.Legacy112;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/** Ice and Fire 1.7.1 AmphithereAIFollowOwner (command 2: follow) */
public class AmphithereAIFollowOwner extends EntityAIBase {
   private final EntityAmphithere ampithere;
   private LivingEntity owner;
   Level world;
   private final double followSpeed;
   private int timeToRecalcPath;
   float maxDist;
   float minDist;
   private float oldWaterCost;

   public AmphithereAIFollowOwner(EntityAmphithere ampithereIn, double followSpeedIn, float minDistIn, float maxDistIn) {
      this.ampithere = ampithereIn;
      this.world = ampithereIn.level();
      this.followSpeed = followSpeedIn;
      this.minDist = minDistIn;
      this.maxDist = maxDistIn;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      LivingEntity living = this.ampithere.getOwner();
      if (this.ampithere.getCommand() != 2) {
         return false;
      } else if (living == null) {
         return false;
      } else if (living instanceof Player p && p.isSpectator()) {
         return false;
      } else if (this.ampithere.isSitting()) {
         return false;
      } else if (this.ampithere.distanceToSqr(living) < (double)(this.minDist * this.minDist)) {
         return false;
      } else {
         this.owner = living;
         return true;
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.noPath() && this.ampithere.distanceToSqr(this.owner) > (double)(this.maxDist * this.maxDist) && !this.ampithere.isSitting();
   }

   /** 1.7.1: flying, "no path" is moveHelper.action != WAIT (inverted, kept) */
   private boolean noPath() {
      return !this.ampithere.isFlying() ? this.ampithere.getNavigation().isDone() : this.ampithere.getMoveControl().hasWanted();
   }

   @Override
   public void startExecuting() {
      this.timeToRecalcPath = 0;
      this.oldWaterCost = this.ampithere.getPathfindingMalus(PathType.WATER);
      this.ampithere.setPathfindingMalus(PathType.WATER, 0.0F);
   }

   @Override
   public void resetTask() {
      this.owner = null;
      this.ampithere.getNavigation().stop();
      this.ampithere.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
   }

   @Override
   public void updateTask() {
      this.ampithere.getLookControl().setLookAt(this.owner, 10.0F, Legacy112.verticalFaceSpeed(this.ampithere));
      if (!this.ampithere.isSitting() && --this.timeToRecalcPath <= 0) {
         this.timeToRecalcPath = 10;
         this.tryMoveTo();
         if (!this.ampithere.isLeashed() && !this.ampithere.isPassenger() && this.ampithere.distanceToSqr(this.owner) >= 144.0) {
            int i = Mth.floor(this.owner.getX()) - 2;
            int j = Mth.floor(this.owner.getZ()) - 2;
            int k = Mth.floor(this.owner.getBoundingBox().minY);
            for (int l = 0; l <= 4; l++) {
               for (int i1 = 0; i1 <= 4; i1++) {
                  if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(i, j, k, l, i1)) {
                     this.ampithere.moveTo((float)(i + l) + 0.5F, k, (float)(j + i1) + 0.5F, this.ampithere.getYRot(), this.ampithere.getXRot());
                     this.ampithere.getNavigation().stop();
                     return;
                  }
               }
            }
         }
      }
   }

   protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset) {
      BlockPos blockpos = new BlockPos(x + xOffset, y - 1, z + zOffset);
      BlockState state = this.world.getBlockState(blockpos);
      return state.isFaceSturdy(this.world, blockpos, Direction.UP) && state.isValidSpawn(this.world, blockpos, this.ampithere.getType())
         && this.world.isEmptyBlock(blockpos.above()) && this.world.isEmptyBlock(blockpos.above(2));
   }

   private boolean tryMoveTo() {
      if (!this.ampithere.isFlying()) {
         return this.ampithere.getNavigation().moveTo(this.owner, this.followSpeed);
      } else {
         this.ampithere.getMoveControl().setWantedPosition(this.owner.getX(), this.owner.getEyeY() + 5.0 + (double)this.ampithere.getRandom().nextInt(8),
            this.owner.getZ(), 0.25);
         return true;
      }
   }
}
