package com.nukevscity.rlgen.rc;

import com.mojang.logging.LogUtils;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import com.nukevscity.rlgen.world.RcPiece;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.slf4j.Logger;

/**
 * RC 1.4.8 RCBiomeDecorator (DecorateBiomeEvent handler) for BIG_SHROOM with the VanillaDecorationAdapter: RC's decoration structures
 * replace some of the vanilla big mushrooms (reccomplex.cfg baseWeight_big_mushroom = 0.2). Trees have weight 0 in RLCraft.
 */
public final class RcDecoration {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final boolean DEBUG = Boolean.getBoolean("rlgen.rcdebug");
   /** RCConfig baseWeight_big_mushroom */
   static final double BASE_WEIGHT_BIG_MUSHROOM = 0.2;
   static final int STRUCTURE_TRIES = 5;

   record Entry(RcStructureDef def, RcStructureDef.NaturalGen gen, double weight) {
   }

   private static final Map<String, List<Entry>> SELECTORS = new ConcurrentHashMap<>();
   private static volatile List<RcStructureDef> activeOrder;

   private RcDecoration() {
   }

   /** SimpleLeveledRegistry.activeMap is a HashMap (Collectors.toMap) of structure ids: StructureSelector iterates it in that order. */
   private static List<RcStructureDef> activeOrder() {
      List<RcStructureDef> l = activeOrder;
      if (l == null) {
         Map<String, RcStructureDef> m = new HashMap<>();
         for (RcStructureDef d : RcRegistry.get().structures()) {
            m.put(d.id, d);
         }
         l = new ArrayList<>(m.values());
         activeOrder = l;
      }
      return l;
   }

   /** StructureSelector over VanillaDecorationGeneration, entries of one DecorationType. */
   static List<Entry> selector(Biome biome, String type) {
      return SELECTORS.computeIfAbsent(biome.getRegistryName() + "|" + type, k -> {
         List<Entry> out = new ArrayList<>();
         for (RcStructureDef def : activeOrder()) {
            for (RcStructureDef.NaturalGen g : def.decoration) {
               if (g.category.equals(type)) {
                  double w = g.weight(biome);
                  if (w > 0.0) {
                     out.add(new Entry(def, g, w));
                  }
               }
            }
         }
         return out;
      });
   }

   /**
    * RCBiomeDecorator.decorate(world, random, chunkPos, BIG_SHROOM) at the BIG_SHROOM event of BiomeDecorator. Returns how many vanilla
    * big mushrooms the decorator still generates afterwards (the event result: DENY after RC's own adapter loop, else all of them).
    */
   public static int bigMushrooms(DWorld w, Random random, ChunkPos cp, Biome biome, int origAmount) {
      String name = biome.getRegistryName();
      if (name.equals("roofed_forest") || name.equals("mutated_roofed_forest")) {
         return origAmount; // Result.ALLOW
      }
      if (origAmount < 0) {
         return origAmount;
      }
      int vanillaAmount = decorate(w, random, cp, biome, "big_mushroom", BASE_WEIGHT_BIG_MUSHROOM, origAmount, true);
      if (vanillaAmount == origAmount || vanillaAmount < 0) {
         return origAmount; // null result: vanilla goes on
      }
      return vanillaAmount; // VanillaDecorationAdapter.generate draws exactly like the decorator loop, then DENY
   }

   /** RCBiomeDecorator.doDecorate + tryGenerate */
   static int decorate(DWorld w, Random random, ChunkPos cp, Biome biome, String type, double baseWeight, int amount, boolean mayGiveUp) {
      if (baseWeight <= 0.0) {
         return amount;
      }
      List<Entry> entries = selector(biome, type);
      double totalWeight = 0.0;
      for (Entry e : entries) {
         totalWeight += e.weight;
      }
      if (totalWeight <= 0.0) {
         return amount;
      }
      int rcAmount = 0;
      for (int i = 0; i < amount; i++) {
         if (random.nextDouble() * (totalWeight * baseWeight + 1.0) > 1.0) {
            rcAmount++;
         }
      }
      if (rcAmount <= 0 && mayGiveUp) {
         return -1;
      }
      for (int i = 0; i < rcAmount; i++) {
         for (int t = 0; t < STRUCTURE_TRIES; t++) {
            Entry e = RcRegistry.selectWeighted(random, entries, Entry::weight);
            if (generate(e, w, cp, random)) {
               break;
            }
         }
      }
      return amount - rcAmount;
   }

