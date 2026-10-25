package com.nukevscity.rlgen.quark;

import com.nukevscity.rlgen.RLGen;
import com.nukevscity.rlgen.deco.DWorld;
import com.nukevscity.rlgen.deco.Gen;
import com.nukevscity.rlgen.iaf.Mat112;
import com.nukevscity.rlgen.rc.BiomeDict;
import com.nukevscity.rlgen.world.LegacyChunkGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.FancyTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/**
 * Quark r1.6 world generators with RLCraft's quark.cfg: varied dungeons (Populate DUNGEON event, drawing from the populate random),
 * and the IWorldGenerators clay (0), blossom trees (0), monster boxes (200), speleothems (1000), cave roots (2000) and
 * archaeologist houses (3000). Crystal caves and pirate ships are planned with Ice and Fire's (see QuarkPlanned).
 * Calls to world.rand (unseeded in 1.12) use a random seeded from the chunk.
 */
public final class QuarkGen {
   private static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};
   private static final EntityType<?>[] DUNGEON_MOBS = {EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};

   private QuarkGen() {
   }

   /** GameRegistry.generateWorld: the chunk random every IWorldGenerator got. */
   public static Random fmlRandom(long seed, int cx, int cz) {
      Random fml = new Random(seed);
      long xSeed = fml.nextLong() >> 2 + 1L;
      long zSeed = fml.nextLong() >> 2 + 1L;
      return new Random(xSeed * (long)cx + zSeed * (long)cz ^ seed);
   }

   /** What 1.12 drew from world.rand, seeded so a world comes out the same every time. */
   static Random worldRand(long seed, int cx, int cz, long salt) {
      return new Random(seed ^ ((long)cx * 0x5DEECE66DL + (long)cz * 0x2545F491L) ^ salt);
   }

   public static LegacyChunkGenerator gen(DWorld w) {
      return w.level.getLevel().getChunkSource().getGenerator() instanceof LegacyChunkGenerator g ? g : null;
   }

   /** getBlockHardness: -1 where 1.12 had bedrock. */
   static float hardness(DWorld w, BlockPos p) {
      LegacyChunkGenerator g = gen(w);
      if (g != null && g.legacyBedrock(p)) {
         return -1.0F;
      }
      return Mat112.hardness(w.get(p));
   }

   static boolean hasType(DWorld w, BlockPos p, String type) {
      Set<String> t = BiomeDict.TYPES.get(w.biome(p).getRegistryName());
      return t != null && t.contains(type);
   }

   // ------------------------------------------------------------------ VariedDungeons

   /** VariedDungeons.onDungeonSpawn, called with the populate random right before the vanilla dungeons. */
   public static void variedDungeons(DWorld w, Random rand, BlockPos chunkOrigin, int cx, int cz) {
      for (int k = 0; k < 7; k++) {
         int x = rand.nextInt(16) + 8;
         int y = rand.nextInt(256);
         int z = rand.nextInt(16) + 8;
         BlockPos generatePos = chunkOrigin.offset(x, y, z);
         if (couldDungeonGenerate(w, rand, generatePos)) {
            placeDungeonAt(w, rand, generatePos, cx, cz);
            break;
         }
      }
   }

   private static boolean couldDungeonGenerate(DWorld w, Random rand, BlockPos position) {
      int j = rand.nextInt(2) + 2;
      int k = -j - 1;
      int l = j + 1;
      int k1 = rand.nextInt(2) + 2;
      int l1 = -k1 - 1;
      int i2 = k1 + 1;
      int j2 = 0;
      for (int k2 = k; k2 <= l; k2++) {
         for (int l2 = -1; l2 <= 4; l2++) {
            for (int i3 = l1; i3 <= i2; i3++) {
               BlockPos blockpos = position.offset(k2, l2, i3);
               boolean flag = DWorld.isSolid(w.get(blockpos));
               if (l2 == -1 && !flag) {
                  return false;
               }
               if (l2 == 4 && !flag) {
                  return false;
               }
               if ((k2 == k || k2 == l || i3 == l1 || i3 == i2) && l2 == 0 && w.isAir(blockpos) && w.isAir(blockpos.above())) {
                  j2++;
               }
            }
         }
      }
      return j2 >= 1 && j2 <= 5;
   }

   private static void placeDungeonAt(DWorld w, Random rand, BlockPos position, int cx, int cz) {
      int dungeonType = rand.nextInt(10);
      StructureTemplate template = Templates.get(w.level.getLevel().getStructureManager(), "rlgen:quark/dungeon_" + dungeonType).orElse(null);
      Rotation rotation = Rotation.values()[rand.nextInt(Rotation.values().length)];
      if (template == null) {
         return;
      }
      StructurePlaceSettings settings = Templates.settings(rotation, Mirror.NONE, Templates.region(w, cx, cz));
      var size = template.getSize();
      for (int x = 0; x < size.getX(); x++) {
         for (int y = 0; y < size.getY(); y++) {
            for (int z = 0; z < size.getZ(); z++) {
               BlockPos checkPos = position.offset(StructureTemplate.calculateRelativePosition(settings, new BlockPos(x, y, z)));
               if (hardness(w, checkPos) == -1.0F || w.canSeeSky(checkPos)) {
                  return;
               }
            }
         }
      }
      Templates.place(w, template, position, settings, rand.nextLong() ^ position.asLong());
      int spawners = 0;
      List<BlockPos> chests = new ArrayList<>();
      for (Map.Entry<BlockPos, String> e : Templates.dataBlocks(template, position, settings).entrySet()) {
         BlockPos pos = e.getKey();
         if (e.getValue().equals("spawner")) {
            spawners++;
            EntityType<?> mob = DUNGEON_MOBS[rand.nextInt(DUNGEON_MOBS.length)]; // DungeonHooks.getRandomDungeonMob: one draw
            w.set(pos, Blocks.SPAWNER.defaultBlockState());
            if (w.level.getBlockEntity(pos) instanceof SpawnerBlockEntity spawner && w.get(pos).is(Blocks.SPAWNER)) {
               spawner.setEntityId(mob, RandomSource.create(pos.asLong()));
            }
         } else if (e.getValue().equals("chest")) {
            chests.add(pos);
         }
      }
      int maxChests = spawners * 2 + rand.nextInt(spawners + 2);
      while (chests.size() > maxChests) {
         int i = rand.nextInt(chests.size());
         w.set(chests.remove(i), Blocks.AIR.defaultBlockState());
      }
      for (BlockPos pos : chests) {
         chest(w, pos, chestFacing(w, pos), BuiltInLootTables.SIMPLE_DUNGEON, rand.nextLong());
      }
   }

   /** BlockChest.correctFacing: away from a solid neighbour. */
   private static Direction chestFacing(DWorld w, BlockPos pos) {
      Direction facing = Direction.NORTH;
      for (Direction d : HORIZONTALS) {
         if (Mat112.opaqueCube(w.get(pos.relative(d))) && !Mat112.opaqueCube(w.get(pos.relative(d.getOpposite())))) {
            facing = d.getOpposite();
         }
      }
      return facing;
   }

   static void chest(DWorld w, BlockPos pos, Direction facing, net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> loot, long seed) {
      w.set(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing));
      if (w.get(pos).is(Blocks.CHEST) && w.level.getBlockEntity(pos) instanceof RandomizableContainerBlockEntity be) {
         be.setLootTable(loot, seed);
      }
   }

   // ------------------------------------------------------------------ IWorldGenerators (run after populate)

   /** Weight 0: ClayGenerator and SakuraTreeGenerator. */
   public static void weight0(DWorld w, int cx, int cz) {
      Random rand = fmlRandom(w.seed(), cx, cz);
      int x = cx * 16 + rand.nextInt(16);
      int y = 40 + rand.nextInt(60 - 40);
      int z = cz * 16 + rand.nextInt(16);
      Gen.minable(w, rand, new BlockPos(x, y, z), Blocks.CLAY.defaultBlockState(), 20, Gen.NATURAL_STONE);
      sakura(w, fmlRandom(w.seed(), cx, cz), cx, cz);
   }

   private static void sakura(DWorld w, Random random, int cx, int cz) {
      for (double chance = 0.01; random.nextDouble() < chance; chance--) {
         int x = cx * 16 + random.nextInt(16) + 8;
         int z = cz * 16 + random.nextInt(16) + 8;
         BlockPos xz = new BlockPos(x, 0, z);
         if (hasType(w, xz, "MOUNTAIN") && !hasType(w, xz, "FOREST")) {
            BlockPos pos = w.topSolidOrLiquid(xz).below();
            BlockState state = w.get(pos);
            boolean snow = state.is(Blocks.SNOW);
            if (snow) {
               pos = pos.below();
               state = w.get(pos);
            }
            if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL) || state.is(Blocks.FARMLAND)) {
               BlockPos placePos = pos.above();
               if (snow) {
                  w.set(placePos, Blocks.AIR.defaultBlockState());
               }
               // WorldGenBigTree with spruce logs and blossom leaves: 1.21's fancy oak shape with the same blocks
               TreeConfiguration config = new TreeConfiguration.TreeConfigurationBuilder(
                  BlockStateProvider.simple(Blocks.SPRUCE_LOG), new FancyTrunkPlacer(3, 11, 0),
                  BlockStateProvider.simple(RLGen.SAKURA_LEAVES.get().defaultBlockState().setValue(net.minecraft.world.level.block.LeavesBlock.PERSISTENT, true)),
                  new FancyFoliagePlacer(ConstantInt.of(2), ConstantInt.of(4), 4), new TwoLayersFeatureSize(0, 0, 0, java.util.OptionalInt.of(4))).ignoreVines().build();
               Feature.TREE.place(config, w.level, w.level.getLevel().getChunkSource().getGenerator(), RandomSource.create(random.nextLong()), placePos);
            }
         }
      }
   }

   /** Weight 200: MonsterBoxGenerator (chance 0.6, Y 3 to 20). */
   public static void monsterBoxes(DWorld w, int cx, int cz) {
      Random random = fmlRandom(w.seed(), cx, cz);
      Random worldRand = worldRand(w.seed(), cx, cz, 0x4D6F6E73L);
      for (float chance = 0.6F; worldRand.nextFloat() <= chance; chance -= 0.6F) {
         int x = cx * 16 + 8 + random.nextInt(16);
         int y = 3 + random.nextInt(20 - 3 + 1);
         int z = cz * 16 + 8 + random.nextInt(16);
         BlockPos pos = new BlockPos(x, y, z);
         if (w.isAir(pos)) {
            BlockPos testPos = pos;
            do {
               testPos = testPos.below();
            } while (Mat112.of(w.get(testPos)) != Mat112.ROCK && testPos.getY() >= 3);
            BlockPos placePos = testPos.above();
            if (testPos.getY() >= 3 && w.isAir(placePos)) {
               w.set(placePos, RLGen.MONSTER_BOX.get().defaultBlockState());
            }
         }
      }
   }

   /** Weight 1000: SpeleothemGenerator (32 clusters of 4 per chunk below Y 58). */
   public static void speleothems(DWorld w, int cx, int cz) {
      Random random = fmlRandom(w.seed(), cx, cz);
      int x = cx * 16 + 8;
      int z = cz * 16 + 8;
      for (int i = 0; i < 32; i++) {
         BlockPos target = new BlockPos(x + random.nextInt(10), random.nextInt(52) + 6, z + random.nextInt(10));
         if (placeSpeleothemCluster(random, w, target, 6, 4)) {
            i++;
         }
      }
   }

   private static boolean placeSpeleothemCluster(Random random, DWorld w, BlockPos pos, int spread, int tries) {
      if (!findAndPlaceSpeleothem(random, w, pos)) {
         return false;
      }
      for (int i = 0; i < tries; i++) {
         BlockPos target = pos.offset(random.nextInt(spread * 2 + 1) - spread, random.nextInt(spread + 1) - spread, random.nextInt(spread * 2 + 1) - spread);
         findAndPlaceSpeleothem(random, w, target);
      }
      return true;
   }

   private static boolean findAndPlaceSpeleothem(Random random, DWorld w, BlockPos pos) {
      if (!w.isAir(pos)) {
         return false;
      }
      int off = 0;
      boolean up = random.nextBoolean();
      Direction diff = up ? Direction.UP : Direction.DOWN;
      if (!up && w.canSeeSky(pos)) {
         return false;
      }
      BlockState stateAt;
      do {
         pos = pos.relative(diff);
         stateAt = w.get(pos);
         off++;
      } while (pos.getY() > 4 && pos.getY() < 200 && !Mat112.opaqueCube(stateAt) && off < 10);
      placeSpeleothem(random, w, pos, speleothemType(stateAt), !up);
      return true;
   }

   private static void placeSpeleothem(Random random, DWorld w, BlockPos pos, SpeleothemBlock type, boolean up) {
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

   private static SpeleothemBlock speleothemType(BlockState s) {
      String name = s.is(Blocks.STONE) ? "stone" : s.is(Blocks.ANDESITE) ? "andesite" : s.is(Blocks.GRANITE) ? "granite" : s.is(Blocks.DIORITE) ? "diorite"
         : s.is(RLGen.MARBLE.get()) ? "marble" : s.is(RLGen.LIMESTONE.get()) ? "limestone" : s.is(RLGen.JASPER.get()) ? "jasper"
         : s.is(RLGen.SLATE.get()) ? "slate" : null;
      return name == null ? null : RLGen.SPELEOTHEMS.get(name).get();
   }

   /** Weight 2000: CaveRootGenerator (10 attempts, Y 32 to 63, flowers 20%). */
   public static void caveRoots(DWorld w, int cx, int cz) {
      Random random = fmlRandom(w.seed(), cx, cz);
      Random worldRand = worldRand(w.seed(), cx, cz, 0x526F6F74L);
      for (int i = 0; i < 10; i++) {
         int x = cx * 16 + 8 + random.nextInt(16);
         int z = cz * 16 + 8 + random.nextInt(16);
         int y = random.nextInt(64 - 32) + 32;
         BlockPos pos = new BlockPos(x, y, z);
         if (w.isAir(pos)) {
            for (Direction facing : HORIZONTALS) {
               if (canAttachTo(w, pos, facing)) {
                  BlockState state = RLGen.ROOTS.get().defaultBlockState().setValue(VineBlock.getPropertyForFace(facing.getOpposite()), true);
                  w.set(pos, state);
                  growMany(w, worldRand, pos, state, 0.4F);
               }
            }
         }
      }
   }

   private static boolean acceptableNeighbor(DWorld w, BlockPos pos, Direction side) {
      BlockState s = w.get(pos);
      return Mat112.opaqueCube(s) && Mat112.of(s) == Mat112.ROCK;
   }

   private static boolean canAttachTo(DWorld w, BlockPos pos, Direction side) {
      BlockState above = w.get(pos.above());
      return acceptableNeighbor(w, pos.relative(side.getOpposite()), side)
         && (above.isAir() || above.getBlock() instanceof RootsBlock || acceptableNeighbor(w, pos.above(), Direction.UP));
   }

   private static void growMany(DWorld w, Random rand, BlockPos pos, BlockState state, float stopChance) {
      BlockPos next = pos;
      do {
         next = growAndReturnLastPos(w, rand, next, state);
      } while (next != null && rand.nextFloat() >= stopChance);
   }

   private static BlockPos growAndReturnLastPos(DWorld w, Random rand, BlockPos pos, BlockState state) {
      BlockPos down = pos.below();
      for (Direction facing : HORIZONTALS) {
         var prop = VineBlock.getPropertyForFace(facing);
         if (state.getValue(prop)) {
            BlockPos ret = growInFacing(w, down, facing);
            if (ret != null) {
               w.set(ret, nextRootState(rand).setValue(prop, true));
               return ret;
            }
            break;
         }
      }
      return null;
   }

   private static BlockPos growInFacing(DWorld w, BlockPos pos, Direction facing) {
      if (!w.isAir(pos)) {
         return null;
      }
      BlockPos check = pos.relative(facing);
      if (acceptableNeighbor(w, check, facing.getOpposite())) {
         return pos;
      }
      if (!w.isAir(check)) {
         return null;
      }
      return acceptableNeighbor(w, check.relative(facing), facing.getOpposite()) ? check : null;
   }

   private static BlockState nextRootState(Random rand) {
      if (!(rand.nextFloat() > 0.2F)) {
         return switch (rand.nextInt(3)) {
            case 0 -> RLGen.ROOTS_BLUE_FLOWER.get().defaultBlockState();
            case 1 -> RLGen.ROOTS_BLACK_FLOWER.get().defaultBlockState();
            default -> RLGen.ROOTS_WHITE_FLOWER.get().defaultBlockState();
         };
      }
      return RLGen.ROOTS.get().defaultBlockState();
   }

   /** Weight 3000: ArchaeologistHouseGenerator (1% per chunk, Y 20 to 29). The archaeologist is not spawned. */
   public static void archaeologist(DWorld w, int cx, int cz) {
      Random random = fmlRandom(w.seed(), cx, cz);
      if (random.nextDouble() < 0.01) {
         int x = cx * 16 + 8 + random.nextInt(16);
         int z = cz * 16 + 8 + random.nextInt(16);
         int y = random.nextInt(30 - 20) + 20;
         setHouseAt(w, random, new BlockPos(x, y, z), cx, cz);
      }
   }

   private static void setHouseAt(DWorld w, Random random, BlockPos pos, int cx, int cz) {
      if (!w.isAir(pos)) {
         return;
      }
      BlockPos down = pos.below();
      while (w.isAir(down)) {
         down = down.below();
         if (down.getY() < 0) {
            return;
         }
      }
      Direction facing = HORIZONTALS[random.nextInt(HORIZONTALS.length)];
      BlockPos placePos = down.above();
      BoundingBox region = Templates.region(w, cx, cz);
      while (w.isAir(placePos)) {
         placePos = placePos.relative(facing);
         if (placePos.getY() < 0 || placePos.getY() > 255 || !region.isInside(placePos)) {
            return;
         }
      }
      BlockState placeState = w.get(placePos);
      if (Mat112.of(placeState) == Mat112.ROCK && Mat112.opaqueCube(placeState)) {
         StructureTemplate t = Templates.get(w.level.getLevel().getStructureManager(), "rlgen:quark/archaeologist_house").orElse(null);
         if (t == null) {
            return;
         }
         StructurePlaceSettings settings = Templates.settings(rotationFromFacing(facing), Mirror.NONE, region);
         BlockPos at = placePos.relative(facing, 7);
         Templates.place(w, t, at, settings, at.asLong());
         for (Map.Entry<BlockPos, String> e : Templates.dataBlocks(t, at, settings).entrySet()) {
            if (e.getValue().equals("villager")) {
               w.set(e.getKey(), Blocks.AIR.defaultBlockState());
            }
         }
      }
   }

   /** RotationHandler.getRotationFromFacing */
   static Rotation rotationFromFacing(Direction facing) {
      return switch (facing) {
         case SOUTH -> Rotation.CLOCKWISE_180;
         case WEST -> Rotation.COUNTERCLOCKWISE_90;
         case EAST -> Rotation.CLOCKWISE_90;
         default -> Rotation.NONE;
      };
   }

   static void spawner(DWorld w, BlockPos pos, EntityType<?> type) {
      w.set(pos, Blocks.SPAWNER.defaultBlockState());
      BlockEntity be = w.level.getBlockEntity(pos);
      if (w.get(pos).is(Blocks.SPAWNER) && be instanceof SpawnerBlockEntity spawner) {
         spawner.setEntityId(type, RandomSource.create(pos.asLong()));
      }
   }
}
