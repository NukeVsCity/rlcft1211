package com.lycanitesmobs.core.entity.navigate;

import com.lycanitesmobs.ObjectManager;
import com.lycanitesmobs.compat.LycPos;
import com.lycanitesmobs.compat.LycWorld;
import com.lycanitesmobs.compat.Material;
import com.lycanitesmobs.core.entity.BaseCreatureEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.ai.navigation.PathNavigation;

/**
 * Lycanites 1.12 CreaturePathNavigate (a 1.12 PathNavigate) on the 1.21 PathNavigation.
 * 1.12 names: getPathFinder = createPathFinder, canNavigate = canUpdatePath, getEntityPosition = getTempMobPos,
 * pathFollow = followThePath, removeSunnyPath = trimPath, isDirectPathBetweenPoints = canMoveDirectly, onUpdateNavigation = tick,
 * canEntityStandOnPos = isStableDestination, checkForStuck = doStuckDetection, currentPath = path, speed = speedModifier.
 */
public class CreaturePathNavigate extends PathNavigation {
   public BaseCreatureEntity entityCreature;
   protected BlockPos targetPosition;
   protected CreatureNodeProcessor nodeProcessor;

   public CreaturePathNavigate(BaseCreatureEntity entityCreature, Level world) {
      super(entityCreature, world);
      this.entityCreature = entityCreature;
   }

   @Override
   protected PathFinder createPathFinder(int maxVisitedNodes) {
      this.nodeProcessor = new CreatureNodeProcessor();
      this.nodeProcessor.setCanPassDoors(true);
      this.nodeEvaluator = this.nodeProcessor;
      return new PathFinder(this.nodeProcessor, maxVisitedNodes);
   }

   @Override
   protected boolean canUpdatePath() {
      if (this.entityCreature.isFlying()) {
         return true;
      } else {
         return this.entityCreature.isInWater()
            ? this.entityCreature.canWade() || this.entityCreature.isStrongSwimmer()
            : this.mob.onGround() || this.mob.isPassenger();
      }
   }

   public void setCanOpenDoors(boolean setBreakDoors) {
      this.nodeProcessor.setCanOpenDoors(setBreakDoors);
   }

   public boolean getCanOpenDoors() {
      return this.nodeProcessor.canOpenDoors();
   }

   public void setEnterDoors(boolean setBreakDoors) {
      this.nodeProcessor.setCanPassDoors(setBreakDoors);
   }

   public boolean getEnterDoors() {
      return this.nodeProcessor.canPassDoors();
   }

   /** 1.12 getPathToPos */
   @Override
   public Path createPath(BlockPos pos, int accuracy) {
      this.targetPosition = this.getSuitableDestination(pos);
      return super.createPath(this.targetPosition, accuracy);
   }

   /** 1.12 getPathToEntityLiving */
   @Override
   public Path createPath(Entity entity, int accuracy) {
      this.targetPosition = LycPos.of(entity);
      return super.createPath(entity, accuracy);
   }

   protected BlockPos getSuitableDestination(BlockPos pos) {
      BlockState targetBlockState = this.level.getBlockState(pos);
      if (Material.of(targetBlockState) == Material.AIR) {
         return this.entityCreature.isFlying() ? pos : this.getGround(pos);
      } else {
         return !Material.of(targetBlockState).isSolid() ? pos : this.getSurface(pos);
      }
   }

