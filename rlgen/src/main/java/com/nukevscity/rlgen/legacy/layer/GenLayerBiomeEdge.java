package com.nukevscity.rlgen.legacy.layer;

import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.biome.Biome;

public class GenLayerBiomeEdge extends GenLayer {
   public GenLayerBiomeEdge(long i, GenLayer genlayer) {
      super(i);
      this.parent = genlayer;
   }

   @Override
   public int[] getInts(int i, int j, int k, int l) {
      int[] aint = this.parent.getInts(i - 1, j - 1, k + 2, l + 2);
      int[] aint1 = new int[k * l];

      for (int i1 = 0; i1 < l; i1++) {
         for (int j1 = 0; j1 < k; j1++) {
            this.initChunkSeed((long)(j1 + i), (long)(i1 + j));
            int k1 = aint[j1 + 1 + (i1 + 1) * (k + 2)];
            if (!this.replaceBiomeEdgeIfNecessary(
                  aint, aint1, j1, i1, k, k1, Biome.getIdForBiome(Biomes.EXTREME_HILLS), Biome.getIdForBiome(Biomes.EXTREME_HILLS_EDGE)
               )
               && !this.replaceBiomeEdge(aint, aint1, j1, i1, k, k1, Biome.getIdForBiome(Biomes.MESA_ROCK), Biome.getIdForBiome(Biomes.MESA))
               && !this.replaceBiomeEdge(aint, aint1, j1, i1, k, k1, Biome.getIdForBiome(Biomes.MESA_CLEAR_ROCK), Biome.getIdForBiome(Biomes.MESA))
               && !this.replaceBiomeEdge(aint, aint1, j1, i1, k, k1, Biome.getIdForBiome(Biomes.REDWOOD_TAIGA), Biome.getIdForBiome(Biomes.TAIGA))) {
               if (k1 == Biome.getIdForBiome(Biomes.DESERT)) {
                  int l1 = aint[j1 + 1 + (i1 + 1 - 1) * (k + 2)];
                  int i2 = aint[j1 + 1 + 1 + (i1 + 1) * (k + 2)];
                  int j2 = aint[j1 + 1 - 1 + (i1 + 1) * (k + 2)];
                  int k2 = aint[j1 + 1 + (i1 + 1 + 1) * (k + 2)];
                  if (l1 != Biome.getIdForBiome(Biomes.ICE_PLAINS)
                     && i2 != Biome.getIdForBiome(Biomes.ICE_PLAINS)
                     && j2 != Biome.getIdForBiome(Biomes.ICE_PLAINS)
                     && k2 != Biome.getIdForBiome(Biomes.ICE_PLAINS)) {
                     aint1[j1 + i1 * k] = k1;
                  } else {
                     aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES);
                  }
               } else if (k1 == Biome.getIdForBiome(Biomes.SWAMPLAND)) {
                  int l2 = aint[j1 + 1 + (i1 + 1 - 1) * (k + 2)];
                  int i3 = aint[j1 + 1 + 1 + (i1 + 1) * (k + 2)];
                  int j3 = aint[j1 + 1 - 1 + (i1 + 1) * (k + 2)];
                  int k3 = aint[j1 + 1 + (i1 + 1 + 1) * (k + 2)];
                  if (l2 == Biome.getIdForBiome(Biomes.DESERT)
                     || i3 == Biome.getIdForBiome(Biomes.DESERT)
                     || j3 == Biome.getIdForBiome(Biomes.DESERT)
                     || k3 == Biome.getIdForBiome(Biomes.DESERT)
                     || l2 == Biome.getIdForBiome(Biomes.COLD_TAIGA)
                     || i3 == Biome.getIdForBiome(Biomes.COLD_TAIGA)
                     || j3 == Biome.getIdForBiome(Biomes.COLD_TAIGA)
                     || k3 == Biome.getIdForBiome(Biomes.COLD_TAIGA)
                     || l2 == Biome.getIdForBiome(Biomes.ICE_PLAINS)
                     || i3 == Biome.getIdForBiome(Biomes.ICE_PLAINS)
                     || j3 == Biome.getIdForBiome(Biomes.ICE_PLAINS)
                     || k3 == Biome.getIdForBiome(Biomes.ICE_PLAINS)) {
                     aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.PLAINS);
                  } else if (l2 != Biome.getIdForBiome(Biomes.JUNGLE)
                     && k3 != Biome.getIdForBiome(Biomes.JUNGLE)
                     && i3 != Biome.getIdForBiome(Biomes.JUNGLE)
                     && j3 != Biome.getIdForBiome(Biomes.JUNGLE)) {
                     aint1[j1 + i1 * k] = k1;
                  } else {
                     aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.JUNGLE_EDGE);
                  }
               } else {
                  aint1[j1 + i1 * k] = k1;
               }
            }
         }
      }

      return aint1;
   }

   private boolean replaceBiomeEdgeIfNecessary(int[] aint, int[] aint1, int i, int j, int k, int l, int i1, int j1) {
      if (!biomesEqualOrMesaPlateau(l, i1)) {
         return false;
      } else {
         int k1 = aint[i + 1 + (j + 1 - 1) * (k + 2)];
         int l1 = aint[i + 1 + 1 + (j + 1) * (k + 2)];
         int i2 = aint[i + 1 - 1 + (j + 1) * (k + 2)];
         int j2 = aint[i + 1 + (j + 1 + 1) * (k + 2)];
         if (this.canBiomesBeNeighbors(k1, i1) && this.canBiomesBeNeighbors(l1, i1) && this.canBiomesBeNeighbors(i2, i1) && this.canBiomesBeNeighbors(j2, i1)) {
            aint1[i + j * k] = l;
         } else {
            aint1[i + j * k] = j1;
         }

         return true;
      }
   }

   private boolean replaceBiomeEdge(int[] aint, int[] aint1, int i, int j, int k, int l, int i1, int j1) {
      if (l != i1) {
         return false;
      } else {
         int k1 = aint[i + 1 + (j + 1 - 1) * (k + 2)];
         int l1 = aint[i + 1 + 1 + (j + 1) * (k + 2)];
         int i2 = aint[i + 1 - 1 + (j + 1) * (k + 2)];
         int j2 = aint[i + 1 + (j + 1 + 1) * (k + 2)];
         if (biomesEqualOrMesaPlateau(k1, i1) && biomesEqualOrMesaPlateau(l1, i1) && biomesEqualOrMesaPlateau(i2, i1) && biomesEqualOrMesaPlateau(j2, i1)) {
            aint1[i + j * k] = l;
         } else {
            aint1[i + j * k] = j1;
         }

         return true;
      }
   }

   private boolean canBiomesBeNeighbors(int i, int j) {
      if (biomesEqualOrMesaPlateau(i, j)) {
         return true;
      } else {
         Biome biome = Biome.getBiome(i);
         Biome biome1 = Biome.getBiome(j);
         if (biome != null && biome1 != null) {
            Biome.TempCategory biome$tempcategory = biome.getTempCategory();
            Biome.TempCategory biome$tempcategory1 = biome1.getTempCategory();
            return biome$tempcategory == biome$tempcategory1
               || biome$tempcategory == Biome.TempCategory.MEDIUM
               || biome$tempcategory1 == Biome.TempCategory.MEDIUM;
         } else {
            return false;
         }
      }
   }
}
