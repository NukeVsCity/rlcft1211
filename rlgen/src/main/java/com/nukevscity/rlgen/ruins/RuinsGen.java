package com.nukevscity.rlgen.ruins;

import atomicstryker.ruins.common.RuinData;
import atomicstryker.ruins.common.RuinTemplate;
import atomicstryker.ruins.common.RuinsTemplates;
import atomicstryker.ruins.common.RuinsWorld;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;

/**
 * Ruins' RuinGenerator.generateNormal / createBuilding for the overworld. Every chunk makes its tries with the chunk random Forge gave
 * IWorldGenerators; a ruin is tested (surface, spawn and min distances, leveling) the first time any chunk it covers is generated, and
 * that decision is kept with the world (RuinsSavedData), so a ruin is always built whole, like 1.12 building it in one go.
 */
public final class RuinsGen {
   private static final Logger LOGGER = LogUtils.getLogger();
   static final Object LOCK = new Object();

   /** One try that picked a template: the 1.12 x, z, rotation and template, and a seed for its build. */
   record Candidate(int chunkX, int chunkZ, int index, RuinTemplate template, int x, int z, int rotate, long buildSeed) {
      String key() {
         return this.chunkX + "," + this.chunkZ + "," + this.index;
      }
   }

   private static final Map<String, List<Candidate>> CANDIDATES = new LinkedHashMap<>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, List<Candidate>> e) {
         return this.size() > 8192;
      }
   };
   private static final Map<String, RuinsWorld> BUILT = new LinkedHashMap<>(16, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, RuinsWorld> e) {
         return this.size() > 64;
      }
   };

   private RuinsGen() {
   }

   /** GameRegistry.generateWorld: the chunk random every IWorldGenerator got ("nextLong() >> 2 + 1" shifts by 3). */
   static Random chunkRandom(long seed, int cx, int cz) {
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      return new Random(xSeed * (long)cx + zSeed * (long)cz ^ seed);
   }

   static List<Candidate> candidates(LegacyChunkGenerator gen, long seed, int cx, int cz) {
      String k = seed + "/" + cx + "/" + cz;
      synchronized (CANDIDATES) {
         List<Candidate> c = CANDIDATES.get(k);
         if (c != null) {
            return c;
         }
      }
      RuinsTemplates files = RuinsTemplates.get();
      Random random = chunkRandom(seed, cx, cz);
      List<Candidate> out = new ArrayList<>();
      int xBase = cx * 16;
      int zBase = cz * 16;
      for (int c = 0; c < files.triesPerChunkNormal; c++) {
         if (random.nextFloat() * 100.0F < files.chanceToSpawnNormal) {
            int x = xBase + random.nextInt(16);
            int z = zBase + random.nextInt(16);
            int rotate = random.nextInt(4);
            String biomeID = RuinTemplate.pathOf(gen.legacyBiome(x, z));
            if (files.useGeneric(random, biomeID)) {
               biomeID = "generic";
            }
            RuinTemplate t = files.getTemplate(random, biomeID);
            if (t != null) {
               // the build draws from its own random (1.12 went on with this one, so later tries in a chunk differ after a build)
               out.add(new Candidate(cx, cz, c, t, x, z, rotate, seed ^ ((long)cx * 341873128712L + (long)cz * 132897987541L) * 31L + c));
            }
         }
      }
      synchronized (CANDIDATES) {
         CANDIDATES.put(k, out);
      }
      return out;
   }

   /** Candidates from nearby chunks whose build area could reach this chunk. */
   static List<Candidate> reaching(LegacyChunkGenerator gen, long seed, ChunkPos cp) {
      int r = (RuinsTemplates.get().maxRadius >> 4) + 1;
      List<Candidate> out = new ArrayList<>();
      for (int i = cp.x - r; i <= cp.x + r; i++) {
         for (int j = cp.z - r; j <= cp.z + r; j++) {
            for (Candidate c : candidates(gen, seed, i, j)) {
               int rad = c.template().radius();
               if (c.x() + rad >= cp.getMinBlockX() && c.x() - rad <= cp.getMaxBlockX() && c.z() + rad >= cp.getMinBlockZ() && c.z() - rad <= cp.getMaxBlockZ()) {
                  out.add(c);
               }
            }
         }
      }
      return out;
   }

   /** The y the ruin is built at, or -1 if it is not built (decided once and remembered). */
   static int decide(LegacyChunkGenerator gen, Candidate c) {
      synchronized (LOCK) {
         RuinsSavedData data = RuinsSavedData.current();
         Integer known = data.decision(c.key());
         if (known != null) {
            return known;
         }
         int y = test(gen, c, data);
         data.decide(c.key(), y, y >= 0 ? c.template().getRuinData(c.x(), y, c.z(), c.rotate()) : null);
         if (y >= 0) {
            LOGGER.debug("rlgen: ruin {} at {} {} {}", c.template().getName(), c.x(), y, c.z());
         }
         return y;
      }
   }

   private static int test(LegacyChunkGenerator gen, Candidate c, RuinsSavedData data) {
      RuinTemplate t = c.template();
      RuinsWorld world = new RuinsWorld(gen::legacyState);
      int y = findSuitableY(world, t, c.x(), c.z());
      if (y <= 0) {
         return -1;
      }
      if (!checkMinDistance(t, t.getRuinData(c.x(), y, c.z(), c.rotate()), data)) {
         return -1;
      }
      return t.checkArea(world, c.x(), y, c.z(), c.rotate());
   }

   private static int findSuitableY(RuinsWorld world, RuinTemplate r, int x, int z) {
      for (int y = 255; y > 7; y--) {
         var state = world.getBlockState(new BlockPos(x, y, z));
         if (!r.isIgnoredBlock(state)) {
            return r.isAcceptableSurface(state) ? y + 1 : -1;
         }
      }
      return -1;
   }

   /** RuinGenerator.checkMinDistance with the world spawn taken as 0, 0. */
   private static boolean checkMinDistance(RuinTemplate ruinTemplate, RuinData ruinData, RuinsSavedData data) {
      RuinsTemplates files = RuinsTemplates.get();
      int min_distance = Math.max(files.anySpawnMinDistance, ruinTemplate.spawnMinDistance);
      if (ruinData.xMin < min_distance && -ruinData.xMax < min_distance && ruinData.zMin < min_distance && -ruinData.zMax < min_distance) {
         return false;
      }
      int max_distance = Math.min(files.anySpawnMaxDistance, ruinTemplate.spawnMaxDistance);
      if (ruinData.xMax > max_distance || -ruinData.xMin > max_distance || ruinData.zMax > max_distance || -ruinData.zMin > max_distance) {
         return false;
      }
      int self = (int)(ruinTemplate.uniqueMinDistance == 0 ? files.templateInstancesMinDistance : (float)ruinTemplate.uniqueMinDistance);
      int any = (int)files.anyRuinsMinDistance;
      RuinData checkSelf = grow(ruinData, self);
      RuinData checkOther = grow(ruinData, any);
      return !data.anyIntersecting(checkSelf, checkOther, ruinData.name);
   }

   private static RuinData grow(RuinData d, int by) {
      return new RuinData(d.xMin - by, d.xMax + by, d.yMin - by, d.yMax + by, d.zMin - by, d.zMax + by, d.name);
   }

   static RuinsWorld built(LegacyChunkGenerator gen, Candidate c, int y) {
      synchronized (LOCK) {
         RuinsWorld w = BUILT.get(c.key());
         if (w == null) {
            w = new RuinsWorld(gen::legacyState);
            c.template().doBuild(w, new Random(c.buildSeed()), c.x(), y, c.z(), c.rotate(), false, false);
            BUILT.put(c.key(), w);
         }
         return w;
      }
   }

   static Candidate find(LegacyChunkGenerator gen, long seed, int cx, int cz, int index) {
      for (Candidate c : candidates(gen, seed, cx, cz)) {
         if (c.index() == index) {
            return c;
         }
      }
      return null;
   }
}
