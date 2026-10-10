package com.nukevscity.rlgen.legacy.biome;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import java.util.Random;

public class BiomeTaiga extends Biome {
   private final BiomeTaiga.Type type;

   public BiomeTaiga(BiomeTaiga.Type type, Biome.BiomeProperties p) {
      super(p);
      this.type = type;
   }

   @Override
   public void genTerrainBlocks(LegacyWorld world, Random random, ChunkPrimer chunkprimer, int i, int j, double d0) {
      if (this.type == BiomeTaiga.Type.MEGA || this.type == BiomeTaiga.Type.MEGA_SPRUCE) {
         this.topBlock = LBlock.GRASS;
         this.fillerBlock = LBlock.DIRT;
         if (d0 > 1.75) {
            this.topBlock = LBlock.COARSE_DIRT;
         } else if (d0 > -0.95) {
            this.topBlock = LBlock.PODZOL;
         }
      }

      this.generateBiomeTerrain(world, random, chunkprimer, i, j, d0);
   }

   public enum Type {
      NORMAL,
      MEGA,
      MEGA_SPRUCE;
   }
}
