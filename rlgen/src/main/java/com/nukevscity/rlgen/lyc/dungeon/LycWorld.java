package com.nukevscity.rlgen.lyc.dungeon;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.yungnickyoung.minecraft.bettermineshafts.mc.World;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The 1.12 nether (-1) or End (1) as a Lycanites dungeon saw it, on the 1.21 terrain the user keeps there: the whole dimension is
 * one 1.12 biome (hell or sky, like ChunkGeneratorHell/End's single-biome providers) and blocks are read from the 1.21 region.
 */
public final class LycWorld extends World {
   public final int dimension;
   private final ChunkPos chunk;

   /** level and chunk are null while a dungeon is planned. */
   public LycWorld(WorldGenLevel level, ChunkPos chunk, int dimension) {
      super(level, null, chunk);
      this.chunk = chunk;
      this.dimension = dimension;
   }

   private boolean near(BlockPos pos) {
      return this.chunk != null && Math.abs((pos.getX() >> 4) - this.chunk.x) <= 1 && Math.abs((pos.getZ() >> 4) - this.chunk.z) <= 1;
   }

   @Override
   public BlockState getBlockState(BlockPos pos) {
      // the placers only write inside the populate area, which the 1.21 region always covers
      return this.near(pos) ? super.getBlockState(pos) : Blocks.AIR.defaultBlockState();
   }

   @Override
   public Biome getBiome(BlockPos pos) {
      return this.dimension == -1 ? Biomes.HELL : Biomes.SKY;
   }

   @Override
   public BlockState getTerrainState(BlockPos pos) {
      return this.getBlockState(pos);
   }

   @Override
   public int topFilledSegment(BlockPos pos) {
      return 0;
   }
}
