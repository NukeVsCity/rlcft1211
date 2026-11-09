package com.github.alexthe666.iceandfire.entity.ai.legacy;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.phys.Vec3;

/** 1.12.2 RandomPositionGenerator */
public final class RandomPositionGenerator {
   private RandomPositionGenerator() {
   }

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
      return findRandomTargetBlock(entity, xz, y, target.subtract(entity.getX(), entity.getY(), entity.getZ()));
   }

   @Nullable
   public static Vec3 findRandomTargetBlockAwayFrom(PathfinderMob entity, int xz, int y, Vec3 target) {
      return findRandomTargetBlock(entity, xz, y, new Vec3(entity.getX(), entity.getY(), entity.getZ()).subtract(target));
   }

   @Nullable
   private static Vec3 findRandomTargetBlock(PathfinderMob entity, int xz, int y, @Nullable Vec3 direction) {
      return generateRandomPos(entity, xz, y, direction, true);
   }

   @Nullable
   private static Vec3 generateRandomPos(PathfinderMob entity, int xz, int y, @Nullable Vec3 direction, boolean skipWater) {
      PathNavigation navigation = entity.getNavigation();
      RandomSource random = entity.getRandom();
      boolean flag;
      if (entity.hasRestriction()) {
         double d0 = entity.getRestrictCenter().distToLowCornerSqr(Mth.floor(entity.getX()), Mth.floor(entity.getY()), Mth.floor(entity.getZ())) + 4.0;
         double d1 = entity.getRestrictRadius() + (float)xz;
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
            if ((!flag || entity.isWithinRestriction(pos)) && navigation.isStableDestination(pos)) {
               if (!skipWater) {
                  pos = moveAboveSolid(pos, entity);
                  if (Legacy112.isWaterMaterial(entity.level().getBlockState(pos))) {
                     continue;
                  }
               }

               float f1 = entity.getWalkTargetValue(pos);
               if (f1 > best) {
                  best = f1;
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

   private static BlockPos moveAboveSolid(BlockPos pos, PathfinderMob entity) {
      if (!entity.level().getBlockState(pos).isSolid()) {
         return pos;
      } else {
         BlockPos up = pos.above();
         while (up.getY() < entity.level().getMaxBuildHeight() && entity.level().getBlockState(up).isSolid()) {
            up = up.above();
         }
         return up;
      }
   }
}
