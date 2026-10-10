package com.nukevscity.rlgen.legacy.gen;

import com.nukevscity.rlgen.legacy.ChunkPrimer;
import com.nukevscity.rlgen.legacy.LBlock;
import com.nukevscity.rlgen.legacy.LegacyWorld;
import com.nukevscity.rlgen.legacy.MathHelper;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.Random;

/** 1.12.2 MapGenBase. */
public class MapGenBase {
   protected int range = 8;
   protected Random rand = new Random();
   protected LegacyWorld world;

   public void generate(LegacyWorld world, int i, int j, ChunkPrimer chunkprimer) {
      int k = this.range;
      this.world = world;
      this.rand.setSeed(world.getSeed());
      long l = this.rand.nextLong();
      long i1 = this.rand.nextLong();

      for (int j1 = i - k; j1 <= i + k; j1++) {
         for (int k1 = j - k; k1 <= j + k; k1++) {
            long l1 = (long)j1 * l;
            long i2 = (long)k1 * i1;
            this.rand.setSeed(l1 ^ i2 ^ world.getSeed());
            this.recursiveGenerate(world, j1, k1, i, j, chunkprimer);
         }
      }
   }

   public static void setupChunkSeed(long i, Random random, int j, int k) {
      random.setSeed(i);
      long l = random.nextLong();
      long i1 = random.nextLong();
      long j1 = (long)j * l;
      long k1 = (long)k * i1;
      random.setSeed(j1 ^ k1 ^ i);
   }

   protected void recursiveGenerate(LegacyWorld world, int i, int j, int k, int l, ChunkPrimer chunkprimer) {
   }
}
