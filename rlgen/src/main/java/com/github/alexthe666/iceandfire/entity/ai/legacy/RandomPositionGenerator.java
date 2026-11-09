package com.github.alexthe666.iceandfire.entity.ai.legacy;

import com.github.alexthe666.iceandfire.entity.DragonCompat;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** 1.12.2 net.minecraft.entity.ai.RandomPositionGenerator. */
public class RandomPositionGenerator {
   private static Vec3 staticVector = Vec3.ZERO;

   @Nullable
   public static Vec3 findRandomTarget(PathfinderMob entity, int xz, int y) {
      return findRandomTargetBlock(entity, xz, y, null);
   }

   @Nullable
   public static Vec3 getLandPos(PathfinderMob entity, int xz, int y) {
      return generateRandomPos(entity, xz, y, null, false);
   }

   @Nullable
   public static Vec3 findRandomTargetBlockTowards(PathfinderMob entity, int xz, int y, Vec3 target) {
      staticVector = target.subtract(entity.getX(), entity.getY(), entity.getZ());
      return findRandomTargetBlock(entity, xz, y, staticVector);
   }

   @Nullable
   public static Vec3 findRandomTargetBlockAwayFrom(PathfinderMob entity, int xz, int y, Vec3 target) {
      staticVector = new Vec3(entity.getX(), entity.getY(), entity.getZ()).subtract(target);
      return findRandomTargetBlock(entity, xz, y, staticVector);
   }

   @Nullable
   private static Vec3 findRandomTargetBlock(PathfinderMob entity, int xz, int y, @Nullable Vec3 direction) {
      return generateRandomPos(entity, xz, y, direction, true);
   }

   @Nullable
   private static Vec3 generateRandomPos(PathfinderMob entity, int xz, int y, @Nullable Vec3 direction, boolean water) {
      RandomSource random = entity.getRandom();
      boolean flag;
      if (entity.hasRestriction()) {
         double d0 = entity.getRestrictCenter().distToLowCornerSqr((double)Mth.floor(entity.getX()), (double)Mth.floor(entity.getY()), (double)Mth.floor(entity.getZ()))
            + 4.0;
         double d1 = (double)(entity.getRestrictRadius() + (float)xz);
         flag = d0 < d1 * d1;
      } else {
         flag = false;
      }

      boolean found = false;
      float best = -99999.0F;
      int bx = 0;
      int by = 0;
      int bz = 0;

      for (int k = 0; k < 10; k++) {
         int l = random.nextInt(2 * xz + 1) - xz;
         int i1 = random.nextInt(2 * y + 1) - y;
         int j1 = random.nextInt(2 * xz + 1) - xz;
         if (direction == null || !((double)l * direction.x + (double)j1 * direction.z < 0.0)) {
            if (entity.hasRestriction() && xz > 1) {
               BlockPos home = entity.getRestrictCenter();
               if (entity.getX() > (double)home.getX()) {
                  l -= random.nextInt(xz / 2);
               } else {
                  l += random.nextInt(xz / 2);
               }

               if (entity.getZ() > (double)home.getZ()) {
                  j1 -= random.nextInt(xz / 2);
               } else {
                  j1 += random.nextInt(xz / 2);
               }
            }

            BlockPos pos = BlockPos.containing((double)l + entity.getX(), (double)i1 + entity.getY(), (double)j1 + entity.getZ());
            if ((!flag || entity.isWithinRestriction(pos)) && canEntityStandOnPos(entity, pos)) {
               if (!water) {
                  pos = moveAboveSolid(pos, entity);
                  if (isWaterDestination(pos, entity)) {
                     continue;
                  }
               }

               float weight = entity.getWalkTargetValue(pos);
               if (weight > best) {
                  best = weight;
                  bx = l;
                  by = i1;
                  bz = j1;
                  found = true;
               }
            }
         }
      }

      return found ? new Vec3((double)bx + entity.getX(), (double)by + entity.getY(), (double)bz + entity.getZ()) : null;
   }

   /** 1.12 PathNavigateGround.canEntityStandOnPos: the block below is a full opaque cube. */
   private static boolean canEntityStandOnPos(PathfinderMob entity, BlockPos pos) {
      BlockState below = entity.level().getBlockState(pos.below());
      return below.isSolidRender(entity.level(), pos.below());
   }

   private static boolean isSolid(BlockState state) {
      DragonCompat.Material m = DragonCompat.material(state);
      return m != DragonCompat.Material.AIR && m != DragonCompat.Material.WATER && m != DragonCompat.Material.LAVA && m != DragonCompat.Material.FIRE
         && m != DragonCompat.Material.PLANTS && state.blocksMotion();
   }

   private static BlockPos moveAboveSolid(BlockPos pos, PathfinderMob entity) {
      if (!isSolid(entity.level().getBlockState(pos))) {
         return pos;
      } else {
         BlockPos up = pos.above();

         while (up.getY() < entity.level().getMaxBuildHeight() && isSolid(entity.level().getBlockState(up))) {
            up = up.above();
         }

         return up;
      }
   }

   private static boolean isWaterDestination(BlockPos pos, PathfinderMob entity) {
      return DragonCompat.material(entity.level().getBlockState(pos)) == DragonCompat.Material.WATER;
   }
}
