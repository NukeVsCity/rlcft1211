package com.nukevscity.rlgen.deco;

import com.nukevscity.rlgen.legacy.LegacyGenerator;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.rc.RcGenerator;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 ChunkGeneratorOverworld.populate + BiomeDecorator, split over three 1.21 decoration steps so Dynamic Trees still plants
 * its trees where 1.12 did (after ores and patches, before flowers). One java.util.Random runs through all three, like 1.12's.
 */
public final class LegacyPopulator {
   private static final int GRANITE_DIORITE_ANDESITE = 0; // Quark's "Revamp stone gen" denied these in RLCraft

   private record State(long chunk, Random random, Biome biome, BiomeDeco deco, BiomeDeco.Counts counts) {
   }

   private static final ThreadLocal<State> STATE = new ThreadLocal<>();

   /** Chunk (as a long) whose populate area a village reached, for the lake check; set before the decoration steps run. */
   private static final ThreadLocal<Long> VILLAGE = new ThreadLocal<>();

   private LegacyPopulator() {
   }

   public static void setVillage(ChunkPos cp, boolean village) {
      VILLAGE.set(village ? cp.toLong() : null);
   }

   private static boolean village(ChunkPos cp) {
      Long v = VILLAGE.get();
      return v != null && v == cp.toLong();
   }

   private static BlockPos origin(ChunkPos cp) {
      return new BlockPos(cp.getMinBlockX(), 0, cp.getMinBlockZ());
   }

   private static State state(DWorld w, ChunkPos cp) {
      State s = STATE.get();
      if (s != null && s.chunk == cp.toLong()) {
         return s;
      }
      // a step ran without the earlier ones (should not happen): start a fresh random for this chunk
      Random r = new Random(w.seed ^ cp.toLong());
      Biome biome = w.biome(origin(cp).offset(16, 0, 16));
      BiomeDeco deco = BiomeDeco.of(biome);
      s = new State(cp.toLong(), r, biome, deco, deco.counts());
      STATE.set(s);
      return s;
   }

   /** Step LAKES: the populate random, Recurrent Complex's draws (PopulateChunkEvent.Pre), lakes, lava lakes, dungeons. */
   public static void lakes(DWorld w, ChunkPos cp, LegacyGenerator legacy) {
      BlockPos pos = origin(cp);
      Biome biome = w.biome(pos.offset(16, 0, 16));
      Random rand = new Random(w.seed);
      long i1 = rand.nextLong() / 2L * 2L + 1L;
      long j1 = rand.nextLong() / 2L * 2L + 1L;
      rand.setSeed((long)cp.x * i1 + (long)cp.z * j1 ^ w.seed);
      RcGenerator.consumeSelection(rand, cp.x, cp.z, legacy);
      BiomeDeco deco = BiomeDeco.of(biome);
      STATE.set(new State(cp.toLong(), rand, biome, deco, deco.counts()));
      String name = biome.getRegistryName();
      boolean flag = village(cp);
      if (!name.equals("desert") && !name.equals("desert_hills") && !flag && rand.nextInt(4) == 0) {
         int k1 = rand.nextInt(16) + 8;
         int l1 = rand.nextInt(256);
         int i2 = rand.nextInt(16) + 8;
         Gen.lake(w, rand, pos.offset(k1, l1, i2), false);
      }
      if (!flag && rand.nextInt(80 / 10) == 0) {
         int k2 = rand.nextInt(16) + 8;
         int j3 = rand.nextInt(rand.nextInt(248) + 8);
         int i4 = rand.nextInt(16) + 8;
         if (j3 < 63 || rand.nextInt(80 / 8) == 0) {
            Gen.lake(w, rand, pos.offset(k2, j3, i4), true);
         }
      }
      for (int l2 = 0; l2 < 8; l2++) {
         int k3 = rand.nextInt(16) + 8;
         int j4 = rand.nextInt(256);
         int j2 = rand.nextInt(16) + 8;
         Gen.dungeon(w, rand, pos.offset(k3, j4, j2));
      }
   }

