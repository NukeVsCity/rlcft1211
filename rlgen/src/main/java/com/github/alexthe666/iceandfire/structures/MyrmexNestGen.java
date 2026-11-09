package com.github.alexthe666.iceandfire.structures;

import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import com.nukevscity.rlgen.RLGen;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 1.12 WorldGenMyrmexHive with small = true, as a queen digs her nest (EntityMyrmexQueen, ANIMATION_DIGNEST): the main room,
 * one path to an entrance and up to two rooms, recorded in a new MyrmexHive. The resin shell, tunnels and rooms follow the 1.12
 * code and random calls; the room decorations (cocoons, biolights, resin piles, chests) are not placed yet.
 */
public class MyrmexNestGen {
   private final boolean jungle;
   private final BlockState resin;
   private final BlockState stickyResin;
   public MyrmexHive hive;
   private int entrances;
   private int totalRooms;
   private boolean hasFoodRoom;
   private boolean hasNursery;

   public MyrmexNestGen(boolean jungle) {
      this.jungle = jungle;
      this.resin = (jungle ? RLGen.JUNGLE_MYRMEX_RESIN : RLGen.DESERT_MYRMEX_RESIN).get().defaultBlockState();
      this.stickyResin = (jungle ? RLGen.JUNGLE_MYRMEX_RESIN_STICKY : RLGen.DESERT_MYRMEX_RESIN_STICKY).get().defaultBlockState();
   }

   public void generate(Level world, Random rand, BlockPos position) {
      this.hasFoodRoom = false;
      this.hasNursery = false;
      this.totalRooms = 0;
      this.entrances = 0;
      this.hive = new MyrmexHive(world, position, 100);
      MyrmexWorldData.addHive(world, this.hive);
      this.generateSphere(world, rand, position, 14, 7, this.resin, this.stickyResin);
      this.generateSphereFill(world, rand, position, 12, 5, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, 12, 5);
      this.generatePath(world, rand, position.relative(Direction.NORTH, 9).below(), 15 + rand.nextInt(10), Direction.NORTH, 100);
      this.generatePath(world, rand, position.relative(Direction.SOUTH, 9).below(), 15 + rand.nextInt(10), Direction.SOUTH, 100);
      this.generatePath(world, rand, position.relative(Direction.WEST, 9).below(), 15 + rand.nextInt(10), Direction.WEST, 100);
      this.generatePath(world, rand, position.relative(Direction.EAST, 9).below(), 15 + rand.nextInt(10), Direction.EAST, 100);
   }

   private void generatePath(Level world, Random rand, BlockPos offset, int length, Direction direction, int roomChance) {
      if (roomChance != 0) {
         length /= 2;
         if (this.entrances < 1) {
            for (int i = 0; i < length; i++) {
               this.generateCircle(world, rand, offset.relative(direction, i), 3, direction, false);
            }

            this.generateEntrance(world, rand, offset.relative(direction, length), 4, 4, direction);
         } else if (this.totalRooms < 2) {
            for (int i = 0; i < length; i++) {
               this.generateCircle(world, rand, offset.relative(direction, i), 3, direction, false);
            }

            this.generateRoom(world, rand, offset.relative(direction, length), 6, 4, roomChance / 2, direction);

            for (int i = -3; i < 3; i++) {
               this.generateCircleAir(world, rand, offset.relative(direction, i), 3, direction);
               this.generateCircleAir(world, rand, offset.relative(direction, length + i), 3, direction);
            }

            this.totalRooms++;
         }
      }
   }

   private void generateRoom(Level world, Random rand, BlockPos position, int size, int height, int roomChance, Direction direction) {
      WorldGenMyrmexHive.RoomType type = WorldGenMyrmexHive.RoomType.random(rand);
      if (!this.hasFoodRoom) {
         type = WorldGenMyrmexHive.RoomType.FOOD;
         this.hasFoodRoom = true;
      } else if (!this.hasNursery) {
         type = WorldGenMyrmexHive.RoomType.NURSERY;
         this.hasNursery = true;
      }

      this.generateSphereRespectResin(world, rand, position, size + 2, height + 2);
      this.generateSphereFill(world, rand, position, size, height - 1, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, size, height - 1);
      this.hive.addRoom(position, type);
   }