   @Override
   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), (double)this.getPathablePosY(), this.mob.getZ());
   }

   protected int getPathablePosY() {
      if (this.entityCreature.isInWater() && this.entityCreature.canFloat() && !this.entityCreature.canDive()) {
         int posY = (int)this.mob.getBoundingBox().minY;
         Block block = this.level.getBlockState(new BlockPos(Mth.floor(this.mob.getX()), posY, Mth.floor(this.mob.getZ()))).getBlock();
         int searchCount = 0;

         while (this.isSwimmableBlock(block)) {
            block = this.level.getBlockState(new BlockPos(Mth.floor(this.mob.getX()), ++posY, Mth.floor(this.mob.getZ()))).getBlock();
            if (++searchCount > 16) {
               return (int)this.mob.getBoundingBox().minY;
            }
         }

         return posY;
      } else {
         return (int)(this.mob.getBoundingBox().minY + 0.5);
      }
   }

   /** 1.12 tryMoveToEntityLiving */
   @Override
   public boolean moveTo(Entity entityIn, double speedIn) {
      Path path = this.createPath(entityIn, 0);
      if (path != null) {
         return this.moveTo(path, speedIn);
      } else if (this.entityCreature.canClimb()) {
         this.targetPosition = LycPos.of(entityIn);
         this.speedModifier = speedIn;
         return true;
      } else {
         return false;
      }
   }

   protected boolean isSwimmableBlock(Block block) {
      return this.isSwimmableBlock(block, 0);
   }

   protected boolean isSwimmableBlock(Block block, int cachedCheck) {
      if (block == null || block == Blocks.AIR) {
         return false;
      } else if (cachedCheck == 1 || this.isWaterBlock(block)) {
         return !this.entityCreature.waterDamage();
      } else if (cachedCheck == 2 || this.isLavaBlock(block)) {
         return !this.entityCreature.canBurn();
      } else {
         return cachedCheck != 3 && !this.isOozeBlock(block) ? false : !this.entityCreature.canFreeze();
      }
   }

   protected boolean isWaterBlock(Block block) {
      return block == Blocks.WATER;
   }

   protected boolean isLavaBlock(Block block) {
      return block == Blocks.LAVA || block == ObjectManager.getBlock("purelava");
   }

   protected boolean isOozeBlock(Block block) {
      return block == ObjectManager.getBlock("ooze");
   }

   @Override
   public boolean isStableDestination(BlockPos pos) {
      if (this.entityCreature.isFlying() || this.entityCreature.isInWater() && this.entityCreature.isStrongSwimmer()) {
         BlockState blockState = this.level.getBlockState(pos);
         return Material.of(blockState).isLiquid() ? this.entityCreature.isStrongSwimmer() : !Material.of(blockState).isSolid();
      } else {
         return super.isStableDestination(pos);
      }
   }

   public BlockPos getGround(BlockPos pos) {
      BlockPos resultPos = pos.below();
      int minY = this.level.getMinBuildHeight();

      while (resultPos.getY() > minY && !Material.of(this.level.getBlockState(resultPos)).isSolid()) {
         resultPos = resultPos.below();
      }

      resultPos = resultPos.above();
      return resultPos.getY() == minY && !this.level.getBlockState(resultPos).isFaceSturdy(this.level, resultPos, net.minecraft.core.Direction.UP) ? pos : resultPos;
   }

   public BlockPos getSurface(BlockPos pos) {
      BlockPos resultPos = pos.above();
      int height = this.level.getMaxBuildHeight();

      while (resultPos.getY() < height && Material.of(this.level.getBlockState(resultPos)).isSolid()) {
         resultPos = resultPos.above();
      }

      return resultPos.getY() == height && Material.of(this.level.getBlockState(resultPos)).isSolid() ? pos : resultPos;
   }

   /** 1.12 PathNavigate.pathFollow called isDirectPathBetweenPoints with the ceil'd entity size. */
   @Override
   protected boolean canMoveDirectly(Vec3 startVec, Vec3 endVec) {
      int w = Mth.ceil(this.mob.getBbWidth());
      return this.isDirectPathBetweenPoints(startVec, endVec, w, Mth.ceil(this.mob.getBbHeight()), w);
   }

   protected boolean isDirectPathBetweenPoints(Vec3 startVec, Vec3 endVec, int sizeX, int sizeY, int sizeZ) {
      if (!this.entityCreature.isFlying() && !this.entityCreature.isInWater()) {
         int i = Mth.floor(startVec.x);
         int j = Mth.floor(startVec.z);
         double distanceX = endVec.x - startVec.x;
         double distanceY = endVec.z - startVec.z;
         double distance = distanceX * distanceX + distanceY * distanceY;
         if (distance < 1.0E-8) {
            return false;
         } else {
            double d3 = 1.0 / Math.sqrt(distance);
            distanceX *= d3;
            distanceY *= d3;
            sizeX += 2;
            sizeZ += 2;
            if (!this.isSafeToStandAt(i, (int)startVec.y, j, sizeX, sizeY, sizeZ, startVec, distanceX, distanceY)) {
               return false;
            } else {
               sizeX -= 2;
               sizeZ -= 2;
               double d4 = 1.0 / Math.abs(distanceX);
               double d5 = 1.0 / Math.abs(distanceY);
               double d6 = (double)i - startVec.x;
               double d7 = (double)j - startVec.z;
               if (distanceX >= 0.0) {
                  d6++;
               }

               if (distanceY >= 0.0) {
                  d7++;
               }

               d6 /= distanceX;
               d7 /= distanceY;
               int k = distanceX < 0.0 ? -1 : 1;
               int l = distanceY < 0.0 ? -1 : 1;
               int i1 = Mth.floor(endVec.x);
               int j1 = Mth.floor(endVec.z);
               int k1 = i1 - i;
               int l1 = j1 - j;

               while (k1 * k > 0 || l1 * l > 0) {
                  if (d6 < d7) {
                     d6 += d4;
                     i += k;
                     k1 = i1 - i;
                  } else {
                     d7 += d5;
                     j += l;
                     l1 = j1 - j;
                  }

                  if (!this.isSafeToStandAt(i, (int)startVec.y, j, sizeX, sizeY, sizeZ, startVec, distanceX, distanceY)) {
                     return false;
                  }
               }

               return true;
            }
         }
      } else {
         BlockHitResult raytraceresult = LycWorld.rayTraceBlocks(
            this.level, startVec, new Vec3(endVec.x, endVec.y + (double)this.mob.getBbHeight() * 0.5, endVec.z), false, true, false
         );
         if (raytraceresult == null) {
            return true;
         } else if (raytraceresult.getType() == HitResult.Type.BLOCK) {
            BlockState hitState = this.level.getBlockState(raytraceresult.getBlockPos());
            return !Material.of(hitState).isSolid();
         } else {
            return raytraceresult.getType() == HitResult.Type.MISS;
         }
      }
   }

   protected boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 startVec, double distanceX, double distanceZ) {
      sizeX = Math.min(sizeX, 2);
      sizeY = Math.min(sizeY, 3);
      sizeZ = Math.min(sizeZ, 2);
      int i = x - sizeX / 2;
      int j = z - sizeZ / 2;
      if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, startVec, distanceX, distanceZ)) {
         return false;
      } else {
         for (int k = i; k < i + sizeX; k++) {
            for (int l = j; l < j + sizeZ; l++) {
               double d0 = (double)k + 0.5 - startVec.x;
               double d1 = (double)l + 0.5 - startVec.z;
               if (d0 * distanceX + d1 * distanceZ >= 0.0) {
                  PathType pathnodetype = this.nodeProcessor.getPathNodeType(this.level, k, y - 1, l, this.mob, sizeX, sizeY, sizeZ, true, true);
                  if (pathnodetype == PathType.WATER) {
                     return false;
                  }

                  if (pathnodetype == PathType.LAVA) {
                     return false;
                  }

                  if (pathnodetype == PathType.OPEN) {
                     return false;
                  }

                  pathnodetype = this.nodeProcessor.getPathNodeType(this.level, k, y, l, this.mob, sizeX, sizeY, sizeZ, true, true);
                  float f = this.mob.getPathfindingMalus(pathnodetype);
                  if (f < 0.0F || f >= 8.0F) {
                     return false;
                  }

                  if (this.entityCreature.canBurn()
                     && (pathnodetype == PathType.DAMAGE_FIRE || pathnodetype == PathType.DANGER_FIRE || pathnodetype == PathType.DAMAGE_OTHER)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vec3 startVec, double distanceX, double distanceZ) {
      for (BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
         double d0 = (double)blockpos.getX() + 0.5 - startVec.x;
         double d1 = (double)blockpos.getZ() + 0.5 - startVec.z;
         if (d0 * distanceX + d1 * distanceZ >= 0.0) {
            if (!this.level.getBlockState(blockpos).isPathfindable(PathComputationType.LAND)) {
               return false;
            }
         }
      }

      return true;
   }

   @Override
   protected void trimPath() {
      super.trimPath();
      if (this.path == null) {
         return;
      }
      if (this.entityCreature.level().isDay()) {
         if (this.entityCreature.daylightBurns()) {
            if (this.level.canSeeSky(this.mob.blockPosition())) {
               return;
            }

            for (int j = 0; j < this.path.getNodeCount(); j++) {
               Node pathpoint2 = this.path.getNode(j);
               BlockPos pathPos = new BlockPos(pathpoint2.x, pathpoint2.y, pathpoint2.z);
               if (this.level.getBlockState(pathPos).getBlock() != Blocks.WATER
                  && this.level.getBlockState(pathPos.below()).getBlock() != Blocks.WATER
                  && this.level.canSeeSky(pathPos)) {
                  this.path.truncateNodes(j - 1);
                  return;
               }
            }
         }
      }
   }

   @Override
   protected void followThePath() {
      if (!this.entityCreature.isFlying() && !this.entityCreature.isInWater()) {
         super.followThePath();
      } else {
         Vec3 entityVector = this.getTempMobPos();
         float entitySize = this.mob.getBbWidth() * this.mob.getBbWidth();
         if (LycMath.distSq(entityVector, this.path.getEntityPosAtNode(this.mob, this.path.getNextNodeIndex())) < (double)entitySize) {
            this.path.advance();
         }

         int pathIndexRange = 6;

         for (int pathIndex = Math.min(this.path.getNextNodeIndex() + pathIndexRange, this.path.getNodeCount() - 1);
            pathIndex > this.path.getNextNodeIndex();
            pathIndex--
         ) {
            Vec3 pathVector = this.path.getEntityPosAtNode(this.mob, pathIndex);
            if (pathVector.distanceToSqr(entityVector) <= 36.0 && this.isDirectPathBetweenPoints(entityVector, pathVector, 0, 0, 0)) {
               this.path.setNextNodeIndex(pathIndex);
               break;
            }
         }

         this.doStuckDetection(entityVector);
      }
   }

   @Override
   public void tick() {
      if (!this.isDone() || !this.entityCreature.canClimb()) {
         super.tick();
      } else if (this.targetPosition != null) {
         double d0 = (double)(this.mob.getBbWidth() * this.mob.getBbWidth());
         if (!(LycMath.distSq(this.mob, Vec3.atCenterOf(this.targetPosition)) >= d0)
            || !(this.mob.getY() <= (double)this.targetPosition.getY())
               && !(LycMath.distSq(this.mob, Vec3.atCenterOf(new BlockPos(this.targetPosition.getX(), Mth.floor(this.mob.getY()), this.targetPosition.getZ()))) >= d0)) {
            this.targetPosition = null;
         } else {
            this.mob.getMoveControl().setWantedPosition(
               (double)this.targetPosition.getX(), (double)this.targetPosition.getY(), (double)this.targetPosition.getZ(), this.speedModifier
            );
         }
      }
   }
}
