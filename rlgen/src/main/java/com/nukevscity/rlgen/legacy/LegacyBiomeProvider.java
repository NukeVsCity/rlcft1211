package com.nukevscity.rlgen.legacy;

import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.legacy.layer.BiomeLists;
import com.nukevscity.rlgen.legacy.layer.GenLayer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1.12.2 BiomeProvider: genBiomes (1:4, used for terrain shape) and biomeIndexLayer (1:1 voronoi,
 * used for surfaces and the saved biome array). Not thread-safe; use under {@link LegacyGenerator#LOCK}.
 */
public final class LegacyBiomeProvider {
   private final GenLayer genBiomes;
   private final GenLayer biomeIndexLayer;
   private final Map<Long, Biome[]> chunkCache = new LinkedHashMap<>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<Long, Biome[]> eldest) {
         return this.size() > 1024;
      }
   };

   public LegacyBiomeProvider(long seed, BiomeLists lists) {
      Biomes.init();
      GenLayer[] agenlayer = GenLayer.initializeAllBiomeGenerators(seed, lists);
      this.genBiomes = agenlayer[0];
      this.biomeIndexLayer = agenlayer[1];
   }

   public Biome[] getBiomesForGeneration(Biome[] abiome, int i, int j, int k, int l) {
      if (abiome == null || abiome.length < k * l) {
         abiome = new Biome[k * l];
      }

      int[] aint = this.genBiomes.getInts(i, j, k, l);

      for (int i1 = 0; i1 < k * l; i1++) {
         abiome[i1] = Biome.getBiome(aint[i1], Biomes.DEFAULT);
      }

      return abiome;
   }

   public Biome[] getBiomes(Biome[] abiome, int i, int j, int k, int l) {
      if (abiome == null || abiome.length < k * l) {
         abiome = new Biome[k * l];
      }

      int[] aint = this.biomeIndexLayer.getInts(i, j, k, l);

      for (int i1 = 0; i1 < k * l; i1++) {
         abiome[i1] = Biome.getBiome(aint[i1], Biomes.DEFAULT);
      }

      return abiome;
   }

   /** World.getBiome(pos) through BiomeCache: the chunk's 16x16 voronoi biomes. */
   public Biome getBiome(int x, int z) {
      int cx = x >> 4;
      int cz = z >> 4;
      long key = (long)cx & 0xFFFFFFFFL | ((long)cz & 0xFFFFFFFFL) << 32;
      Biome[] chunk = this.chunkCache.get(key);
      if (chunk == null) {
         chunk = this.getBiomes(null, cx << 4, cz << 4, 16, 16);
         this.chunkCache.put(key, chunk);
      }

      return chunk[(x & 15) | (z & 15) << 4];
   }

   /** Raw 1:4 layer ids, for quick maps. */
   public int[] getGenBiomeIds(int x, int z, int w, int h) {
      return this.genBiomes.getInts(x, z, w, h);
   }
}
