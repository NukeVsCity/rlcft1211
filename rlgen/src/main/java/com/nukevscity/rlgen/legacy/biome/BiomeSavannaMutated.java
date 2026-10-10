package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import java.util.Random;

public class BiomeSavannaMutated extends BiomeSavanna {
   public BiomeSavannaMutated(Biome.BiomeProperties p) {
      super(p);
   }

   @Override
   public void genTerrainBlocks(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      this.topBlock = LBlock.GRASS;
      this.fillerBlock = LBlock.DIRT;
      if (d0 > 1.75) {
         this.topBlock = LBlock.STONE;
         this.fillerBlock = LBlock.STONE;
      } else if (d0 > -0.5) {
         this.topBlock = LBlock.COARSE_DIRT;
      }

      this.generateBiomeTerrain(world, random, chunkprimer, i, j, d0);
   }
}
