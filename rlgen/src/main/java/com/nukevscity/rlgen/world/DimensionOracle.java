package com.nukevscity.rlgen.world;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.RcDim;
import com.nukevscity.rlgen.rc.RcGenerator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * What RC sees when planning in the End or the nether: the real (1.21) terrain of that dimension from its chunk generator's base columns
 * (noise terrain, no surface rules or features), and the single 1.12 biome of the dimension.
 */
public final class DimensionOracle implements RcGenerator.Oracle {
   private final RcDim dim;
   private final ChunkGenerator generator;
   private final LevelHeightAccessor height;
   private final RandomState randomState;
   private final Map<Long, NoiseColumn> columns = new ConcurrentHashMap<>();

   public DimensionOracle(RcDim dim, ChunkGenerator generator, LevelHeightAccessor height, RandomState randomState) {
      this.dim = dim;
      this.generator = generator;
      this.height = height;
      this.randomState = randomState;
   }

   @Override
   public BlockState get(BlockPos pos) {
      NoiseColumn column = this.columns.computeIfAbsent(ChunkPos.asLong(pos.getX(), pos.getZ()),
         k -> this.generator.getBaseColumn(pos.getX(), pos.getZ(), this.height, this.randomState));
      return column.getBlock(pos.getY());
   }

   @Override
   public Biome biome(int x, int z) {
      return this.dim.fixedBiome();
   }

   @Override
   public RcDim dim() {
      return this.dim;
   }
}