   /** Step UNDERGROUND_ORES: Biome.decorate up to the trees (biome extras that run first, ores, sand/clay/gravel). */
   public static void ores(DWorld w, ChunkPos cp) {
      State s = state(w, cp);
      Random r = s.random;
      BlockPos pos = origin(cp);
      BiomeDeco d = s.deco;
      BiomeDeco.Counts c = s.counts;
      d.pre(w, r, pos, c);
      // generateOres (ChunkGeneratorSettings defaults); Quark's revamped stones ran from the DIRT ore event
      com.nukevscity.rlgen.quark.RevampStoneGen.generate(w, cp);
      ore(w, r, pos, 10, Blocks.DIRT.defaultBlockState(), 33, 0, 256);
      ore(w, r, pos, 8, Blocks.GRAVEL.defaultBlockState(), 33, 0, 256);
      ore(w, r, pos, GRANITE_DIORITE_ANDESITE, Blocks.DIORITE.defaultBlockState(), 33, 0, 80);
      ore(w, r, pos, GRANITE_DIORITE_ANDESITE, Blocks.GRANITE.defaultBlockState(), 33, 0, 80);
      ore(w, r, pos, GRANITE_DIORITE_ANDESITE, Blocks.ANDESITE.defaultBlockState(), 33, 0, 80);
      ore(w, r, pos, 20, Blocks.COAL_ORE.defaultBlockState(), 17, 0, 128);
      ore(w, r, pos, 20, Blocks.IRON_ORE.defaultBlockState(), 9, 0, 64);
      ore(w, r, pos, 2, Blocks.GOLD_ORE.defaultBlockState(), 9, 0, 32);
      ore(w, r, pos, 8, Blocks.REDSTONE_ORE.defaultBlockState(), 8, 0, 16);
      ore(w, r, pos, 1, Blocks.DIAMOND_ORE.defaultBlockState(), 8, 0, 16);
      for (int l = 0; l < 1; l++) {
         BlockPos b = pos.offset(r.nextInt(16), r.nextInt(16) + r.nextInt(16) + 16 - 16, r.nextInt(16));
         Gen.minable(w, r, b, Blocks.LAPIS_ORE.defaultBlockState(), 7, Gen.NATURAL_STONE);
      }
      if (d.mesaGold) {
         ore(w, r, pos, 20, Blocks.GOLD_ORE.defaultBlockState(), 9, 32, 80);
      }
      for (int i = 0; i < c.sandPatchesPerChunk; i++) {
         int j = r.nextInt(16) + 8;
         int k = r.nextInt(16) + 8;
         Gen.sandDisc(w, r, w.topSolidOrLiquid(pos.offset(j, 0, k)), Blocks.SAND.defaultBlockState(), 7);
      }
      for (int i1 = 0; i1 < c.clayPerChunk; i1++) {
         int l1 = r.nextInt(16) + 8;
         int i6 = r.nextInt(16) + 8;
         Gen.clay(w, r, w.topSolidOrLiquid(pos.offset(l1, 0, i6)), 4);
      }
      for (int j1 = 0; j1 < c.gravelPatchesPerChunk; j1++) {
         int i2 = r.nextInt(16) + 8;
         int j6 = r.nextInt(16) + 8;
         Gen.sandDisc(w, r, w.topSolidOrLiquid(pos.offset(i2, 0, j6)), Blocks.GRAVEL.defaultBlockState(), 6);
      }
   }

   /** BiomeDecorator.genStandardOre1 */
   static void ore(DWorld w, Random r, BlockPos pos, int count, BlockState ore, int size, int min, int max) {
      if (max < min) {
         int t = min;
         min = max;
         max = t;
      } else if (max == min) {
         if (min < 255) {
            max++;
         } else {
            min--;
         }
      }
      for (int i = 0; i < count; i++) {
         BlockPos b = pos.offset(r.nextInt(16), r.nextInt(max - min) + min, r.nextInt(16));
         Gen.minable(w, r, b, ore, size, Gen.NATURAL_STONE);
      }
   }

