package com.nukevscity.rlgen.nether;

import com.nukevscity.rlgen.iaf.Mat112;
import com.nukevscity.rlgen.quark.QuarkGen;
import com.nukevscity.rlgen.quark.SpeleothemBlock;
import com.nukevscity.rlgen.quark.Templates;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Quark r1.6 nether IWorldGenerators with RLCraft's quark.cfg: BasaltGenerator (0), SmokerGenerator (0), NetherFossilGenerator (0),
 * ObsidianSpikeGenerator (0), BrokenPortalGenerator (1) and SpeleothemGenerator (1000, nether branch). Each gets the FML chunk random.
 */
public final class QuarkNether {
   private static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
   private static final String[] FOSSILS = {"spine_1", "spine_2", "spine_3", "spine_4", "skull_1", "skull_2", "skull_3", "skull_4"};
   // quark.cfg
   private static final int BASALT_RADIUS = 18;
   private static final int BASALT_RARITY = 20;
   private static final int BASALT_MIN_Y = 20;
   private static final int BASALT_MAX_Y = 120;
   private static final int FOSSIL_CHANCE = 75;
   private static final double SPIKE_CHUNK_CHANCE = 0.03;
   private static final int SPIKE_TRIES = 4;
   private static final double BIG_SPIKE_CHANCE = 0.1;
   private static final double PORTAL_CHANCE = 0.01;
   private static final int SPELEOTHEM_TRIES = 4;
   private static final int SPELEOTHEM_CLUSTER = 4;

   private QuarkNether() {
   }

   /** Blocks.LAVA in 1.12 is the still block (the lava sea); flowing lava was another block. */
   private static boolean stillLava(BlockState s) {
      return s.is(Blocks.LAVA) && s.getFluidState().isSource();
   }

   // ------------------------------------------------------------------ BasaltGenerator (StoneInfoBasedGenerator / MultiChunkFeatureGenerator)

   static void basalt(NetherWorld w, int chunkX, int chunkZ) {
      int chunkRadius = (int)Math.ceil(BASALT_RADIUS / 16.0);
      long worldSeed = w.seed ^ (long)"basalt".hashCode();
      Random worldRandom = new Random(worldSeed);
      long xSeed = worldRandom.nextLong() >> 3;
      long zSeed = worldRandom.nextLong() >> 3;
      for (int x = chunkX - chunkRadius; x <= chunkX + chunkRadius; x++) {
         for (int z = chunkZ - chunkRadius; z <= chunkZ + chunkRadius; z++) {
            Random chunkRandom = new Random(xSeed * (long)x + zSeed * (long)z ^ worldSeed);
            BlockPos source = basaltSource(chunkRandom, x, z);
            if (source != null) { // isSourceValid: the 1.12 nether is all Hell (NETHER)
               basaltPart(w, source, chunkX, chunkZ);
            }
         }
      }
   }

   private static BlockPos basaltSource(Random random, int chunkX, int chunkZ) {
      if (random.nextInt(BASALT_RARITY) != 0) {
         return null;
      }
      int lower = Math.abs(BASALT_MIN_Y);
      int range = Math.abs(BASALT_MAX_Y - BASALT_MIN_Y);
      int x = chunkX * 16 + random.nextInt(16) + 8;
      int y = random.nextInt(range) + lower;
      int z = chunkZ * 16 + random.nextInt(16) + 8;
      return new BlockPos(x, y, z);
   }

   private static void basaltPart(NetherWorld w, BlockPos src, int chunkX, int chunkZ) {
      BlockState basalt = NetherGen.BASALT.get().defaultBlockState();
      int minY = Math.max(1, BASALT_MIN_Y - BASALT_RADIUS);
      int maxY = Math.min(255, BASALT_MAX_Y + BASALT_RADIUS);
      int x0 = chunkX * 16 + 8;
      int z0 = chunkZ * 16 + 8;
      BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
      for (int x = 0; x < 16; x++) {
         for (int y = minY; y < maxY; y++) {
            for (int z = 0; z < 16; z++) {
               pos.set(x0 + x, y, z0 + z);
               // Vec3i.distanceSq: whole-block differences
               long dx = pos.getX() - src.getX();
               long dy = pos.getY() - src.getY();
               long dz = pos.getZ() - src.getZ();
               if (dx * dx + dy * dy + dz * dz < (long)(BASALT_RADIUS * BASALT_RADIUS)) {
                  BlockState s = w.get(pos);
                  if (s.is(Blocks.STONE) || s.is(Blocks.NETHERRACK)) {
                     w.set(pos, basalt);
                  }
               }
            }
         }
      }
   }

