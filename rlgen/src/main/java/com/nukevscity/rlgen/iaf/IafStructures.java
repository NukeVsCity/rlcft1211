package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ice and Fire 1.7.1 WorldGenFire/IceDragonRoosts, WorldGenFire/IceDragonCave, WorldGenCyclopsCave and WorldGenSirenIsland, line by
 * line on the planning world. Calls to an unseeded new Random() use the plan's own random.
 */
final class IafStructures {
   private static final Direction[] HORIZONTALS = {Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST};

   private IafStructures() {
   }

   private static boolean inSphere(BlockPos a, BlockPos b, float f) {
      return a.distSqr(b) <= (double)(f * f);
   }

   private static BlockState pile(boolean fire, int layers) {
      return (fire ? RLGen.GOLD_PILE : RLGen.SILVER_PILE).get().defaultBlockState().setValue(PileBlock.LAYERS, layers);
   }

   private static BlockState cobble(boolean fire) {
      return (fire ? RLGen.CHARED_COBBLESTONE : RLGen.FROZEN_COBBLESTONE).get().defaultBlockState();
   }

   private static boolean isChest(BlockState s) {
      return s.getBlock() instanceof ChestBlock;
   }

   // ------------------------------------------------------------------ roosts

   static void roost(IafWorld world, Random rand, Random unseeded, BlockPos position, boolean fire) {
      int dragonAge = 50 + rand.nextInt(25);
      burnGround(world, rand, position, dragonAge / 5, fire);
      generateStructures(world, rand, unseeded, position, dragonAge / 5, fire);
      rand.nextFloat(); // the dragon's rotation (the dragon itself is not spawned)
   }

