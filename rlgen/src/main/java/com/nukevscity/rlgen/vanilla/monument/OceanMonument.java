package com.nukevscity.rlgen.vanilla.monument;

import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;

/**
 * 1.12.2 StructureOceanMonument (MapGenStructure): starts on the 32/5 grid (seed 10387313) whose centre is deep ocean (radius 16)
 * and surroundings water (radius 29) on the 1:4 biome layer. Forge 1.12 does not patch the lists, so modded oceans don't count.
 * The pieces are built in the populate step (ChunkGeneratorOverworld.populate -> oceanMonumentGenerator.generateStructure).
 */
public final class OceanMonument {
   public static final int SPACING = 32;
   public static final int SEPARATION = 5;
   public static final int SALT = 10387313;

   private record Key(long seed, long chunk) {
   }

   /** One start per chunk that passed canSpawnStructureAtCoords (MapGenStructure.structureMap); NO_START caches a miss. */
   private static final Object NO_START = new Object();
   private static final Map<Key, Object> CACHE = new LinkedHashMap<>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<Key, Object> eldest) {
         return this.size() > 2048;
      }
   };

   private OceanMonument() {
   }

   private static List<Biome> waterBiomes() {
      return Arrays.asList(Biomes.OCEAN, Biomes.DEEP_OCEAN, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER);
   }

   /** BiomeProvider.areBiomesViable */
   private static boolean areBiomesViable(LegacyGenerator legacy, int i, int j, int k, List<Biome> list) {
      int l = i - k >> 2;
      int i1 = j - k >> 2;
      int j1 = i + k >> 2;
      int k1 = j + k >> 2;
      int l1 = j1 - l + 1;
      int i2 = k1 - i1 + 1;
      int[] aint;
      synchronized (LegacyGenerator.LOCK) {
         aint = legacy.getBiomeProvider().getGenBiomeIds(l, i1, l1, i2);
      }
      for (int j2 = 0; j2 < l1 * i2; j2++) {
         Biome biome = Biome.getBiome(aint[j2]);
         if (!list.contains(biome)) {
            return false;
         }
      }
      return true;
   }

   /** StructureOceanMonument.canSpawnStructureAtCoords */
   public static boolean canSpawnStructureAtCoords(LegacyGenerator legacy, long seed, int i, int j) {
      int k = i;
      int l = j;
      if (i < 0) {
         i -= SPACING - 1;
      }

      if (j < 0) {
         j -= SPACING - 1;
      }

      int i1 = i / SPACING;
      int j1 = j / SPACING;
      // World.setRandomSeed
      Random random = new Random((long)i1 * 341873128712L + (long)j1 * 132897987541L + seed + (long)SALT);
      i1 *= SPACING;
      j1 *= SPACING;
      i1 += (random.nextInt(SPACING - SEPARATION) + random.nextInt(SPACING - SEPARATION)) / 2;
      j1 += (random.nextInt(SPACING - SEPARATION) + random.nextInt(SPACING - SEPARATION)) / 2;
      if (k == i1 && l == j1) {
         if (!areBiomesViable(legacy, k * 16 + 8, l * 16 + 8, 16, Arrays.asList(Biomes.DEEP_OCEAN))) {
            return false;
         }

         return areBiomesViable(legacy, k * 16 + 8, l * 16 + 8, 29, waterBiomes());
      }

      return false;
   }

   /** StartMonument.create: the monument building of the start in chunk (i, j), or null when there is none. */
   public static MonumentPieces.MonumentBuilding start(LegacyGenerator legacy, long seed, int i, int j) {
      Key key = new Key(seed, ChunkPos.asLong(i, j));
      Object cached;
      synchronized (CACHE) {
         cached = CACHE.get(key);
      }
      if (cached == null) {
         if (canSpawnStructureAtCoords(legacy, seed, i, j)) {
            Random random = new Random(seed);
            long k = random.nextLong();
            long l = random.nextLong();
            long i1 = (long)i * k;
            long j1 = (long)j * l;
            random.setSeed(i1 ^ j1 ^ seed);
            int k1 = i * 16 + 8 - 29;
            int l1 = j * 16 + 8 - 29;
            // EnumFacing.Plane.HORIZONTAL.random: {NORTH, EAST, SOUTH, WEST}
            Direction facing = new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}[random.nextInt(4)];
            cached = new MonumentPieces.MonumentBuilding(random, k1, l1, facing);
         } else {
            cached = NO_START;
         }
         synchronized (CACHE) {
            CACHE.put(key, cached);
         }
      }
      return cached == NO_START ? null : (MonumentPieces.MonumentBuilding)cached;
   }

   /**
    * MapGenStructure.generateStructure for the populate box of chunk cp: the start's only component (the monument building) builds
    * the part inside the box with the populate random. Starts were made in 1.12 for every chunk within 8 of a generated chunk, so
    * every start whose box can reach this populate box is known; at most one can (32 chunk grid).
    */
   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      int i = (cp.x << 4) + 8;
      int j = (cp.z << 4) + 8;
      for (int sx = cp.x - 2; sx <= cp.x + 2; sx++) {
         for (int sz = cp.z - 2; sz <= cp.z + 2; sz++) {
            if (!isCandidate(w.seed(), sx, sz)) {
               continue;
            }
            MonumentPieces.MonumentBuilding building = start(legacy, w.seed(), sx, sz);
            if (building != null && building.getBoundingBox().intersectsWith(i, j, i + 15, j + 15)) {
               StructureBoundingBox box = new StructureBoundingBox(i, j, i + 15, j + 15);
               // StructureStart.generateStructure: components intersecting the box (the start's box is the building's)
               if (building.getBoundingBox().intersectsWith(box)) {
                  building.addComponentParts(w, rand, box);
               }
            }
         }
      }
   }

   /** The grid test of canSpawnStructureAtCoords without the biome check (cheap filter). */
   public static boolean isCandidate(long seed, int i, int j) {
      int k = i;
      int l = j;
      if (i < 0) {
         i -= SPACING - 1;
      }
      if (j < 0) {
         j -= SPACING - 1;
      }
      int i1 = i / SPACING;
      int j1 = j / SPACING;
      Random random = new Random((long)i1 * 341873128712L + (long)j1 * 132897987541L + seed + (long)SALT);
      i1 *= SPACING;
      j1 *= SPACING;
      i1 += (random.nextInt(SPACING - SEPARATION) + random.nextInt(SPACING - SEPARATION)) / 2;
      j1 += (random.nextInt(SPACING - SEPARATION) + random.nextInt(SPACING - SEPARATION)) / 2;
      return k == i1 && l == j1;
   }
}