   /** Step TOP_LAYER_MODIFICATION: the rest of Biome.decorate (Dynamic Trees grew in between), then the snow and ice pass. */
   public static void plants(DWorld w, ChunkPos cp) {
      State s = state(w, cp);
      STATE.remove();
      Random r = s.random;
      BlockPos pos = origin(cp);
      BiomeDeco d = s.deco;
      BiomeDeco.Counts c = s.counts;
      // trees: the extra-tree roll still happened, the TREE event was denied by Dynamic Trees
      if (r.nextFloat() < c.extraTreeChance) {
         c.treesPerChunk++;
      }
      // Quark "Big mushrooms generate in swamps" (0.5 per chunk) took the BIG_SHROOM event in swamps, by the biome at the chunk corner
      String corner = w.biome(pos).getRegistryName();
      if (corner.equals("swampland") || corner.equals("mutated_swampland")) {
         if (!(r.nextDouble() > 0.5)) {
            int x = r.nextInt(16) + 8;
            int z = r.nextInt(16) + 8;
            Gen.bigMushroom(w, r, w.height(pos.offset(x, 0, z)), null);
         }
      } else {
         for (int k2 = 0; k2 < c.bigMushroomsPerChunk; k2++) {
            int x = r.nextInt(16) + 8;
            int z = r.nextInt(16) + 8;
            Gen.bigMushroom(w, r, w.height(pos.offset(x, 0, z)), null);
         }
      }
      for (int l2 = 0; l2 < c.flowersPerChunk; l2++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) + 32;
         if (h > 0) {
            BlockPos b = pos.offset(x, r.nextInt(h), z);
            Gen.bushes(w, r, b, d.flower(r, b));
         }
      }
      for (int i3 = 0; i3 < c.grassPerChunk; i3++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            int y = r.nextInt(h);
            Gen.tallGrass(w, r, pos.offset(x, y, z), d.grass(r));
         }
      }
      for (int j3 = 0; j3 < c.deadBushPerChunk; j3++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.deadBush(w, r, pos.offset(x, r.nextInt(h), z));
         }
      }
      for (int k3 = 0; k3 < c.waterlilyPerChunk; k3++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            BlockPos b = pos.offset(x, r.nextInt(h), z);
            while (b.getY() > 0 && w.isAir(b.below())) {
               b = b.below();
            }
            Gen.waterlily(w, r, b);
         }
      }
      BlockState brown = Blocks.BROWN_MUSHROOM.defaultBlockState();
      BlockState red = Blocks.RED_MUSHROOM.defaultBlockState();
      for (int l3 = 0; l3 < c.mushroomsPerChunk; l3++) {
         if (r.nextInt(4) == 0) {
            int x = r.nextInt(16) + 8;
            int z = r.nextInt(16) + 8;
            Gen.bushes(w, r, w.height(pos.offset(x, 0, z)), brown);
         }
         if (r.nextInt(8) == 0) {
            int x = r.nextInt(16) + 8;
            int z = r.nextInt(16) + 8;
            int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
            if (h > 0) {
               Gen.bushes(w, r, pos.offset(x, r.nextInt(h), z), red);
            }
         }
      }
      if (r.nextInt(4) == 0) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.bushes(w, r, pos.offset(x, r.nextInt(h), z), brown);
         }
      }
      if (r.nextInt(8) == 0) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.bushes(w, r, pos.offset(x, r.nextInt(h), z), red);
         }
      }
      for (int k4 = 0; k4 < c.reedsPerChunk; k4++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.reeds(w, r, pos.offset(x, r.nextInt(h), z));
         }
      }
      for (int l4 = 0; l4 < 10; l4++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.reeds(w, r, pos.offset(x, r.nextInt(h), z));
         }
      }
      if (r.nextInt(32) == 0) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.pumpkin(w, r, pos.offset(x, r.nextInt(h), z));
         }
      }
      for (int j5 = 0; j5 < c.cactiPerChunk; j5++) {
         int x = r.nextInt(16) + 8;
         int z = r.nextInt(16) + 8;
         int h = w.height(pos.getX() + x, pos.getZ() + z) * 2;
         if (h > 0) {
            Gen.cactus(w, r, pos.offset(x, r.nextInt(h), z));
         }
      }
      if (d.generateFalls) {
         for (int k5 = 0; k5 < 50; k5++) {
            int x = r.nextInt(16) + 8;
            int z = r.nextInt(16) + 8;
            int i17 = r.nextInt(248) + 8;
            Gen.liquids(w, r, pos.offset(x, r.nextInt(i17), z), true);
         }
         for (int l5 = 0; l5 < 20; l5++) {
            int x = r.nextInt(16) + 8;
            int z = r.nextInt(16) + 8;
            int y = r.nextInt(r.nextInt(r.nextInt(240) + 8) + 8);
            Gen.liquids(w, r, pos.offset(x, y, z), false);
         }
      }
      d.post(w, r, pos);
      freeze(w, pos.offset(8, 0, 8));
      // IWorldGenerators that ran after populate, by weight (No Tree Punching 3, Quark fairy rings 5)
      com.nukevscity.rlgen.ntp.LooseRocks.generate(w, cp);
      com.nukevscity.rlgen.quark.FairyRings.generate(w, cp);
   }

   /** The populate snow/ice pass over the decorated 16x16 (offset by 8, like population). */
   static void freeze(DWorld w, BlockPos from) {
      for (int x = 0; x < 16; x++) {
         for (int z = 0; z < 16; z++) {
            BlockPos top = w.precipitationHeight(from.offset(x, 0, z));
            BlockPos below = top.below();
            if (w.canBlockFreezeWater(below)) {
               w.set(below, Blocks.ICE.defaultBlockState());
            }
            if (w.canSnowAt(top)) {
               w.set(top, Blocks.SNOW.defaultBlockState());
            }
         }
      }
   }
}
