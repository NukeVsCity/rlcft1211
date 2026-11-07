package com.nukevscity.rlgen.stronghold;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slf4j.Logger;

/**
 * 1.12.2 MapGenStronghold (Forge 1.12: every biome with baseHeight above 0 that BiomeManager did not blacklist; no RLCraft mod
 * changed the lists. Recurrent Complex wraps it through InitMapGenEvent but has no stronghold decorations, so it stays vanilla).
 * The 128 ring positions come from the 1.12 biome layers; the starts are built with the MapGenBase random and placed in the
 * populate step ({@link #generate}), like MapGenStructure.generateStructure.
 */
public final class Strongholds {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEBUG = Boolean.getBoolean("rlgen.rcdebug");
   private static final int COUNT = 128;
   private static final double DISTANCE = 32.0;
   private static final int SPREAD = 3;
   /** Strongholds reach at most 112 blocks plus a piece (16) from the start's corner. */
   private static final int REACH = 112 + 32;

   /** coords as 1.12 kept them; distinct: one start per chunk (structureMap), in first-seen order. */
   private record Positions(long seed, ChunkPos[] coords, Set<Long> set, List<ChunkPos> distinct) {
   }

   private static volatile Positions positions;
   private static final Map<Long, Start> STARTS = new ConcurrentHashMap<>();
   private static volatile long startsSeed;
   /** StructureStrongholdPieces keeps its piece weights in static fields. */
   private static final Object BUILD_LOCK = new Object();

   private Strongholds() {
   }

   /** MapGenStronghold.Start: the components in 1.12 order and the start's box. */
   public static final class Start {
      final int chunkX;
      final int chunkZ;
      final List<Component> components;
      final SBox box;

      Start(int chunkX, int chunkZ, List<Component> components, SBox box) {
         this.chunkX = chunkX;
         this.chunkZ = chunkZ;
         this.components = components;
         this.box = box;
      }

      public SBox box() {
         return this.box;
      }
   }

   /** MapGenStronghold constructor (with Forge's patch): the allowed biomes. */
   private static boolean allowed(@Nullable Biome biome) {
      return biome != null && biome.getBaseHeight() > 0.0F;
   }

   /** MapGenStronghold.generatePositions (no starts saved yet, so all 128 come from the rings). */
   private static ChunkPos[] generatePositions(LegacyGenerator legacy) {
      ChunkPos[] coords = new ChunkPos[COUNT];
      Random random = new Random();
      random.setSeed(legacy.getSeed());
      double d1 = random.nextDouble() * Math.PI * 2.0;
      int j = 0;
      int k = 0;
      int spread = SPREAD;
      synchronized (LegacyGenerator.LOCK) {
         for (int i1 = 0; i1 < coords.length; i1++) {
            double d0 = 4.0 * DISTANCE + DISTANCE * (double)j * 6.0 + (random.nextDouble() - 0.5) * DISTANCE * 2.5;
            int j1 = (int)Math.round(Math.cos(d1) * d0);
            int k1 = (int)Math.round(Math.sin(d1) * d0);
            BlockPos blockpos = findBiomePosition(legacy, (j1 << 4) + 8, (k1 << 4) + 8, 112, random);
            if (blockpos != null) {
               j1 = blockpos.getX() >> 4;
               k1 = blockpos.getZ() >> 4;
            }
            coords[i1] = new ChunkPos(j1, k1);
            d1 += (Math.PI * 2) / (double)spread;
            if (++k == spread) {
               j++;
               k = 0;
               spread = spread + 2 * spread / (j + 1);
               spread = Math.min(spread, coords.length - i1);
               d1 += random.nextDouble() * Math.PI * 2.0;
            }
         }
      }
      return coords;
   }

   /** BiomeProvider.findBiomePosition on the 1:4 layer. */
   @Nullable
   private static BlockPos findBiomePosition(LegacyGenerator legacy, int i, int j, int k, Random random) {
      int l = i - k >> 2;
      int i1 = j - k >> 2;
      int j1 = i + k >> 2;
      int k1 = j + k >> 2;
      int l1 = j1 - l + 1;
      int i2 = k1 - i1 + 1;
      int[] aint = legacy.getBiomeProvider().getGenBiomeIds(l, i1, l1, i2);
      BlockPos blockpos = null;
      int j2 = 0;
      for (int k2 = 0; k2 < l1 * i2; k2++) {
         int l2 = l + k2 % l1 << 2;
         int i3 = i1 + k2 / l1 << 2;
         if (allowed(Biome.getBiome(aint[k2])) && (blockpos == null || random.nextInt(j2 + 1) == 0)) {
            blockpos = new BlockPos(l2, 0, i3);
            j2++;
         }
      }
      return blockpos;
   }

   static Positions positions(LegacyGenerator legacy) {
      Positions p = positions;
      long seed = legacy.getSeed();
      if (p == null || p.seed != seed) {
         synchronized (Strongholds.class) {
            p = positions;
            if (p == null || p.seed != seed) {
               ChunkPos[] coords = generatePositions(legacy);
               Set<Long> set = new HashSet<>();
               List<ChunkPos> distinct = new ArrayList<>();
               for (ChunkPos c : coords) {
                  if (set.add(c.toLong())) {
                     distinct.add(c);
                  }
               }
               p = new Positions(seed, coords, set, distinct);
               positions = p;
               if (DEBUG) {
                  LOGGER.info("rlgen: strongholds for seed {}: first {} {}, {} {}, {} {}", seed, coords[0].x, coords[0].z, coords[1].x, coords[1].z, coords[2].x,
                     coords[2].z);
               }
            }
         }
      }
      return p;
   }

