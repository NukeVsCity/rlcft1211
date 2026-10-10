package com.nukevscity.rlgen.legacy.layer;

import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.biome.Biome;

public class GenLayerHills extends GenLayer {
   private final GenLayer riverLayer;

   public GenLayerHills(long i, GenLayer genlayer, GenLayer genlayer1) {
      super(i);
      this.parent = genlayer;
      this.riverLayer = genlayer1;
   }

   @Override
   public int[] getInts(int i, int j, int k, int l) {
      int[] aint = this.parent.getInts(i - 1, j - 1, k + 2, l + 2);
      int[] aint1 = this.riverLayer.getInts(i - 1, j - 1, k + 2, l + 2);
      int[] aint2 = new int[k * l];

      for (int i1 = 0; i1 < l; i1++) {
         for (int j1 = 0; j1 < k; j1++) {
            this.initChunkSeed((long)(j1 + i), (long)(i1 + j));
            int k1 = aint[j1 + 1 + (i1 + 1) * (k + 2)];
            int l1 = aint1[j1 + 1 + (i1 + 1) * (k + 2)];
            boolean flag = (l1 - 2) % 29 == 0;
            if (k1 > 255) {
               // 1.12 logged "old!" here
            }

            Biome biome = Biome.getBiomeForId(k1);
            boolean flag1 = biome != null && biome.isMutation();
            if (k1 != 0 && l1 >= 2 && (l1 - 2) % 29 == 1 && !flag1) {
               Biome biome3 = Biome.getMutationForBiome(biome);
               aint2[j1 + i1 * k] = biome3 == null ? k1 : Biome.getIdForBiome(biome3);
            } else if (this.nextInt(3) != 0 && !flag) {
               aint2[j1 + i1 * k] = k1;
            } else {
               Biome biome1 = biome;
               if (biome == Biomes.DESERT) {
                  biome1 = Biomes.DESERT_HILLS;
               } else if (biome == Biomes.FOREST) {
                  biome1 = Biomes.FOREST_HILLS;
               } else if (biome == Biomes.BIRCH_FOREST) {
                  biome1 = Biomes.BIRCH_FOREST_HILLS;
               } else if (biome == Biomes.ROOFED_FOREST) {
                  biome1 = Biomes.PLAINS;
               } else if (biome == Biomes.TAIGA) {
                  biome1 = Biomes.TAIGA_HILLS;
               } else if (biome == Biomes.REDWOOD_TAIGA) {
                  biome1 = Biomes.REDWOOD_TAIGA_HILLS;
               } else if (biome == Biomes.COLD_TAIGA) {
                  biome1 = Biomes.COLD_TAIGA_HILLS;
               } else if (biome == Biomes.PLAINS) {
                  if (this.nextInt(3) == 0) {
                     biome1 = Biomes.FOREST_HILLS;
                  } else {
                     biome1 = Biomes.FOREST;
                  }
               } else if (biome == Biomes.ICE_PLAINS) {
                  biome1 = Biomes.ICE_MOUNTAINS;
               } else if (biome == Biomes.JUNGLE) {
                  biome1 = Biomes.JUNGLE_HILLS;
               } else if (biome == Biomes.OCEAN) {
                  biome1 = Biomes.DEEP_OCEAN;
               } else if (biome == Biomes.EXTREME_HILLS) {
                  biome1 = Biomes.EXTREME_HILLS_WITH_TREES;
               } else if (biome == Biomes.SAVANNA) {
                  biome1 = Biomes.SAVANNA_PLATEAU;
               } else if (biomesEqualOrMesaPlateau(k1, Biome.getIdForBiome(Biomes.MESA_ROCK))) {
                  biome1 = Biomes.MESA;
               } else if (biome == Biomes.DEEP_OCEAN && this.nextInt(3) == 0) {
                  int i2 = this.nextInt(2);
                  if (i2 == 0) {
                     biome1 = Biomes.PLAINS;
                  } else {
                     biome1 = Biomes.FOREST;
                  }
               }

               int j3 = Biome.getIdForBiome(biome1);
               if (flag && j3 != k1) {
                  Biome biome2 = Biome.getMutationForBiome(biome1);
                  j3 = biome2 == null ? k1 : Biome.getIdForBiome(biome2);
               }

               if (j3 == k1) {
                  aint2[j1 + i1 * k] = k1;
               } else {
                  int k3 = aint[j1 + 1 + (i1 + 0) * (k + 2)];
                  int j2 = aint[j1 + 2 + (i1 + 1) * (k + 2)];
                  int k2 = aint[j1 + 0 + (i1 + 1) * (k + 2)];
                  int l2 = aint[j1 + 1 + (i1 + 2) * (k + 2)];
                  int i3 = 0;
                  if (biomesEqualOrMesaPlateau(k3, k1)) {
                     i3++;
                  }

                  if (biomesEqualOrMesaPlateau(j2, k1)) {
                     i3++;
                  }

                  if (biomesEqualOrMesaPlateau(k2, k1)) {
                     i3++;
                  }

                  if (biomesEqualOrMesaPlateau(l2, k1)) {
                     i3++;
                  }

                  if (i3 >= 3) {
                     aint2[j1 + i1 * k] = j3;
                  } else {
                     aint2[j1 + i1 * k] = k1;
                  }
               }
            }
         }
      }

      return aint2;
   }
}
