package com.nukevscity.rlgen.stronghold;

import com.nukevscity.rlgen.deco.DWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

/** 1.12.2 StructureStrongholdPieces, blocks flattened to 1.21. Not thread-safe while building (static piece weights, like 1.12). */
public class StrongholdPieces {
   private static final PieceWeight[] PIECE_WEIGHTS = new PieceWeight[]{
      new PieceWeight(Straight.class, 40, 0),
      new PieceWeight(Prison.class, 5, 5),
      new PieceWeight(LeftTurn.class, 20, 0),
      new PieceWeight(RightTurn.class, 20, 0),
      new PieceWeight(RoomCrossing.class, 10, 6),
      new PieceWeight(StairsStraight.class, 5, 5),
      new PieceWeight(Stairs.class, 5, 5),
      new PieceWeight(Crossing.class, 5, 4),
      new PieceWeight(ChestCorridor.class, 5, 4),
      new PieceWeight(Library.class, 10, 2) {
         @Override
         public boolean canSpawnMoreStructuresOfType(int i) {
            return super.canSpawnMoreStructuresOfType(i) && i > 4;
         }
      },
      new PieceWeight(PortalRoom.class, 20, 1) {
         @Override
         public boolean canSpawnMoreStructuresOfType(int i) {
            return super.canSpawnMoreStructuresOfType(i) && i > 5;
         }
      }
   };
   private static List<PieceWeight> structurePieceList;
   private static Class<? extends Stronghold> strongComponentType;
   static int totalWeight;
   private static final Component.BlockSelector STRONGHOLD_STONES = StrongholdPieces::stones;
   private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
   /** 1.12 double_stone_slab (stone variant). */
   private static final BlockState DOUBLE_STONE_SLAB = Blocks.SMOOTH_STONE_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE);

   /** 1.12 torch: UP is a floor torch, the rest wall torches. */
   private static BlockState torch(Direction facing) {
      return facing == Direction.UP ? Blocks.TORCH.defaultBlockState() : Blocks.WALL_TORCH.defaultBlockState().setValue(WallTorchBlock.FACING, facing);
   }

   public static void prepareStructurePieces() {
      structurePieceList = new ArrayList<>();

      for (PieceWeight pieceweight : PIECE_WEIGHTS) {
         pieceweight.instancesSpawned = 0;
         structurePieceList.add(pieceweight);
      }

      strongComponentType = null;
   }

   private static boolean canAddStructurePieces() {
      boolean flag = false;
      totalWeight = 0;

      for (PieceWeight pieceweight : structurePieceList) {
         if (pieceweight.instancesLimit > 0
            && pieceweight.instancesSpawned < pieceweight.instancesLimit) {
            flag = true;
         }

         totalWeight = totalWeight + pieceweight.pieceWeight;
      }

      return flag;
   }

   private static Stronghold findAndCreatePieceFactory(
      Class<? extends Stronghold> oclass,
      List<Component> list,
      Random random,
      int i,
      int j,
      int k,
      @Nullable Direction enumfacing,
      int l
   ) {
      Stronghold stronghold = null;
      if (oclass == Straight.class) {
         stronghold = Straight.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == Prison.class) {
         stronghold = Prison.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == LeftTurn.class) {
         stronghold = LeftTurn.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == RightTurn.class) {
         stronghold = RightTurn.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == RoomCrossing.class) {
         stronghold = RoomCrossing.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == StairsStraight.class) {
         stronghold = StairsStraight.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == Stairs.class) {
         stronghold = Stairs.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == Crossing.class) {
         stronghold = Crossing.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == ChestCorridor.class) {
         stronghold = ChestCorridor.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == Library.class) {
         stronghold = Library.createPiece(list, random, i, j, k, enumfacing, l);
      } else if (oclass == PortalRoom.class) {
         stronghold = PortalRoom.createPiece(list, random, i, j, k, enumfacing, l);
      }

      return stronghold;
   }

   private static Stronghold generatePieceFromSmallDoor(
      Stairs2 stairs2,
      List<Component> list,
      Random random,
      int i,
      int j,
      int k,
      Direction enumfacing,
      int l
   ) {
      if (!canAddStructurePieces()) {
         return null;
      } else {
         if (strongComponentType != null) {
            Stronghold stronghold = findAndCreatePieceFactory(
               strongComponentType, list, random, i, j, k, enumfacing, l
            );
            strongComponentType = null;
            if (stronghold != null) {
               return stronghold;
            }
         }

         int j1 = 0;

         while (j1 < 5) {
            j1++;
            int i1 = random.nextInt(totalWeight);

            for (PieceWeight pieceweight : structurePieceList) {
               i1 -= pieceweight.pieceWeight;
               if (i1 < 0) {
                  if (!pieceweight.canSpawnMoreStructuresOfType(l)
                     || pieceweight == stairs2.lastPlaced) {
                     break;
                  }

                  Stronghold stronghold1 = findAndCreatePieceFactory(
                     pieceweight.pieceClass, list, random, i, j, k, enumfacing, l
                  );
                  if (stronghold1 != null) {
                     pieceweight.instancesSpawned++;
                     stairs2.lastPlaced = pieceweight;
                     if (!pieceweight.canSpawnMoreStructures()) {
                        structurePieceList.remove(pieceweight);
                     }

                     return stronghold1;
                  }
               }
            }
         }

         SBox box = Corridor.findPieceBox(list, random, i, j, k, enumfacing);
         return box != null && box.minY > 1
            ? new Corridor(l, random, box, enumfacing)
            : null;
      }
   }

   private static Component generateAndAddPiece(
      Stairs2 stairs2,
      List<Component> list,
      Random random,
      int i,
      int j,
      int k,
      @Nullable Direction enumfacing,
      int l
   ) {
      if (l > 50) {
         return null;
      } else if (Math.abs(i - stairs2.getBoundingBox().minX) <= 112
         && Math.abs(k - stairs2.getBoundingBox().minZ) <= 112) {
         Component structurecomponent = generatePieceFromSmallDoor(stairs2, list, random, i, j, k, enumfacing, l + 1);
         if (structurecomponent != null) {
            list.add(structurecomponent);
            stairs2.pendingChildren.add(structurecomponent);
         }

         return structurecomponent;
      } else {
         return null;
      }
   }

   public static class ChestCorridor extends Stronghold {
      private boolean hasMadeChest;

      public ChestCorridor(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
      }


      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 1, 1);
      }

      public static ChestCorridor createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 5, 5, 7, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new ChestCorridor(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 4, 4, 6, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 1, 0);
            this.placeDoor(world, random, box, Stronghold.Door.OPENING, 1, 1, 6);
            this.fillWithBlocks(world, box, 3, 1, 2, 3, 1, 4, Blocks.STONE_BRICKS.defaultBlockState(), Blocks.STONE_BRICKS.defaultBlockState(), false);
            this.setBlockState(world, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 1, box);
            this.setBlockState(world, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 1, 5, box);
            this.setBlockState(world, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 2, box);
            this.setBlockState(world, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 3, 2, 4, box);

            for (int i = 2; i <= 4; i++) {
               this.setBlockState(world, Blocks.STONE_BRICK_SLAB.defaultBlockState(), 2, 1, i, box);
            }

            if (!this.hasMadeChest
               && box.isVecInside(new BlockPos(this.getXWithOffset(3, 3), this.getYWithOffset(2), this.getZWithOffset(3, 3)))) {
               this.hasMadeChest = true;
               this.generateChest(world, box, random, 3, 2, 3, BuiltInLootTables.STRONGHOLD_CORRIDOR);
            }

            return true;
         }
      }
   }

   public static class Corridor extends Stronghold {
      private int steps;

      public Corridor(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.boundingBox = box;
         this.steps = enumfacing != Direction.NORTH && enumfacing != Direction.SOUTH ? box.getXSize() : box.getZSize();
      }


      public static SBox findPieceBox(List<Component> list, Random random, int i, int j, int k, Direction enumfacing) {
         int l = 3;
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 5, 5, 4, enumfacing);
         Component structurecomponent = Component.findIntersecting(list, box);
         if (structurecomponent == null) {
            return null;
         } else {
            if (structurecomponent.getBoundingBox().minY == box.minY) {
               for (int i1 = 3; i1 >= 1; i1--) {
                  box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 5, 5, i1 - 1, enumfacing);
                  if (!structurecomponent.getBoundingBox().intersectsWith(box)) {
                     return SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 5, 5, i1, enumfacing);
                  }
               }
            }

            return null;
         }
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            for (int i = 0; i < this.steps; i++) {
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 0, 0, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 0, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 2, 0, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 0, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 4, 0, i, box);

               for (int j = 1; j <= 3; j++) {
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 0, j, i, box);
                  this.setBlockState(world, Blocks.AIR.defaultBlockState(), 1, j, i, box);
                  this.setBlockState(world, Blocks.AIR.defaultBlockState(), 2, j, i, box);
                  this.setBlockState(world, Blocks.AIR.defaultBlockState(), 3, j, i, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 4, j, i, box);
               }

               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 0, 4, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 4, i, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 4, 4, i, box);
            }

            return true;
         }
      }
   }

   public static class Crossing extends Stronghold {
      private boolean leftLow;
      private boolean leftHigh;
      private boolean rightLow;
      private boolean rightHigh;

      public Crossing(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
         this.leftLow = random.nextBoolean();
         this.leftHigh = random.nextBoolean();
         this.rightLow = random.nextBoolean();
         this.rightHigh = random.nextInt(3) > 0;
      }


      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         int i = 3;
         int j = 5;
         Direction enumfacing = this.getCoordBaseMode();
         if (enumfacing == Direction.WEST || enumfacing == Direction.NORTH) {
            i = 8 - i;
            j = 8 - j;
         }

         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 5, 1);
         if (this.leftLow) {
            this.getNextComponentX((Stairs2)structurecomponent, list, random, i, 1);
         }

         if (this.leftHigh) {
            this.getNextComponentX((Stairs2)structurecomponent, list, random, j, 7);
         }

         if (this.rightLow) {
            this.getNextComponentZ((Stairs2)structurecomponent, list, random, i, 1);
         }

         if (this.rightHigh) {
            this.getNextComponentZ((Stairs2)structurecomponent, list, random, j, 7);
         }
      }

      public static Crossing createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -4, -3, 0, 10, 9, 11, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new Crossing(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 9, 8, 10, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 4, 3, 0);
            if (this.leftLow) {
               this.fillWithBlocks(world, box, 0, 3, 1, 0, 5, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            if (this.rightLow) {
               this.fillWithBlocks(world, box, 9, 3, 1, 9, 5, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            if (this.leftHigh) {
               this.fillWithBlocks(world, box, 0, 5, 7, 0, 7, 9, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            if (this.rightHigh) {
               this.fillWithBlocks(world, box, 9, 5, 7, 9, 7, 9, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            this.fillWithBlocks(world, box, 5, 1, 10, 7, 3, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.fillWithRandomizedBlocks(world, box, 1, 2, 1, 8, 2, 6, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 4, 1, 5, 4, 4, 9, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 8, 1, 5, 8, 4, 9, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 1, 4, 7, 3, 4, 9, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 1, 3, 5, 3, 3, 6, false, random, STRONGHOLD_STONES);
            this.fillWithBlocks(world, box, 1, 3, 4, 3, 3, 4, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 1, 4, 6, 3, 4, 6, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.fillWithRandomizedBlocks(world, box, 5, 1, 7, 7, 1, 8, false, random, STRONGHOLD_STONES);
            this.fillWithBlocks(world, box, 5, 1, 9, 7, 1, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 5, 2, 7, 7, 2, 7, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 4, 5, 7, 4, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 8, 5, 7, 8, 5, 9, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), false);
            this.fillWithBlocks(
               world, box, 5, 5, 7, 7, 5, 9, DOUBLE_STONE_SLAB, DOUBLE_STONE_SLAB, false
            );
            this.setBlockState(world, torch(Direction.SOUTH), 6, 5, 6, box);
            return true;
         }
      }
   }

   public static class LeftTurn extends Stronghold {

      public LeftTurn(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
      }

      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         Direction enumfacing = this.getCoordBaseMode();
         if (enumfacing != Direction.NORTH && enumfacing != Direction.EAST) {
            this.getNextComponentZ((Stairs2)structurecomponent, list, random, 1, 1);
         } else {
            this.getNextComponentX((Stairs2)structurecomponent, list, random, 1, 1);
         }
      }

      public static LeftTurn createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 5, 5, 5, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new LeftTurn(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 4, 4, 4, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 1, 0);
            Direction enumfacing = this.getCoordBaseMode();
            if (enumfacing != Direction.NORTH && enumfacing != Direction.EAST) {
               this.fillWithBlocks(world, box, 4, 1, 1, 4, 3, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            } else {
               this.fillWithBlocks(world, box, 0, 1, 1, 0, 3, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            return true;
         }
      }
   }

   public static class Library extends Stronghold {
      private boolean isLargeRoom;

      public Library(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
         this.isLargeRoom = box.getYSize() > 6;
      }


      public static Library createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -4, -1, 0, 14, 11, 15, enumfacing);
         if (!canStrongholdGoDeeper(box) || Component.findIntersecting(list, box) != null) {
            box = SBox.getComponentToAddBoundingBox(i, j, k, -4, -1, 0, 14, 6, 15, enumfacing);
            if (!canStrongholdGoDeeper(box) || Component.findIntersecting(list, box) != null) {
               return null;
            }
         }

         return new Library(l, random, box, enumfacing);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            int i = 11;
            if (!this.isLargeRoom) {
               i = 6;
            }

            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 13, i - 1, 14, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 4, 1, 0);
            this.generateMaybeBox(
               world, box, random, 0.07F, 2, 1, 1, 11, 4, 13, Blocks.COBWEB.defaultBlockState(), Blocks.COBWEB.defaultBlockState(), false, 0
            );
            int j = 1;
            int k = 12;

            for (int l = 1; l <= 13; l++) {
               if ((l - 1) % 4 == 0) {
                  this.fillWithBlocks(world, box, 1, 1, l, 1, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                  this.fillWithBlocks(world, box, 12, 1, l, 12, 4, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                  this.setBlockState(world, torch(Direction.EAST), 2, 3, l, box);
                  this.setBlockState(world, torch(Direction.WEST), 11, 3, l, box);
                  if (this.isLargeRoom) {
                     this.fillWithBlocks(world, box, 1, 6, l, 1, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
                     this.fillWithBlocks(
                        world, box, 12, 6, l, 12, 9, l, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false
                     );
                  }
               } else {
                  this.fillWithBlocks(
                     world, box, 1, 1, l, 1, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
                  );
                  this.fillWithBlocks(
                     world, box, 12, 1, l, 12, 4, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
                  );
                  if (this.isLargeRoom) {
                     this.fillWithBlocks(
                        world, box, 1, 6, l, 1, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
                     );
                     this.fillWithBlocks(
                        world, box, 12, 6, l, 12, 9, l, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
                     );
                  }
               }
            }

            for (int k1 = 3; k1 < 12; k1 += 2) {
               this.fillWithBlocks(
                  world, box, 3, 1, k1, 4, 3, k1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
               );
               this.fillWithBlocks(
                  world, box, 6, 1, k1, 7, 3, k1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
               );
               this.fillWithBlocks(
                  world, box, 9, 1, k1, 10, 3, k1, Blocks.BOOKSHELF.defaultBlockState(), Blocks.BOOKSHELF.defaultBlockState(), false
               );
            }

            if (this.isLargeRoom) {
               this.fillWithBlocks(world, box, 1, 5, 1, 3, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.fillWithBlocks(world, box, 10, 5, 1, 12, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.fillWithBlocks(world, box, 4, 5, 1, 9, 5, 2, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.fillWithBlocks(world, box, 4, 5, 12, 9, 5, 13, Blocks.OAK_PLANKS.defaultBlockState(), Blocks.OAK_PLANKS.defaultBlockState(), false);
               this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 11, box);
               this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 8, 5, 11, box);
               this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 9, 5, 10, box);
               this.fillWithBlocks(
                  world, box, 3, 6, 2, 3, 6, 12, Blocks.OAK_FENCE.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), false
               );
               this.fillWithBlocks(
                  world, box, 10, 6, 2, 10, 6, 10, Blocks.OAK_FENCE.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), false
               );
               this.fillWithBlocks(world, box, 4, 6, 2, 9, 6, 2, Blocks.OAK_FENCE.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), false);
               this.fillWithBlocks(
                  world, box, 4, 6, 12, 8, 6, 12, Blocks.OAK_FENCE.defaultBlockState(), Blocks.OAK_FENCE.defaultBlockState(), false
               );
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 9, 6, 11, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 8, 6, 11, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 9, 6, 10, box);
               BlockState iblockstate1 = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
               this.setBlockState(world, iblockstate1, 10, 1, 13, box);
               this.setBlockState(world, iblockstate1, 10, 2, 13, box);
               this.setBlockState(world, iblockstate1, 10, 3, 13, box);
               this.setBlockState(world, iblockstate1, 10, 4, 13, box);
               this.setBlockState(world, iblockstate1, 10, 5, 13, box);
               this.setBlockState(world, iblockstate1, 10, 6, 13, box);
               this.setBlockState(world, iblockstate1, 10, 7, 13, box);
               int i1 = 7;
               int j1 = 7;
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 6, 9, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 7, 9, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 6, 8, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 7, 8, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 6, 7, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 7, 7, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 5, 7, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 8, 7, 7, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 6, 7, 6, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 6, 7, 8, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 7, 7, 6, box);
               this.setBlockState(world, Blocks.OAK_FENCE.defaultBlockState(), 7, 7, 8, box);
               BlockState iblockstate = torch(Direction.UP);
               this.setBlockState(world, iblockstate, 5, 8, 7, box);
               this.setBlockState(world, iblockstate, 8, 8, 7, box);
               this.setBlockState(world, iblockstate, 6, 8, 6, box);
               this.setBlockState(world, iblockstate, 6, 8, 8, box);
               this.setBlockState(world, iblockstate, 7, 8, 6, box);
               this.setBlockState(world, iblockstate, 7, 8, 8, box);
            }

            this.generateChest(world, box, random, 3, 3, 5, BuiltInLootTables.STRONGHOLD_LIBRARY);
            if (this.isLargeRoom) {
               this.setBlockState(world, Blocks.AIR.defaultBlockState(), 12, 9, 1, box);
               this.generateChest(world, box, random, 12, 8, 1, BuiltInLootTables.STRONGHOLD_LIBRARY);
            }

            return true;
         }
      }
   }

   static class PieceWeight {
      public Class<? extends Stronghold> pieceClass;
      public final int pieceWeight;
      public int instancesSpawned;
      public int instancesLimit;

      public PieceWeight(Class<? extends Stronghold> oclass, int i, int j) {
         this.pieceClass = oclass;
         this.pieceWeight = i;
         this.instancesLimit = j;
      }

      public boolean canSpawnMoreStructuresOfType(int i) {
         return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
      }

      public boolean canSpawnMoreStructures() {
         return this.instancesLimit == 0 || this.instancesSpawned < this.instancesLimit;
      }
   }

   public static class PortalRoom extends Stronghold {
      private boolean hasSpawner;

      public PortalRoom(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.boundingBox = box;
      }


      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         if (structurecomponent != null) {
            ((Stairs2)structurecomponent).strongholdPortalRoom = this;
         }
      }

      public static PortalRoom createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -4, -1, 0, 11, 8, 16, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new PortalRoom(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 10, 7, 15, false, random, STRONGHOLD_STONES);
         this.placeDoor(world, random, box, Stronghold.Door.GRATES, 4, 1, 0);
         int i = 6;
         this.fillWithRandomizedBlocks(world, box, 1, i, 1, 1, i, 14, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 9, i, 1, 9, i, 14, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 2, i, 1, 8, i, 2, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 2, i, 14, 8, i, 14, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 1, 1, 1, 2, 1, 4, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 8, 1, 1, 9, 1, 4, false, random, STRONGHOLD_STONES);
         this.fillWithBlocks(world, box, 1, 1, 1, 1, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.fillWithBlocks(world, box, 9, 1, 1, 9, 1, 3, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false);
         this.fillWithRandomizedBlocks(world, box, 3, 1, 8, 7, 1, 12, false, random, STRONGHOLD_STONES);
         this.fillWithBlocks(
            world, box, 4, 1, 9, 6, 1, 11, Blocks.LAVA.defaultBlockState(), Blocks.LAVA.defaultBlockState(), false
         );

         for (int j = 3; j < 14; j += 2) {
            this.fillWithBlocks(world, box, 0, 3, j, 0, 4, j, Blocks.IRON_BARS.defaultBlockState(), Blocks.IRON_BARS.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 10, 3, j, 10, 4, j, Blocks.IRON_BARS.defaultBlockState(), Blocks.IRON_BARS.defaultBlockState(), false);
         }

         for (int i1 = 2; i1 < 9; i1 += 2) {
            this.fillWithBlocks(
               world, box, i1, 3, 15, i1, 4, 15, Blocks.IRON_BARS.defaultBlockState(), Blocks.IRON_BARS.defaultBlockState(), false
            );
         }

         BlockState iblockstate3 = Blocks.STONE_BRICK_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
         this.fillWithRandomizedBlocks(world, box, 4, 1, 5, 6, 1, 7, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 4, 2, 6, 6, 2, 7, false, random, STRONGHOLD_STONES);
         this.fillWithRandomizedBlocks(world, box, 4, 3, 7, 6, 3, 7, false, random, STRONGHOLD_STONES);

         for (int k = 4; k <= 6; k++) {
            this.setBlockState(world, iblockstate3, k, 1, 4, box);
            this.setBlockState(world, iblockstate3, k, 2, 5, box);
            this.setBlockState(world, iblockstate3, k, 3, 6, box);
         }

         BlockState iblockstate4 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.NORTH);
         BlockState iblockstate = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.SOUTH);
         BlockState iblockstate1 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.EAST);
         BlockState iblockstate2 = Blocks.END_PORTAL_FRAME.defaultBlockState().setValue(EndPortalFrameBlock.FACING, Direction.WEST);
         boolean flag = true;
         boolean[] aboolean = new boolean[12];

         for (int l = 0; l < aboolean.length; l++) {
            aboolean[l] = random.nextFloat() > 0.9F;
            flag &= aboolean[l];
         }

         this.setBlockState(world, iblockstate4.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[0]), 4, 3, 8, box);
         this.setBlockState(world, iblockstate4.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[1]), 5, 3, 8, box);
         this.setBlockState(world, iblockstate4.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[2]), 6, 3, 8, box);
         this.setBlockState(world, iblockstate.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[3]), 4, 3, 12, box);
         this.setBlockState(world, iblockstate.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[4]), 5, 3, 12, box);
         this.setBlockState(world, iblockstate.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[5]), 6, 3, 12, box);
         this.setBlockState(world, iblockstate1.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[6]), 3, 3, 9, box);
         this.setBlockState(world, iblockstate1.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[7]), 3, 3, 10, box);
         this.setBlockState(world, iblockstate1.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[8]), 3, 3, 11, box);
         this.setBlockState(world, iblockstate2.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[9]), 7, 3, 9, box);
         this.setBlockState(world, iblockstate2.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[10]), 7, 3, 10, box);
         this.setBlockState(world, iblockstate2.setValue(EndPortalFrameBlock.HAS_EYE, aboolean[11]), 7, 3, 11, box);
         if (flag) {
            BlockState iblockstate5 = Blocks.END_PORTAL.defaultBlockState();
            this.setBlockState(world, iblockstate5, 4, 3, 9, box);
            this.setBlockState(world, iblockstate5, 5, 3, 9, box);
            this.setBlockState(world, iblockstate5, 6, 3, 9, box);
            this.setBlockState(world, iblockstate5, 4, 3, 10, box);
            this.setBlockState(world, iblockstate5, 5, 3, 10, box);
            this.setBlockState(world, iblockstate5, 6, 3, 10, box);
            this.setBlockState(world, iblockstate5, 4, 3, 11, box);
            this.setBlockState(world, iblockstate5, 5, 3, 11, box);
            this.setBlockState(world, iblockstate5, 6, 3, 11, box);
         }

         if (!this.hasSpawner) {
            i = this.getYWithOffset(3);
            BlockPos blockpos = new BlockPos(this.getXWithOffset(5, 6), i, this.getZWithOffset(5, 6));
            if (box.isVecInside(blockpos)) {
               this.hasSpawner = true;
               place(world, blockpos, Blocks.SPAWNER.defaultBlockState());
               if (world.level.getBlockEntity(blockpos) instanceof SpawnerBlockEntity spawner) {
                  spawner.setEntityId(EntityType.SILVERFISH, RandomSource.create(0L));
               }
            }
         }

         return true;
      }
   }

   public static class Prison extends Stronghold {

      public Prison(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
      }

      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 1, 1);
      }

      public static Prison createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 9, 5, 11, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new Prison(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 8, 4, 10, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 1, 0);
            this.fillWithBlocks(world, box, 1, 1, 10, 3, 3, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.fillWithRandomizedBlocks(world, box, 4, 1, 1, 4, 3, 1, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 4, 1, 3, 4, 3, 3, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 4, 1, 7, 4, 3, 7, false, random, STRONGHOLD_STONES);
            this.fillWithRandomizedBlocks(world, box, 4, 1, 9, 4, 3, 9, false, random, STRONGHOLD_STONES);
            this.fillWithBlocks(world, box, 4, 1, 4, 4, 3, 6, Blocks.IRON_BARS.defaultBlockState(), Blocks.IRON_BARS.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 5, 1, 5, 7, 3, 5, Blocks.IRON_BARS.defaultBlockState(), Blocks.IRON_BARS.defaultBlockState(), false);
            this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), 4, 3, 2, box);
            this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), 4, 3, 8, box);
            BlockState iblockstate = Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST);
            BlockState iblockstate1 = Blocks.IRON_DOOR
               .defaultBlockState()
               .setValue(DoorBlock.FACING, Direction.WEST)
               .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
            this.setBlockState(world, iblockstate, 4, 1, 2, box);
            this.setBlockState(world, iblockstate1, 4, 2, 2, box);
            this.setBlockState(world, iblockstate, 4, 1, 8, box);
            this.setBlockState(world, iblockstate1, 4, 2, 8, box);
            return true;
         }
      }
   }

   /** Never built: RightTurn.createPiece is LeftTurn's static createPiece, so 1.12 "right turns" are left turns. */
   public static class RightTurn extends LeftTurn {
      private RightTurn(int i, Random random, SBox box, Direction facing) {
         super(i, random, box, facing);
      }

      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         Direction enumfacing = this.getCoordBaseMode();
         if (enumfacing != Direction.NORTH && enumfacing != Direction.EAST) {
            this.getNextComponentX((Stairs2)structurecomponent, list, random, 1, 1);
         } else {
            this.getNextComponentZ((Stairs2)structurecomponent, list, random, 1, 1);
         }
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 4, 4, 4, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 1, 0);
            Direction enumfacing = this.getCoordBaseMode();
            if (enumfacing != Direction.NORTH && enumfacing != Direction.EAST) {
               this.fillWithBlocks(world, box, 0, 1, 1, 0, 3, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            } else {
               this.fillWithBlocks(world, box, 4, 1, 1, 4, 3, 3, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            return true;
         }
      }
   }

   public static class RoomCrossing extends Stronghold {
      protected int roomType;

      public RoomCrossing(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
         this.roomType = random.nextInt(5);
      }


      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 4, 1);
         this.getNextComponentX((Stairs2)structurecomponent, list, random, 1, 4);
         this.getNextComponentZ((Stairs2)structurecomponent, list, random, 1, 4);
      }

      public static RoomCrossing createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -4, -1, 0, 11, 7, 11, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new RoomCrossing(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 10, 6, 10, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 4, 1, 0);
            this.fillWithBlocks(world, box, 4, 1, 10, 6, 3, 10, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 0, 1, 4, 0, 3, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            this.fillWithBlocks(world, box, 10, 1, 4, 10, 3, 6, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            switch (this.roomType) {
               case 0:
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, box);
                  this.setBlockState(world, torch(Direction.WEST), 4, 3, 5, box);
                  this.setBlockState(world, torch(Direction.EAST), 6, 3, 5, box);
                  this.setBlockState(world, torch(Direction.SOUTH), 5, 3, 4, box);
                  this.setBlockState(world, torch(Direction.NORTH), 5, 3, 6, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 4, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 5, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 4, 1, 6, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 4, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 5, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 6, 1, 6, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 4, box);
                  this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 5, 1, 6, box);
                  break;
               case 1:
                  for (int i1 = 0; i1 < 5; i1++) {
                     this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 1, 3 + i1, box);
                     this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 7, 1, 3 + i1, box);
                     this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i1, 1, 3, box);
                     this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3 + i1, 1, 7, box);
                  }

                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 5, 1, 5, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 5, 2, 5, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 5, 3, 5, box);
                  this.setBlockState(world, Blocks.WATER.defaultBlockState(), 5, 4, 5, box);
                  break;
               case 2:
                  for (int i = 1; i <= 9; i++) {
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 1, 3, i, box);
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 9, 3, i, box);
                  }

                  for (int j = 1; j <= 9; j++) {
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), j, 3, 1, box);
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), j, 3, 9, box);
                  }

                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 4, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 5, 1, 6, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 4, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 5, 3, 6, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 4, 1, 5, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 6, 1, 5, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 4, 3, 5, box);
                  this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 6, 3, 5, box);

                  for (int k = 1; k <= 3; k++) {
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 4, k, 4, box);
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 6, k, 4, box);
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 4, k, 6, box);
                     this.setBlockState(world, Blocks.COBBLESTONE.defaultBlockState(), 6, k, 6, box);
                  }

                  this.setBlockState(world, Blocks.TORCH.defaultBlockState(), 5, 3, 5, box);

                  for (int l = 2; l <= 8; l++) {
                     this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 2, 3, l, box);
                     this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 3, 3, l, box);
                     if (l <= 3 || l >= 7) {
                        this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 4, 3, l, box);
                        this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 5, 3, l, box);
                        this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 6, 3, l, box);
                     }

                     this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 7, 3, l, box);
                     this.setBlockState(world, Blocks.OAK_PLANKS.defaultBlockState(), 8, 3, l, box);
                  }

                  BlockState iblockstate = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST);
                  this.setBlockState(world, iblockstate, 9, 1, 3, box);
                  this.setBlockState(world, iblockstate, 9, 2, 3, box);
                  this.setBlockState(world, iblockstate, 9, 3, 3, box);
                  this.generateChest(world, box, random, 3, 4, 8, BuiltInLootTables.STRONGHOLD_CROSSING);
            }

            return true;
         }
      }
   }

   public static class Stairs extends Stronghold {
      private boolean source;

      public Stairs(int i, Random random, int j, int k) {
         super(i);
         this.source = true;
         this.setCoordBaseMode(HORIZONTAL[random.nextInt(HORIZONTAL.length)]);
         this.entryDoor = Stronghold.Door.OPENING;
         if (this.getCoordBaseMode().getAxis() == Direction.Axis.Z) {
            this.boundingBox = new SBox(j, 64, k, j + 5 - 1, 74, k + 5 - 1);
         } else {
            this.boundingBox = new SBox(j, 64, k, j + 5 - 1, 74, k + 5 - 1);
         }
      }

      public Stairs(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.source = false;
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
      }


      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         if (this.source) {
            strongComponentType = Crossing.class;
         }

         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 1, 1);
      }

      public static Stairs createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -7, 0, 5, 11, 5, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new Stairs(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 4, 10, 4, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 7, 0);
            this.placeDoor(world, random, box, Stronghold.Door.OPENING, 1, 1, 4);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 2, 6, 1, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 1, box);
            this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 6, 1, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5, 2, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 4, 3, box);
            this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 5, 3, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 2, 4, 3, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 3, box);
            this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 4, 3, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 3, 2, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 2, 1, box);
            this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 3, 3, 1, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 2, 2, 1, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 1, box);
            this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 2, 1, box);
            this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 1, 2, box);
            this.setBlockState(world, Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 1, 1, 3, box);
            return true;
         }
      }
   }

   public static class Stairs2 extends Stairs {
      public PieceWeight lastPlaced;
      public PortalRoom strongholdPortalRoom;
      public List<Component> pendingChildren = new ArrayList<>();

      public Stairs2(int i, Random random, int j, int k) {
         super(0, random, j, k);
      }
   }

   public static class StairsStraight extends Stronghold {

      public StairsStraight(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
      }

      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 1, 1);
      }

      public static StairsStraight createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -7, 0, 5, 11, 8, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new StairsStraight(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 4, 10, 7, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 7, 0);
            this.placeDoor(world, random, box, Stronghold.Door.OPENING, 1, 1, 7);
            BlockState iblockstate = Blocks.COBBLESTONE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);

            for (int i = 0; i < 6; i++) {
               this.setBlockState(world, iblockstate, 1, 6 - i, 1 + i, box);
               this.setBlockState(world, iblockstate, 2, 6 - i, 1 + i, box);
               this.setBlockState(world, iblockstate, 3, 6 - i, 1 + i, box);
               if (i < 5) {
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 1, 5 - i, 1 + i, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 2, 5 - i, 1 + i, box);
                  this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), 3, 5 - i, 1 + i, box);
               }
            }

            return true;
         }
      }
   }

   /** StructureStrongholdPieces.Stones */
   private static BlockState stones(Random random, int i, int j, int k, boolean wall) {
      if (wall) {
         float f = random.nextFloat();
         if (f < 0.2F) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
         } else if (f < 0.5F) {
            return Blocks.MOSSY_STONE_BRICKS.defaultBlockState();
         } else if (f < 0.55F) {
            return Blocks.INFESTED_STONE_BRICKS.defaultBlockState();
         } else {
            return Blocks.STONE_BRICKS.defaultBlockState();
         }
      } else {
         return Blocks.AIR.defaultBlockState();
      }
   }

   public static class Straight extends Stronghold {
      private boolean expandsX;
      private boolean expandsZ;

      public Straight(int i, Random random, SBox box, Direction enumfacing) {
         super(i);
         this.setCoordBaseMode(enumfacing);
         this.entryDoor = this.getRandomDoor(random);
         this.boundingBox = box;
         this.expandsX = random.nextInt(2) == 0;
         this.expandsZ = random.nextInt(2) == 0;
      }


      @Override
      public void buildComponent(Component structurecomponent, List<Component> list, Random random) {
         this.getNextComponentNormal((Stairs2)structurecomponent, list, random, 1, 1);
         if (this.expandsX) {
            this.getNextComponentX((Stairs2)structurecomponent, list, random, 1, 2);
         }

         if (this.expandsZ) {
            this.getNextComponentZ((Stairs2)structurecomponent, list, random, 1, 2);
         }
      }

      public static Straight createPiece(
         List<Component> list, Random random, int i, int j, int k, Direction enumfacing, int l
      ) {
         SBox box = SBox.getComponentToAddBoundingBox(i, j, k, -1, -1, 0, 5, 5, 7, enumfacing);
         return canStrongholdGoDeeper(box) && Component.findIntersecting(list, box) == null
            ? new Straight(l, random, box, enumfacing)
            : null;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, SBox box) {
         if (this.isLiquidInStructureBoundingBox(world, box)) {
            return false;
         } else {
            this.fillWithRandomizedBlocks(world, box, 0, 0, 0, 4, 4, 6, true, random, STRONGHOLD_STONES);
            this.placeDoor(world, random, box, this.entryDoor, 1, 1, 0);
            this.placeDoor(world, random, box, Stronghold.Door.OPENING, 1, 1, 6);
            BlockState iblockstate = torch(Direction.EAST);
            BlockState iblockstate1 = torch(Direction.WEST);
            this.randomlyPlaceBlock(world, box, random, 0.1F, 1, 2, 1, iblockstate);
            this.randomlyPlaceBlock(world, box, random, 0.1F, 3, 2, 1, iblockstate1);
            this.randomlyPlaceBlock(world, box, random, 0.1F, 1, 2, 5, iblockstate);
            this.randomlyPlaceBlock(world, box, random, 0.1F, 3, 2, 5, iblockstate1);
            if (this.expandsX) {
               this.fillWithBlocks(world, box, 0, 1, 2, 0, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            if (this.expandsZ) {
               this.fillWithBlocks(world, box, 4, 1, 2, 4, 3, 4, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false);
            }

            return true;
         }
      }
   }

   abstract static class Stronghold extends Component {
      protected Stronghold.Door entryDoor = Stronghold.Door.OPENING;

      protected Stronghold(int i) {
         super(i);
      }


      protected void placeDoor(
         DWorld world,
         Random random,
         SBox box,
         Stronghold.Door stronghold$door,
         int i,
         int j,
         int k
      ) {
         switch (stronghold$door) {
            case OPENING:
               this.fillWithBlocks(
                  world, box, i, j, k, i + 3 - 1, j + 3 - 1, k, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState(), false
               );
               break;
            case WOOD_DOOR:
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i, j, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 1, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 2, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, j + 2, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 2, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 1, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j, k, box);
               this.setBlockState(world, Blocks.OAK_DOOR.defaultBlockState(), i + 1, j, k, box);
               this.setBlockState(
                  world, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, j + 1, k, box
               );
               break;
            case GRATES:
               this.setBlockState(world, Blocks.AIR.defaultBlockState(), i + 1, j, k, box);
               this.setBlockState(world, Blocks.AIR.defaultBlockState(), i + 1, j + 1, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i, j, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i, j + 1, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i, j + 2, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i + 1, j + 2, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i + 2, j + 2, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i + 2, j + 1, k, box);
               this.setBlockState(world, Blocks.IRON_BARS.defaultBlockState(), i + 2, j, k, box);
               break;
            case IRON_DOOR:
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i, j, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 1, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i, j + 2, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 1, j + 2, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 2, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j + 1, k, box);
               this.setBlockState(world, Blocks.STONE_BRICKS.defaultBlockState(), i + 2, j, k, box);
               this.setBlockState(world, Blocks.IRON_DOOR.defaultBlockState(), i + 1, j, k, box);
               this.setBlockState(
                  world, Blocks.IRON_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), i + 1, j + 1, k, box
               );
               this.setBlockState(
                  world, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.NORTH), i + 2, j + 1, k + 1, box
               );
               this.setBlockState(
                  world, Blocks.STONE_BUTTON.defaultBlockState().setValue(ButtonBlock.FACING, Direction.SOUTH), i + 2, j + 1, k - 1, box
               );
         }
      }

      protected Stronghold.Door getRandomDoor(Random random) {
         int i = random.nextInt(5);
         switch (i) {
            case 0:
            case 1:
            default:
               return Stronghold.Door.OPENING;
            case 2:
               return Stronghold.Door.WOOD_DOOR;
            case 3:
               return Stronghold.Door.GRATES;
            case 4:
               return Stronghold.Door.IRON_DOOR;
         }
      }

      @Nullable
      protected Component getNextComponentNormal(
         Stairs2 stairs2, List<Component> list, Random random, int i, int j
      ) {
         Direction enumfacing = this.getCoordBaseMode();
         if (enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX + i,
                     this.boundingBox.minY + j,
                     this.boundingBox.minZ - 1,
                     enumfacing,
                     this.getComponentType()
                  );
               case SOUTH:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX + i,
                     this.boundingBox.minY + j,
                     this.boundingBox.maxZ + 1,
                     enumfacing,
                     this.getComponentType()
                  );
               case WEST:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX - 1,
                     this.boundingBox.minY + j,
                     this.boundingBox.minZ + i,
                     enumfacing,
                     this.getComponentType()
                  );
               case EAST:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.maxX + 1,
                     this.boundingBox.minY + j,
                     this.boundingBox.minZ + i,
                     enumfacing,
                     this.getComponentType()
                  );
            }
         }

         return null;
      }

      @Nullable
      protected Component getNextComponentX(
         Stairs2 stairs2, List<Component> list, Random random, int i, int j
      ) {
         Direction enumfacing = this.getCoordBaseMode();
         if (enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX - 1,
                     this.boundingBox.minY + i,
                     this.boundingBox.minZ + j,
                     Direction.WEST,
                     this.getComponentType()
                  );
               case SOUTH:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX - 1,
                     this.boundingBox.minY + i,
                     this.boundingBox.minZ + j,
                     Direction.WEST,
                     this.getComponentType()
                  );
               case WEST:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX + j,
                     this.boundingBox.minY + i,
                     this.boundingBox.minZ - 1,
                     Direction.NORTH,
                     this.getComponentType()
                  );
               case EAST:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX + j,
                     this.boundingBox.minY + i,
                     this.boundingBox.minZ - 1,
                     Direction.NORTH,
                     this.getComponentType()
                  );
            }
         }

         return null;
      }

      @Nullable
      protected Component getNextComponentZ(
         Stairs2 stairs2, List<Component> list, Random random, int i, int j
      ) {
         Direction enumfacing = this.getCoordBaseMode();
         if (enumfacing != null) {
            switch (enumfacing) {
               case NORTH:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.maxX + 1,
                     this.boundingBox.minY + i,
                     this.boundingBox.minZ + j,
                     Direction.EAST,
                     this.getComponentType()
                  );
               case SOUTH:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.maxX + 1,
                     this.boundingBox.minY + i,
                     this.boundingBox.minZ + j,
                     Direction.EAST,
                     this.getComponentType()
                  );
               case WEST:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX + j,
                     this.boundingBox.minY + i,
                     this.boundingBox.maxZ + 1,
                     Direction.SOUTH,
                     this.getComponentType()
                  );
               case EAST:
                  return generateAndAddPiece(
                     stairs2,
                     list,
                     random,
                     this.boundingBox.minX + j,
                     this.boundingBox.minY + i,
                     this.boundingBox.maxZ + 1,
                     Direction.SOUTH,
                     this.getComponentType()
                  );
            }
         }

         return null;
      }

      protected static boolean canStrongholdGoDeeper(SBox box) {
         return box != null && box.minY > 10;
      }

      public static enum Door {
         OPENING,
         WOOD_DOOR,
         GRATES,
         IRON_DOOR;
      }
   }
}
