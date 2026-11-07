package com.nukevscity.rlgen.nether;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Quark TileSmoker.update: large smoke up the chimney and out of its end, every tick. */
public class SmokerBlockEntity extends BlockEntity {
   public SmokerBlockEntity(BlockPos pos, BlockState state) {
      super(NetherGen.SMOKER_BE.get(), pos, state);
   }

   static void clientTick(Level level, BlockPos pos) {
      if (isFullBlock(level, pos.above())) {
         return;
      }
      BlockPos source = chimneyEnd(level, pos);
      int yDiff = source.getY() - pos.getY();
      RandomSource rand = level.random;
      for (int i = 0; i < yDiff - 1; i++) {
         float x = pos.getX() + 0.2F + rand.nextFloat() * 0.6F;
         float y = pos.getY() + rand.nextFloat() * (float)(yDiff - 1) - 0.4F;
         float z = pos.getZ() + 0.2F + rand.nextFloat() * 0.6F;
         level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.0, 0.0);
      }
      for (int i = 0; i < 6; i++) {
         float x = source.getX() + 0.2F + rand.nextFloat() * 0.6F;
         float y = source.getY() + rand.nextFloat() - 0.4F;
         float z = source.getZ() + 0.2F + rand.nextFloat() * 0.6F;
         level.addAlwaysVisibleParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0, 0.0, 0.0);
      }
   }

   private static boolean isFullBlock(Level level, BlockPos pos) {
      return Block.isShapeFullBlock(level.getBlockState(pos).getCollisionShape(level, pos));
   }

   private static BlockPos chimneyEnd(Level level, BlockPos pos) {
      BlockPos curr = pos;
      while (curr.getY() < 255) {
         BlockPos next = curr.above();
         if (isFullBlock(level, next)) {
            return curr;
         }
         for (Direction face : Direction.Plane.HORIZONTAL) {
            if (!isFullBlock(level, next.relative(face))) {
               return next;
            }
         }
         curr = next;
      }
      return pos;
   }
}
