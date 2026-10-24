package com.nukevscity.rlgen.deco;

import com.nukevscity.rlgen.legacy.LBlock;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.core.registries.Registries;

/** 1.12 WorldGenerator classes (MCP names in comments), ported to 1.21 blocks on a {@link DWorld}. */
public final class Gen {
   private Gen() {
   }

   static BlockPos add(BlockPos p, int x, int y, int z) {
      return p.offset(x, y, z);
   }

   static BlockPos scatter(BlockPos p, Random r) {
      return p.offset(r.nextInt(8) - r.nextInt(8), r.nextInt(4) - r.nextInt(4), r.nextInt(8) - r.nextInt(8));
   }

   /** WorldGenMinable.StonePredicate: natural stone. */
   public static final Predicate<BlockState> NATURAL_STONE = s -> s.is(Blocks.STONE) || s.is(Blocks.GRANITE) || s.is(Blocks.DIORITE) || s.is(Blocks.ANDESITE);

   // ------------------------------------------------------------------ WorldGenMinable

   public static void minable(DWorld w, Random random, BlockPos pos, BlockState ore, int size, Predicate<BlockState> predicate) {
      float f = random.nextFloat() * (float)Math.PI;
      double d0 = (float)(pos.getX() + 8) + Mth.sin(f) * (float)size / 8.0F;
      double d1 = (float)(pos.getX() + 8) - Mth.sin(f) * (float)size / 8.0F;
      double d2 = (float)(pos.getZ() + 8) + Mth.cos(f) * (float)size / 8.0F;
      double d3 = (float)(pos.getZ() + 8) - Mth.cos(f) * (float)size / 8.0F;
      double d4 = pos.getY() + random.nextInt(3) - 2;
      double d5 = pos.getY() + random.nextInt(3) - 2;
      for (int i = 0; i < size; i++) {
         float f1 = (float)i / (float)size;
         double d6 = d0 + (d1 - d0) * f1;
         double d7 = d4 + (d5 - d4) * f1;
         double d8 = d2 + (d3 - d2) * f1;
         double d9 = random.nextDouble() * size / 16.0;
         double d10 = (Mth.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0;
         double d11 = (Mth.sin((float)Math.PI * f1) + 1.0F) * d9 + 1.0;
         int j = Mth.floor(d6 - d10 / 2.0);
         int k = Mth.floor(d7 - d11 / 2.0);
         int l = Mth.floor(d8 - d10 / 2.0);
         int i1 = Mth.floor(d6 + d10 / 2.0);
         int j1 = Mth.floor(d7 + d11 / 2.0);
         int k1 = Mth.floor(d8 + d10 / 2.0);
         for (int l1 = j; l1 <= i1; l1++) {
            double d12 = (l1 + 0.5 - d6) / (d10 / 2.0);
            if (d12 * d12 < 1.0) {
               for (int i2 = k; i2 <= j1; i2++) {
                  double d13 = (i2 + 0.5 - d7) / (d11 / 2.0);
                  if (d12 * d12 + d13 * d13 < 1.0) {
                     for (int j2 = l; j2 <= k1; j2++) {
                        double d14 = (j2 + 0.5 - d8) / (d10 / 2.0);
                        if (d12 * d12 + d13 * d13 + d14 * d14 < 1.0) {
                           BlockPos b = new BlockPos(l1, i2, j2);
                           if (predicate.test(w.get(b))) {
                              w.set(b, ore);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   // ------------------------------------------------------------------ WorldGenLakes

   public static boolean lake(DWorld w, Random random, BlockPos pos, boolean lava) {
      return lake(w, random, pos, lava ? Blocks.LAVA.defaultBlockState() : Blocks.WATER.defaultBlockState(), lava);
   }

   /** WorldGenLakes with any fluid; lava = Material.LAVA (stone border), else water material (ice on top). */
   public static boolean lake(DWorld w, Random random, BlockPos pos, BlockState fluid, boolean lava) {
      pos = pos.offset(-8, 0, -8);
      while (pos.getY() > 5 && w.isAir(pos)) {
         pos = pos.below();
      }
      if (pos.getY() <= 4) {
         return false;
      }
      pos = pos.below(4);
      boolean[] a = new boolean[2048];
      int i = random.nextInt(4) + 4;
      for (int j = 0; j < i; j++) {
         double d0 = random.nextDouble() * 6.0 + 3.0;
         double d1 = random.nextDouble() * 4.0 + 2.0;
         double d2 = random.nextDouble() * 6.0 + 3.0;
         double d3 = random.nextDouble() * (16.0 - d0 - 2.0) + 1.0 + d0 / 2.0;
         double d4 = random.nextDouble() * (8.0 - d1 - 4.0) + 2.0 + d1 / 2.0;
         double d5 = random.nextDouble() * (16.0 - d2 - 2.0) + 1.0 + d2 / 2.0;
         for (int l = 1; l < 15; l++) {
            for (int i1 = 1; i1 < 15; i1++) {
               for (int j1 = 1; j1 < 7; j1++) {
                  double d6 = (l - d3) / (d0 / 2.0);
                  double d7 = (j1 - d4) / (d1 / 2.0);
                  double d8 = (i1 - d5) / (d2 / 2.0);
                  if (d6 * d6 + d7 * d7 + d8 * d8 < 1.0) {
                     a[(l * 16 + i1) * 8 + j1] = true;
                  }
               }
            }
         }
      }
      for (int k1 = 0; k1 < 16; k1++) {
         for (int l2 = 0; l2 < 16; l2++) {
            for (int k = 0; k < 8; k++) {
               if (edge(a, k1, l2, k)) {
                  BlockState s = w.get(pos.offset(k1, k, l2));
                  if (k >= 4 && DWorld.isLiquid(s)) {
                     return false;
                  }
                  if (k < 4 && !DWorld.isSolid(s) && s.getBlock() != fluid.getBlock()) {
                     return false;
                  }
               }
            }
         }
      }
      for (int l1 = 0; l1 < 16; l1++) {
         for (int i3 = 0; i3 < 16; i3++) {
            for (int i4 = 0; i4 < 8; i4++) {
               if (a[(l1 * 16 + i3) * 8 + i4]) {
                  w.set(pos.offset(l1, i4, i3), i4 >= 4 ? Blocks.AIR.defaultBlockState() : fluid);
               }
            }
         }
      }
      for (int i2 = 0; i2 < 16; i2++) {
         for (int j3 = 0; j3 < 16; j3++) {
            for (int j4 = 4; j4 < 8; j4++) {
               if (a[(i2 * 16 + j3) * 8 + j4]) {
                  BlockPos b = pos.offset(i2, j4 - 1, j3);
                  if (w.get(b).is(Blocks.DIRT) && w.canSeeSky(pos.offset(i2, j4, j3))) {
                     w.set(b, w.biome(b).topBlock == LBlock.MYCELIUM ? Blocks.MYCELIUM.defaultBlockState() : Blocks.GRASS_BLOCK.defaultBlockState());
                  }
               }
            }
         }
      }
      if (lava) {
         for (int j2 = 0; j2 < 16; j2++) {
            for (int k3 = 0; k3 < 16; k3++) {
               for (int k4 = 0; k4 < 8; k4++) {
                  if (edge(a, j2, k3, k4) && (k4 < 4 || random.nextInt(2) != 0) && DWorld.isSolid(w.get(pos.offset(j2, k4, k3)))) {
                     w.set(pos.offset(j2, k4, k3), Blocks.STONE.defaultBlockState());
                  }
               }
            }
         }
      } else {
         for (int k2 = 0; k2 < 16; k2++) {
            for (int l3 = 0; l3 < 16; l3++) {
               if (w.canBlockFreezeWater(pos.offset(k2, 4, l3))) {
                  w.set(pos.offset(k2, 4, l3), Blocks.ICE.defaultBlockState());
               }
            }
         }
      }
      return true;
   }

   private static boolean edge(boolean[] a, int x, int z, int y) {
      return !a[(x * 16 + z) * 8 + y]
         && (x < 15 && a[((x + 1) * 16 + z) * 8 + y] || x > 0 && a[((x - 1) * 16 + z) * 8 + y] || z < 15 && a[(x * 16 + z + 1) * 8 + y]
            || z > 0 && a[(x * 16 + (z - 1)) * 8 + y] || y < 7 && a[(x * 16 + z) * 8 + y + 1] || y > 0 && a[(x * 16 + z) * 8 + (y - 1)]);
   }

   // ------------------------------------------------------------------ WorldGenDungeons (and Defiled Lands' copy)

   private static final EntityType<?>[] DUNGEON_MOBS = {EntityType.SKELETON, EntityType.ZOMBIE, EntityType.ZOMBIE, EntityType.SPIDER};

   public static boolean dungeon(DWorld w, Random random, BlockPos pos, BlockState wall, BlockState floorMossy, BlockState floorAlt,
                                 ResourceKey<LootTable> loot, EntityType<?>[] mobs, boolean defiled) {
      int j = random.nextInt(2) + 2;
      int k = -j - 1;
      int l = j + 1;
      int k1 = random.nextInt(2) + 2;
      int l1 = -k1 - 1;
      int i2 = k1 + 1;
      int j2 = 0;
      for (int k2 = k; k2 <= l; k2++) {
         for (int l2 = -1; l2 <= 4; l2++) {
            for (int i3 = l1; i3 <= i2; i3++) {
               BlockPos b = pos.offset(k2, l2, i3);
               boolean solid = DWorld.isSolid(w.get(b));
               if (l2 == -1 && !solid) {
                  return false;
               }
               if (l2 == 4 && !solid) {
                  return false;
               }
               if ((k2 == k || k2 == l || i3 == l1 || i3 == i2) && l2 == 0 && w.isAir(b) && w.isAir(b.above())) {
                  j2++;
               }
            }
         }
      }
      if (j2 < 1 || j2 > 5) {
         return false;
      }
      for (int k3 = k; k3 <= l; k3++) {
         for (int i4 = 3; i4 >= -1; i4--) {
            for (int k4 = l1; k4 <= i2; k4++) {
               BlockPos b = pos.offset(k3, i4, k4);
               if (k3 != k && i4 != -1 && k4 != l1 && k3 != l && i4 != 4 && k4 != i2) {
                  if (!w.get(b).is(Blocks.CHEST)) {
                     w.set(b, Blocks.AIR.defaultBlockState());
                  }
               } else if (b.getY() >= 0 && !DWorld.isSolid(w.get(b.below()))) {
                  w.set(b, Blocks.AIR.defaultBlockState());
               } else if (DWorld.isSolid(w.get(b)) && !w.get(b).is(Blocks.CHEST)) {
                  if (defiled) {
                     w.set(b, random.nextInt(4) == 0 ? floorAlt : wall);
                  } else if (i4 == -1 && random.nextInt(4) != 0) {
                     w.set(b, floorMossy);
                  } else {
                     w.set(b, wall);
                  }
               }
            }
         }
      }
      for (int l3 = 0; l3 < 2; l3++) {
         for (int j4 = 0; j4 < 3; j4++) {
            BlockPos b = new BlockPos(pos.getX() + random.nextInt(j * 2 + 1) - j, pos.getY(), pos.getZ() + random.nextInt(k1 * 2 + 1) - k1);
            if (w.isAir(b)) {
               int solid = 0;
               Direction wallSide = Direction.NORTH;
               for (Direction d : Direction.Plane.HORIZONTAL) {
                  if (DWorld.isSolid(w.get(b.relative(d)))) {
                     solid++;
                     wallSide = d;
                  }
               }
               if (solid == 1) {
                  w.set(b, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, wallSide.getOpposite()));
                  RandomizableContainer.setBlockEntityLootTable(w.level, RandomSource.create(random.nextLong()), b, loot);
                  break;
               }
            }
         }
      }
      w.set(pos, Blocks.SPAWNER.defaultBlockState());
      BlockEntity be = w.level.getBlockEntity(pos);
      if (be instanceof SpawnerBlockEntity spawner) {
         spawner.setEntityId(mobs[random.nextInt(mobs.length)], RandomSource.create(random.nextLong()));
      }
      return true;
   }

   public static boolean dungeon(DWorld w, Random random, BlockPos pos) {
      return dungeon(w, random, pos, Blocks.COBBLESTONE.defaultBlockState(), Blocks.MOSSY_COBBLESTONE.defaultBlockState(), null,
         BuiltInLootTables.SIMPLE_DUNGEON, DUNGEON_MOBS, false);
   }

   // ------------------------------------------------------------------ patches

   /** WorldGenSand (sand / gravel discs under water). */
   public static boolean sandDisc(DWorld w, Random random, BlockPos pos, BlockState block, int radius) {
      if (!DWorld.isWater(w.get(pos))) {
         return false;
      }
      int i = random.nextInt(radius - 2) + 2;
      for (int k = pos.getX() - i; k <= pos.getX() + i; k++) {
         for (int l = pos.getZ() - i; l <= pos.getZ() + i; l++) {
            int dx = k - pos.getX();
            int dz = l - pos.getZ();
            if (dx * dx + dz * dz <= i * i) {
               for (int y = pos.getY() - 2; y <= pos.getY() + 2; y++) {
                  BlockPos b = new BlockPos(k, y, l);
                  BlockState s = w.get(b);
                  if (s.is(Blocks.DIRT) || s.is(Blocks.GRASS_BLOCK)) {
                     w.set(b, block);
                  }
               }
            }
         }
      }
      return true;
   }

   /** WorldGenClay */
   public static boolean clay(DWorld w, Random random, BlockPos pos, int size) {
      if (!DWorld.isWater(w.get(pos))) {
         return false;
      }
      int i = random.nextInt(size - 2) + 2;
      for (int k = pos.getX() - i; k <= pos.getX() + i; k++) {
         for (int l = pos.getZ() - i; l <= pos.getZ() + i; l++) {
            int dx = k - pos.getX();
            int dz = l - pos.getZ();
            if (dx * dx + dz * dz <= i * i) {
               for (int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                  BlockPos b = new BlockPos(k, y, l);
                  BlockState s = w.get(b);
                  if (s.is(Blocks.DIRT) || s.is(Blocks.CLAY)) {
                     w.set(b, Blocks.CLAY.defaultBlockState());
                  }
               }
            }
         }
      }
      return true;
   }

   /** Traverse WorldGenPatch / WorldGenSurfacePatch (surface = only under air or plants). */
   public static void patch(DWorld w, Random random, BlockPos pos, BlockState state, int size, List<Block> replaceable, boolean surface) {
      int i = random.nextInt(size - 2) + 2;
      for (int k = pos.getX() - i; k <= pos.getX() + i; k++) {
         for (int l = pos.getZ() - i; l <= pos.getZ() + i; l++) {
            int dx = k - pos.getX();
            int dz = l - pos.getZ();
            if (dx * dx + dz * dz <= i * i) {
               for (int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                  BlockPos b = new BlockPos(k, y, l);
                  BlockState up = w.get(b.above());
                  if (surface && !(up.isAir() || isPlant(up))) {
                     continue;
                  }
                  if (replaceable.contains(w.get(b).getBlock())) {
                     w.set(b, state);
                     if (isPlant(w.get(b.above()))) {
                        w.set(b.above(), Blocks.AIR.defaultBlockState());
                     }
                  }
               }
            }
         }
      }
   }

   /** Material.PLANTS */
   static boolean isPlant(BlockState s) {
      return s.is(net.minecraft.tags.BlockTags.FLOWERS) || s.is(net.minecraft.tags.BlockTags.SAPLINGS) || s.is(Blocks.SHORT_GRASS)
         || s.is(Blocks.FERN) || s.is(Blocks.DEAD_BUSH) || s.is(Blocks.BROWN_MUSHROOM) || s.is(Blocks.RED_MUSHROOM)
         || s.getBlock() instanceof net.minecraft.world.level.block.BushBlock;
   }

   // ------------------------------------------------------------------ plants

   /** WorldGenFlowers / WorldGenBush (64 tries). */
   public static void bushes(DWorld w, Random random, BlockPos pos, BlockState state) {
      for (int i = 0; i < 64; i++) {
         BlockPos b = scatter(pos, random);
         if (w.isAir(b) && canStay(w, b, state)) {
            w.set(b, state);
         }
      }
   }

   static boolean canStay(DWorld w, BlockPos b, BlockState state) {
      if (b.getY() < 1 || b.getY() > 255 || !w.inRegion(b)) {
         return false;
      }
      if (state.is(Blocks.BROWN_MUSHROOM) || state.is(Blocks.RED_MUSHROOM)) {
         BlockState down = w.get(b.below());
         return down.is(Blocks.MYCELIUM) || down.is(Blocks.PODZOL)
            || w.isDark(b) && down.isSolidRender(w.level, b.below());
      }
      return state.canSurvive(w.level, b);
   }

   /** WorldGenTallGrass (and Traverse WorldGenPlant): 128 tries from the ground under leaves. */
   public static void tallGrass(DWorld w, Random random, BlockPos pos, BlockState state) {
      for (BlockState s = w.get(pos); (s.isAir() || DWorld.isLeaves(s)) && pos.getY() > 0; s = w.get(pos)) {
         pos = pos.below();
      }
      for (int i = 0; i < 128; i++) {
         BlockPos b = scatter(pos, random);
         if (w.isAir(b) && canStay(w, b, state)) {
            w.set(b, state);
         }
      }
   }

   /** WorldGenDeadBush */
   public static void deadBush(DWorld w, Random random, BlockPos pos) {
      for (BlockState s = w.get(pos); (s.isAir() || DWorld.isLeaves(s)) && pos.getY() > 0; s = w.get(pos)) {
         pos = pos.below();
      }
      BlockState bush = Blocks.DEAD_BUSH.defaultBlockState();
      for (int i = 0; i < 4; i++) {
         BlockPos b = scatter(pos, random);
         if (w.isAir(b) && canStay(w, b, bush)) {
            w.set(b, bush);
         }
      }
   }

   /** WorldGenDoublePlant */
   public static boolean doublePlant(DWorld w, Random random, BlockPos pos, Block plant) {
      boolean flag = false;
      BlockState state = plant.defaultBlockState();
      for (int i = 0; i < 64; i++) {
         BlockPos b = scatter(pos, random);
         if (w.isAir(b) && w.isAir(b.above()) && b.getY() < 255 && canStay(w, b, state)) {
            if (w.inRegion(b)) {
               DoublePlantBlock.placeAt(w.level, state, b, 2);
            }
            flag = true;
         }
      }
      return flag;
   }

   /** WorldGenReed */
   public static void reeds(DWorld w, Random random, BlockPos pos) {
      for (int i = 0; i < 20; i++) {
         BlockPos b = pos.offset(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
         if (w.isAir(b)) {
            BlockPos d = b.below();
            if (DWorld.isWater(w.get(d.west())) || DWorld.isWater(w.get(d.east())) || DWorld.isWater(w.get(d.north())) || DWorld.isWater(w.get(d.south()))) {
               int j = 2 + random.nextInt(random.nextInt(3) + 1);
               for (int k = 0; k < j; k++) {
                  if (canStay(w, b, Blocks.SUGAR_CANE.defaultBlockState())) {
                     w.set(b.above(k), Blocks.SUGAR_CANE.defaultBlockState());
                  }
               }
            }
         }
      }
   }

   /** WorldGenCactus */
   public static void cactus(DWorld w, Random random, BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         BlockPos b = scatter(pos, random);
         if (w.isAir(b)) {
            int j = 1 + random.nextInt(random.nextInt(3) + 1);
            for (int k = 0; k < j; k++) {
               if (canStay(w, b, Blocks.CACTUS.defaultBlockState())) {
                  w.set(b.above(k), Blocks.CACTUS.defaultBlockState());
               }
            }
         }
      }
   }

   /** WorldGenWaterlily */
   public static void waterlily(DWorld w, Random random, BlockPos pos) {
      for (int i = 0; i < 10; i++) {
         BlockPos b = new BlockPos(pos.getX() + random.nextInt(8) - random.nextInt(8), pos.getY() + random.nextInt(4) - random.nextInt(4),
            pos.getZ() + random.nextInt(8) - random.nextInt(8));
         if (w.isAir(b) && canStay(w, b, Blocks.LILY_PAD.defaultBlockState())) {
            w.set(b, Blocks.LILY_PAD.defaultBlockState());
         }
      }
   }

   /** WorldGenPumpkin */
   public static void pumpkin(DWorld w, Random random, BlockPos pos) {
      for (int i = 0; i < 64; i++) {
         BlockPos b = scatter(pos, random);
         if (w.isAir(b) && w.get(b.below()).is(Blocks.GRASS_BLOCK)) {
            w.set(b, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(
               RandomSource.create(random.nextLong()))));
         }
      }
   }

   /** WorldGenMelon */
   public static void melon(DWorld w, Random random, BlockPos pos) {
      for (int i = 0; i < 64; i++) {
         BlockPos b = scatter(pos, random);
         if (w.get(b).canBeReplaced() && w.get(b.below()).is(Blocks.GRASS_BLOCK)) {
            w.set(b, Blocks.MELON.defaultBlockState());
         }
      }
   }

   /** WorldGenVines */
   public static void vines(DWorld w, Random random, BlockPos pos) {
      while (pos.getY() < 128) {
         if (w.isAir(pos)) {
            for (Direction facing : new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST}) {
               Direction toward = facing.getOpposite();
               BlockPos n = pos.relative(toward);
               if (w.inRegion(n) && VineBlock.isAcceptableNeighbour(w.level, n, facing)) {
                  w.set(pos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(toward), true));
                  break;
               }
            }
         } else {
            pos = pos.offset(random.nextInt(4) - random.nextInt(4), 0, random.nextInt(4) - random.nextInt(4));
         }
         pos = pos.above();
      }
   }

   /** WorldGenLiquids (springs) */
   public static void liquids(DWorld w, Random random, BlockPos pos, boolean water) {
      if (!w.get(pos.above()).is(Blocks.STONE) || !w.get(pos.below()).is(Blocks.STONE)) {
         return;
      }
      BlockState s = w.get(pos);
      if (!s.isAir() && !s.is(Blocks.STONE)) {
         return;
      }
      int stone = 0;
      int air = 0;
      for (Direction d : new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH}) {
         if (w.get(pos.relative(d)).is(Blocks.STONE)) {
            stone++;
         }
         if (w.isAir(pos.relative(d))) {
            air++;
         }
      }
      if (stone == 3 && air == 1) {
         w.set(pos, water ? Blocks.WATER.defaultBlockState() : Blocks.LAVA.defaultBlockState());
         w.scheduleFluidTick(pos, water);
      }
   }

   // ------------------------------------------------------------------ rocks, ice, mushrooms

   /** WorldGenBlockBlob (boulders) */
   public static boolean blockBlob(DWorld w, Random random, BlockPos pos, BlockState block, int startRadius, List<Block> ground) {
      while (pos.getY() > 3) {
         if (!w.isAir(pos.below())) {
            Block b = w.get(pos.below()).getBlock();
            if (ground.contains(b)) {
               break;
            }
         }
         pos = pos.below();
      }
      if (pos.getY() <= 3) {
         return false;
      }
      int i1 = startRadius;
      for (int i = 0; i1 >= 0 && i < 3; i++) {
         int j = i1 + random.nextInt(2);
         int k = i1 + random.nextInt(2);
         int l = i1 + random.nextInt(2);
         float f = (j + k + l) * 0.333F + 0.5F;
         for (BlockPos b : BlockPos.betweenClosed(pos.offset(-j, -k, -l), pos.offset(j, k, l))) {
            if (b.distSqr(pos) <= (double)(f * f)) {
               w.set(b.immutable(), block);
            }
         }
         pos = pos.offset(-(i1 + 1) + random.nextInt(2 + i1 * 2), -random.nextInt(2), -(i1 + 1) + random.nextInt(2 + i1 * 2));
      }
      return true;
   }

   public static final List<Block> BLOB_GROUND = List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.STONE);

   private static boolean iceSoft(BlockState s) {
      return s.isAir() || s.is(Blocks.DIRT) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.ICE);
   }

   /** WorldGenIceSpike */
   public static boolean iceSpike(DWorld w, Random random, BlockPos pos) {
      while (w.isAir(pos) && pos.getY() > 2) {
         pos = pos.below();
      }
      if (!w.get(pos).is(Blocks.SNOW_BLOCK)) {
         return false;
      }
      BlockState ice = Blocks.PACKED_ICE.defaultBlockState();
      pos = pos.above(random.nextInt(4));
      int i = random.nextInt(4) + 7;
      int j = i / 4 + random.nextInt(2);
      if (j > 1 && random.nextInt(60) == 0) {
         pos = pos.above(10 + random.nextInt(30));
      }
      for (int k = 0; k < i; k++) {
         float f = (1.0F - (float)k / (float)i) * (float)j;
         int l = Mth.ceil(f);
         for (int i1 = -l; i1 <= l; i1++) {
            float f1 = (float)Mth.abs(i1) - 0.25F;
            for (int j1 = -l; j1 <= l; j1++) {
               float f2 = (float)Mth.abs(j1) - 0.25F;
               if ((i1 == 0 && j1 == 0 || !(f1 * f1 + f2 * f2 > f * f)) && (i1 != -l && i1 != l && j1 != -l && j1 != l || !(random.nextFloat() > 0.75F))) {
                  if (iceSoft(w.get(pos.offset(i1, k, j1)))) {
                     w.set(pos.offset(i1, k, j1), ice);
                  }
                  if (k != 0 && l > 1 && iceSoft(w.get(pos.offset(i1, -k, j1)))) {
                     w.set(pos.offset(i1, -k, j1), ice);
                  }
               }
            }
         }
      }
      int k1 = Math.max(0, Math.min(1, j - 1));
      for (int l1 = -k1; l1 <= k1; l1++) {
         for (int i2 = -k1; i2 <= k1; i2++) {
            BlockPos b = pos.offset(l1, -1, i2);
            int j2 = 50;
            if (Math.abs(l1) == 1 && Math.abs(i2) == 1) {
               j2 = random.nextInt(5);
            }
            while (b.getY() > 50) {
               BlockState s = w.get(b);
               if (!iceSoft(s) && !s.is(Blocks.PACKED_ICE)) {
                  break;
               }
               w.set(b, ice);
               b = b.below();
               if (--j2 <= 0) {
                  b = b.below(random.nextInt(5) + 1);
                  j2 = random.nextInt(5);
               }
            }
         }
      }
      return true;
   }

   /** WorldGenIcePath */
   public static boolean icePath(DWorld w, Random random, BlockPos pos, int width) {
      while (w.isAir(pos) && pos.getY() > 2) {
         pos = pos.below();
      }
      if (!w.get(pos).is(Blocks.SNOW_BLOCK)) {
         return false;
      }
      int i = random.nextInt(width - 2) + 2;
      for (int k = pos.getX() - i; k <= pos.getX() + i; k++) {
         for (int l = pos.getZ() - i; l <= pos.getZ() + i; l++) {
            int dx = k - pos.getX();
            int dz = l - pos.getZ();
            if (dx * dx + dz * dz <= i * i) {
               for (int y = pos.getY() - 1; y <= pos.getY() + 1; y++) {
                  BlockPos b = new BlockPos(k, y, l);
                  BlockState s = w.get(b);
                  if (s.is(Blocks.DIRT) || s.is(Blocks.SNOW_BLOCK) || s.is(Blocks.ICE)) {
                     w.set(b, Blocks.PACKED_ICE.defaultBlockState());
                  }
               }
            }
         }
      }
      return true;
   }

   /** 1.12 BlockHugeMushroom.EnumType by metadata, as 1.21 face booleans (north, east, south, west, up, down). */
   private static BlockState mushroomBlock(Block block, int meta) {
      boolean n = false, e = false, s = false, wst = false, up = false, down = false;
      switch (meta) {
         case 1 -> { n = true; wst = true; up = true; }
         case 2 -> { n = true; up = true; }
         case 3 -> { n = true; e = true; up = true; }
         case 4 -> { wst = true; up = true; }
         case 5 -> up = true;
         case 6 -> { e = true; up = true; }
         case 7 -> { s = true; wst = true; up = true; }
         case 8 -> { s = true; up = true; }
         case 9 -> { s = true; e = true; up = true; }
         case 10 -> {
            return Blocks.MUSHROOM_STEM.defaultBlockState().setValue(HugeMushroomBlock.UP, false).setValue(HugeMushroomBlock.DOWN, false);
         }
         default -> {
         }
      }
      return block.defaultBlockState().setValue(HugeMushroomBlock.NORTH, n).setValue(HugeMushroomBlock.EAST, e).setValue(HugeMushroomBlock.SOUTH, s)
         .setValue(HugeMushroomBlock.WEST, wst).setValue(HugeMushroomBlock.UP, up).setValue(HugeMushroomBlock.DOWN, down);
   }

   /** WorldGenBigMushroom (random type when {@code red} is null). */
   public static boolean bigMushroom(DWorld w, Random random, BlockPos pos, Boolean red) {
      boolean isRed = red != null ? red : !random.nextBoolean();
      Block block = isRed ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK;
      int i = random.nextInt(3) + 4;
      if (random.nextInt(12) == 0) {
         i *= 2;
      }
      if (pos.getY() < 1 || pos.getY() + i + 1 >= 256) {
         return false;
      }
      for (int j = pos.getY(); j <= pos.getY() + 1 + i; j++) {
         int k = j <= pos.getY() + 3 ? 0 : 3;
         for (int l = pos.getX() - k; l <= pos.getX() + k; l++) {
            for (int i1 = pos.getZ() - k; i1 <= pos.getZ() + k; i1++) {
               BlockState s = w.get(new BlockPos(l, j, i1));
               if (!s.isAir() && !DWorld.isLeaves(s)) {
                  return false;
               }
            }
         }
      }
      BlockState below = w.get(pos.below());
      if (!below.is(Blocks.DIRT) && !below.is(Blocks.GRASS_BLOCK) && !below.is(Blocks.MYCELIUM)) {
         return false;
      }
      int k2 = pos.getY() + i;
      if (isRed) {
         k2 = pos.getY() + i - 3;
      }
      for (int l2 = k2; l2 <= pos.getY() + i; l2++) {
         int j3 = l2 < pos.getY() + i ? 2 : 1;
         if (!isRed) {
            j3 = 3;
         }
         int k3 = pos.getX() - j3;
         int l3 = pos.getX() + j3;
         int j1 = pos.getZ() - j3;
         int k1 = pos.getZ() + j3;
         for (int l1 = k3; l1 <= l3; l1++) {
            for (int i2 = j1; i2 <= k1; i2++) {
               int j2 = 5;
               if (l1 == k3) {
                  j2--;
               } else if (l1 == l3) {
                  j2++;
               }
               if (i2 == j1) {
                  j2 -= 3;
               } else if (i2 == k1) {
                  j2 += 3;
               }
               int type = j2;
               if (!isRed || l2 < pos.getY() + i) {
                  if ((l1 == k3 || l1 == l3) && (i2 == j1 || i2 == k1)) {
                     continue;
                  }
                  if (l1 == pos.getX() - (j3 - 1) && i2 == j1 || l1 == k3 && i2 == pos.getZ() - (j3 - 1)) {
                     type = 1;
                  }
                  if (l1 == pos.getX() + (j3 - 1) && i2 == j1 || l1 == l3 && i2 == pos.getZ() - (j3 - 1)) {
                     type = 3;
                  }
                  if (l1 == pos.getX() - (j3 - 1) && i2 == k1 || l1 == k3 && i2 == pos.getZ() + (j3 - 1)) {
                     type = 7;
                  }
                  if (l1 == pos.getX() + (j3 - 1) && i2 == k1 || l1 == l3 && i2 == pos.getZ() + (j3 - 1)) {
                     type = 9;
                  }
               }
               if (type == 5 && l2 < pos.getY() + i) {
                  type = 0;
               }
               if (pos.getY() >= pos.getY() + i - 1 || type != 0) {
                  BlockPos b = new BlockPos(l1, l2, i2);
                  if (!w.get(b).isSolidRender(w.level, b)) {
                     w.set(b, mushroomBlock(block, type));
                  }
               }
            }
         }
      }
      for (int i3 = 0; i3 < i; i3++) {
         if (!w.get(pos.above(i3)).isSolidRender(w.level, pos.above(i3))) {
            w.set(pos.above(i3), mushroomBlock(block, 10));
         }
      }
      return true;
   }

   // ------------------------------------------------------------------ desert well, fossils

   /** WorldGenDesertWells */
   public static boolean desertWell(DWorld w, Random random, BlockPos pos) {
      while (w.isAir(pos) && pos.getY() > 2) {
         pos = pos.below();
      }
      if (!w.get(pos).is(Blocks.SAND)) {
         return false;
      }
      for (int i = -2; i <= 2; i++) {
         for (int j = -2; j <= 2; j++) {
            if (w.isAir(pos.offset(i, -1, j)) && w.isAir(pos.offset(i, -2, j))) {
               return false;
            }
         }
      }
      BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
      BlockState slab = Blocks.SANDSTONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM);
      for (int l = -1; l <= 0; l++) {
         for (int l1 = -2; l1 <= 2; l1++) {
            for (int k = -2; k <= 2; k++) {
               w.set(pos.offset(l1, l, k), sandstone);
            }
         }
      }
      w.set(pos, Blocks.WATER.defaultBlockState());
      w.scheduleFluidTick(pos, true);
      for (Direction d : Direction.Plane.HORIZONTAL) {
         w.set(pos.relative(d), Blocks.WATER.defaultBlockState());
         w.scheduleFluidTick(pos.relative(d), true);
      }
      for (int i1 = -2; i1 <= 2; i1++) {
         for (int i2 = -2; i2 <= 2; i2++) {
            if (i1 == -2 || i1 == 2 || i2 == -2 || i2 == 2) {
               w.set(pos.offset(i1, 1, i2), sandstone);
            }
         }
      }
      w.set(pos.offset(2, 1, 0), slab);
      w.set(pos.offset(-2, 1, 0), slab);
      w.set(pos.offset(0, 1, 2), slab);
      w.set(pos.offset(0, 1, -2), slab);
      for (int j1 = -1; j1 <= 1; j1++) {
         for (int j2 = -1; j2 <= 1; j2++) {
            w.set(pos.offset(j1, 4, j2), j1 == 0 && j2 == 0 ? sandstone : slab);
         }
      }
      for (int k1 = 1; k1 <= 3; k1++) {
         w.set(pos.offset(-1, k1, -1), sandstone);
         w.set(pos.offset(-1, k1, 1), sandstone);
         w.set(pos.offset(1, k1, -1), sandstone);
         w.set(pos.offset(1, k1, 1), sandstone);
      }
      return true;
   }

   private static final String[] FOSSILS = {"spine_1", "spine_2", "spine_3", "spine_4", "skull_1", "skull_2", "skull_3", "skull_4"};

   /** WorldGenFossils, with the 1.21 copies of the same templates. */
   public static void fossil(DWorld w, BlockPos pos) {
      Random random1 = w.chunkRandom(pos, 987234911L);
      Rotation rotation = Rotation.values()[random1.nextInt(Rotation.values().length)];
      int i = random1.nextInt(FOSSILS.length);
      var manager = w.level.getLevel().getStructureManager();
      StructureTemplate template = manager.get(ResourceLocation.withDefaultNamespace("fossil/" + FOSSILS[i])).orElse(null);
      StructureTemplate coal = manager.get(ResourceLocation.withDefaultNamespace("fossil/" + FOSSILS[i] + "_coal")).orElse(null);
      if (template == null || coal == null) {
         return;
      }
      int cx = pos.getX() >> 4 << 4;
      int cz = pos.getZ() >> 4 << 4;
      BoundingBox box = new BoundingBox(cx, 0, cz, cx + 15, 256, cz + 15);
      var size = template.getSize(rotation);
      int j = random1.nextInt(Math.max(1, 16 - size.getX()));
      int k = random1.nextInt(Math.max(1, 16 - size.getZ()));
      int l = 256;
      for (int i1 = 0; i1 < size.getX(); i1++) {
         for (int j1 = 0; j1 < size.getX(); j1++) {
            l = Math.min(l, w.height(pos.getX() + i1 + j, pos.getZ() + j1 + k));
         }
      }
      int k1 = Math.max(l - 15 - random1.nextInt(10), 10);
      BlockPos origin = template.getZeroPositionWithTransform(pos.offset(j, k1, k), net.minecraft.world.level.block.Mirror.NONE, rotation);
      RandomSource rs = RandomSource.create(random1.nextLong());
      StructurePlaceSettings s1 = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(box).setRandom(rs)
         .addProcessor(new BlockRotProcessor(0.9F));
      template.placeInWorld(w.level, origin, origin, s1, rs, 20);
      StructurePlaceSettings s2 = new StructurePlaceSettings().setRotation(rotation).setBoundingBox(box).setRandom(rs)
         .addProcessor(new BlockRotProcessor(0.1F));
      coal.placeInWorld(w.level, origin, origin, s2, rs, 20);
   }

   static ResourceKey<LootTable> loot(String id) {
      return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse(id));
   }
}
