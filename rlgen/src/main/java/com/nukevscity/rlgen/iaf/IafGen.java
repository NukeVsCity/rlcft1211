package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.legacy.biome.Biome;
import com.nukevscity.rlgen.legacy.biome.Biomes;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ice and Fire 1.7.1 StructureGenerator (an IWorldGenerator of weight 0) with RLCraft's ice_and_fire.cfg. Each 1.12 chunk is planned
 * once on the 1.12 terrain with the chunk random Forge gave IWorldGenerators; the planned writes are replayed on the real world at the
 * end of each populate area's decoration (like 1.12, where it ran right after populate), so roosts burn the trees and plants there.
 * Mobs (dragons, cyclopes, sirens, hippocampi, sea serpents, stymphalian birds, dragon skeletons) are not spawned, but their random
 * draws are kept so everything after them lands where 1.12 put it.
 */
public final class IafGen {
   private static final int DANGEROUS_DISTANCE = 200;
   private static final int GORGON_CHANCE = 75;
   private static final int SIREN_CHANCE = 400;
   private static final int CYCLOPS_CHANCE = 170;
   private static final int PIXIE_CHANCE = 60;
   private static final int ROOST_CHANCE = 400;
   private static final int SKELETON_CHANCE = 700;
   private static final int HIPPOCAMPUS_CHANCE = 70;
   private static final int SERPENT_CHANCE = 200;
   private static final int BIRD_CHANCE = 100;
   private static final int DEN_CHANCE = 400;
   private static final int MYRMEX_CHANCE = 150;
   static final int ORE_TO_STONE_RATIO = 45;
   static final int DEN_GOLD_AMOUNT = 4;
   static final String FIRE_CHEST = "rlgen:iceandfire/fire_dragon_cave";
   static final String ICE_CHEST = "rlgen:iceandfire/ice_dragon_cave";
   static final String CYCLOPS_CHEST = "rlgen:iceandfire/cyclops_cave";
   /** EnumFacing.HORIZONTALS */
   private static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
   /** Planned chunks within this many chunks can reach a populate area (a gorgon temple ends up to 47 blocks from the chunk centre). */
   private static final int REACH = 4;
   private static final int HIVE_REACH = 9;
   static final String GORGON_TEMPLE = "rlgen:iceandfire/gorgon_temple";
   /** The gorgon temple template is 32 x 25 x 32. */
   private static final int TEMPLATE_SIZE = 32;

   private record Plan(Map<Long, List<IafWorld.Op>> byArea, List<String> found) {
   }