   // ------------------------------------------------------------------ SmokerGenerator

   static void smokers(NetherWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed, chunkX, chunkZ);
      if (!(random.nextFloat() < 0.4F)) {
         return;
      }
      tries:
      for (int i = 0; i < 20; i++) {
         int x = chunkX * 16 + random.nextInt(16) + 8;
         int z = chunkZ * 16 + random.nextInt(16) + 8;
         for (BlockPos pos = new BlockPos(x, 50, z); pos.getY() > 10; pos = pos.below()) {
            if (stillLava(w.get(pos))) {
               if (w.get(pos.below()).is(Blocks.NETHERRACK)) {
                  for (Direction face : HORIZONTALS) {
                     if (w.get(pos.relative(face)).is(Blocks.NETHERRACK)) {
                        w.set(pos.below(), NetherGen.SMOKER.get().defaultBlockState());
                        continue tries;
                     }
                  }
               }
               break;
            }
         }
      }
   }

   // ------------------------------------------------------------------ NetherFossilGenerator

   static void fossils(NetherWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed, chunkX, chunkZ);
      if (random.nextInt(FOSSIL_CHANCE) != 0) {
         return;
      }
      int x = chunkX * 16 + random.nextInt(16) + 8;
      int z = chunkZ * 16 + random.nextInt(16) + 8;
      BlockPos pos = new BlockPos(x, 40, z);
      BlockState stateAt;
      do {
         stateAt = w.get(pos);
         pos = pos.below();
      } while (stateAt.isAir() && pos.getY() > 0);
      if (stillLava(stateAt)) {
         fossil(w, random, pos.above(random.nextInt(2)));
      }
   }

   private static void fossil(NetherWorld w, Random random, BlockPos pos) {
      Rotation rotation = Rotation.values()[random.nextInt(Rotation.values().length)];
      int i = random.nextInt(FOSSILS.length);
      // the 1.12 fossils/fossil_* templates are the same bone blocks as 1.21's fossil/*
      StructureTemplate template = w.level.getLevel().getStructureManager().get(ResourceLocation.withDefaultNamespace("fossil/" + FOSSILS[i])).orElse(null);
      if (template == null) {
         return;
      }
      ChunkPos cp = new ChunkPos(pos);
      BoundingBox box = new BoundingBox(cp.getMinBlockX(), 0, cp.getMinBlockZ(), cp.getMaxBlockX(), 256, cp.getMaxBlockZ());
      StructurePlaceSettings settings = Templates.settings(rotation, Mirror.NONE, box);
      BlockPos basePos = template.getZeroPositionWithTransform(pos, Mirror.NONE, rotation);
      template.placeInWorld(w.level, basePos, basePos, settings, RandomSource.create(random.nextLong()), 2);
   }

   // ------------------------------------------------------------------ ObsidianSpikeGenerator

   static void spikes(NetherWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed, chunkX, chunkZ);
      if (!((double)random.nextFloat() < SPIKE_CHUNK_CHANCE)) {
         return;
      }
      Random worldRand = w.worldRand(0x5370696BL);
      for (int i = 0; i < SPIKE_TRIES; i++) {
         int x = chunkX * 16 + random.nextInt(16) + 8;
         int z = chunkZ * 16 + random.nextInt(16) + 8;
         for (BlockPos pos = new BlockPos(x, 50, z); pos.getY() > 10; pos = pos.below()) {
            if (stillLava(w.get(pos))) {
               spikeAt(w, worldRand, pos);
               break;
            }
         }
      }
   }

   private static void spikeAt(NetherWorld w, Random rand, BlockPos pos) {
      int heightBelow = 10;
      int heightBottom = 3 + rand.nextInt(3);
      int heightMiddle = 2 + rand.nextInt(4);
      int heightTop = 2 + rand.nextInt(3);
      boolean addSpawner = false;
      if ((double)rand.nextFloat() < BIG_SPIKE_CHANCE) {
         heightBottom += 7;
         heightMiddle += 8;
         heightTop += 4;
         addSpawner = true;
      }
      int checkHeight = heightBottom + heightMiddle + heightTop + 2;
      for (int i = 0; i < 5; i++) {
         for (int j = 0; j < 5; j++) {
            for (int k = 0; k < checkHeight; k++) {
               BlockState s = w.get(pos.offset(i - 2, k, j - 2));
               if (!s.isAir() && !s.is(Blocks.LAVA)) { // Material.LAVA: still and flowing
                  return;
               }
            }
         }
      }
      BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
      for (int i = 0; i < 3; i++) {
         for (int j = 0; j < 3; j++) {
            for (int k = 0; k < heightBottom + heightBelow; k++) {
               BlockPos placePos = pos.offset(i - 1, k - heightBelow, j - 1);
               if (w.inRegion(placePos) && w.get(placePos).getDestroySpeed(w.level, placePos) != -1.0F) {
                  w.set(placePos, obsidian);
               }
            }
         }
      }
      for (int i = 0; i < heightMiddle; i++) {
         BlockPos placePos = pos.above(heightBottom + i);
         w.set(placePos, obsidian);
         for (Direction face : HORIZONTALS) {
            w.set(placePos.relative(face), obsidian);
         }
      }
      for (int i = 0; i < heightTop; i++) {
         BlockPos placePos = pos.above(heightBottom + heightMiddle + i);
         w.set(placePos, obsidian);
         if (addSpawner && i == 0) {
            w.set(placePos, NetherGen.BLAZE_LANTERN.get().defaultBlockState());
            placePos = placePos.below();
            w.set(placePos, Blocks.SPAWNER.defaultBlockState());
            if (w.inRegion(placePos) && w.level.getBlockEntity(placePos) instanceof SpawnerBlockEntity spawner) {
               spawner.setEntityId(EntityType.BLAZE, RandomSource.create(placePos.asLong()));
            }
            placePos = placePos.below();
            w.set(placePos, Blocks.CHEST.defaultBlockState());
            long lootSeed = rand.nextLong();
            if (w.inRegion(placePos) && w.level.getBlockEntity(placePos) instanceof RandomizableContainerBlockEntity chest) {
               chest.setLootTable(BuiltInLootTables.NETHER_BRIDGE, lootSeed);
            }
         }
      }
   }

   // ------------------------------------------------------------------ BrokenPortalGenerator

   static void brokenPortals(NetherWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed, chunkX, chunkZ);
      if (random.nextDouble() > PORTAL_CHANCE) {
         return;
      }
      int x = chunkX * 16 + 8 + random.nextInt(16);
      int z = chunkZ * 16 + 8 + random.nextInt(16);
      int y = random.nextInt(100) + 20;
      BlockPos pos = new BlockPos(x, y, z);
      while (w.isAir(pos)) {
         pos = pos.below();
         if (pos.getY() < 10) {
            return;
         }
         if (Mat112.fullCube(w.get(pos))) {
            brokenPortal(w, w.worldRand(0x506F7274L), pos.above());
            break;
         }
      }
   }

   /** RotationHandler.applyRotation (AutoRegLib). */
   private static int[] rotate(Rotation rot, int x, int z) {
      return switch (rot) {
         case CLOCKWISE_180 -> new int[]{-x, -z};
         case CLOCKWISE_90 -> new int[]{z, -x};
         case COUNTERCLOCKWISE_90 -> new int[]{-z, x};
         default -> new int[]{x, z};
      };
   }

   private static void brokenPortal(NetherWorld w, Random rand, BlockPos pos) {
      Rotation rot = Rotation.values()[rand.nextInt(Rotation.values().length)];
      for (int i = 0; i < 4; i++) {
         for (int j = 0; j < 5; j++) {
            int[] xz = rotate(rot, i, 0);
            if (!w.isAir(pos.offset(xz[0], j, xz[1]))) {
               return;
            }
         }
      }
      BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
      for (int i = 0; i < 4; i++) {
         int[] xz = rotate(rot, i, 0);
         w.set(pos.offset(xz[0], 0, xz[1]), obsidian);
      }
      int count = 2 + rand.nextInt(3);
      int placed = 4 + count;
      for (int i = 0; i < count; i++) {
         w.set(pos.above(i + 1), obsidian);
      }
      count = 2 + rand.nextInt(3);
      placed += count;
      int[] xz = rotate(rot, 3, 0);
      for (int i = 0; i < count; i++) {
         w.set(pos.offset(xz[0], i + 1, xz[1]), obsidian);
      }
      int missing = 13 - placed;
      int spread = 3;
      xz = rotate(rot, 1, 0);
      for (int i = 0; i < missing; i++) {
         tries:
         for (int tries = 0; tries < 20; tries++) {
            BlockPos tryPos = pos.offset(xz[0] + rand.nextInt(spread * 2) - spread, 3, xz[1] + rand.nextInt(spread * 2) - spread);
            if (w.isAir(tryPos)) {
               while (w.isAir(tryPos)) {
                  tryPos = tryPos.below();
                  if (tryPos.getY() < 2) {
                     continue tries;
                  }
               }
               w.set(tryPos.above(), obsidian);
               break;
            }
         }
      }
   }

   // ------------------------------------------------------------------ SpeleothemGenerator (isNether branch)

   static void speleothems(NetherWorld w, int chunkX, int chunkZ) {
      Random random = QuarkGen.fmlRandom(w.seed, chunkX, chunkZ);
      int x = chunkX * 16 + 8;
      int z = chunkZ * 16 + 8;
      for (int i = 0; i < SPELEOTHEM_TRIES; i++) {
         BlockPos target = new BlockPos(x + random.nextInt(10), random.nextInt(128), z + random.nextInt(10));
         if (speleothemCluster(random, w, target, 6, SPELEOTHEM_CLUSTER)) {
            i++;
         }
      }
   }

   private static boolean speleothemCluster(Random random, NetherWorld w, BlockPos pos, int spread, int tries) {
      if (!findAndPlaceSpeleothem(random, w, pos)) {
         return false;
      }
      for (int i = 0; i < tries; i++) {
         BlockPos target = pos.offset(random.nextInt(spread * 2 + 1) - spread, random.nextInt(spread + 1) - spread, random.nextInt(spread * 2 + 1) - spread);
         findAndPlaceSpeleothem(random, w, target);
      }
      return true;
   }

   private static boolean findAndPlaceSpeleothem(Random random, NetherWorld w, BlockPos pos) {
      if (!w.isAir(pos)) {
         return false;
      }
      int off = -1000; // isNether: no search limit
      boolean up = random.nextBoolean();
      Direction diff = up ? Direction.UP : Direction.DOWN;
      // canBlockSeeSky is false everywhere under the 1.12 nether's bedrock roof
      BlockState stateAt;
      do {
         pos = pos.relative(diff);
         stateAt = w.get(pos);
         off++;
      } while (pos.getY() > 4 && pos.getY() < 200 && !Mat112.opaqueCube(stateAt) && off < 10);
      placeSpeleothem(random, w, pos, speleothemType(stateAt), !up);
      return true;
   }

   private static void placeSpeleothem(Random random, NetherWorld w, BlockPos pos, Block type, boolean up) {
      if (type == null) {
         return;
      }
      Direction diff = up ? Direction.UP : Direction.DOWN;
      int size = random.nextInt(3) == 0 ? 2 : 3;
      if (!up && random.nextInt(20) == 0) {
         size = 1;
      }
      for (int i = 0; i < size; i++) {
         pos = pos.relative(diff);
         if (!w.isAir(pos)) {
            return;
         }
         w.set(pos, type.defaultBlockState().setValue(SpeleothemBlock.SIZE, SpeleothemBlock.Size.values()[size - i - 1]));
      }
   }

   private static Block speleothemType(BlockState s) {
      if (s.is(Blocks.NETHERRACK)) {
         return NetherGen.NETHERRACK_SPELEOTHEM.get();
      }
      if (s.is(NetherGen.BASALT.get())) {
         return NetherGen.BASALT_SPELEOTHEM.get();
      }
      String name = s.is(Blocks.STONE) ? "stone" : s.is(Blocks.ANDESITE) ? "andesite" : s.is(Blocks.GRANITE) ? "granite" : s.is(Blocks.DIORITE) ? "diorite"
         : s.is(com.nukevscity.rlgen.RLGen.MARBLE.get()) ? "marble" : s.is(com.nukevscity.rlgen.RLGen.LIMESTONE.get()) ? "limestone"
         : s.is(com.nukevscity.rlgen.RLGen.JASPER.get()) ? "jasper" : s.is(com.nukevscity.rlgen.RLGen.SLATE.get()) ? "slate" : null;
      return name == null ? null : com.nukevscity.rlgen.RLGen.SPELEOTHEMS.get(name).get();
   }
}
