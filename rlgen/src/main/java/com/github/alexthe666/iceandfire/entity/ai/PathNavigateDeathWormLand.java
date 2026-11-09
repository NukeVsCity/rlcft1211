package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.IafLand;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

/**
 * 1.12 PathNavigateDeathWormLand: a walking navigator (can swim) whose start height is the sand surface while in sand. The 1.21
 * ground navigator's direct-path test is kept (1.12's differed only in treating sand as blocking).
 */
public class PathNavigateDeathWormLand extends GroundPathNavigation {
   private EntityDeathWorm worm;

   public PathNavigateDeathWormLand(EntityDeathWorm worm, Level worldIn) {
      super(worm, worldIn);
      this.worm = worm;
      this.setCanFloat(true);
   }

   @Override
   protected boolean canUpdatePath() {
      return this.mob.onGround() || this.worm != null && this.worm.isInSand() || this.mob.isPassenger();
   }

   @Override
   protected Vec3 getTempMobPos() {
      return new Vec3(this.mob.getX(), (double)this.getPathablePosY(), this.mob.getZ());
   }

   private static boolean solid(BlockState state) {
      return state.blocksMotion();
   }

   @Override
   public Path createPath(BlockPos pos, int accuracy) {
      if (this.level.getBlockState(pos).isAir()) {
         BlockPos blockpos = pos.below();

         while (blockpos.getY() > this.level.getMinBuildHeight() && this.level.getBlockState(blockpos).isAir()) {
            blockpos = blockpos.below();
         }

         if (blockpos.getY() > this.level.getMinBuildHeight()) {
            return super.createPath(blockpos.above(), accuracy);
         }

         while (blockpos.getY() < this.level.getMaxBuildHeight() && this.level.getBlockState(blockpos).isAir()) {
            blockpos = blockpos.above();
         }

         pos = blockpos;
      }

      if (!solid(this.level.getBlockState(pos))) {
         return super.createPath(pos, accuracy);
      } else {
         BlockPos blockpos1 = pos.above();

         while (blockpos1.getY() < this.level.getMaxBuildHeight() && solid(this.level.getBlockState(blockpos1))) {
            blockpos1 = blockpos1.above();
         }

         return super.createPath(blockpos1, accuracy);
      }
   }

   @Override
   public Path createPath(Entity entityIn, int accuracy) {
      return this.createPath(entityIn.blockPosition(), accuracy);
   }

   private int getPathablePosY() {
      if (this.worm != null && this.worm.isInSand()) {
         int i = (int)this.mob.getBoundingBox().minY;
         BlockState blockstate = this.level.getBlockState(new BlockPos(Mth.floor(this.mob.getX()), i, Mth.floor(this.mob.getZ())));
         int j = 0;

         while (IafLand.isSandMaterial(blockstate)) {
            blockstate = this.level.getBlockState(new BlockPos(Mth.floor(this.mob.getX()), ++i, Mth.floor(this.mob.getZ())));
            if (++j > 16) {
               return (int)this.mob.getBoundingBox().minY;
            }
         }

         return i;
      } else {
         return (int)(this.mob.getBoundingBox().minY + 0.5);
      }
   }
}