   private void generateEntrance(Level world, Random rand, BlockPos position, int size, int height, Direction direction) {
      BlockPos up = position.above();
      this.hive.getEntranceBottoms().put(up, direction);

      while (!world.canSeeSky(up) && up.getY() < world.getMaxBuildHeight()) {
         this.generateCircle(world, rand, up, size, direction, true);
         up = up.above().relative(direction);
      }

      this.generateSphereRespectAir(world, rand, up, size + 2, height + 2);
      this.generateSphereFill(world, rand, up.above(), size, height - 1, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, size, height - 1);
      this.hive.getEntrances().put(up, direction);
      this.entrances++;
   }

   private static BlockPos ring(BlockPos position, int x, int z, Direction direction) {
      return direction != Direction.WEST && direction != Direction.EAST ? position.offset(x, z, 0) : position.offset(0, x, z);
   }

   private BlockState pick(Random rand) {
      return rand.nextInt(3) == 0 ? this.stickyResin : this.resin;
   }

   private void generateCircle(Level world, Random rand, BlockPos position, int size, Direction direction, boolean respectSky) {
      int radius = size + 2;

      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i);
            int z = (int)Math.floor(Math.cos((double)j) * (double)i);
            BlockPos p = ring(position, x, z, direction);
            if (!respectSky || !world.canSeeSky(p)) {
               world.setBlock(p, this.pick(rand), 3);
            } else {
               rand.nextInt(3);
            }
         }
      }

      this.generateCircleAirInner(world, rand, position, radius - 2, direction);
   }

   private void generateCircleAir(Level world, Random rand, BlockPos position, int size, Direction direction) {
      this.generateCircleAirInner(world, rand, position, size, direction);
   }

   private void generateCircleAirInner(Level world, Random rand, BlockPos position, int radius, Direction direction) {
      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i * (double)Mth.clamp(rand.nextFloat(), 0.5F, 1.0F));
            int z = (int)Math.floor(Math.cos((double)j) * (double)i * (double)Mth.clamp(rand.nextFloat(), 0.5F, 1.0F));
            world.setBlock(ring(position, x, z, direction), Blocks.AIR.defaultBlockState(), 3);
         }
      }
   }

   private void generateSphereFill(Level world, Random rand, BlockPos position, int size, int height, BlockState fill) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F)) && !world.isEmptyBlock(blockpos)) {
            world.setBlock(blockpos, fill, 3);
         }
      }
   }

   private void generateSphere(Level world, Random rand, BlockPos position, int size, int height, BlockState fill, BlockState fill2) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F))) {
            world.setBlock(blockpos, rand.nextInt(3) == 0 ? fill2 : fill, 3);
         }
      }
   }

   private void generateSphereRespectResin(Level world, Random rand, BlockPos position, int size, int height) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F))
            && (!world.isEmptyBlock(blockpos) || world.isEmptyBlock(blockpos) && !this.hasResinUnder(blockpos, world))) {
            world.setBlock(blockpos, this.pick(rand), 3);
         }
      }
   }

   private void generateSphereRespectAir(Level world, Random rand, BlockPos position, int size, int height) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F)) && !world.isEmptyBlock(blockpos)) {
            world.setBlock(blockpos, this.pick(rand), 3);
         }
      }
   }

   private boolean hasResinUnder(BlockPos pos, Level world) {
      BlockPos copy = pos.below();

      while (world.isEmptyBlock(copy) && copy.getY() > world.getMinBuildHeight() + 1) {
         copy = copy.below();
      }

      return com.github.alexthe666.iceandfire.entity.EntityMyrmexBase.isResin(world.getBlockState(copy));
   }

   /** decorateSphere: draws the sphere size like 1.12 (keeps the random sequence); the decorations themselves are not placed. */
   private void decorateSphere(Random rand, int size, int height) {
      rand.nextInt(2);
      rand.nextInt(2);
      rand.nextInt(2);
   }
}
