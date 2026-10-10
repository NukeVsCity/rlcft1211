package com.nukevscity.rlgen.legacy.layer;

import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.BiomeJungle;
import com.nukevscity.rlgen.legacy.biome.BiomeMesa;

public class GenLayerShore extends GenLayer {
   public GenLayerShore(long i, GenLayer genlayer) {
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
            Biome biome = Biome.getBiome(k1);
            if (k1 == Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND)) {
               int l1 = aint[j1 + 1 + (i1 + 1 - 1) * (k + 2)];
               int i2 = aint[j1 + 1 + 1 + (i1 + 1) * (k + 2)];
               int j2 = aint[j1 + 1 - 1 + (i1 + 1) * (k + 2)];
               int k2 = aint[j1 + 1 + (i1 + 1 + 1) * (k + 2)];
               if (l1 != Biome.getIdForBiome(Biomes.OCEAN)
                  && i2 != Biome.getIdForBiome(Biomes.OCEAN)
                  && j2 != Biome.getIdForBiome(Biomes.OCEAN)
                  && k2 != Biome.getIdForBiome(Biomes.OCEAN)) {
                  aint1[j1 + i1 * k] = k1;
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND_SHORE);
               }
            } else if (biome != null && biome.getBiomeClass() == BiomeJungle.class) {
               int j3 = aint[j1 + 1 + (i1 + 1 - 1) * (k + 2)];
               int i4 = aint[j1 + 1 + 1 + (i1 + 1) * (k + 2)];
               int l4 = aint[j1 + 1 - 1 + (i1 + 1) * (k + 2)];
               int k5 = aint[j1 + 1 + (i1 + 1 + 1) * (k + 2)];
               if (!this.isJungleCompatible(j3) || !this.isJungleCompatible(i4) || !this.isJungleCompatible(l4) || !this.isJungleCompatible(k5)) {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.JUNGLE_EDGE);
               } else if (!isBiomeOceanic(j3) && !isBiomeOceanic(i4) && !isBiomeOceanic(l4) && !isBiomeOceanic(k5)) {
                  aint1[j1 + i1 * k] = k1;
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.BEACH);
               }
            } else if (k1 == Biome.getIdForBiome(Biomes.EXTREME_HILLS)
               || k1 == Biome.getIdForBiome(Biomes.EXTREME_HILLS_WITH_TREES)
               || k1 == Biome.getIdForBiome(Biomes.EXTREME_HILLS_EDGE)) {
               this.replaceIfNeighborOcean(aint, aint1, j1, i1, k, k1, Biome.getIdForBiome(Biomes.STONE_BEACH));
            } else if (biome != null && biome.isSnowyBiome()) {
               this.replaceIfNeighborOcean(aint, aint1, j1, i1, k, k1, Biome.getIdForBiome(Biomes.COLD_BEACH));
            } else if (k1 == Biome.getIdForBiome(Biomes.MESA) || k1 == Biome.getIdForBiome(Biomes.MESA_ROCK)) {
               int i3 = aint[j1 + 1 + (i1 + 1 - 1) * (k + 2)];
               int l3 = aint[j1 + 1 + 1 + (i1 + 1) * (k + 2)];
               int k4 = aint[j1 + 1 - 1 + (i1 + 1) * (k + 2)];
               int j5 = aint[j1 + 1 + (i1 + 1 + 1) * (k + 2)];
               if (isBiomeOceanic(i3) || isBiomeOceanic(l3) || isBiomeOceanic(k4) || isBiomeOceanic(j5)) {
                  aint1[j1 + i1 * k] = k1;
               } else if (this.isMesa(i3) && this.isMesa(l3) && this.isMesa(k4) && this.isMesa(j5)) {
                  aint1[j1 + i1 * k] = k1;
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.DESERT);
               }
            } else if (k1 != Biome.getIdForBiome(Biomes.OCEAN)
               && k1 != Biome.getIdForBiome(Biomes.DEEP_OCEAN)
               && k1 != Biome.getIdForBiome(Biomes.RIVER)
               && k1 != Biome.getIdForBiome(Biomes.SWAMPLAND)) {
               int l2 = aint[j1 + 1 + (i1 + 1 - 1) * (k + 2)];
               int k3 = aint[j1 + 1 + 1 + (i1 + 1) * (k + 2)];
               int j4 = aint[j1 + 1 - 1 + (i1 + 1) * (k + 2)];
               int i5 = aint[j1 + 1 + (i1 + 1 + 1) * (k + 2)];
               if (!isBiomeOceanic(l2) && !isBiomeOceanic(k3) && !isBiomeOceanic(j4) && !isBiomeOceanic(i5)) {
                  aint1[j1 + i1 * k] = k1;
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.BEACH);
               }
            } else {
               aint1[j1 + i1 * k] = k1;
            }
         }
      }

      return aint1;
   }

   private void replaceIfNeighborOcean(int[] aint, int[] aint1, int i, int j, int k, int l, int i1) {
      if (isBiomeOceanic(l)) {
         aint1[i + j * k] = l;
      } else {
         int j1 = aint[i + 1 + (j + 1 - 1) * (k + 2)];
         int k1 = aint[i + 1 + 1 + (j + 1) * (k + 2)];
         int l1 = aint[i + 1 - 1 + (j + 1) * (k + 2)];
         int i2 = aint[i + 1 + (j + 1 + 1) * (k + 2)];
         if (!isBiomeOceanic(j1) && !isBiomeOceanic(k1) && !isBiomeOceanic(l1) && !isBiomeOceanic(i2)) {
            aint1[i + j * k] = l;
         } else {
            aint1[i + j * k] = i1;
         }
      }
   }

   private boolean isJungleCompatible(int i) {
      return Biome.getBiome(i) != null && Biome.getBiome(i).getBiomeClass() == BiomeJungle.class
         ? true
         : i == Biome.getIdForBiome(Biomes.JUNGLE_EDGE)
            || i == Biome.getIdForBiome(Biomes.JUNGLE)
            || i == Biome.getIdForBiome(Biomes.JUNGLE_HILLS)
            || i == Biome.getIdForBiome(Biomes.FOREST)
            || i == Biome.getIdForBiome(Biomes.TAIGA)
            || isBiomeOceanic(i);
   }

   private boolean isMesa(int i) {
      return Biome.getBiome(i) instanceof BiomeMesa;
   }
}
