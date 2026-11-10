package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/** 1.12.2 net.minecraft.entity.ai.EntityAIFollowOwner. A mob with its own 1.12 isSitting() implements {@link SittingState}. */
public class EntityAIFollowOwner extends EntityAIBase {
   public interface SittingState {
      boolean isSitting();
   }

   private final TamableAnimal tameable;
   private LivingEntity owner;
   Level world;
   private final double followSpeed;
   private final PathNavigation petPathfinder;
   private int timeToRecalcPath;
   float maxDist;
   float minDist;
   private float oldWaterCost;

   public EntityAIFollowOwner(TamableAnimal tameable, double speed, float minDist, float maxDist) {
      this.tameable = tameable;
      this.world = tameable.level();
      this.followSpeed = speed;
      this.petPathfinder = tameable.getNavigation();
      this.minDist = minDist;
      this.maxDist = maxDist;
      this.setMutexBits(3);
      if (!(tameable.getNavigation() instanceof GroundPathNavigation) && !(tameable.getNavigation() instanceof FlyingPathNavigation)) {
         throw new IllegalArgumentException("Unsupported mob type for FollowOwnerGoal");
      }
   }

   private boolean isSitting() {
      return this.tameable instanceof SittingState ? ((SittingState)this.tameable).isSitting() : this.tameable.isInSittingPose();
   }

   @Override
   public boolean shouldExecute() {
      LivingEntity living = this.tameable.getOwner();
      if (living == null) {
         return false;
      } else if (living instanceof Player && living.isSpectator()) {
         return false;
      } else if (this.isSitting()) {
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
      return !this.petPathfinder.isDone() && this.tameable.distanceToSqr(this.owner) > (double)(this.maxDist * this.maxDist) && !this.isSitting();
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
      this.petPathfinder.stop();
      this.tameable.setPathfindingMalus(PathType.WATER, this.oldWaterCost);
   }

   @Override
   public void updateTask() {
      int pitchSpeed = this.tameable instanceof EntityAIWatchClosest.FaceSpeeds ? ((EntityAIWatchClosest.FaceSpeeds)this.tameable).getVerticalFaceSpeed() : 40;
      this.tameable.getLookControl().setLookAt(this.owner, 10.0F, (float)pitchSpeed);
      if (!this.isSitting()) {
         if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = 10;
            if (!this.petPathfinder.moveTo(this.owner, this.followSpeed)) {
               if (!this.tameable.isLeashed() && !this.tameable.isPassenger()) {
                  if (!(this.tameable.distanceToSqr(this.owner) < 144.0)) {
                     int i = Mth.floor(this.owner.getX()) - 2;
                     int j = Mth.floor(this.owner.getZ()) - 2;
                     int k = Mth.floor(this.owner.getBoundingBox().minY);

                     for (int l = 0; l <= 4; l++) {
                        for (int i1 = 0; i1 <= 4; i1++) {
                           if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && this.isTeleportFriendlyBlock(i, j, k, l, i1)) {
                              this.tameable
                                 .moveTo((double)((float)(i + l) + 0.5F), (double)k, (double)((float)(j + i1) + 0.5F), this.tameable.getYRot(), this.tameable.getXRot());
                              this.petPathfinder.stop();
                              return;
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   protected boolean isTeleportFriendlyBlock(int x, int z, int y, int xOffset, int zOffset) {
      BlockPos blockpos = new BlockPos(x + xOffset, y - 1, z + zOffset);
      BlockState state = this.world.getBlockState(blockpos);
      return state.isFaceSturdy(this.world, blockpos, Direction.DOWN)
         && state.isValidSpawn(this.world, blockpos, this.tameable.getType())
         && this.world.isEmptyBlock(blockpos.above())
         && this.world.isEmptyBlock(blockpos.above(2));
   }
}