   private static final Map<String, Plan> PLANS = new LinkedHashMap<>(256, 0.75F, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Plan> e) {
         return this.size() > 16384;
      }
   };

   private IafGen() {
   }

   /** GameRegistry.generateWorld: the chunk random every IWorldGenerator got ("nextLong() >> 2 + 1" shifts by 3). */
   private static Random chunkRandom(long seed, int cx, int cz) {
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      return new Random(xSeed * (long)cx + zSeed * (long)cz ^ seed);
   }

   private static Plan plan(LegacyChunkGenerator gen, long seed, int cx, int cz) {
      String key = seed + "/" + cx + "/" + cz;
      synchronized (PLANS) {
         Plan p = PLANS.get(key);
         if (p != null) {
            return p;
         }
      }
      IafWorld world = new IafWorld(gen);
      // what Ice and Fire drew from an unseeded new Random(): seeded here so a world always comes out the same
      Random unseeded = new Random(seed ^ ((long)cx * 0x2F0B3A49L + (long)cz * 0x61C88647L) ^ 0x1CEA9DF1L);
      generate(world, chunkRandom(seed, cx, cz), unseeded, cx, cz);
      Map<Long, List<IafWorld.Op>> byArea = new HashMap<>();
      for (IafWorld.Op op : world.ops) {
         if (op.kind() == IafWorld.TEMPLATE) {
            // rotated and mirrored around its corner it can reach TEMPLATE_SIZE - 1 blocks to any side
            for (int ax = (op.x() - TEMPLATE_SIZE - 8) >> 4; ax <= (op.x() + TEMPLATE_SIZE - 8) >> 4; ax++) {
               for (int az = (op.z() - TEMPLATE_SIZE - 8) >> 4; az <= (op.z() + TEMPLATE_SIZE - 8) >> 4; az++) {
                  byArea.computeIfAbsent(ChunkPos.asLong(ax, az), k -> new ArrayList<>()).add(op);
               }
            }
         } else {
            byArea.computeIfAbsent(ChunkPos.asLong((op.x() - 8) >> 4, (op.z() - 8) >> 4), k -> new ArrayList<>()).add(op);
         }
      }
      Plan p = new Plan(byArea.isEmpty() ? Map.of() : byArea, world.found.isEmpty() ? List.of() : world.found);
      synchronized (PLANS) {
         PLANS.put(key, p);
      }
      return p;
   }

   /** Replays every planned write that lands in the populate area of this chunk (x and z from +8 to +23). */
   public static void apply(DWorld w, ChunkPos cp) {
      if (!(w.level.getLevel().getChunkSource().getGenerator() instanceof LegacyChunkGenerator gen)) {
         return;
      }
      long area = ChunkPos.asLong(cp.x, cp.z);
      List<BlockPos> shaped = new ArrayList<>();
      for (int i = -HIVE_REACH; i <= HIVE_REACH; i++) {
         for (int j = -HIVE_REACH; j <= HIVE_REACH; j++) {
            if ((Math.abs(i) > REACH || Math.abs(j) > REACH) && !hiveBiome(gen, cp.x + i, cp.z + j)) {
               continue;
            }
            List<IafWorld.Op> ops = plan(gen, w.seed(), cp.x + i, cp.z + j).byArea().get(area);
            if (ops != null) {
               for (IafWorld.Op op : ops) {
                  apply(w, op, shaped, cp);
               }
            }
         }
      }
      for (BlockPos p : shaped) {
         BlockState s = w.get(p);
         BlockState u = Block.updateFromNeighbourShapes(s, w.level, p);
         if (u != s && !u.isAir()) {
            w.set(p, u);
         }
      }
   }

   private static void apply(DWorld w, IafWorld.Op op, List<BlockPos> shaped, ChunkPos cp) {
      BlockPos p = new BlockPos(op.x(), op.y(), op.z());
      switch (op.kind()) {
         case IafWorld.SET -> {
            w.set(p, op.state());
            if (op.state().getBlock() instanceof FenceBlock) {
               shaped.add(p);
            }
         }
         case IafWorld.BURN, IafWorld.FREEZE -> {
            BlockState s = w.get(p);
            BlockState n = op.kind() == IafWorld.BURN ? Mat112.burn(s) : Mat112.freeze(s, s.is(Blocks.SNOW_BLOCK) && w.canSeeSky(p));
            if (n != null && n != s) {
               w.set(p, n);
            }
         }
         case IafWorld.ORE -> {
            if (Mat112.stone(w.get(p))) {
               w.set(p, op.state());
            }
         }
         case IafWorld.CHEST -> {
            w.set(p, op.state());
            BlockEntity be = w.level.getBlockEntity(p);
            if (be instanceof RandomizableContainerBlockEntity chest) {
               chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(op.loot())), op.lootSeed());
            }
         }
         case IafWorld.TEMPLATE -> {
            net.minecraft.server.level.ServerLevel level = w.level.getLevel();
            level.getStructureManager().get(ResourceLocation.parse(op.loot())).ifPresent(t -> {
               int x0 = cp.getMinBlockX() + 8;
               int z0 = cp.getMinBlockZ() + 8;
               net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings settings =
                  new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings()
                     .setRotation(net.minecraft.world.level.block.Rotation.values()[(int)(op.lootSeed() >> 2)])
                     .setMirror(net.minecraft.world.level.block.Mirror.values()[(int)(op.lootSeed() & 3)])
                     .setBoundingBox(new net.minecraft.world.level.levelgen.structure.BoundingBox(x0, 0, z0, x0 + 15, 255, z0 + 15));
               t.placeInWorld(w.level, p, p, settings, w.level.getRandom(), 2);
            });
         }
         case IafWorld.LILY -> {
            BlockPos surface = w.height(p);
            if (w.get(surface).canBeReplaced() && ((LilyBlock)op.state().getBlock()).canPlaceOn(w.get(surface.below()))) {
               w.set(surface, op.state());
            }
         }
         default -> {
         }
      }
   }

   /** /rlgen iaf: what Ice and Fire generates in the chunks around a position. */
   public static List<String> find(LegacyChunkGenerator gen, long seed, ChunkPos center, int radius) {
      List<String> out = new ArrayList<>();
      for (int i = -radius; i <= radius; i++) {
         for (int j = -radius; j <= radius; j++) {
            out.addAll(plan(gen, seed, center.x + i, center.z + j).found());
         }
      }
      return out;
   }

   // ------------------------------------------------------------------ StructureGenerator.generate

   private static boolean far(BlockPos pos) {
      return (double)pos.getX() * pos.getX() + (double)pos.getZ() * pos.getZ() > (double)(DANGEROUS_DISTANCE * DANGEROUS_DISTANCE);
   }

   private static void generate(IafWorld world, Random random, Random unseeded, int chunkX, int chunkZ) {
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      BlockPos height = world.height(new BlockPos(x, 0, z));
      Biome biome = world.biome(height);
      if (far(height)) {
         BlockState blockState = world.get(height.below());
         if (Mat112.opaqueCube(blockState) && world.isAir(height.above()) && random.nextInt(GORGON_CHANCE + 1) == 0 && world.hasType(height, "BEACH")) {
            int rotation = random.nextInt(4);
            int mirror = random.nextInt(3);
            BlockPos center = height.offset(TEMPLATE_SIZE / 2, -9, TEMPLATE_SIZE / 2);
            BlockPos corner1 = height.below();
            BlockPos corner2 = height.offset(TEMPLATE_SIZE, -1, 0);
            BlockPos corner3 = height.offset(TEMPLATE_SIZE, -1, TEMPLATE_SIZE);
            BlockPos corner4 = height.offset(0, -1, TEMPLATE_SIZE);
            if (Mat112.opaqueCube(world.get(center)) && Mat112.opaqueCube(world.get(corner1)) && Mat112.opaqueCube(world.get(corner2))
               && Mat112.opaqueCube(world.get(corner3)) && Mat112.opaqueCube(world.get(corner4))) {
               // Template.addBlocksToWorldChunk: the template's corner (not its middle) at "center"
               world.found.add("gorgon temple " + center.toShortString());
               world.template(center, GORGON_TEMPLE, rotation, mirror);
            }
         }
      }

      if (far(height) && world.hasType(height, "OCEAN") && !world.hasType(height, "COLD") && random.nextInt(SIREN_CHANCE + 1) == 0) {
         world.found.add("siren island " + height.toShortString());
         IafStructures.sirenIsland(world, random, height);
      }

      if (far(height) && world.hasType(height, "BEACH") && random.nextInt(CYCLOPS_CHANCE + 1) == 0 && Mat112.opaqueCube(world.get(height.below()))) {
         world.found.add("cyclops cave " + height.toShortString());
         IafStructures.cyclopsCave(world, random, unseeded, height);
      }

      if (far(height)) {
         boolean isSpookyForest = world.hasType(height, "FOREST") && (world.hasType(height, "SPOOKY") || world.hasType(height, "MAGICAL"));
         if (isSpookyForest && random.nextInt(PIXIE_CHANCE + 1) == 0) {
            // TODO pixie village (MapGenPixieVillage)
         }
      }

      if (far(height)) {
         boolean isHills = world.hasType(height, "HILLS") || world.hasType(height, "MOUNTAIN") && !world.hasType(height, "SNOWY");
         if (!biome.isSnowyBiome()
            && (double)biome.getDefaultTemperature() > -0.5
            && biome != Biomes.ICE_PLAINS
            && !world.hasType(height, "SNOWY")
            && !world.hasType(height, "WET")
            && !world.hasType(height, "OCEAN")
            && !world.hasType(height, "RIVER")
            && random.nextInt((isHills ? ROOST_CHANCE : ROOST_CHANCE * 2) + 1) == 0) {
            world.found.add("fire dragon roost " + world.height(new BlockPos(x, 0, z)).toShortString());
            IafStructures.roost(world, random, unseeded, world.height(new BlockPos(x, 0, z)), true);
         }
         if (world.hasType(height, "COLD") && world.hasType(height, "SNOWY") && random.nextInt((isHills ? ROOST_CHANCE : ROOST_CHANCE * 2) + 1) == 0) {
            world.found.add("ice dragon roost " + world.height(new BlockPos(x, 0, z)).toShortString());
            IafStructures.roost(world, random, unseeded, world.height(new BlockPos(x, 0, z)), false);
         }
      }

      // dragon skeletons (entities)
      if (world.hasType(height, "DRY") && world.hasType(height, "SANDY") && random.nextInt(SKELETON_CHANCE + 1) == 0) {
         random.nextInt(100);
         random.nextInt(360);
      }
      if (world.hasType(height, "COLD") && world.hasType(height, "SNOWY") && random.nextInt(SKELETON_CHANCE + 1) == 0) {
         random.nextInt(100);
         random.nextInt(360);
      }

      // hippocampi, sea serpents, stymphalian birds (entities)
      if (world.hasType(height, "OCEAN") && random.nextInt(HIPPOCAMPUS_CHANCE + 1) == 0) {
         for (int i = 0; i < random.nextInt(5); i++) {
            BlockPos pos = new BlockPos(x + random.nextInt(10) - 5, 20 + random.nextInt(40), z + random.nextInt(10) - 5);
            if (Mat112.of(world.get(pos)) == Mat112.WATER) {
               random.nextInt(5);
            }
         }
      }
      if (world.hasType(height, "OCEAN") && random.nextInt(SERPENT_CHANCE + 1) == 0) {
         BlockPos pos = new BlockPos(x + random.nextInt(10) - 5, 20 + random.nextInt(40), z + random.nextInt(10) - 5);
         if (Mat112.of(world.get(pos)) == Mat112.WATER) {
            random.nextInt(6);
            random.nextInt(15);
            random.nextFloat();
         }
      }
      if (far(height) && world.hasType(height, "SWAMP") && random.nextInt(BIRD_CHANCE + 1) == 0) {
         for (int ix = 0; ix < 4 + random.nextInt(4); ix++) {
            random.nextInt(10);
            random.nextInt(10);
         }
      }

      if (far(height)) {
         boolean isHillsx = world.hasType(height, "HILLS") || world.hasType(height, "MOUNTAIN");
         if (!biome.isSnowyBiome()
            && (double)biome.getDefaultTemperature() > -0.5
            && !world.hasType(height, "COLD")
            && !world.hasType(height, "SNOWY")
            && !world.hasType(height, "WET")
            && !world.hasType(height, "OCEAN")
            && !world.hasType(height, "RIVER")
            && !world.hasType(height, "BEACH")
            && random.nextInt((isHillsx ? DEN_CHANCE : DEN_CHANCE * 2) + 1) == 0) {
            int newY = 20 + random.nextInt(20);
            BlockPos pos = new BlockPos(x, newY, z);
            if (!world.canSeeSky(pos)) {
               world.found.add("fire dragon den " + pos.toShortString());
               IafStructures.dragonCave(world, random, unseeded, pos, true);
            }
         }
         if (world.hasType(height, "COLD") && world.hasType(height, "SNOWY") && !world.hasType(height, "BEACH")
            && random.nextInt((isHillsx ? DEN_CHANCE : DEN_CHANCE * 2) + 1) == 0) {
            int newY = 20 + random.nextInt(20);
            world.found.add("ice dragon den " + x + ", " + newY + ", " + z);
            IafStructures.dragonCave(world, random, unseeded, new BlockPos(x, newY, z), false);
         }
      }

      for (int silverAmount = 0; silverAmount < 2; silverAmount++) {
         int oreHeight = random.nextInt(32);
         int xOre = chunkX * 16 + random.nextInt(16);
         int zOre = chunkZ * 16 + random.nextInt(16);
         IafStructures.minable(world, random, new BlockPos(xOre, oreHeight, zOre), RLGen.SILVER_ORE.get().defaultBlockState(), 4 + random.nextInt(4));
      }

      if (world.hasType(height, "SNOWY")) {
         int count = 3 + random.nextInt(6);
         for (int sapphireAmount = 0; sapphireAmount < count; sapphireAmount++) {
            int oreHeight = random.nextInt(28) + 4;
            int xOre = chunkX * 16 + random.nextInt(16);
            int zOre = chunkZ * 16 + random.nextInt(16);
            world.ore(new BlockPos(xOre, oreHeight, zOre), RLGen.SAPPHIRE_ORE.get().defaultBlockState());
         }
      }

      if (world.hasType(height, "COLD") && world.hasType(height, "SNOWY")) {
         // TODO snow village (MapGenSnowVillage: an unseeded 1 in 301 chance, then a village start drawn from this random)
      }

      if (random.nextInt(MYRMEX_CHANCE) == 0 && far(height) && hiveBiome(world, height)) {
         int lowest = 256;
         for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
               lowest = Math.min(lowest, world.height(chunkX * 16 + i, chunkZ * 16 + j));
            }
         }
         // MyrmexWorldData.getNearestHive(height, 500) == null was tested before the biome, without drawing from the random
         if (MyrmexSavedData.claim(new BlockPos(height.getX(), lowest, height.getZ()))) {
            int down = Math.max(15, lowest - 20 + random.nextInt(10));
            boolean jungle = world.hasType(height, "JUNGLE");
            world.found.add((jungle ? "jungle" : "desert") + " myrmex hive " + height.getX() + ", " + down + ", " + height.getZ());
            new IafHive(world, unseeded, jungle).generate(random, new BlockPos(height.getX(), down, height.getZ()));
         }
      }

      if (world.hasType(height, "COLD") && world.hasType(height, "SNOWY") && random.nextInt(5) == 0) {
         world.lily(x, z, RLGen.FROST_LILY.get().defaultBlockState());
      }
      if (world.hasType(height, "HOT") && world.hasType(height, "SANDY") && random.nextInt(5) == 0) {
         world.lily(x, z, RLGen.FIRE_LILY.get().defaultBlockState());
      }
   }

   private static boolean hiveBiome(IafWorld world, BlockPos pos) {
      return world.hasType(pos, "JUNGLE") || world.hasType(pos, "HOT") && world.hasType(pos, "DRY") && world.hasType(pos, "SANDY");
   }

   /** Only chunks that can hold a myrmex hive are planned this far out (hives spread over more than a hundred blocks). */
   private static boolean hiveBiome(LegacyChunkGenerator gen, int cx, int cz) {
      java.util.Set<String> t = com.nukevscity.rlgen.rc.BiomeDict.TYPES.get(gen.legacyBiome(cx * 16 + 8, cz * 16 + 8).getRegistryName());
      return t != null && (t.contains("JUNGLE") || t.contains("HOT") && t.contains("DRY") && t.contains("SANDY"));
   }

   static BlockState chest(Random unseeded) {
      return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, HORIZONTALS[unseeded.nextInt(3)]);
   }

}
