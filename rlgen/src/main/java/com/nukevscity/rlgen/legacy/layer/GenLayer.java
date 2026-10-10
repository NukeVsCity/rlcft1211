package com.nukevscity.rlgen.legacy.layer;

import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.biome.Biome;

public abstract class GenLayer {
   private long worldGenSeed;
   protected GenLayer parent;
   private long chunkSeed;
   protected long baseSeed;

   /** 1.12.2 GenLayer.initializeAllBiomeGenerators for WorldType.DEFAULT with default settings, plus Forge's getBiomeLayer hook. */
   public static GenLayer[] initializeAllBiomeGenerators(long i, BiomeLists lists) {
      GenLayer genlayer = new GenLayerIsland(1L);
      genlayer = new GenLayerFuzzyZoom(2000L, genlayer);
      genlayer = new GenLayerAddIsland(1L, genlayer);
      genlayer = new GenLayerZoom(2001L, genlayer);
      genlayer = new GenLayerAddIsland(2L, genlayer);
      genlayer = new GenLayerAddIsland(50L, genlayer);
      genlayer = new GenLayerAddIsland(70L, genlayer);
      genlayer = new GenLayerRemoveTooMuchOcean(2L, genlayer);
      genlayer = new GenLayerAddSnow(2L, genlayer);
      genlayer = new GenLayerAddIsland(3L, genlayer);
      genlayer = new GenLayerEdge(2L, genlayer, GenLayerEdge.Mode.COOL_WARM);
      genlayer = new GenLayerEdge(2L, genlayer, GenLayerEdge.Mode.HEAT_ICE);
      genlayer = new GenLayerEdge(3L, genlayer, GenLayerEdge.Mode.SPECIAL);
      genlayer = new GenLayerZoom(2002L, genlayer);
      genlayer = new GenLayerZoom(2003L, genlayer);
      genlayer = new GenLayerAddIsland(4L, genlayer);
      genlayer = new GenLayerAddMushroomIsland(5L, genlayer);
      genlayer = new GenLayerDeepOcean(4L, genlayer);
      genlayer = GenLayerZoom.magnify(1000L, genlayer, 0);
      int j = 4;
      int k = j;
      GenLayer genlayer1 = GenLayerZoom.magnify(1000L, genlayer, 0);
      GenLayerRiverInit genlayerriverinit = new GenLayerRiverInit(100L, genlayer1);
      // Forge: WorldType.getBiomeLayer (same three layers as vanilla, GenLayerBiome uses BiomeManager lists)
      GenLayer genlayerbiome = new GenLayerBiome(200L, genlayer, lists);
      GenLayer genlayer4 = GenLayerZoom.magnify(1000L, genlayerbiome, 2);
      GenLayerBiomeEdge genlayerbiomeedge = new GenLayerBiomeEdge(1000L, genlayer4);
      GenLayer genlayer2 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
      GenLayerHills genlayerhills = new GenLayerHills(1000L, genlayerbiomeedge, genlayer2);
      genlayer1 = GenLayerZoom.magnify(1000L, genlayerriverinit, 2);
      genlayer1 = GenLayerZoom.magnify(1000L, genlayer1, k);
      GenLayerRiver genlayerriver = new GenLayerRiver(1L, genlayer1);
      GenLayerSmooth genlayersmooth = new GenLayerSmooth(1000L, genlayerriver);
      genlayerbiome = new GenLayerRareBiome(1001L, genlayerhills);

      for (int l = 0; l < j; l++) {
         genlayerbiome = new GenLayerZoom((long)(1000 + l), genlayerbiome);
         if (l == 0) {
            genlayerbiome = new GenLayerAddIsland(3L, genlayerbiome);
         }

         if (l == 1 || j == 1) {
            genlayerbiome = new GenLayerShore(1000L, genlayerbiome);
         }
      }

      GenLayerSmooth genlayersmooth1 = new GenLayerSmooth(1000L, genlayerbiome);
      GenLayerRiverMix genlayerrivermix = new GenLayerRiverMix(100L, genlayersmooth1, genlayersmooth);
      GenLayer genlayer3 = new GenLayerVoronoiZoom(10L, genlayerrivermix);
      genlayerrivermix.initWorldGenSeed(i);
      genlayer3.initWorldGenSeed(i);
      return new GenLayer[]{genlayerrivermix, genlayer3, genlayerrivermix};
   }

