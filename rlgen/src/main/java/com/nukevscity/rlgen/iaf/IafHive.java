package com.nukevscity.rlgen.iaf;

import com.nukevscity.rlgen.RLGen;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Ice and Fire 1.7.1 WorldGenMyrmexHive (small = false, as StructureGenerator used it) and WorldGenMyrmexDecoration. Myrmex are not spawned. */
final class IafHive {
   private enum RoomType {
      DEFAULT(false), TUNNEL(false), ENTERANCE(false), QUEEN(false), FOOD(true), EMPTY(true), NURSERY(true), SHINY(true), TRASH(true);

      final boolean random;

      RoomType(boolean random) {
         this.random = random;
      }

      static RoomType random(Random rand) {
         RoomType[] list = {FOOD, EMPTY, NURSERY, SHINY, TRASH};
         return list[rand.nextInt(list.length)];
      }
   }

   static final String FOOD_DESERT = "rlgen:iceandfire/myrmex_desert_food_chest";
   static final String FOOD_JUNGLE = "rlgen:iceandfire/myrmex_jungle_food_chest";
   static final String TRASH = "rlgen:iceandfire/myrmex_trash_chest";
   static final String GOLD = "rlgen:iceandfire/myrmex_loot_chest";

   private final IafWorld world;
   private final Random unseeded;
   private final boolean jungle;
   private final BlockState resin;
   private final BlockState stickyResin;
   private int entrances;
   private int totalRooms;
   private boolean hasFoodRoom;
   private boolean hasNursery;

   IafHive(IafWorld world, Random unseeded, boolean jungle) {
      this.world = world;
      this.unseeded = unseeded;
      this.jungle = jungle;
      this.resin = (jungle ? RLGen.JUNGLE_MYRMEX_RESIN : RLGen.DESERT_MYRMEX_RESIN).get().defaultBlockState();
      this.stickyResin = (jungle ? RLGen.JUNGLE_MYRMEX_RESIN_STICKY : RLGen.DESERT_MYRMEX_RESIN_STICKY).get().defaultBlockState();
   }

   void generate(Random rand, BlockPos position) {
      this.generateMainRoom(rand, position);
   }

   private BlockState pick(Random rand) {
      return rand.nextInt(3) == 0 ? this.stickyResin : this.resin;
   }

   private static double clampedFloat(Random rand, float min, float max) {
      return (double)Mth.clamp(rand.nextFloat(), min, max);
   }

   private boolean isResin(BlockState s) {
      return s.is(RLGen.DESERT_MYRMEX_RESIN.get()) || s.is(RLGen.JUNGLE_MYRMEX_RESIN.get()) || s.is(RLGen.DESERT_MYRMEX_RESIN_STICKY.get())
         || s.is(RLGen.JUNGLE_MYRMEX_RESIN_STICKY.get());
   }

