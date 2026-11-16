package com.lycanitesmobs.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 distance helpers whose meaning depends on the receiver (BlockPos.getDistance vs Entity.getDistance). */
public final class LycMath {
   private LycMath() {
   }

   /** 1.12 BlockPos/Vec3i.getDistance(x, y, z) (to the low corner) or Entity.getDistance(x, y, z). */
   public static double dist(Object from, double x, double y, double z) {
      return Math.sqrt(distSq(from, x, y, z));
   }

   public static double distSq(Object from, double x, double y, double z) {
      if (from instanceof Vec3i v) {
         return v.distToLowCornerSqr(x, y, z);
      }
      return ((Entity)from).distanceToSqr(x, y, z);
   }

   /** 1.12 Entity.getDistanceSq(BlockPos) = pos.distanceSq(posX, posY, posZ). */
   public static double distSq(Entity entity, BlockPos pos) {
      return pos.distToLowCornerSqr(entity.getX(), entity.getY(), entity.getZ());
   }

   /** 1.12 BlockDoor.isOpen(world, pos) */
   public static boolean isDoorOpen(BlockGetter world, BlockPos pos) {
      BlockState state = world.getBlockState(pos);
      return state.getBlock() instanceof DoorBlock && state.getValue(DoorBlock.OPEN);
   }

   /** 1.12 World.isSideSolid(pos, side) */
   public static boolean isSideSolid(BlockGetter world, BlockPos pos, Direction side) {
      return world.getBlockState(pos).isFaceSturdy(world, pos, side);
   }

   /** 1.12 World.isBlockNormalCube(pos, default) */
   public static boolean isBlockNormalCube(BlockGetter world, BlockPos pos) {
      return world.getBlockState(pos).isRedstoneConductor(world, pos);
   }
}