   /** MapGenStronghold.canSpawnStructureAtCoords */
   public static boolean isStart(LegacyGenerator legacy, int cx, int cz) {
      return positions(legacy).set.contains(ChunkPos.asLong(cx, cz));
   }

   /** The start at a ring position (built once per seed). */
   public static Start start(LegacyGenerator legacy, int cx, int cz) {
      long seed = legacy.getSeed();
      if (startsSeed != seed) {
         synchronized (STARTS) {
            if (startsSeed != seed) {
               STARTS.clear();
               startsSeed = seed;
            }
         }
      }
      return STARTS.computeIfAbsent(ChunkPos.asLong(cx, cz), key -> build(seed, cx, cz));
   }

   /** MapGenBase.generate + MapGenStructure.recursiveGenerate + MapGenStronghold.getStructureStart. */
   private static Start build(long seed, int cx, int cz) {
      Random rand = new Random(seed);
      long j = rand.nextLong();
      long k = rand.nextLong();
      rand.setSeed((long)cx * j ^ (long)cz * k ^ seed);
      rand.nextInt();
      synchronized (BUILD_LOCK) {
         Start start = newStart(rand, cx, cz);
         while (start.components.isEmpty() || ((StrongholdPieces.Stairs2)start.components.get(0)).strongholdPortalRoom == null) {
            start = newStart(rand, cx, cz);
         }
         if (DEBUG) {
            LOGGER.info("rlgen: stronghold {} {}: {} pieces, box {}, portal room {}", cx, cz, start.components.size(), start.box,
               ((StrongholdPieces.Stairs2)start.components.get(0)).strongholdPortalRoom.getBoundingBox());
         }
         return start;
      }
   }

   /** MapGenStronghold.Start(world, random, x, z) */
   private static Start newStart(Random random, int i, int j) {
      List<Component> components = new ArrayList<>();
      StrongholdPieces.prepareStructurePieces();
      StrongholdPieces.Stairs2 stairs2 = new StrongholdPieces.Stairs2(0, random, (i << 4) + 2, (j << 4) + 2);
      components.add(stairs2);
      stairs2.buildComponent(stairs2, components, random);
      List<Component> list = stairs2.pendingChildren;
      while (!list.isEmpty()) {
         int k = random.nextInt(list.size());
         Component c = list.remove(k);
         c.buildComponent(stairs2, components, random);
      }
      // StructureStart.updateBoundingBox
      SBox box = SBox.getNewBoundingBox();
      for (Component c : components) {
         box.expandTo(c.getBoundingBox());
      }
      // StructureStart.markAvailableHeight(world, random, 10), sea level 63
      int i1 = 63 - 10;
      int j1 = box.getYSize() + 1;
      if (j1 < i1) {
         j1 += random.nextInt(i1 - j1);
      }
      int k1 = j1 - box.maxY;
      box.offset(0, k1, 0);
      for (Component c : components) {
         c.offset(0, k1, 0);
      }
      return new Start(i, j, components, box);
   }

   /**
    * LegacyStructures.STRONGHOLD: MapGenStructure.generateStructure for the populate area of this chunk. A component that finds
    * liquid returns false and is dropped from its start for good (StructureStart.generateStructure), remembered in the world.
    */
   public static void generate(DWorld w, Random rand, ChunkPos cp, LegacyGenerator legacy) {
      if (!w.level.getLevel().getServer().getWorldData().worldGenOptions().generateStructures()) {
         return;
      }
      int i = (cp.x << 4) + 8;
      int j = (cp.z << 4) + 8;
      SBox area = new SBox(i, j, i + 15, j + 15);
      for (ChunkPos c : positions(legacy).distinct) {
         int sx = (c.x << 4) + 2;
         int sz = (c.z << 4) + 2;
         if (Math.abs(sx - i) > REACH + 16 || Math.abs(sz - j) > REACH + 16) {
            continue;
         }
         Start start = start(legacy, c.x, c.z);
         if (!start.box.intersectsWith(i, j, i + 15, j + 15)) {
            continue;
         }
         StrongholdSavedData data = StrongholdSavedData.current();
         synchronized (start) {
            List<Component> components = start.components;
            for (int n = 0; n < components.size(); n++) {
               Component comp = components.get(n);
               if (!comp.getBoundingBox().intersectsWith(area) || data.isRemoved(c, n)) {
                  continue;
               }
               try {
                  if (!comp.addComponentParts(w, rand, area)) {
                     data.remove(c, n);
                  }
               } catch (RuntimeException e) {
                  LOGGER.error("rlgen: stronghold piece {} at {} failed in chunk {}", comp.getClass().getSimpleName(), comp.getBoundingBox(), cp, e);
               }
            }
         }
      }
   }

   /** MapGenStronghold.getNearestStructurePos for /locate and eyes of ender, or null when the set has no 1.12 stronghold. */
   @Nullable
   public static Pair<BlockPos, Holder<Structure>> locate(LegacyChunkGenerator generator, HolderSet<Structure> set, BlockPos pos) {
      Holder<Structure> holder = null;
      for (Holder<Structure> h : set) {
         if (h.value() instanceof StrongholdStructure) {
            holder = h;
            break;
         }
      }
      LegacyGenerator legacy = generator.legacyGenerator();
      if (holder == null || legacy == null) {
         return null;
      }
      BlockPos best = null;
      double d0 = Double.MAX_VALUE;
      for (ChunkPos c : positions(legacy).coords) {
         BlockPos p = new BlockPos((c.x << 4) + 8, 32, (c.z << 4) + 8);
         double d1 = p.distSqr(pos);
         if (best == null || d1 < d0) {
            best = p;
            d0 = d1;
         }
      }
      return best == null ? null : Pair.of(best, holder);
   }
}
