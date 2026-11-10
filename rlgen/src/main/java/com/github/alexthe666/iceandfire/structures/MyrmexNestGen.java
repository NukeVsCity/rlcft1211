package com.github.alexthe666.iceandfire.structures;

import com.github.alexthe666.iceandfire.block.BlockGoldPile;
import com.github.alexthe666.iceandfire.block.BlockMyrmexBiolight;
import com.github.alexthe666.iceandfire.block.BlockMyrmexResin;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.world.LegacyBiomeTypes;
import com.github.alexthe666.iceandfire.world.MyrmexWorldData;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * 1.12 WorldGenMyrmexHive with small = true, as a queen digs her nest (EntityMyrmexQueen, ANIMATION_DIGNEST), with the 1.12
 * blocks (iceandfire myrmex resin, biolights, cocoons) and WorldGenMyrmexDecoration: the queen room, one path to an entrance
 * and two rooms (food, nursery), recorded in a new MyrmexHive. Same random calls in the same order as 1.12.
 */
public class MyrmexNestGen {
   private static final ResourceKey<LootTable> MYRMEX_GOLD_CHEST = loot("myrmex_loot_chest");
   private static final ResourceKey<LootTable> DESERT_MYRMEX_FOOD_CHEST = loot("myrmex_desert_food_chest");
   private static final ResourceKey<LootTable> JUNGLE_MYRMEX_FOOD_CHEST = loot("myrmex_jungle_food_chest");
   private static final ResourceKey<LootTable> MYRMEX_TRASH_CHEST = loot("myrmex_trash_chest");

   private final boolean jungle;
   private final boolean small = true;
   private final BlockState resin;
   private final BlockState stickyResin;
   private Level world;
   public MyrmexHive hive;
   private int entrances;
   private int totalRooms;
   private boolean hasFoodRoom;
   private boolean hasNursery;

   public MyrmexNestGen(boolean jungle) {
      this.jungle = jungle;
      BlockMyrmexResin.EnumType variant = jungle ? BlockMyrmexResin.EnumType.JUNGLE : BlockMyrmexResin.EnumType.DESERT;
      this.resin = ModBlocks.myrmex_resin.defaultBlockState().setValue(BlockMyrmexResin.VARIANT, variant);
      this.stickyResin = ModBlocks.myrmex_resin_sticky.defaultBlockState().setValue(BlockMyrmexResin.VARIANT, variant);
   }

