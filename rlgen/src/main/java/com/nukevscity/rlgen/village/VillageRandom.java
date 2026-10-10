package com.nukevscity.rlgen.village;

import java.util.Random;
import net.minecraft.world.level.ChunkPos;

/**
 * The random village pieces draw from while being placed. In 1.12 this was the chunk's populate random (shared by all pieces
 * placed in that chunk, in order); 1.21 places the pieces of a chunk one after another on one thread, so one random per thread is
 * reseeded whenever the chunk changes.
 */
final class VillageRandom {
   private static final ThreadLocal<VillageRandom> CURRENT = ThreadLocal.withInitial(VillageRandom::new);
   private final Random random = new Random();
   private long seed;
   private long chunk = Long.MIN_VALUE;

   static Random forChunk(long seed, ChunkPos pos) {
      VillageRandom r = CURRENT.get();
      if (r.chunk != pos.toLong() || r.seed != seed) {
         r.chunk = pos.toLong();
         r.seed = seed;
         // ChunkGeneratorOverworld.populate
         r.random.setSeed(seed);
         long k = r.random.nextLong() / 2L * 2L + 1L;
         long l = r.random.nextLong() / 2L * 2L + 1L;
         r.random.setSeed((long)pos.x * k + (long)pos.z * l ^ seed);
      }
      return r.random;
   }
}