   public GenLayer(long i) {
      this.baseSeed = i;
      this.baseSeed = this.baseSeed * (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
      this.baseSeed += i;
      this.baseSeed = this.baseSeed * (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
      this.baseSeed += i;
      this.baseSeed = this.baseSeed * (this.baseSeed * 6364136223846793005L + 1442695040888963407L);
      this.baseSeed += i;
   }

   public void initWorldGenSeed(long i) {
      this.worldGenSeed = i;
      if (this.parent != null) {
         this.parent.initWorldGenSeed(i);
      }

      this.worldGenSeed = this.worldGenSeed * (this.worldGenSeed * 6364136223846793005L + 1442695040888963407L);
      this.worldGenSeed = this.worldGenSeed + this.baseSeed;
      this.worldGenSeed = this.worldGenSeed * (this.worldGenSeed * 6364136223846793005L + 1442695040888963407L);
      this.worldGenSeed = this.worldGenSeed + this.baseSeed;
      this.worldGenSeed = this.worldGenSeed * (this.worldGenSeed * 6364136223846793005L + 1442695040888963407L);
      this.worldGenSeed = this.worldGenSeed + this.baseSeed;
   }

   public void initChunkSeed(long i, long j) {
      this.chunkSeed = this.worldGenSeed;
      this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
      this.chunkSeed += i;
      this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
      this.chunkSeed += j;
      this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
      this.chunkSeed += i;
      this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
      this.chunkSeed += j;
   }

   protected int nextInt(int i) {
      int j = (int)((this.chunkSeed >> 24) % (long)i);
      if (j < 0) {
         j += i;
      }

      this.chunkSeed = this.chunkSeed * (this.chunkSeed * 6364136223846793005L + 1442695040888963407L);
      this.chunkSeed = this.chunkSeed + this.worldGenSeed;
      return j;
   }

   public abstract int[] getInts(int i, int j, int k, int l);

   protected static boolean biomesEqualOrMesaPlateau(int i, int j) {
      if (i == j) {
         return true;
      } else {
         Biome biome = Biome.getBiome(i);
         Biome biome1 = Biome.getBiome(j);
         if (biome == null || biome1 == null) {
            return false;
         } else {
            return biome != Biomes.MESA_ROCK && biome != Biomes.MESA_CLEAR_ROCK
               ? biome == biome1 || biome.getBiomeClass() == biome1.getBiomeClass()
               : biome1 == Biomes.MESA_ROCK || biome1 == Biomes.MESA_CLEAR_ROCK;
         }
      }
   }

   protected static boolean isBiomeOceanic(int i) {
      // Forge: BiomeManager.oceanBiomes (ocean, deep ocean, frozen ocean)
      Biome biome = Biome.getBiome(i);
      return biome == Biomes.OCEAN || biome == Biomes.DEEP_OCEAN || biome == Biomes.FROZEN_OCEAN;
   }

   protected int selectRandom(int... aint) {
      return aint[this.nextInt(aint.length)];
   }

   protected int selectModeOrRandom(int i, int j, int k, int l) {
      if (j == k && k == l) {
         return j;
      } else if (i == j && i == k) {
         return i;
      } else if (i == j && i == l) {
         return i;
      } else if (i == k && i == l) {
         return i;
      } else if (i == j && k != l) {
         return i;
      } else if (i == k && j != l) {
         return i;
      } else if (i == l && j != k) {
         return i;
      } else if (j == k && i != l) {
         return j;
      } else if (j == l && i != k) {
         return j;
      } else {
         return k == l && i != j ? k : this.selectRandom(i, j, k, l);
      }
   }
}