   private static ResourceKey<LootTable> loot(String name) {
      return ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.fromNamespaceAndPath("iceandfire", name));
   }

   public void generate(Level world, Random rand, BlockPos position) {
      this.world = world;
      this.hasFoodRoom = false;
      this.hasNursery = false;
      this.totalRooms = 0;
      this.entrances = 0;
      this.generateMainRoom(rand, position);
   }

   private void generateMainRoom(Random rand, BlockPos position) {
      this.hive = new MyrmexHive(this.world, position, 100);
      MyrmexWorldData.addHive(this.world, this.hive);
      this.generateSphere(rand, position, 14, 7, this.resin, this.stickyResin);
      this.generateSphere(rand, position, 12, 5, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, position, 12, 5, WorldGenMyrmexHive.RoomType.QUEEN);
      this.generatePath(rand, position.relative(Direction.NORTH, 9).below(), 15 + rand.nextInt(10), Direction.NORTH, 100);
      this.generatePath(rand, position.relative(Direction.SOUTH, 9).below(), 15 + rand.nextInt(10), Direction.SOUTH, 100);
      this.generatePath(rand, position.relative(Direction.WEST, 9).below(), 15 + rand.nextInt(10), Direction.WEST, 100);
      this.generatePath(rand, position.relative(Direction.EAST, 9).below(), 15 + rand.nextInt(10), Direction.EAST, 100);
   }

   private void generatePath(Random rand, BlockPos offset, int length, Direction direction, int roomChance) {
      if (roomChance != 0) {
         length /= 2;
         if (this.entrances < 1) {
            for (int i = 0; i < length; i++) {
               this.generateCircle(rand, offset.relative(direction, i), 3, 5, direction);
            }

            this.generateEntrance(rand, offset.relative(direction, length), 4, 4, direction);
         } else if (this.totalRooms < 2) {
            for (int i = 0; i < length; i++) {
               this.generateCircle(rand, offset.relative(direction, i), 3, 5, direction);
            }

            this.generateRoom(rand, offset.relative(direction, length), 6, 4, roomChance / 2, direction);

            for (int i = -3; i < 3; i++) {
               this.generateCircleAir(rand, offset.relative(direction, i), 3, 5, direction);
               this.generateCircleAir(rand, offset.relative(direction, length + i), 3, 5, direction);
            }

            this.totalRooms++;
         }
      }
   }

   private void generateRoom(Random rand, BlockPos position, int size, int height, int roomChance, Direction direction) {
      WorldGenMyrmexHive.RoomType type = WorldGenMyrmexHive.RoomType.random(rand);
      if (!this.hasFoodRoom) {
         type = WorldGenMyrmexHive.RoomType.FOOD;
         this.hasFoodRoom = true;
      } else if (!this.hasNursery) {
         type = WorldGenMyrmexHive.RoomType.NURSERY;
         this.hasNursery = true;
      }

      this.generateSphereRespectResin(rand, position, size + 2, height + 2);
      this.generateSphere(rand, position, size, height - 1, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, position, size, height - 1, type);
      this.hive.addRoom(position, type);
   }

   private void generateEntrance(Random rand, BlockPos position, int size, int height, Direction direction) {
      BlockPos up = position.above();
      this.hive.getEntranceBottoms().put(up, direction);

      while (!this.world.canSeeSky(up) && up.getY() < this.world.getMaxBuildHeight()) {
         this.generateCircleRespectSky(rand, up, size, height, direction);
         up = up.above().relative(direction);
      }

      this.generateSphereRespectAir(rand, up, size + 2, height + 2);
      this.generateSphere(rand, up.above(), size, height - 1, Blocks.AIR.defaultBlockState());
      this.decorateSphere(rand, up.above(), size, height - 1, WorldGenMyrmexHive.RoomType.ENTERANCE);
      this.hive.getEntrances().put(up, direction);
      this.entrances++;
   }

   private static BlockPos ring(BlockPos position, int x, int z, Direction direction) {
      return direction != Direction.WEST && direction != Direction.EAST ? position.offset(x, z, 0) : position.offset(0, x, z);
   }

   private BlockState pick(Random rand) {
      return rand.nextInt(3) == 0 ? this.stickyResin : this.resin;
   }

   private void set(BlockPos pos, BlockState state) {
      this.world.setBlock(pos, state, 3);
   }

   private boolean isAir(BlockPos pos) {
      return this.world.isEmptyBlock(pos);
   }

   private void generateCircle(Random rand, BlockPos position, int size, int height, Direction direction) {
      int radius = size + 2;

      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i);
            int z = (int)Math.floor(Math.cos((double)j) * (double)i);
            this.set(ring(position, x, z, direction), this.pick(rand));
         }
      }

      this.carveCircle(rand, position, radius - 2, direction);
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
               this.set(p, this.pick(rand));
            }
         }
      }

      this.carveCircle(rand, position, radius - 2, direction);
      this.decorateCircle(rand, position, size, height, direction);
   }

   private void generateCircleAir(Random rand, BlockPos position, int size, int height, Direction direction) {
      this.carveCircle(rand, position, size, direction);
      this.decorateCircle(rand, position, size, height, direction);
   }

   private void carveCircle(Random rand, BlockPos position, int radius, Direction direction) {
      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i * (double)Mth.clamp(rand.nextFloat(), 0.5F, 1.0F));
            int z = (int)Math.floor(Math.cos((double)j) * (double)i * (double)Mth.clamp(rand.nextFloat(), 0.5F, 1.0F));
            this.set(ring(position, x, z, direction), Blocks.AIR.defaultBlockState());
         }
      }
   }

   /** 1.12 generateSphere(world, rand, pos, size, height, fill): only replaces blocks that are not air. */
   private void generateSphere(Random rand, BlockPos position, int size, int height, BlockState fill) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F)) && !this.isAir(blockpos)) {
            this.set(blockpos, fill);
         }
      }
   }

   private void generateSphere(Random rand, BlockPos position, int size, int height, BlockState fill, BlockState fill2) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F))) {
            this.set(blockpos, rand.nextInt(3) == 0 ? fill2 : fill);
         }
      }
   }

   private void generateSphereRespectResin(Random rand, BlockPos position, int size, int height) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F))
            && (!this.isAir(blockpos) || this.isAir(blockpos) && !this.hasResinUnder(blockpos))) {
            this.set(blockpos, this.pick(rand));
         }
      }
   }

   private void generateSphereRespectAir(Random rand, BlockPos position, int size, int height) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos blockpos : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k, l))) {
         if (blockpos.distSqr(position) <= (double)(f * f * Mth.clamp(rand.nextFloat(), 0.75F, 1.0F)) && !this.isAir(blockpos)) {
            this.set(blockpos, this.pick(rand));
         }
      }
   }

   private boolean hasResinUnder(BlockPos pos) {
      BlockPos copy = pos.below();

      while (this.isAir(copy) && copy.getY() > this.world.getMinBuildHeight() + 1) {
         copy = copy.below();
      }

      return this.world.getBlockState(copy).getBlock() instanceof BlockMyrmexResin;
   }

   private void decorateCircle(Random rand, BlockPos position, int size, int height, Direction direction) {
      int radius = size + 2;

      for (float i = 0.0F; i < (float)radius; i = (float)((double)i + 0.5)) {
         for (float j = 0.0F; (double)j < (Math.PI * 2) * (double)i; j = (float)((double)j + 0.5)) {
            int x = (int)Math.floor(Math.sin((double)j) * (double)i);
            int z = (int)Math.floor(Math.cos((double)j) * (double)i);
            // 1.12: decorate(..., TUNNEL) places nothing; the tubers are tested at position.add(0, x, z) for every direction
            if (this.isAir(position.offset(0, x, z))) {
               this.decorateTubers(position.offset(0, x, z), rand, WorldGenMyrmexHive.RoomType.TUNNEL);
            }
         }
      }
   }

   private void decorateSphere(Random rand, BlockPos position, int size, int height, WorldGenMyrmexHive.RoomType roomType) {
      int ySize = rand.nextInt(2);
      int j = size + rand.nextInt(2);
      int k = height + ySize;
      int l = size + rand.nextInt(2);
      float f = (float)(j + k + l) * 0.333F;

      for (BlockPos b : BlockPos.betweenClosed(position.offset(-j, -k, -l), position.offset(j, k + 2, l))) {
         BlockPos blockpos = b.immutable();
         if (blockpos.distSqr(position) <= (double)(f * f)) {
            if (fullCube(this.world.getBlockState(blockpos.below()), blockpos.below()) && this.isAir(blockpos)) {
               this.decorate(blockpos, position, size, rand, roomType);
            }

            if (this.isAir(blockpos)) {
               this.decorateTubers(blockpos, rand, roomType);
            }
         }
      }
   }

   /** 1.12 IBlockState.isFullCube. */
   private boolean fullCube(BlockState state, BlockPos pos) {
      return state.isCollisionShapeFullBlock(this.world, pos);
   }

   private void decorate(BlockPos blockpos, BlockPos center, int size, Random random, WorldGenMyrmexHive.RoomType roomType) {
      switch (roomType) {
         case FOOD:
            if (random.nextInt(45) == 0 && this.world.getBlockState(blockpos.below()).getBlock() instanceof BlockMyrmexResin) {
               this.generateSkeleton(blockpos, center, size, random);
            }

            if (random.nextInt(13) == 0) {
               this.generateLeaves(blockpos, random);
            }

            if (random.nextInt(12) == 0) {
               this.generatePumpkins(blockpos, random);
            }

            if (random.nextInt(6) == 0) {
               this.generateMushrooms(blockpos, random);
            }

            if (random.nextInt(12) == 0) {
               this.generateCocoon(blockpos, random, this.jungle ? JUNGLE_MYRMEX_FOOD_CHEST : DESERT_MYRMEX_FOOD_CHEST);
            }
            break;
         case NURSERY:
            // 1.12 spawns a baby of a random caste here unless the hive is small (always small for a queen's nest)
            if (random.nextInt(4) == 0 && !this.small) {
               random.nextInt(4);
            }
            break;
         case SHINY:
            if (random.nextInt(12) == 0) {
               this.generateGold(blockpos, random);
            }
            break;
         case TRASH:
            if (random.nextInt(24) == 0) {
               this.generateTrashHeap(blockpos, random);
            }

            if (random.nextBoolean()) {
               this.generateTrashOre(blockpos, center, size, random);
            }

            if (random.nextInt(12) == 0) {
               this.generateCocoon(blockpos, random, MYRMEX_TRASH_CHEST);
            }
            break;
         default:
      }
   }

   private void decorateTubers(BlockPos blockpos, Random random, WorldGenMyrmexHive.RoomType roomType) {
      boolean tunnel = roomType == WorldGenMyrmexHive.RoomType.ENTERANCE || roomType == WorldGenMyrmexHive.RoomType.TUNNEL;
      if (this.world.getBlockState(blockpos.above()).isSolidRender(this.world, blockpos.above()) && random.nextInt(tunnel ? 20 : 6) == 0) {
         int tuberLength = tunnel ? 1 : (roomType == WorldGenMyrmexHive.RoomType.QUEEN ? 1 + random.nextInt(5) : 1 + random.nextInt(3));
         Block light = this.jungle ? ModBlocks.myrmex_jungle_biolight : ModBlocks.myrmex_desert_biolight;

         for (int i = 0; i < tuberLength; i++) {
            if (this.isAir(blockpos.below(i))) {
               this.set(blockpos.below(i), light.defaultBlockState().setValue(BlockMyrmexBiolight.CONNECTED_DOWN, i + 1 < tuberLength));
            }
         }
      }
   }

   // ---- 1.12 WorldGenMyrmexDecoration ----

   private boolean solidTop(BlockPos below) {
      return this.world.getBlockState(below).isFaceSturdy(this.world, below, Direction.UP);
   }

   private boolean hasType(BlockPos pos, String type) {
      Holder<Biome> biome = this.world.getBiome(pos);
      return LegacyBiomeTypes.hasType(biome, type);
   }

   private void generateSkeleton(BlockPos blockpos, BlockPos origin, int radius, Random rand) {
      if (this.solidTop(blockpos.below())) {
         Direction direction = Direction.from2DDataValue(new Random().nextInt(3));
         Direction.Axis oppositeAxis = direction.getAxis() == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
         int maxRibHeight = rand.nextInt(2);
         BlockState bone = Blocks.BONE_BLOCK.defaultBlockState();

         for (int spine = 0; spine < 5 + rand.nextInt(2) * 2; spine++) {
            BlockPos segment = blockpos.relative(direction, spine);
            if (origin.distSqr(segment) <= (double)(radius * radius)) {
               this.world.setBlock(segment, bone.setValue(RotatedPillarBlock.AXIS, direction.getAxis()), 3);
            }

            if (spine % 2 != 0) {
               BlockPos rightRib = segment.relative(direction.getCounterClockWise());
               BlockPos leftRib = segment.relative(direction.getClockWise());
               if (origin.distSqr(rightRib) <= (double)(radius * radius)) {
                  this.world.setBlock(rightRib, bone.setValue(RotatedPillarBlock.AXIS, oppositeAxis), 3);
               }

               if (origin.distSqr(leftRib) <= (double)(radius * radius)) {
                  this.world.setBlock(leftRib, bone.setValue(RotatedPillarBlock.AXIS, oppositeAxis), 3);
               }

               for (int ribHeight = 1; ribHeight < maxRibHeight + 2; ribHeight++) {
                  if (origin.distSqr(rightRib.above(ribHeight).relative(direction.getCounterClockWise())) <= (double)(radius * radius)) {
                     this.world.setBlock(rightRib.above(ribHeight).relative(direction.getCounterClockWise()), bone.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 3);
                  }

                  if (origin.distSqr(leftRib.above(ribHeight).relative(direction.getClockWise())) <= (double)(radius * radius)) {
                     this.world.setBlock(leftRib.above(ribHeight).relative(direction.getClockWise()), bone.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 3);
                  }
               }

               if (origin.distSqr(rightRib.above(maxRibHeight + 2)) <= (double)(radius * radius)) {
                  this.world.setBlock(rightRib.above(maxRibHeight + 2), bone.setValue(RotatedPillarBlock.AXIS, oppositeAxis), 3);
               }

               if (origin.distSqr(leftRib.above(maxRibHeight + 2)) <= (double)(radius * radius)) {
                  this.world.setBlock(leftRib.above(maxRibHeight + 2), bone.setValue(RotatedPillarBlock.AXIS, oppositeAxis), 3);
               }
            }
         }
      }
   }

   private void generateLeaves(BlockPos blockpos, Random rand) {
      if (this.solidTop(blockpos.below())) {
         Block leafBlock = Blocks.OAK_LEAVES;
         if (this.hasType(blockpos, "SANDY") || this.hasType(blockpos, "SAVANNA") || this.hasType(blockpos, "WASTELAND")) {
            leafBlock = Blocks.ACACIA_LEAVES;
         } else if (this.hasType(blockpos, "JUNGLE")) {
            leafBlock = Blocks.JUNGLE_LEAVES;
         } else if (this.hasType(blockpos, "CONIFEROUS")) {
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
               if (pos.distSqr(blockpos) <= (double)(f * f) && this.isAir(pos)) {
                  this.world.setBlock(pos.immutable(), leaf, 4);
               }
            }

            blockpos = blockpos.offset(-(i1 + 1) + rand.nextInt(2 + i1 * 2), 0 - rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
         }
      }
   }

   private void generatePumpkins(BlockPos blockpos, Random rand) {
      if (this.solidTop(blockpos.below())) {
         if (this.hasType(blockpos, "JUNGLE")) {
            this.set(blockpos, Blocks.MELON.defaultBlockState());
         } else {
            // 1.12 BlockHorizontal.FACING of the pumpkin: EnumFacing.byHorizontalIndex(rand.nextInt(3)); 1.21 pumpkins have no facing
            rand.nextInt(3);
            this.set(blockpos, Blocks.PUMPKIN.defaultBlockState());
         }
      }
   }

   private void generateCocoon(BlockPos blockpos, Random rand, ResourceKey<LootTable> lootTable) {
      if (this.solidTop(blockpos.below())) {
         this.set(blockpos, (this.jungle ? ModBlocks.jungle_myrmex_cocoon : ModBlocks.desert_myrmex_cocoon).defaultBlockState());
         BlockEntity te = this.world.getBlockEntity(blockpos);
         if (te instanceof RandomizableContainerBlockEntity container && !te.isRemoved()) {
            container.setLootTable(lootTable, rand.nextLong());
         }
      }
   }

   private void generateMushrooms(BlockPos blockpos, Random rand) {
      if (this.solidTop(blockpos.below())) {
         this.set(blockpos, rand.nextBoolean() ? Blocks.BROWN_MUSHROOM.defaultBlockState() : Blocks.RED_MUSHROOM.defaultBlockState());
      }
   }

   private void generateGold(BlockPos blockpos, Random rand) {
      BlockState gold = rand.nextBoolean() ? ModBlocks.goldPile.defaultBlockState() : ModBlocks.silverPile.defaultBlockState();
      if (this.solidTop(blockpos.below())) {
         this.set(blockpos, gold.setValue(BlockGoldPile.LAYERS, 8));
         this.set(MyrmexHive.getGroundedPos(this.world, blockpos.north()), gold.setValue(BlockGoldPile.LAYERS, 1 + new Random().nextInt(7)));
         this.set(MyrmexHive.getGroundedPos(this.world, blockpos.south()), gold.setValue(BlockGoldPile.LAYERS, 1 + new Random().nextInt(7)));
         this.set(MyrmexHive.getGroundedPos(this.world, blockpos.west()), gold.setValue(BlockGoldPile.LAYERS, 1 + new Random().nextInt(7)));
         this.set(MyrmexHive.getGroundedPos(this.world, blockpos.east()), gold.setValue(BlockGoldPile.LAYERS, 1 + new Random().nextInt(7)));
         if (rand.nextInt(3) == 0) {
            this.set(blockpos.above(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.from2DDataValue(new Random().nextInt(3))));
            if (this.world.getBlockEntity(blockpos.above()) instanceof RandomizableContainerBlockEntity chest && !chest.isRemoved()) {
               chest.setLootTable(MYRMEX_GOLD_CHEST, rand.nextLong());
            }
         }
      }
   }

   private void generateTrashHeap(BlockPos blockpos, Random rand) {
      if (this.solidTop(blockpos.below())) {
         Block blob = switch (rand.nextInt(3)) {
            case 1 -> Blocks.SAND;
            case 2 -> Blocks.COBBLESTONE;
            default -> Blocks.DIRT;
         };
         int i1 = 0;

         for (int i = 0; i1 >= 0 && i < 3; i++) {
            int j = i1 + rand.nextInt(2);
            int k = i1 + rand.nextInt(2);
            int l = i1 + rand.nextInt(2);
            float f = (float)(j + k + l) * 0.333F + 0.5F;

            for (BlockPos pos : BlockPos.betweenClosed(blockpos.offset(-j, -k, -l), blockpos.offset(j, k, l))) {
               if (pos.distSqr(blockpos) <= (double)(f * f)) {
                  this.world.setBlock(pos.immutable(), blob.defaultBlockState(), 4);
               }
            }

            blockpos = blockpos.offset(-(i1 + 1) + rand.nextInt(2 + i1 * 2), 0 - rand.nextInt(2), -(i1 + 1) + rand.nextInt(2 + i1 * 2));
         }
      }
   }

   private void generateTrashOre(BlockPos blockpos, BlockPos origin, int radius, Random rand) {
      BlockState current = this.world.getBlockState(blockpos);
      if (origin.distSqr(blockpos) <= (double)(radius * radius)
         && (current.is(Blocks.DIRT) || current.is(Blocks.SAND) || current.is(Blocks.COBBLESTONE) || current.is(Blocks.GRAVEL))) {
         Block ore = Blocks.REDSTONE_ORE;
         if (rand.nextInt(3) == 0) {
            ore = rand.nextBoolean() ? Blocks.GOLD_ORE : ModBlocks.silverOre;
         } else if (rand.nextInt(3) == 0) {
            ore = Blocks.DIAMOND_ORE;
         } else if (rand.nextInt(2) == 0) {
            ore = rand.nextBoolean() ? Blocks.EMERALD_ORE : ModBlocks.sapphireOre;
         }

         this.set(blockpos, ore.defaultBlockState());
      }
   }
}
