package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import java.util.Random;

public class BiomeHills extends Biome {
   private final BiomeHills.Type type;

   public BiomeHills(BiomeHills.Type type, Biome.BiomeProperties p) {
      super(p);
      this.type = type;
   }

   @Override
   public void genTerrainBlocks(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      this.topBlock = LBlock.GRASS;
      this.fillerBlock = LBlock.DIRT;
      if ((d0 < -1.0 || d0 > 2.0) && this.type == BiomeHills.Type.MUTATED) {
         this.topBlock = LBlock.GRAVEL;
         this.fillerBlock = LBlock.GRAVEL;
      } else if (d0 > 1.0 && this.type != BiomeHills.Type.EXTRA_TREES) {
         this.topBlock = LBlock.STONE;
         this.fillerBlock = LBlock.STONE;
      }

      this.generateBiomeTerrain(world, random, chunkprimer, i, j, d0);
   }

   public enum Type {
      NORMAL,
      EXTRA_TREES,
      MUTATED;
   }
}