   private static void burnGround(IafWorld world, Random rand, BlockPos position, int radius, boolean fire) {
      for (int i = 0; radius >= 0 && i < 3; i++) {
         int j = radius + rand.nextInt(2);
         int k = radius + rand.nextInt(2);
         int l = radius + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
            if (inSphere(blockpos, position, f)) {
               world.convert(blockpos, fire);
            }
         }
      }
   }

   private static void generateStructures(IafWorld world, Random rand, Random unseeded, BlockPos position, int radius, boolean fire) {
      for (int i = 0; radius >= 0 && i < 3; i++) {
         int j = radius + rand.nextInt(2);
         int k = radius + rand.nextInt(2);
         int l = radius + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
            if (inSphere(b, position, f) && world.isAir(b) && Mat112.charred(world.get(b.below()), fire)) {
               BlockPos blockpos = b.immutable();
               int chance = rand.nextInt(100);
               if (chance < 4) {
                  int chance2 = rand.nextInt(20);
                  switch (chance2) {
                     case 1 -> arch(world, rand, blockpos, fire, true);
                     case 2 -> arch(world, rand, blockpos, fire, false);
                     default -> roostPile(world, rand, unseeded, blockpos, fire);
                  }
               }
            }
         }
      }
   }

   private static void roostPile(IafWorld world, Random rand, Random unseeded, BlockPos position, boolean fire) {
      int height = 1 + unseeded.nextInt(7);
      int chance = rand.nextInt(100);
      if (chance < 20) {
         world.chest(position, IafGen.chest(unseeded), fire ? IafGen.FIRE_CHEST : IafGen.ICE_CHEST, unseeded.nextLong());
      } else {
         world.set(position, pile(fire, height));
      }
   }

   /** generateArchNS (along east-west posts) / generateArchEW */
   private static void arch(IafWorld world, Random rand, BlockPos position, boolean fire, boolean ns) {
      int height = 3 + rand.nextInt(1);
      int width = 1 + rand.nextInt(2);
      BlockState c = cobble(fire);
      for (int sides = 0; sides < height; sides++) {
         if (ns) {
            world.set(position.above(sides).east(width / 2), c);
            world.set(position.above(sides).west(width / 2), c);
         } else {
            world.set(position.above(sides).north(width / 2), c);
            world.set(position.above(sides).south(width / 2), c);
         }
      }
      if (ns) {
         for (int way = -1; way < width; way++) {
            world.set(position.above(height).east(way), c);
         }
      } else {
         for (int way = 0; way < width; way++) {
            world.set(position.above(height).south(way), c);
         }
      }
   }

   // ------------------------------------------------------------------ dragon dens

   static void dragonCave(IafWorld world, Random rand, Random unseeded, BlockPos position, boolean fire) {
      int dragonAge = 75 + rand.nextInt(50);
      int i1 = dragonAge / 4;
      int i2 = i1 - 2;
      int ySize = rand.nextInt(2);

      for (int i = 0; i1 >= 0 && i < 3; i++) {
         int j = i1 + rand.nextInt(2);
         int k = i1 / 2 + ySize;
         int l = i1 + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
            if (inSphere(blockpos, position, f) && !isChest(world.get(position)) && (!fire || Mat112.hardness(world.get(position)) >= 0.0F)) {
               world.set(blockpos.immutable(), Blocks.STONE.defaultBlockState());
            }
         }
      }

      for (int i = 0; i2 >= 0 && i < 3; i++) {
         int j = i2 + rand.nextInt(2);
         int k = i2 / 2 + ySize;
         int l = i2 + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
            if (inSphere(blockpos, position, f) && !isChest(world.get(position)) && (fire || Mat112.hardness(world.get(position)) >= 0.0F)) {
               world.set(blockpos.immutable(), Blocks.AIR.defaultBlockState());
            }
         }
      }

      for (int i = 0; i2 >= 0 && i < 3; i++) {
         int j = i2 + rand.nextInt(2);
         int k = i2 + rand.nextInt(2);
         int l = i2 + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
            float hardness = Mat112.hardness(world.get(position));
            boolean ok = fire
               ? inSphere(blockpos, position, f) && hardness >= 0.0F && hardness != -1.0F
               : inSphere(blockpos, position, f) && Mat112.of(world.get(blockpos)) == Mat112.ROCK && hardness >= 0.0F;
            if (ok) {
               setOres(world, unseeded, blockpos.immutable(), fire);
            }
         }
      }

      for (int i = 0; i2 >= 0 && i < 3; i++) {
         int j = i2 + rand.nextInt(2);
         int k = (i2 + rand.nextInt(2)) / 2;
         int l = i2 + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
            if (inSphere(blockpos, position, f)
               && Mat112.of(world.get(blockpos.below())) == Mat112.ROCK
               && Mat112.of(world.get(blockpos)) != Mat112.ROCK
               && (fire || Mat112.hardness(world.get(position)) >= 0.0F)) {
               setGoldPile(world, unseeded, blockpos.immutable(), fire);
            }
         }
      }
      rand.nextFloat(); // the sleeping dragon's rotation
   }

   private static void setGoldPile(IafWorld world, Random unseeded, BlockPos pos, boolean fire) {
      int chance = unseeded.nextInt(99) + 1;
      if (!isChest(world.get(pos))) {
         if (chance < 60) {
            boolean generateGold = IafGen.DEN_GOLD_AMOUNT > 1 ? unseeded.nextInt(IafGen.DEN_GOLD_AMOUNT) == 0 : true;
            world.set(pos, generateGold ? pile(fire, 1 + unseeded.nextInt(7)) : Blocks.AIR.defaultBlockState());
         } else if (chance > 60 && chance < 62) {
            world.chest(pos, IafGen.chest(unseeded), fire ? IafGen.FIRE_CHEST : IafGen.ICE_CHEST, unseeded.nextLong());
         }
      }
   }

   private static void setOres(IafWorld world, Random unseeded, BlockPos pos, boolean fire) {
      float hardness = Mat112.hardness(world.get(pos));
      if (hardness != -1.0F && !world.isAir(pos)) {
         boolean vienChance = unseeded.nextInt(IafGen.ORE_TO_STONE_RATIO + 1) == 0;
         if (vienChance) {
            int chance = unseeded.nextInt(199) + 1;
            if (chance < 30) {
               world.set(pos, Blocks.IRON_ORE.defaultBlockState());
            }
            if (chance > 30 && chance < 40) {
               world.set(pos, Blocks.GOLD_ORE.defaultBlockState());
            }
            if (chance > 40 && chance < 50) {
               world.set(pos, RLGen.SILVER_ORE.get().defaultBlockState());
            }
            if (chance > 50 && chance < 60) {
               world.set(pos, Blocks.COAL_ORE.defaultBlockState());
            }
            if (chance > 60 && chance < 70) {
               world.set(pos, Blocks.REDSTONE_ORE.defaultBlockState());
            }
            if (chance > 70 && chance < 80) {
               world.set(pos, Blocks.LAPIS_ORE.defaultBlockState());
            }
            if (chance > 80 && chance < 90) {
               world.set(pos, Blocks.DIAMOND_ORE.defaultBlockState());
            }
            if (chance > 90 && chance < 1000) {
               world.set(pos, fire ? Blocks.EMERALD_ORE.defaultBlockState() : RLGen.SAPPHIRE_ORE.get().defaultBlockState());
            }
         } else if (unseeded.nextInt(2) == 0) {
            world.set(pos, (fire ? RLGen.CHARED_STONE : RLGen.FROZEN_STONE).get().defaultBlockState());
         } else {
            world.set(pos, cobble(fire));
         }
      }
   }

   // ------------------------------------------------------------------ cyclops caves

   static void cyclopsCave(IafWorld world, Random rand, Random unseeded, BlockPos position) {
      int i1 = 16;
      int i2 = i1 - 2;
      int sheepPenCount = 0;
      int dist = 6;
      if (world.isAir(position.offset(i1 - dist, -3, -i1 + dist))
         || world.isAir(position.offset(i1 - dist, -3, i1 - dist))
         || world.isAir(position.offset(-i1 + dist, -3, -i1 + dist))
         || world.isAir(position.offset(-i1 + dist, -3, i1 - dist))) {
         return;
      }
      int ySize = rand.nextInt(2);
      int j = i1 + rand.nextInt(2);
      int k = 12 + ySize;
      int l = i1 + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F + 0.5F;
      int px = position.getX();
      int py = position.getY();
      int pz = position.getZ();

      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         BlockPos blockpos = b.immutable();
         boolean doorwayX = blockpos.getX() >= px - 2 + rand.nextInt(2) && blockpos.getX() <= px + 2 + rand.nextInt(2);
         boolean doorwayZ = blockpos.getZ() >= pz - 2 + rand.nextInt(2) && blockpos.getZ() <= pz + 2 + rand.nextInt(2);
         boolean isNotInDoorway = !doorwayX && !doorwayZ && blockpos.getY() > py || blockpos.getY() > py + k - (3 + rand.nextInt(2));
         if (inSphere(blockpos, position, f)) {
            if (!isChest(world.get(position)) && Mat112.hardness(world.get(position)) >= 0.0F && isNotInDoorway) {
               world.set(blockpos, Blocks.STONE.defaultBlockState());
            }
            if (blockpos.getY() == py) {
               world.set(blockpos, Blocks.MOSSY_COBBLESTONE.defaultBlockState());
            }
            if (blockpos.getY() <= py - 1 && !Mat112.fullCube(world.get(blockpos))) {
               world.set(blockpos, Blocks.COBBLESTONE.defaultBlockState());
            }
         }
      }

      ySize = rand.nextInt(2);
      j = i2 + rand.nextInt(2);
      k = 10 + ySize;
      l = i2 + rand.nextInt(2);
      f = (float)(j + k + l) * 0.333F + 0.5F;

      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (inSphere(b, position, f) && b.getY() > py && !isChest(world.get(position))) {
            world.set(b.immutable(), Blocks.AIR.defaultBlockState());
         }
      }

      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         BlockPos blockpos = b.immutable();
         if (inSphere(blockpos, position, f) && blockpos.getY() == py) {
            if (rand.nextInt(130) == 0 && isTouchingAir(world, blockpos.above())) {
               genSkeleton(world, blockpos.above(), rand, unseeded, position, f);
            }
            if (rand.nextInt(130) == 0 && blockpos.distSqr(position) <= (double)(f * f) * 0.8F && sheepPenCount < 2) {
               genSheepPen(world, blockpos.above(), rand, position, f);
               sheepPenCount++;
            }
            if (rand.nextInt(80) == 0 && isTouchingAir(world, blockpos.above())) {
               BlockPos up = blockpos.above();
               world.set(up, pile(true, 8));
               world.set(up.north(), pile(true, 1 + unseeded.nextInt(7)));
               world.set(up.south(), pile(true, 1 + unseeded.nextInt(7)));
               world.set(up.west(), pile(true, 1 + unseeded.nextInt(7)));
               world.set(up.east(), pile(true, 1 + unseeded.nextInt(7)));
               BlockState chest = IafGen.chest(unseeded);
               world.chest(blockpos.above(2), chest, IafGen.CYCLOPS_CHEST, rand.nextLong());
            }
            if (rand.nextInt(50) == 0 && isTouchingAir(world, blockpos.above())) {
               int torchHeight = rand.nextInt(2) + 1;
               for (int fence = 0; fence < torchHeight; fence++) {
                  world.set(blockpos.above(1 + fence), Blocks.OAK_FENCE.defaultBlockState());
               }
               world.set(blockpos.above(1 + torchHeight), Blocks.TORCH.defaultBlockState());
            }
         }
      }
      rand.nextInt(3); // the cyclops' variant
      rand.nextFloat(); // and rotation (the cyclops itself is not spawned)
   }

   private static void genSheepPen(IafWorld world, BlockPos blockpos, Random rand, BlockPos origin, float radius) {
      int width = 5 + rand.nextInt(3);
      int sheeps = 2 + rand.nextInt(3);
      int sheepsSpawned = 0;
      Direction direction = Direction.NORTH;
      BlockPos end = blockpos;
      for (int sideCount = 0; sideCount < 4; sideCount++) {
         for (int side = 0; side < width; side++) {
            BlockPos fencePos = end.relative(direction, side);
            if (origin.distSqr(fencePos) <= (double)(radius * radius)) {
               world.set(fencePos, Blocks.OAK_FENCE.defaultBlockState());
               if (world.isAir(fencePos.relative(direction.getClockWise())) && sheepsSpawned < sheeps) {
                  rand.nextInt(4); // the sheep's fleece (sheep are not spawned)
                  sheepsSpawned++;
               }
            }
         }
         end = end.relative(direction, width);
         direction = direction.getClockWise();
      }
      for (int x = 1; x < width - 1; x++) {
         for (int z = 1; z < width - 1; z++) {
            if (origin.distSqr(end.offset(x, 0, z)) <= (double)(radius * radius)) {
               world.set(end.offset(x, 0, z), Blocks.AIR.defaultBlockState());
            }
         }
      }
   }

   private static boolean isTouchingAir(IafWorld world, BlockPos pos) {
      boolean isTouchingAir = true;
      for (Direction direction : HORIZONTALS) {
         if (!world.isAir(pos.relative(direction))) {
            isTouchingAir = false;
         }
      }
      return isTouchingAir;
   }

   static void genSkeleton(IafWorld world, BlockPos blockpos, Random rand, Random unseeded, BlockPos origin, float radius) {
      Direction direction = HORIZONTALS[unseeded.nextInt(3)];
      Direction.Axis oppositeAxis = direction.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
      int maxRibHeight = rand.nextInt(2);
      double r2 = (double)(radius * radius);
      BlockState along = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, direction.getAxis());
      BlockState across = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, oppositeAxis);
      BlockState up = Blocks.BONE_BLOCK.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
      for (int spine = 0; spine < 5 + rand.nextInt(2) * 2; spine++) {
         BlockPos segment = blockpos.relative(direction, spine);
         if (origin.distSqr(segment) <= r2) {
            world.set(segment, along);
         }
         if (spine % 2 != 0) {
            BlockPos rightRib = segment.relative(direction.getCounterClockWise());
            BlockPos leftRib = segment.relative(direction.getClockWise());
            if (origin.distSqr(rightRib) <= r2) {
               world.set(rightRib, across);
            }
            if (origin.distSqr(leftRib) <= r2) {
               world.set(leftRib, across);
            }
            for (int ribHeight = 1; ribHeight < maxRibHeight + 2; ribHeight++) {
               BlockPos r = rightRib.above(ribHeight).relative(direction.getCounterClockWise());
               BlockPos le = leftRib.above(ribHeight).relative(direction.getClockWise());
               if (origin.distSqr(r) <= r2) {
                  world.set(r, up);
               }
               if (origin.distSqr(le) <= r2) {
                  world.set(le, up);
               }
            }
            if (origin.distSqr(rightRib.above(maxRibHeight + 2)) <= r2) {
               world.set(rightRib.above(maxRibHeight + 2), across);
            }
            if (origin.distSqr(leftRib.above(maxRibHeight + 2)) <= r2) {
               world.set(leftRib.above(maxRibHeight + 2), across);
            }
         }
      }
   }

   // ------------------------------------------------------------------ siren islands

   static void sirenIsland(IafWorld world, Random rand, BlockPos position) {
      int up = rand.nextInt(4) + 1;
      BlockPos center = position.above(up);
      int layer = 0;
      int sirens = 0;
      for (int sirensMax = 1 + rand.nextInt(3); !Mat112.opaqueCube(world.get(center)) && center.getY() >= 0; center = center.below()) {
         layer++;
         for (float i = 0.0F; i < (float)sirenRadius(layer, up); i = (float)((double)i + 0.5)) {
            for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i + (double)rand.nextInt(2); j = (float)((double)j + 0.5)) {
               BlockPos stonePos = BlockPos.containing(
                  Math.floor((double)center.getX() + Math.sin((double)j) * (double)i + (double)rand.nextInt(2)),
                  (double)center.getY(),
                  Math.floor((double)center.getZ() + Math.cos((double)j) * (double)i + (double)rand.nextInt(2)));
               world.set(stonePos, sirenStone(rand));
               BlockPos upPos = stonePos.above();
               if (world.isAir(upPos) && world.isAir(upPos.east()) && world.isAir(upPos.north()) && world.isAir(upPos.north().east())
                  && rand.nextInt(3) == 0 && sirens < sirensMax) {
                  sirens++;
                  rand.nextInt(2); // hair colour
                  rand.nextInt(2); // singing pose
                  rand.nextFloat(); // rotation (sirens are not spawned)
               }
            }
         }
      }
      layer++;
      for (float i = 0.0F; i < (float)sirenRadius(layer, up); i = (float)((double)i + 0.5)) {
         for (float jx = 0.0F; (double)jx < (Math.PI * 2) * (double)i + (double)rand.nextInt(2); jx = (float)((double)jx + 0.5)) {
            for (BlockPos stonePos = BlockPos.containing(
                  Math.floor((double)center.getX() + Math.sin((double)jx) * (double)i + (double)rand.nextInt(2)),
                  (double)center.getY(),
                  Math.floor((double)center.getZ() + Math.cos((double)jx) * (double)i + (double)rand.nextInt(2)));
                 !Mat112.opaqueCube(world.get(stonePos)) && stonePos.getY() >= 0;
                 stonePos = stonePos.below()) {
               world.set(stonePos, sirenStone(rand));
            }
         }
      }
   }

   private static int sirenRadius(int layer, int up) {
      return layer > up ? (int)((double)layer * 0.25) + up : layer;
   }

   private static BlockState sirenStone(Random random) {
      int chance = random.nextInt(100);
      if (chance > 90) {
         return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
      } else if (chance > 70) {
         return Blocks.GRAVEL.defaultBlockState();
      } else {
         return chance > 45 ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.STONE.defaultBlockState();
      }
   }

   // ------------------------------------------------------------------ WorldGenMinable

   static void minable(IafWorld w, Random random, BlockPos pos, BlockState ore, int size) {
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
                           w.ore(new BlockPos(l1, i2, j2), ore);
                        }
                     }
                  }
               }
            }
         }
      }
   }
}
