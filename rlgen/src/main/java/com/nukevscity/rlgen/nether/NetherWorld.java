package com.nukevscity.rlgen.nether;

import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The 1.12 nether World as an IWorldGenerator saw it, over the 3x3 chunks a 1.21 feature may touch (outside: air, writes dropped). */
public final class NetherWorld {
   public final WorldGenLevel level;
   public final long seed;
   public final ChunkPos center;

   NetherWorld(WorldGenLevel level, ChunkPos center) {
      this.level = level;
      this.seed = level.getSeed();
      this.center = center;
   }

   boolean inRegion(BlockPos p) {
      return p.getY() >= 0 && p.getY() <= 255 && Math.abs((p.getX() >> 4) - this.center.x) <= 1 && Math.abs((p.getZ() >> 4) - this.center.z) <= 1;
   }

   public BlockState get(BlockPos p) {
      return this.inRegion(p) ? this.level.getBlockState(p) : Blocks.AIR.defaultBlockState();
   }

   public void set(BlockPos p, BlockState s) {
      if (this.inRegion(p)) {
         this.level.setBlock(p, s, 2);
      }
   }

   public boolean isAir(BlockPos p) {
      return this.get(p).isAir();
   }

   /** What 1.12 drew from world.rand (unseeded), seeded from the chunk so a world comes out the same every time. */
   Random worldRand(long salt) {
      return new Random(this.seed ^ ((long)this.center.x * 0x5DEECE66DL + (long)this.center.z * 0x2545F491L) ^ salt);
   }
}
