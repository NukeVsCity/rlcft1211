package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import java.util.Random;

public class BiomeSwamp extends Biome {
   public BiomeSwamp(Biome.BiomeProperties p) {
      super(p);
   }

   @Override
   public void genTerrainBlocks(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      lilyPadPass(chunkprimer, i, j);
      this.generateBiomeTerrain(world, random, chunkprimer, i, j, d0);
   }

   /** Shared with Traverse Lush Swamp, which copied this code. */
   static void lilyPadPass(ChunkPrimer chunkprimer, int i, int j) {
      double d1 = GRASS_COLOR_NOISE.getValue((double)i * 0.25, (double)j * 0.25);
      if (d1 > 0.0) {
         int k = i & 15;
         int l = j & 15;

         for (int i1 = 255; i1 >= 0; i1--) {
            if (!chunkprimer.getBlockState(l, i1, k).isAir()) {
               if (i1 == 62 && chunkprimer.getBlockState(l, i1, k).getBlock() != LBlock.WATER) {
                  chunkprimer.setBlockState(l, i1, k, WATER);
                  if (d1 < 0.12) {
                     chunkprimer.setBlockState(l, i1 + 1, k, LBlock.WATERLILY);
                  }
               }
               break;
            }
         }
      }
   }
}