   private static boolean sturdyUp(BlockState s) {
      return s.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, Direction.UP);
   }

   /** MyrmexHive.getGroundedPos */
   private BlockPos grounded(BlockPos pos) {
      BlockPos current = pos;
      while (this.world.isAir(current.below()) && current.getY() > 0) {
         current = current.below();
      }
      return current;
   }

   private void generateMainRoom(Random rand, BlockPos position) {
      this.generateSphere(rand, position, 14, 7, this.resin, this.stickyResin);
      this.generateSphere(rand, position, 12, 5, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, position, 12, 5, RoomType.QUEEN);
      this.generatePath(rand, position.relative(Direction.NORTH, 9).below(), 15 + rand.nextInt(10), Direction.NORTH, 100);
      this.generatePath(rand, position.relative(Direction.SOUTH, 9).below(), 15 + rand.nextInt(10), Direction.SOUTH, 100);
      this.generatePath(rand, position.relative(Direction.WEST, 9).below(), 15 + rand.nextInt(10), Direction.WEST, 100);
      this.generatePath(rand, position.relative(Direction.EAST, 9).below(), 15 + rand.nextInt(10), Direction.EAST, 100);
      // the queen, workers, soldiers and sentinels (not spawned; their counts are drawn in the loop conditions)
      for (int i = 0; i < 4 + rand.nextInt(3); i++) {
      }
      for (int i = 0; i < 2 + rand.nextInt(2); i++) {
      }
      for (int i = 0; i < rand.nextInt(2); i++) {
      }
   }

   private void generatePath(Random rand, BlockPos offset, int length, Direction direction, int roomChance) {
      if (roomChance != 0 && rand.nextInt(100) < roomChance) {
         for (int i = 0; i < length; i++) {
            this.generateCircle(rand, offset.relative(direction, i), 3, 5, direction);
         }
         if (this.entrances < 3 && rand.nextInt(1 + this.entrances * 2) == 0 && this.hasFoodRoom && this.hasNursery && this.totalRooms > 3) {
            this.generateEntrance(rand, offset.relative(direction, length), 4, 4, direction);
         } else {
            this.generateRoom(rand, offset.relative(direction, length), 7, 4, roomChance / 2, direction);
            for (int i = -3; i < 3; i++) {
               this.generateCircleAir(rand, offset.relative(direction, length + i), 3, 5, direction);
            }
            this.totalRooms++;
         }
      }
   }

   private void generateRoom(Random rand, BlockPos position, int size, int height, int roomChance, Direction direction) {
      RoomType type = RoomType.random(rand);
      if (!this.hasFoodRoom) {
         type = RoomType.FOOD;
         this.hasFoodRoom = true;
      } else if (!this.hasNursery) {
         type = RoomType.NURSERY;
         this.hasNursery = true;
      }
      this.generateSphereRespectResin(rand, position, size + 2, height + 2);
      this.generateSphere(rand, position, size, height - 1, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, position, size, height - 1, type);
      for (Direction d : new Direction[]{Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST}) {
         if (rand.nextInt(3) == 0 && direction.getOpposite() != d) {
            this.generatePath(rand, position.relative(d, size - 2), 5 + rand.nextInt(20), d, roomChance);
         }
      }
   }

   private void generateEntrance(Random rand, BlockPos position, int size, int height, Direction direction) {
      BlockPos up = position.above();
      while (!this.world.canSeeSky(up)) {
         this.generateCircleRespectSky(rand, up, size, height, direction);
         up = up.above().relative(direction);
      }
      this.generateSphereRespectAir(rand, up, size + 2, height + 2);
      this.generateSphere(rand, up.above(), size, height - 1, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, up.above(), size, height - 1, RoomType.ENTERANCE);
      this.entrances++;
   }

   /** position.add(x, z, 0) along north/south, position.add(0, x, z) along west/east */
   private static BlockPos ring(BlockPos position, int x, int z, Direction direction) {
      return direction != Direction.WEST && direction != Direction.EAST ? position.offset(x, z, 0) : position.offset(0, x, z);
   }

   private void generateCircle(Random rand, BlockPos position, int size, int height, Direction direction) {
      int radius = size + 2;
      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i);
            int z = (int)Math.floor(Math.cos((double)j) * (double)i);
            this.world.set(ring(position, x, z, direction), this.pick(rand));
         }
      }
      this.carveRing(rand, position, radius - 2, direction);
      this.decorateCircle(rand, position, size, height, direction);
   }

   private void generateCircleRespectSky(Random rand, BlockPos position, int size, int height, Direction direction) {
      int radius = size + 2;
      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i);
            int z = (int)Math.floor(Math.cos((double)j) * (double)i);
            BlockPos p = ring(position, x, z, direction);
            if (!this.world.canSeeSky(p)) {
               this.world.set(p, this.pick(rand));
            }
         }
      }
      this.carveRing(rand, position, radius - 2, direction);
      this.decorateCircle(rand, position, size, height, direction);
   }

   private void generateCircleAir(Random rand, BlockPos position, int size, int height, Direction direction) {
      this.carveRing(rand, position, size, direction);
      this.decorateCircle(rand, position, size, height, direction);
   }

   private void carveRing(Random rand, BlockPos position, int radius, Direction direction) {
      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i * clampedFloat(rand, 0.5F, 1.0F));
            int z = (int)Math.floor(Math.cos((double)j) * (double)i * clampedFloat(rand, 0.5F, 1.0F));
            this.world.set(ring(position, x, z, direction), Blocks.AIR.defaultBlockState());
         }
      }
   }

   /** generateSphere with one fill: only non-air blocks */
   private void generateSphere(Random rand, BlockPos position, int size, int height, BlockState fill) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;
      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (b.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F)) && !this.world.isAir(b)) {
            this.world.set(b.immutable(), fill);
         }
      }
   }

   private void generateSphere(Random rand, BlockPos position, int size, int height, BlockState fill, BlockState fill2) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;
      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (b.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F))) {
            this.world.set(b.immutable(), rand.nextInt(3) == 0 ? fill2 : fill);
         }
      }
   }

   private void generateSphereRespectResin(Random rand, BlockPos position, int size, int height) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;
      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (b.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F))
            && (!this.world.isAir(b) || this.world.isAir(b) && !this.hasResinUnder(b))) {
            this.world.set(b.immutable(), this.pick(rand));
         }
      }
   }

   private void generateSphereRespectAir(Random rand, BlockPos position, int size, int height) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;
      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (b.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F)) && !this.world.isAir(b)) {
            this.world.set(b.immutable(), this.pick(rand));
         }
      }
   }

   private boolean hasResinUnder(BlockPos pos) {
      BlockPos copy = pos.below();
      while (this.world.isAir(copy) && copy.getY() > 1) {
         copy = copy.below();
      }
      return this.isResin(this.world.get(copy));
   }

   private void decorateCircle(Random rand, BlockPos position, int size, int height, Direction direction) {
      int radius = size + 2;
      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i);
            int z = (int)Math.floor(Math.cos((double)j) * (double)i);
            // decorate(..., TUNNEL) does nothing; the tubers are always tested at position.add(0, x, z) (a 1.12 quirk)
            BlockPos p = position.offset(0, x, z);
            if (this.world.isAir(p)) {
               this.decorateTubers(p, rand, RoomType.TUNNEL);
            }
         }
      }
   }

   private void decorateSphere(Random rand, BlockPos position, int size, int height, RoomType roomType) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;
      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k + 2, l))) {
         BlockPos blockpos = b.immutable();
         if (blockpos.distSqr(position) <= (double)(f * f)) {
            if (Mat112.fullCube(this.world.get(blockpos.below())) && this.world.isAir(blockpos)) {
               this.decorate(blockpos, position, size, rand, roomType);
            }
            if (this.world.isAir(blockpos)) {
               this.decorateTubers(blockpos, rand, roomType);
            }
         }
      }
   }

   private void decorate(BlockPos blockpos, BlockPos center, int size, Random random, RoomType roomType) {
      switch (roomType) {
         case FOOD -> {
            if (random.nextInt(45) == 0 && this.isResin(this.world.get(blockpos.below()))) {
               if (sturdyUp(this.world.get(blockpos.below()))) {
                  IafStructures.genSkeleton(this.world, blockpos, random, this.unseeded, center, (float)size);
               }
            }
            if (random.nextInt(13) == 0) {
               this.generateLeaves(blockpos, random);
            }
            if (random.nextInt(12) == 0 && sturdyUp(this.world.get(blockpos.below()))) {
               if (this.world.hasType(blockpos, "JUNGLE")) {
                  this.world.set(blockpos, Blocks.MELON.defaultBlockState());
               } else {
                  random.nextInt(3); // the 1.12 pumpkin's facing
                  this.world.set(blockpos, Blocks.PUMPKIN.defaultBlockState());
               }
            }
            if (random.nextInt(6) == 0 && sturdyUp(this.world.get(blockpos.below()))) {
               this.world.set(blockpos, random.nextBoolean() ? Blocks.BROWN_MUSHROOM.defaultBlockState() : Blocks.RED_MUSHROOM.defaultBlockState());
            }
            if (random.nextInt(12) == 0) {
               this.generateCocoon(blockpos, random, this.jungle ? FOOD_JUNGLE : FOOD_DESERT);
            }
         }
         case NURSERY -> {
            if (random.nextInt(4) == 0) {
               random.nextInt(4); // a baby myrmex (not spawned): caste, growth stage, rotations
               random.nextInt(2);
               random.nextFloat();
               random.nextFloat();
            }
         }
         case SHINY -> {
            if (random.nextInt(12) == 0) {
               this.generateGold(blockpos, random);
            }
         }
         case TRASH -> {
            if (random.nextInt(24) == 0) {
               this.generateTrashHeap(blockpos, random);
            }
            if (random.nextBoolean()) {
               this.generateTrashOre(blockpos, center, size, random);
            }
            if (random.nextInt(12) == 0) {
               this.generateCocoon(blockpos, random, TRASH);
            }
         }
         default -> {
         }
      }
   }

   private void decorateTubers(BlockPos blockpos, Random random, RoomType roomType) {
      boolean tunnel = roomType == RoomType.ENTERANCE || roomType == RoomType.TUNNEL;
      if (Mat112.opaqueCube(this.world.get(blockpos.above())) && random.nextInt(tunnel ? 20 : 6) == 0) {
         int tuberLength = tunnel ? 1 : (roomType == RoomType.QUEEN ? 1 + random.nextInt(5) : 1 + random.nextInt(3));
         BlockState light = (this.jungle ? RLGen.MYRMEX_JUNGLE_BIOLIGHT : RLGen.MYRMEX_DESERT_BIOLIGHT).get().defaultBlockState();
         for (int i = 0; i < tuberLength; i++) {
            if (this.world.isAir(blockpos.below(i))) {
               this.world.set(blockpos.below(i), light.setValue(BiolightBlock.DOWN, i + 1 < tuberLength));
            }
         }
      }
   }

   private void generateLeaves(BlockPos blockpos, Random rand) {
      if (!sturdyUp(this.world.get(blockpos.below()))) {
         return;
      }
      Block leafBlock = Blocks.OAK_LEAVES;
      if (this.world.hasType(blockpos, "SANDY") || this.world.hasType(blockpos, "SAVANNA") || this.world.hasType(blockpos, "WASTELAND")) {
         leafBlock = Blocks.ACACIA_LEAVES;
      } else if (this.world.hasType(blockpos, "JUNGLE")) {
         leafBlock = Blocks.JUNGLE_LEAVES;
      } else if (this.world.hasType(blockpos, "CONIFEROUS")) {
         leafBlock = Blocks.SPRUCE_LEAVES;
      }
      BlockState leaf = leafBlock.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);
      int i1 = 0;
      for (int i = 0; i1 >= 0 && i < 3; i++) {
         int j = i1 + rand.nextInt(2);
         int k = i1 + rand.nextInt(2);
         int l = i1 + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos pos : BlockPos.betweenClosed(blockpos.offset(-j, -k, -l), blockpos.offset(j, k, l))) {
            if (pos.distSqr(blockpos) <= (double)(f * f) && this.world.isAir(pos)) {
               this.world.set(pos.immutable(), leaf);
            }
         }
         blockpos = blockpos.offset(-(i1 + 1) + rand.nextInt(2 + i1 * 2), 0 - rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
      }
   }

   private void generateCocoon(BlockPos blockpos, Random rand, String lootTable) {
      if (sturdyUp(this.world.get(blockpos.below()))) {
         BlockState cocoon = (this.jungle ? RLGen.JUNGLE_MYRMEX_COCOON : RLGen.DESERT_MYRMEX_COCOON).get().defaultBlockState();
         this.world.chest(blockpos, cocoon, lootTable, rand.nextLong());
      }
   }

   private void generateGold(BlockPos blockpos, Random rand) {
      BlockState gold = (rand.nextBoolean() ? RLGen.GOLD_PILE : RLGen.SILVER_PILE).get().defaultBlockState();
      if (sturdyUp(this.world.get(blockpos.below()))) {
         this.world.set(blockpos, gold.setValue(PileBlock.LAYERS, 8));
         this.world.set(this.grounded(blockpos.north()), gold.setValue(PileBlock.LAYERS, 1 + this.unseeded.nextInt(7)));
         this.world.set(this.grounded(blockpos.south()), gold.setValue(PileBlock.LAYERS, 1 + this.unseeded.nextInt(7)));
         this.world.set(this.grounded(blockpos.west()), gold.setValue(PileBlock.LAYERS, 1 + this.unseeded.nextInt(7)));
         this.world.set(this.grounded(blockpos.east()), gold.setValue(PileBlock.LAYERS, 1 + this.unseeded.nextInt(7)));
         if (rand.nextInt(3) == 0) {
            BlockState chest = IafGen.chest(this.unseeded);
            this.world.chest(blockpos.above(), chest, GOLD, rand.nextLong());
         }
      }
   }

   private void generateTrashHeap(BlockPos blockpos, Random rand) {
      if (!sturdyUp(this.world.get(blockpos.below()))) {
         return;
      }
      BlockState blob = switch (rand.nextInt(3)) {
         case 1 -> Blocks.SAND.defaultBlockState();
         case 2 -> Blocks.COBBLESTONE.defaultBlockState();
         default -> Blocks.DIRT.defaultBlockState();
      };
      int i1 = 0;
      for (int i = 0; i1 >= 0 && i < 3; i++) {
         int j = i1 + rand.nextInt(2);
         int k = i1 + rand.nextInt(2);
         int l = i1 + rand.nextInt(2);
         float f = (float)(j + k + l) * 0.333F + 0.5F;
         for (BlockPos pos : BlockPos.betweenClosed(blockpos.offset(-j, -k, -l), blockpos.offset(j, k, l))) {
            if (pos.distSqr(blockpos) <= (double)(f * f)) {
               this.world.set(pos.immutable(), blob);
            }
         }
         blockpos = blockpos.offset(-(i1 + 1) + rand.nextInt(2 + i1 * 2), 0 - rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
      }
   }

   private void generateTrashOre(BlockPos blockpos, BlockPos origin, int radius, Random rand) {
      BlockState current = this.world.get(blockpos);
      if (origin.distSqr(blockpos) <= (double)(radius * radius)
         && (current.is(Blocks.DIRT) || current.is(Blocks.SAND) || current.is(Blocks.COBBLESTONE) || current.is(Blocks.GRAVEL))) {
         Block ore = Blocks.REDSTONE_ORE;
         if (rand.nextInt(3) == 0) {
            ore = rand.nextBoolean() ? Blocks.GOLD_ORE : RLGen.SILVER_ORE.get();
         } else if (rand.nextInt(3) == 0) {
            ore = Blocks.DIAMOND_ORE;
         } else if (rand.nextInt(2) == 0) {
            ore = rand.nextBoolean() ? Blocks.EMERALD_ORE : RLGen.SAPPHIRE_ORE.get();
         }
         this.world.set(blockpos, ore.defaultBlockState());
      }
   }
}
