package com.nukevscity.rlgen.legacy.layer;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.List;

/** 1.12.2 GenLayerBiome with Forge's patch (weighted BiomeManager lists). */
public class GenLayerBiome extends GenLayer {
   private final BiomeLists lists;

   public GenLayerBiome(long i, GenLayer genlayer, BiomeLists lists) {
      super(i);
      this.parent = genlayer;
      this.lists = lists;
   }

   @Override
   public int[] getInts(int i, int j, int k, int l) {
      int[] aint = this.parent.getInts(i, j, k, l);
      int[] aint1 = new int[k * l];

      for (int i1 = 0; i1 < l; i1++) {
         for (int j1 = 0; j1 < k; j1++) {
            this.initChunkSeed((long)(j1 + i), (long)(i1 + j));
            int k1 = aint[j1 + i1 * k];
            int l1 = (k1 & 3840) >> 8;
            k1 &= -3841;
            if (isBiomeOceanic(k1)) {
               aint1[j1 + i1 * k] = k1;
            } else if (k1 == Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND)) {
               aint1[j1 + i1 * k] = k1;
            } else if (k1 == 1) {
               if (l1 > 0) {
                  if (this.nextInt(3) == 0) {
                     aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.MESA_CLEAR_ROCK);
                  } else {
                     aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.MESA_ROCK);
                  }
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeLists.Type.DESERT).biome);
               }
            } else if (k1 == 2) {
               if (l1 > 0) {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.JUNGLE);
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeLists.Type.WARM).biome);
               }
            } else if (k1 == 3) {
               if (l1 > 0) {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.REDWOOD_TAIGA);
               } else {
                  aint1[j1 + i1 * k] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeLists.Type.COOL).biome);
               }
            } else if (k1 == 4) {
               aint1[j1 + i1 * k] = Biome.getIdForBiome(this.getWeightedBiomeEntry(BiomeLists.Type.ICY).biome);
            } else {
               aint1[j1 + i1 * k] = Biome.getIdForBiome(Biomes.MUSHROOM_ISLAND);
            }
         }
      }

      return aint1;
   }

   protected BiomeLists.Entry getWeightedBiomeEntry(BiomeLists.Type type) {
      List<BiomeLists.Entry> biomeList = this.lists.get(type);
      int totalWeight = BiomeLists.getTotalWeight(biomeList);
      int weight = this.lists.isModded(type) ? this.nextInt(totalWeight) : this.nextInt(totalWeight / 10) * 10;
      return BiomeLists.getRandomItem(biomeList, weight);
   }
}
