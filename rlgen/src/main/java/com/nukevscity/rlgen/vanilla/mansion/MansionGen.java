package com.nukevscity.rlgen.vanilla.mansion;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

/**
 * 1.12 WoodlandMansion (MapGenStructure): starts on the 80-chunk grid (separation 20, salt 10387319) where the 1:4 biome layer is
 * all roofed forest within 32 blocks, built with the MapGenBase random of the start chunk and cached. {@link #generate} is the
 * populate step (generateStructure with the populate box cx*16+8..+23).
 */
public final class MansionGen {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEBUG = Boolean.getBoolean("rlgen.rcdebug");
   private static final int SPACING = 80;
   private static final int SALT = 10387319;

   private record Key(long seed, int x, int z) {
   }

   private static final ConcurrentHashMap<Key, Optional<MansionStart>> STARTS = new ConcurrentHashMap<>();

   private MansionGen() {
   }

   /** WoodlandMansion.canSpawnStructureAtCoords: the grid cell's chosen chunk (World.setRandomSeed), x then z. */
   private static int[] gridChunk(long seed, int i, int j) {
      int k = i;
      int l = j;
      if (i < 0) {
         k = i - 79;
      }
      if (j < 0) {
         l = j - 79;
      }
      int i1 = k / 80;
      int j1 = l / 80;
      Random random = new Random((long)i1 * 341873128712L + (long)j1 * 132897987541L + seed + (long)SALT);
      i1 *= 80;
      j1 *= 80;
      i1 += (random.nextInt(60) + random.nextInt(60)) / 2;
      j1 += (random.nextInt(60) + random.nextInt(60)) / 2;
      return new int[]{i1, j1};
   }

   /** BiomeProvider.areBiomesViable(x, z, 32, ALLOWED_BIOMES) on the 1:4 layer. */
   private static boolean biomesViable(LegacyGenerator legacy, int x, int z, int radius) {
      int l = x - radius >> 2;
      int i1 = z - radius >> 2;
      int j1 = x + radius >> 2;
      int k1 = z + radius >> 2;
      int l1 = j1 - l + 1;
      int i2 = k1 - i1 + 1;
      int[] aint;
      synchronized (LegacyGenerator.LOCK) {
         aint = legacy.getBiomeProvider().getGenBiomeIds(l, i1, l1, i2).clone();
      }
      for (int j2 = 0; j2 < l1 * i2; j2++) {
         Biome biome = Biome.getBiome(aint[j2]);
         if (biome != Biomes.ROOFED_FOREST && biome != Biomes.MUTATED_ROOFED_FOREST) {
            return false;
         }
      }
      return true;
   }

   static boolean canSpawnStructureAtCoords(LegacyGenerator legacy, long seed, int i, int j) {
      int[] c = gridChunk(seed, i, j);
      return i == c[0] && j == c[1] && biomesViable(legacy, i * 16 + 8, j * 16 + 8, 32);
   }

   /** The start 1.12 made for this chunk (valid or not), or null when there is none. */
   public static MansionStart start(LegacyGenerator legacy, long seed, int cx, int cz) {
      Key key = new Key(seed, cx, cz);
      Optional<MansionStart> s = STARTS.get(key);
      if (s == null) {
         s = Optional.ofNullable(create(legacy, seed, cx, cz));
         Optional<MansionStart> old = STARTS.putIfAbsent(key, s);
         if (old != null) {
            s = old;
         }
      }
      return s.orElse(null);
   }

   private static MansionStart create(LegacyGenerator legacy, long seed, int cx, int cz) {
      if (!canSpawnStructureAtCoords(legacy, seed, cx, cz)) {
         return null;
      }
      // MapGenBase.generate / MapGenStructure.recursiveGenerate
      Random rand = new Random(seed);
      long j = rand.nextLong();
      long k = rand.nextLong();
      rand.setSeed((long)cx * j ^ (long)cz * k ^ seed);
      rand.nextInt();
      MansionStart start = new MansionStart(legacy, rand, cx, cz);
      if (DEBUG) {
         LOGGER.info("rlgen: mansion {} {} valid={} pieces={} box={},{},{}..{},{},{}", cx, cz, start.isValid, start.components.size(),
            start.isValid ? start.boundingBox.minX : 0, start.isValid ? start.boundingBox.minY : 0, start.isValid ? start.boundingBox.minZ : 0,
            start.isValid ? start.boundingBox.maxX : 0, start.isValid ? start.boundingBox.maxY : 0, start.isValid ? start.boundingBox.maxZ : 0);
      }
      return start;
   }

   /** MapGenStructure.generateStructure(world, rand, chunkPos) for the mansions near this populate area. */
   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      int x0 = (cp.x << 4) + 8;
      int z0 = (cp.z << 4) + 8;
      long seed = w.seed();
      // starts within 8 chunks (MapGenBase range) of the populated chunks: at most one grid cell each way
      for (int gx = Math.floorDiv(cp.x - 9, SPACING); gx <= Math.floorDiv(cp.x + 9, SPACING); gx++) {
         for (int gz = Math.floorDiv(cp.z - 9, SPACING); gz <= Math.floorDiv(cp.z + 9, SPACING); gz++) {
            int[] c = gridChunk(seed, gx * SPACING, gz * SPACING);
            if (Math.abs(c[0] - cp.x) > 9 || Math.abs(c[1] - cp.z) > 9) {
               continue;
            }
            MansionStart start = start(legacy, seed, c[0], c[1]);
            if (start != null && start.isValid && start.boundingBox.intersectsWith(x0, z0, x0 + 15, z0 + 15)) {
               try {
                  start.generateStructure(w, rand, Box.columns(x0, z0, x0 + 15, z0 + 15));
               } catch (RuntimeException e) {
                  LOGGER.error("rlgen: mansion {} {} failed in chunk {}", c[0], c[1], cp, e);
               }
            }
         }
      }
   }
}