   /** RCBiomeDecorator.generate: StructureGenerator with maturity SUGGEST, partially in the chunk, overlaps allowed, from center. */
   static boolean generate(Entry e, DWorld w, ChunkPos cp, Random random) {
      long seed = random.nextLong();
      LegacyChunkGenerator gen = com.nukevscity.rlgen.quark.QuarkGen.gen(w);
      if (gen == null) {
         return false;
      }
      RcStructureDef def = e.def;
      try {
         // WorldGenStructures.randomSurfacePos
         Random posRandom = new Random(seed ^ RcGenerator.POS_SEED);
         int x = cp.getMinBlockX() + posRandom.nextInt(16) + 8;
         int z = cp.getMinBlockZ() + posRandom.nextInt(16) + 8;
         RcTransform t = RcGenerator.transformFor(def, seed);
         int[] size = t.applySize(def.size);
         BlockPos lower0 = new BlockPos(x - size[0] / 2, 0, z - size[2] / 2);
         BoundingBox surfaceBB = new BoundingBox(lower0.getX(), 0, lower0.getZ(), lower0.getX() + size[0] - 1, size[1] - 1, lower0.getZ() + size[2] - 1);
         BlockPos c = RcGenerator.center(surfaceBB);
         View view = new View(w, cp, gen);
         Biome biome = view.biome(c.getX(), c.getZ()); // Environment.inNature
         int y = e.gen.placer.place(new RcPlacer.PlaceCtx(def, t, lower0, size, new Random(seed ^ RcGenerator.PLACE_SEED), view));
         if (y < 0) {
            return false;
         }
         BlockPos lower = lower0.above(y);
         RcGenerator.Instance inst = RcGenerator.prepare(def, e.gen.id, seed, t, lower, new RcEnv(biome, "decoration", e.gen.id, null), view, 0, true);
         if (inst == null) {
            return false;
         }
         if (inst.bb.minY() < 1 || inst.bb.maxY() > RcPlacer.HEIGHT - 1 - 1) {
            return false;
         }
         // 1.12 generated the populate area now and complemented the rest chunk by chunk; here the whole 3x3 region 1.21 lets us write
         BoundingBox region = new BoundingBox(cp.getMinBlockX() - 16, 0, cp.getMinBlockZ() - 16, cp.getMaxBlockX() + 16, RcPlacer.HEIGHT - 1,
            cp.getMaxBlockZ() + 16);
         RcGenerator.spawn(inst, view, region, seed, new RcEnv(biome, "decoration", e.gen.id, null));
         view.live.finish();
         if (DEBUG) {
            LOGGER.info("rlgen: RC decoration {} at {}", def.id, lower);
         }
         return true;
      } catch (RuntimeException ex) {
         LOGGER.error("rlgen: RC decoration {} failed in chunk {}", def.id, cp, ex);
         return false;
      }
   }

   /** The real world during population (1.12 placed and prepared decoration on it), legacy biomes. */
   static final class View implements RcGenerator.Oracle {
      final DWorld w;
      final RcPiece.LiveView live;

      View(DWorld w, ChunkPos cp, LegacyChunkGenerator gen) {
         this.w = w;
         this.live = new RcPiece.LiveView(w.level, cp, gen.oracle());
      }

      @Override
      public BlockState get(BlockPos pos) {
         return this.w.get(pos); // 1.12 world: Y 0..255
      }

      @Override
      public boolean set(BlockPos pos, BlockState state) {
         return pos.getY() >= 0 && pos.getY() < RcPlacer.HEIGHT && this.live.set(pos, state);
      }

      @Override
      public net.minecraft.world.level.WorldGenLevel level() {
         return this.w.level;
      }

      @Override
      public boolean isLive(BlockPos pos) {
         return this.live.isLive(pos);
      }

      @Override
      public Biome biome(int x, int z) {
         return this.w.biome(new BlockPos(x, 0, z));
      }
   }
}
