package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.IafLand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** 1.12 PathNavigateDeathWormSand */
public class PathNavigateDeathWormSand extends net.minecraft.world.entity.ai.navigation.PathNavigation {
   private EntityDeathWorm worm;

   public PathNavigateDeathWormSand(EntityDeathWorm deathworm, Level worldIn) {
      super(deathworm, worldIn);
      this.worm = deathworm;
   }

   @Override
   protected PathFinder createPathFinder(int maxVisitedNodes) {
      this.nodeEvaluator = new SandNodeProcessor();
      return new PathFinder(this.nodeEvaluator, maxVisitedNodes);
   }

   @Override
   protected boolean canUpdatePath() {
      return this.worm == null || this.worm.isInSand();
   }

   @Override
   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ());
   }

   /** pathFollow */
   @Override
   protected void followThePath() {
      Vec3 vec3d = this.getTempMobPos();
      float f = 0.65F;
      if (vec3d.distanceToSqr(this.path.getEntityPosAtNode(this.mob, this.path.getNextNodeIndex())) < (double)f) {
         this.path.advance();
      }

      for (int j = Math.min(this.path.getNextNodeIndex() + 6, this.path.getNodeCount() - 1); j > this.path.getNextNodeIndex(); j--) {
         Vec3 vec3d1 = this.path.getEntityPosAtNode(this.mob, j);
         if (vec3d1.distanceToSqr(vec3d) <= 36.0 && this.canMoveDirectly(vec3d, vec3d1)) {
            this.path.setNextNodeIndex(j);
            break;
         }
      }

      this.doStuckDetection(vec3d);
   }

   /** isDirectPathBetweenPoints: true only if the ray hits a sand block. */
   @Override
   protected boolean canMoveDirectly(Vec3 posVec31, Vec3 posVec32) {
      BlockHitResult raytraceresult = this.level
         .clip(new ClipContext(posVec31, new Vec3(posVec32.x, posVec32.y + 0.5, posVec32.z), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.mob));
      return raytraceresult != null && raytraceresult.getType() == HitResult.Type.BLOCK
         ? IafLand.isSandMaterial(this.mob.level().getBlockState(raytraceresult.getBlockPos()))
         : false;
   }

   @Override
   public boolean isStableDestination(BlockPos pos) {
      return this.level.getBlockState(pos).isCollisionShapeFullBlock(this.level, pos);
   }
}
