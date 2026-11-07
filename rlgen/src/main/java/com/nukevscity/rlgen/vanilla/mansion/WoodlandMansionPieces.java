package com.nukevscity.rlgen.vanilla.mansion;

import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

/**
 * 1.12.2 WoodlandMansionPieces: the grid, the floor rooms and the Placer, unchanged. MansionTemplate (its own file) is the 1.12
 * piece; the Placer only needs each template's size.
 */
public class WoodlandMansionPieces {
   /** Template.getZeroPositionWithTransform(pos, mirror, rotation, sizeX, sizeZ) */
   static BlockPos zeroPositionWithTransform(BlockPos blockpos, Mirror mirror, Rotation rotation, int i, int j) {
      i--;
      j--;
      int k = mirror == Mirror.FRONT_BACK ? i : 0;
      int l = mirror == Mirror.LEFT_RIGHT ? j : 0;
      BlockPos blockpos1 = blockpos;
      switch (rotation) {
         case COUNTERCLOCKWISE_90:
            blockpos1 = blockpos.offset(l, 0, i - k);
            break;
         case CLOCKWISE_90:
            blockpos1 = blockpos.offset(j - l, 0, k);
            break;
         case CLOCKWISE_180:
            blockpos1 = blockpos.offset(i - k, 0, j - l);
            break;
         case NONE:
            blockpos1 = blockpos.offset(k, 0, l);
      }

      return blockpos1;
   }

   public static void generateMansion(
      MansionTemplate.Sizes templatemanager, BlockPos blockpos, Rotation rotation, List<MansionTemplate> list, Random random
   ) {
      Grid grid = new Grid(random);
      Placer placer = new Placer(templatemanager, random);
      placer.createMansion(blockpos, rotation, list, grid);
   }

   static class FirstFloor extends RoomCollection {
      private FirstFloor() {
      }

      @Override
      public String get1x1(Random random) {
         return "1x1_a" + (random.nextInt(5) + 1);
      }

      @Override
      public String get1x1Secret(Random random) {
         return "1x1_as" + (random.nextInt(4) + 1);
      }

      @Override
      public String get1x2SideEntrance(Random random, boolean flag) {
         return "1x2_a" + (random.nextInt(9) + 1);
      }

      @Override
      public String get1x2FrontEntrance(Random random, boolean flag) {
         return "1x2_b" + (random.nextInt(5) + 1);
      }

      @Override
      public String get1x2Secret(Random random) {
         return "1x2_s" + (random.nextInt(2) + 1);
      }

      @Override
      public String get2x2(Random random) {
         return "2x2_a" + (random.nextInt(4) + 1);
      }

      @Override
      public String get2x2Secret(Random random) {
         return "2x2_s1";
      }
   }

   static class Grid {
      private final Random random;
      private final SimpleGrid baseGrid;
      private final SimpleGrid thirdFloorGrid;
      private final SimpleGrid[] floorRooms;
      private final int entranceX;
      private final int entranceY;

      public Grid(Random random) {
         this.random = random;
         int i = 11;
         this.entranceX = 7;
         this.entranceY = 4;
         this.baseGrid = new SimpleGrid(11, 11, 5);
         this.baseGrid.set(this.entranceX, this.entranceY, this.entranceX + 1, this.entranceY + 1, 3);
         this.baseGrid.set(this.entranceX - 1, this.entranceY, this.entranceX - 1, this.entranceY + 1, 2);
         this.baseGrid.set(this.entranceX + 2, this.entranceY - 2, this.entranceX + 3, this.entranceY + 3, 5);
         this.baseGrid.set(this.entranceX + 1, this.entranceY - 2, this.entranceX + 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX + 1, this.entranceY + 2, this.entranceX + 1, this.entranceY + 3, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY - 1, 1);
         this.baseGrid.set(this.entranceX - 1, this.entranceY + 2, 1);
         this.baseGrid.set(0, 0, 11, 1, 5);
         this.baseGrid.set(0, 9, 11, 11, 5);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY - 2, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX, this.entranceY + 3, Direction.WEST, 6);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY - 1, Direction.WEST, 3);
         this.recursiveCorridor(this.baseGrid, this.entranceX - 2, this.entranceY + 2, Direction.WEST, 3);

         while (this.cleanEdges(this.baseGrid)) {
         }

         this.floorRooms = new SimpleGrid[3];
         this.floorRooms[0] = new SimpleGrid(11, 11, 5);
         this.floorRooms[1] = new SimpleGrid(11, 11, 5);
         this.floorRooms[2] = new SimpleGrid(11, 11, 5);
         this.identifyRooms(this.baseGrid, this.floorRooms[0]);
         this.identifyRooms(this.baseGrid, this.floorRooms[1]);
         this.floorRooms[0].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.floorRooms[1].set(this.entranceX + 1, this.entranceY, this.entranceX + 1, this.entranceY + 1, 8388608);
         this.thirdFloorGrid = new SimpleGrid(this.baseGrid.width, this.baseGrid.height, 5);
         this.setupThirdFloor();
         this.identifyRooms(this.thirdFloorGrid, this.floorRooms[2]);
      }

      public static boolean isHouse(SimpleGrid simplegrid, int i, int j) {
         int k = simplegrid.get(i, j);
         return k == 1 || k == 2 || k == 3 || k == 4;
      }

      public boolean isRoomId(SimpleGrid simplegrid, int i, int j, int k, int l) {
         return (this.floorRooms[k].get(i, j) & 65535) == l;
      }

      @Nullable
      public Direction get1x2RoomDirection(SimpleGrid simplegrid, int i, int j, int k, int l) {
         for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
            if (this.isRoomId(simplegrid, i + enumfacing.getStepX(), j + enumfacing.getStepZ(), k, l)) {
               return enumfacing;
            }
         }

         return null;
      }

      private void recursiveCorridor(SimpleGrid simplegrid, int i, int j, Direction enumfacing, int k) {
         if (k > 0) {
            simplegrid.set(i, j, 1);
            simplegrid.setIf(i + enumfacing.getStepX(), j + enumfacing.getStepZ(), 0, 1);

            for (int l = 0; l < 8; l++) {
               Direction enumfacing1 = Direction.from2DDataValue(this.random.nextInt(4));
               if (enumfacing1 != enumfacing.getOpposite() && (enumfacing1 != Direction.EAST || !this.random.nextBoolean())) {
                  int i1 = i + enumfacing.getStepX();
                  int j1 = j + enumfacing.getStepZ();
                  if (simplegrid.get(i1 + enumfacing1.getStepX(), j1 + enumfacing1.getStepZ()) == 0
                     && simplegrid.get(i1 + enumfacing1.getStepX() * 2, j1 + enumfacing1.getStepZ() * 2) == 0) {
                     this.recursiveCorridor(
                        simplegrid,
                        i + enumfacing.getStepX() + enumfacing1.getStepX(),
                        j + enumfacing.getStepZ() + enumfacing1.getStepZ(),
                        enumfacing1,
                        k - 1
                     );
                     break;
                  }
               }
            }

            Direction enumfacing2 = enumfacing.getClockWise();
            Direction enumfacing3 = enumfacing.getCounterClockWise();
            simplegrid.setIf(i + enumfacing2.getStepX(), j + enumfacing2.getStepZ(), 0, 2);
            simplegrid.setIf(i + enumfacing3.getStepX(), j + enumfacing3.getStepZ(), 0, 2);
            simplegrid.setIf(
               i + enumfacing.getStepX() + enumfacing2.getStepX(), j + enumfacing.getStepZ() + enumfacing2.getStepZ(), 0, 2
            );
            simplegrid.setIf(
               i + enumfacing.getStepX() + enumfacing3.getStepX(), j + enumfacing.getStepZ() + enumfacing3.getStepZ(), 0, 2
            );
            simplegrid.setIf(i + enumfacing.getStepX() * 2, j + enumfacing.getStepZ() * 2, 0, 2);
            simplegrid.setIf(i + enumfacing2.getStepX() * 2, j + enumfacing2.getStepZ() * 2, 0, 2);
            simplegrid.setIf(i + enumfacing3.getStepX() * 2, j + enumfacing3.getStepZ() * 2, 0, 2);
         }
      }

      private boolean cleanEdges(SimpleGrid simplegrid) {
         boolean flag = false;

         for (int i = 0; i < simplegrid.height; i++) {
            for (int j = 0; j < simplegrid.width; j++) {
               if (simplegrid.get(j, i) == 0) {
                  int k = 0;
                  k += isHouse(simplegrid, j + 1, i) ? 1 : 0;
                  k += isHouse(simplegrid, j - 1, i) ? 1 : 0;
                  k += isHouse(simplegrid, j, i + 1) ? 1 : 0;
                  k += isHouse(simplegrid, j, i - 1) ? 1 : 0;
                  if (k >= 3) {
                     simplegrid.set(j, i, 2);
                     flag = true;
                  } else if (k == 2) {
                     int l = 0;
                     l += isHouse(simplegrid, j + 1, i + 1) ? 1 : 0;
                     l += isHouse(simplegrid, j - 1, i + 1) ? 1 : 0;
                     l += isHouse(simplegrid, j + 1, i - 1) ? 1 : 0;
                     l += isHouse(simplegrid, j - 1, i - 1) ? 1 : 0;
                     if (l <= 1) {
                        simplegrid.set(j, i, 2);
                        flag = true;
                     }
                  }
               }
            }
         }

         return flag;
      }

      private void setupThirdFloor() {
         List<Tuple<Integer, Integer>> list = Lists.newArrayList();
         SimpleGrid simplegrid = this.floorRooms[1];

         for (int i = 0; i < this.thirdFloorGrid.height; i++) {
            for (int j = 0; j < this.thirdFloorGrid.width; j++) {
               int k = simplegrid.get(j, i);
               int l = k & 983040;
               if (l == 131072 && (k & 2097152) == 2097152) {
                  list.add(new Tuple<>(j, i));
               }
            }
         }

         if (list.isEmpty()) {
            this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
         } else {
            Tuple<Integer, Integer> tuple = list.get(this.random.nextInt(list.size()));
            int l1 = simplegrid.get((Integer)tuple.getA(), (Integer)tuple.getB());
            simplegrid.set((Integer)tuple.getA(), (Integer)tuple.getB(), l1 | 4194304);
            Direction enumfacing1 = this.get1x2RoomDirection(this.baseGrid, (Integer)tuple.getA(), (Integer)tuple.getB(), 1, l1 & 65535);
            int i2 = (Integer)tuple.getA() + enumfacing1.getStepX();
            int i1 = (Integer)tuple.getB() + enumfacing1.getStepZ();

            for (int j1 = 0; j1 < this.thirdFloorGrid.height; j1++) {
               for (int k1 = 0; k1 < this.thirdFloorGrid.width; k1++) {
                  if (!isHouse(this.baseGrid, k1, j1)) {
                     this.thirdFloorGrid.set(k1, j1, 5);
                  } else if (k1 == (Integer)tuple.getA() && j1 == (Integer)tuple.getB()) {
                     this.thirdFloorGrid.set(k1, j1, 3);
                  } else if (k1 == i2 && j1 == i1) {
                     this.thirdFloorGrid.set(k1, j1, 3);
                     this.floorRooms[2].set(k1, j1, 8388608);
                  }
               }
            }

            List<Direction> list1 = Lists.newArrayList();

            for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
               if (this.thirdFloorGrid.get(i2 + enumfacing.getStepX(), i1 + enumfacing.getStepZ()) == 0) {
                  list1.add(enumfacing);
               }
            }

            if (list1.isEmpty()) {
               this.thirdFloorGrid.set(0, 0, this.thirdFloorGrid.width, this.thirdFloorGrid.height, 5);
               simplegrid.set((Integer)tuple.getA(), (Integer)tuple.getB(), l1);
            } else {
               Direction enumfacing2 = list1.get(this.random.nextInt(list1.size()));
               this.recursiveCorridor(this.thirdFloorGrid, i2 + enumfacing2.getStepX(), i1 + enumfacing2.getStepZ(), enumfacing2, 4);

               while (this.cleanEdges(this.thirdFloorGrid)) {
               }
            }
         }
      }

      private void identifyRooms(
         SimpleGrid simplegrid, SimpleGrid simplegrid1
      ) {
         List<Tuple<Integer, Integer>> list = Lists.newArrayList();

         for (int i = 0; i < simplegrid.height; i++) {
            for (int j = 0; j < simplegrid.width; j++) {
               if (simplegrid.get(j, i) == 2) {
                  list.add(new Tuple<>(j, i));
               }
            }
         }

         Collections.shuffle(list, this.random);
         int k3 = 10;

         for (Tuple<Integer, Integer> tuple : list) {
            int k = (Integer)tuple.getA();
            int l = (Integer)tuple.getB();
            if (simplegrid1.get(k, l) == 0) {
               int i1 = k;
               int j1 = k;
               int k1 = l;
               int l1 = l;
               int i2 = 65536;
               if (simplegrid1.get(k + 1, l) == 0
                  && simplegrid1.get(k, l + 1) == 0
                  && simplegrid1.get(k + 1, l + 1) == 0
                  && simplegrid.get(k + 1, l) == 2
                  && simplegrid.get(k, l + 1) == 2
                  && simplegrid.get(k + 1, l + 1) == 2) {
                  j1 = k + 1;
                  l1 = l + 1;
                  i2 = 262144;
               } else if (simplegrid1.get(k - 1, l) == 0
                  && simplegrid1.get(k, l + 1) == 0
                  && simplegrid1.get(k - 1, l + 1) == 0
                  && simplegrid.get(k - 1, l) == 2
                  && simplegrid.get(k, l + 1) == 2
                  && simplegrid.get(k - 1, l + 1) == 2) {
                  i1 = k - 1;
                  l1 = l + 1;
                  i2 = 262144;
               } else if (simplegrid1.get(k - 1, l) == 0
                  && simplegrid1.get(k, l - 1) == 0
                  && simplegrid1.get(k - 1, l - 1) == 0
                  && simplegrid.get(k - 1, l) == 2
                  && simplegrid.get(k, l - 1) == 2
                  && simplegrid.get(k - 1, l - 1) == 2) {
                  i1 = k - 1;
                  k1 = l - 1;
                  i2 = 262144;
               } else if (simplegrid1.get(k + 1, l) == 0 && simplegrid.get(k + 1, l) == 2) {
                  j1 = k + 1;
                  i2 = 131072;
               } else if (simplegrid1.get(k, l + 1) == 0 && simplegrid.get(k, l + 1) == 2) {
                  l1 = l + 1;
                  i2 = 131072;
               } else if (simplegrid1.get(k - 1, l) == 0 && simplegrid.get(k - 1, l) == 2) {
                  i1 = k - 1;
                  i2 = 131072;
               } else if (simplegrid1.get(k, l - 1) == 0 && simplegrid.get(k, l - 1) == 2) {
                  k1 = l - 1;
                  i2 = 131072;
               }

               int j2 = this.random.nextBoolean() ? i1 : j1;
               int k2 = this.random.nextBoolean() ? k1 : l1;
               int l2 = 2097152;
               if (!simplegrid.edgesTo(j2, k2, 1)) {
                  j2 = j2 == i1 ? j1 : i1;
                  k2 = k2 == k1 ? l1 : k1;
                  if (!simplegrid.edgesTo(j2, k2, 1)) {
                     k2 = k2 == k1 ? l1 : k1;
                     if (!simplegrid.edgesTo(j2, k2, 1)) {
                        j2 = j2 == i1 ? j1 : i1;
                        k2 = k2 == k1 ? l1 : k1;
                        if (!simplegrid.edgesTo(j2, k2, 1)) {
                           l2 = 0;
                           j2 = i1;
                           k2 = k1;
                        }
                     }
                  }
               }

               for (int i3 = k1; i3 <= l1; i3++) {
                  for (int j3 = i1; j3 <= j1; j3++) {
                     if (j3 == j2 && i3 == k2) {
                        simplegrid1.set(j3, i3, 1048576 | l2 | i2 | k3);
                     } else {
                        simplegrid1.set(j3, i3, i2 | k3);
                     }
                  }
               }

               k3++;
            }
         }
      }
   }


   static class PlacementData {
      public Rotation rotation;
      public BlockPos position;
      public String wallType;

      private PlacementData() {
      }
   }

   static class Placer {
      private final MansionTemplate.Sizes templateManager;
      private final Random random;
      private int startX;
      private int startY;

      public Placer(MansionTemplate.Sizes templatemanager, Random random) {
         this.templateManager = templatemanager;
         this.random = random;
      }

      public void createMansion(
         BlockPos blockpos, Rotation rotation, List<MansionTemplate> list, Grid grid
      ) {
         PlacementData placementdata = new PlacementData();
         placementdata.position = blockpos;
         placementdata.rotation = rotation;
         placementdata.wallType = "wall_flat";
         PlacementData placementdata1 = new PlacementData();
         this.entrance(list, placementdata);
         placementdata1.position = placementdata.position.above(8);
         placementdata1.rotation = placementdata.rotation;
         placementdata1.wallType = "wall_window";
         if (!list.isEmpty()) {
         }

         SimpleGrid simplegrid = grid.baseGrid;
         SimpleGrid simplegrid1 = grid.thirdFloorGrid;
         this.startX = grid.entranceX + 1;
         this.startY = grid.entranceY + 1;
         int i = grid.entranceX + 1;
         int j = grid.entranceY;
         this.traverseOuterWalls(list, placementdata, simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         this.traverseOuterWalls(list, placementdata1, simplegrid, Direction.SOUTH, this.startX, this.startY, i, j);
         PlacementData placementdata2 = new PlacementData();
         placementdata2.position = placementdata.position.above(19);
         placementdata2.rotation = placementdata.rotation;
         placementdata2.wallType = "wall_window";
         boolean flag = false;

         for (int k = 0; k < simplegrid1.height && !flag; k++) {
            for (int l = simplegrid1.width - 1; l >= 0 && !flag; l--) {
               if (Grid.isHouse(simplegrid1, l, k)) {
                  placementdata2.position = placementdata2.position
                     .relative(rotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  placementdata2.position = placementdata2.position
                     .relative(rotation.rotate(Direction.EAST), (l - this.startX) * 8);
                  this.traverseWallPiece(list, placementdata2);
                  this.traverseOuterWalls(list, placementdata2, simplegrid1, Direction.SOUTH, l, k, l, k);
                  flag = true;
               }
            }
         }

         this.createRoof(list, blockpos.above(16), rotation, simplegrid, simplegrid1);
         this.createRoof(list, blockpos.above(27), rotation, simplegrid1, null);
         if (!list.isEmpty()) {
         }

         RoomCollection[] aroomcollection = new RoomCollection[]{
            new FirstFloor(), new SecondFloor(), new ThirdFloor()
         };

         for (int l2 = 0; l2 < 3; l2++) {
            BlockPos blockpos1 = blockpos.above(8 * l2 + (l2 == 2 ? 3 : 0));
            SimpleGrid simplegrid2 = grid.floorRooms[l2];
            SimpleGrid simplegrid3 = l2 == 2 ? simplegrid1 : simplegrid;
            String s = l2 == 0 ? "carpet_south" : "carpet_south_2";
            String s1 = l2 == 0 ? "carpet_west" : "carpet_west_2";

            for (int i1 = 0; i1 < simplegrid3.height; i1++) {
               for (int j1 = 0; j1 < simplegrid3.width; j1++) {
                  if (simplegrid3.get(j1, i1) == 1) {
                     BlockPos blockpos2 = blockpos1.relative(rotation.rotate(Direction.SOUTH), 8 + (i1 - this.startY) * 8);
                     blockpos2 = blockpos2.relative(rotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
                     list.add(new MansionTemplate(this.templateManager, "corridor_floor", blockpos2, rotation));
                     if (simplegrid3.get(j1, i1 - 1) == 1 || (simplegrid2.get(j1, i1 - 1) & 8388608) == 8388608) {
                        list.add(
                           new MansionTemplate(
                              this.templateManager, "carpet_north", blockpos2.relative(rotation.rotate(Direction.EAST), 1).above(), rotation
                           )
                        );
                     }

                     if (simplegrid3.get(j1 + 1, i1) == 1 || (simplegrid2.get(j1 + 1, i1) & 8388608) == 8388608) {
                        list.add(
                           new MansionTemplate(
                              this.templateManager,
                              "carpet_east",
                              blockpos2.relative(rotation.rotate(Direction.SOUTH), 1).relative(rotation.rotate(Direction.EAST), 5).above(),
                              rotation
                           )
                        );
                     }

                     if (simplegrid3.get(j1, i1 + 1) == 1 || (simplegrid2.get(j1, i1 + 1) & 8388608) == 8388608) {
                        list.add(
                           new MansionTemplate(
                              this.templateManager,
                              s,
                              blockpos2.relative(rotation.rotate(Direction.SOUTH), 5).relative(rotation.rotate(Direction.WEST), 1),
                              rotation
                           )
                        );
                     }

                     if (simplegrid3.get(j1 - 1, i1) == 1 || (simplegrid2.get(j1 - 1, i1) & 8388608) == 8388608) {
                        list.add(
                           new MansionTemplate(
                              this.templateManager,
                              s1,
                              blockpos2.relative(rotation.rotate(Direction.WEST), 1).relative(rotation.rotate(Direction.NORTH), 1),
                              rotation
                           )
                        );
                     }
                  }
               }
            }

            String s2 = l2 == 0 ? "indoors_wall" : "indoors_wall_2";
            String s3 = l2 == 0 ? "indoors_door" : "indoors_door_2";
            List<Direction> list1 = Lists.newArrayList();

            for (int k1 = 0; k1 < simplegrid3.height; k1++) {
               for (int l1 = 0; l1 < simplegrid3.width; l1++) {
                  boolean flag1 = l2 == 2 && simplegrid3.get(l1, k1) == 3;
                  if (simplegrid3.get(l1, k1) == 2 || flag1) {
                     int i2 = simplegrid2.get(l1, k1);
                     int j2 = i2 & 983040;
                     int k2 = i2 & 65535;
                     flag1 = flag1 && (i2 & 8388608) == 8388608;
                     list1.clear();
                     if ((i2 & 2097152) == 2097152) {
                        for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                           if (simplegrid3.get(l1 + enumfacing.getStepX(), k1 + enumfacing.getStepZ()) == 1) {
                              list1.add(enumfacing);
                           }
                        }
                     }

                     Direction enumfacing1 = null;
                     if (!list1.isEmpty()) {
                        enumfacing1 = list1.get(this.random.nextInt(list1.size()));
                     } else if ((i2 & 1048576) == 1048576) {
                        enumfacing1 = Direction.UP;
                     }

                     BlockPos blockpos3 = blockpos1.relative(rotation.rotate(Direction.SOUTH), 8 + (k1 - this.startY) * 8);
                     blockpos3 = blockpos3.relative(rotation.rotate(Direction.EAST), -1 + (l1 - this.startX) * 8);
                     if (Grid.isHouse(simplegrid3, l1 - 1, k1)
                        && !grid.isRoomId(simplegrid3, l1 - 1, k1, l2, k2)) {
                        list.add(new MansionTemplate(this.templateManager, enumfacing1 == Direction.WEST ? s3 : s2, blockpos3, rotation));
                     }

                     if (simplegrid3.get(l1 + 1, k1) == 1 && !flag1) {
                        BlockPos blockpos4 = blockpos3.relative(rotation.rotate(Direction.EAST), 8);
                        list.add(new MansionTemplate(this.templateManager, enumfacing1 == Direction.EAST ? s3 : s2, blockpos4, rotation));
                     }

                     if (Grid.isHouse(simplegrid3, l1, k1 + 1)
                        && !grid.isRoomId(simplegrid3, l1, k1 + 1, l2, k2)) {
                        BlockPos blockpos5 = blockpos3.relative(rotation.rotate(Direction.SOUTH), 7);
                        blockpos5 = blockpos5.relative(rotation.rotate(Direction.EAST), 7);
                        list.add(
                           new MansionTemplate(
                              this.templateManager, enumfacing1 == Direction.SOUTH ? s3 : s2, blockpos5, rotation.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     }

                     if (simplegrid3.get(l1, k1 - 1) == 1 && !flag1) {
                        BlockPos blockpos6 = blockpos3.relative(rotation.rotate(Direction.NORTH), 1);
                        blockpos6 = blockpos6.relative(rotation.rotate(Direction.EAST), 7);
                        list.add(
                           new MansionTemplate(
                              this.templateManager, enumfacing1 == Direction.NORTH ? s3 : s2, blockpos6, rotation.getRotated(Rotation.CLOCKWISE_90)
                           )
                        );
                     }

                     if (j2 == 65536) {
                        this.addRoom1x1(list, blockpos3, rotation, enumfacing1, aroomcollection[l2]);
                     } else if (j2 == 131072 && enumfacing1 != null) {
                        Direction enumfacing3 = grid.get1x2RoomDirection(simplegrid3, l1, k1, l2, k2);
                        boolean flag2 = (i2 & 4194304) == 4194304;
                        this.addRoom1x2(list, blockpos3, rotation, enumfacing3, enumfacing1, aroomcollection[l2], flag2);
                     } else if (j2 == 262144 && enumfacing1 != null && enumfacing1 != Direction.UP) {
                        Direction enumfacing2 = enumfacing1.getClockWise();
                        if (!grid.isRoomId(
                           simplegrid3, l1 + enumfacing2.getStepX(), k1 + enumfacing2.getStepZ(), l2, k2
                        )) {
                           enumfacing2 = enumfacing2.getOpposite();
                        }

                        this.addRoom2x2(list, blockpos3, rotation, enumfacing2, enumfacing1, aroomcollection[l2]);
                     } else if (j2 == 262144 && enumfacing1 == Direction.UP) {
                        this.addRoom2x2Secret(list, blockpos3, rotation, aroomcollection[l2]);
                     }
                  }
               }
            }
         }
      }

      private void traverseOuterWalls(
         List<MansionTemplate> list,
         PlacementData placementdata,
         SimpleGrid simplegrid,
         Direction enumfacing,
         int i,
         int j,
         int k,
         int l
      ) {
         int i1 = i;
         int j1 = j;
         Direction enumfacing1 = enumfacing;

         do {
            if (!Grid.isHouse(simplegrid, i1 + enumfacing.getStepX(), j1 + enumfacing.getStepZ())) {
               this.traverseTurn(list, placementdata);
               enumfacing = enumfacing.getClockWise();
               if (i1 != k || j1 != l || enumfacing1 != enumfacing) {
                  this.traverseWallPiece(list, placementdata);
               }
            } else if (Grid.isHouse(simplegrid, i1 + enumfacing.getStepX(), j1 + enumfacing.getStepZ())
               && Grid.isHouse(
                  simplegrid,
                  i1 + enumfacing.getStepX() + enumfacing.getCounterClockWise().getStepX(),
                  j1 + enumfacing.getStepZ() + enumfacing.getCounterClockWise().getStepZ()
               )) {
               this.traverseInnerTurn(list, placementdata);
               i1 += enumfacing.getStepX();
               j1 += enumfacing.getStepZ();
               enumfacing = enumfacing.getCounterClockWise();
            } else {
               i1 += enumfacing.getStepX();
               j1 += enumfacing.getStepZ();
               if (i1 != k || j1 != l || enumfacing1 != enumfacing) {
                  this.traverseWallPiece(list, placementdata);
               }
            }
         } while (i1 != k || j1 != l || enumfacing1 != enumfacing);
      }

      private void createRoof(
         List<MansionTemplate> list,
         BlockPos blockpos,
         Rotation rotation,
         SimpleGrid simplegrid,
         @Nullable SimpleGrid simplegrid1
      ) {
         for (int i = 0; i < simplegrid.height; i++) {
            for (int j = 0; j < simplegrid.width; j++) {
               BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.SOUTH), 8 + (i - this.startY) * 8);
               blockpos1 = blockpos1.relative(rotation.rotate(Direction.EAST), (j - this.startX) * 8);
               boolean flag = simplegrid1 != null && Grid.isHouse(simplegrid1, j, i);
               if (Grid.isHouse(simplegrid, j, i) && !flag) {
                  list.add(new MansionTemplate(this.templateManager, "roof", blockpos1.above(3), rotation));
                  if (!Grid.isHouse(simplegrid, j + 1, i)) {
                     BlockPos blockpos2 = blockpos1.relative(rotation.rotate(Direction.EAST), 6);
                     list.add(new MansionTemplate(this.templateManager, "roof_front", blockpos2, rotation));
                  }

                  if (!Grid.isHouse(simplegrid, j - 1, i)) {
                     BlockPos blockpos6 = blockpos1.relative(rotation.rotate(Direction.EAST), 0);
                     blockpos6 = blockpos6.relative(rotation.rotate(Direction.SOUTH), 7);
                     list.add(new MansionTemplate(this.templateManager, "roof_front", blockpos6, rotation.getRotated(Rotation.CLOCKWISE_180)));
                  }

                  if (!Grid.isHouse(simplegrid, j, i - 1)) {
                     BlockPos blockpos7 = blockpos1.relative(rotation.rotate(Direction.WEST), 1);
                     list.add(
                        new MansionTemplate(this.templateManager, "roof_front", blockpos7, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90))
                     );
                  }

                  if (!Grid.isHouse(simplegrid, j, i + 1)) {
                     BlockPos blockpos8 = blockpos1.relative(rotation.rotate(Direction.EAST), 6);
                     blockpos8 = blockpos8.relative(rotation.rotate(Direction.SOUTH), 6);
                     list.add(new MansionTemplate(this.templateManager, "roof_front", blockpos8, rotation.getRotated(Rotation.CLOCKWISE_90)));
                  }
               }
            }
         }

         if (simplegrid1 != null) {
            for (int k = 0; k < simplegrid.height; k++) {
               for (int i1 = 0; i1 < simplegrid.width; i1++) {
                  BlockPos blockpos4 = blockpos.relative(rotation.rotate(Direction.SOUTH), 8 + (k - this.startY) * 8);
                  blockpos4 = blockpos4.relative(rotation.rotate(Direction.EAST), (i1 - this.startX) * 8);
                  boolean flag1 = Grid.isHouse(simplegrid1, i1, k);
                  if (Grid.isHouse(simplegrid, i1, k) && flag1) {
                     if (!Grid.isHouse(simplegrid, i1 + 1, k)) {
                        BlockPos blockpos9 = blockpos4.relative(rotation.rotate(Direction.EAST), 7);
                        list.add(new MansionTemplate(this.templateManager, "small_wall", blockpos9, rotation));
                     }

                     if (!Grid.isHouse(simplegrid, i1 - 1, k)) {
                        BlockPos blockpos10 = blockpos4.relative(rotation.rotate(Direction.WEST), 1);
                        blockpos10 = blockpos10.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(
                           new MansionTemplate(this.templateManager, "small_wall", blockpos10, rotation.getRotated(Rotation.CLOCKWISE_180))
                        );
                     }

                     if (!Grid.isHouse(simplegrid, i1, k - 1)) {
                        BlockPos blockpos11 = blockpos4.relative(rotation.rotate(Direction.WEST), 0);
                        blockpos11 = blockpos11.relative(rotation.rotate(Direction.NORTH), 1);
                        list.add(
                           new MansionTemplate(this.templateManager, "small_wall", blockpos11, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90))
                        );
                     }

                     if (!Grid.isHouse(simplegrid, i1, k + 1)) {
                        BlockPos blockpos12 = blockpos4.relative(rotation.rotate(Direction.EAST), 6);
                        blockpos12 = blockpos12.relative(rotation.rotate(Direction.SOUTH), 7);
                        list.add(new MansionTemplate(this.templateManager, "small_wall", blockpos12, rotation.getRotated(Rotation.CLOCKWISE_90)));
                     }

                     if (!Grid.isHouse(simplegrid, i1 + 1, k)) {
                        if (!Grid.isHouse(simplegrid, i1, k - 1)) {
                           BlockPos blockpos13 = blockpos4.relative(rotation.rotate(Direction.EAST), 7);
                           blockpos13 = blockpos13.relative(rotation.rotate(Direction.NORTH), 2);
                           list.add(new MansionTemplate(this.templateManager, "small_wall_corner", blockpos13, rotation));
                        }

                        if (!Grid.isHouse(simplegrid, i1, k + 1)) {
                           BlockPos blockpos14 = blockpos4.relative(rotation.rotate(Direction.EAST), 8);
                           blockpos14 = blockpos14.relative(rotation.rotate(Direction.SOUTH), 7);
                           list.add(
                              new MansionTemplate(
                                 this.templateManager, "small_wall_corner", blockpos14, rotation.getRotated(Rotation.CLOCKWISE_90)
                              )
                           );
                        }
                     }

                     if (!Grid.isHouse(simplegrid, i1 - 1, k)) {
                        if (!Grid.isHouse(simplegrid, i1, k - 1)) {
                           BlockPos blockpos15 = blockpos4.relative(rotation.rotate(Direction.WEST), 2);
                           blockpos15 = blockpos15.relative(rotation.rotate(Direction.NORTH), 1);
                           list.add(
                              new MansionTemplate(
                                 this.templateManager, "small_wall_corner", blockpos15, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)
                              )
                           );
                        }

                        if (!Grid.isHouse(simplegrid, i1, k + 1)) {
                           BlockPos blockpos16 = blockpos4.relative(rotation.rotate(Direction.WEST), 1);
                           blockpos16 = blockpos16.relative(rotation.rotate(Direction.SOUTH), 8);
                           list.add(
                              new MansionTemplate(
                                 this.templateManager, "small_wall_corner", blockpos16, rotation.getRotated(Rotation.CLOCKWISE_180)
                              )
                           );
                        }
                     }
                  }
               }
            }
         }

         for (int l = 0; l < simplegrid.height; l++) {
            for (int j1 = 0; j1 < simplegrid.width; j1++) {
               BlockPos blockpos5 = blockpos.relative(rotation.rotate(Direction.SOUTH), 8 + (l - this.startY) * 8);
               blockpos5 = blockpos5.relative(rotation.rotate(Direction.EAST), (j1 - this.startX) * 8);
               boolean flag2 = simplegrid1 != null && Grid.isHouse(simplegrid1, j1, l);
               if (Grid.isHouse(simplegrid, j1, l) && !flag2) {
                  if (!Grid.isHouse(simplegrid, j1 + 1, l)) {
                     BlockPos blockpos17 = blockpos5.relative(rotation.rotate(Direction.EAST), 6);
                     if (!Grid.isHouse(simplegrid, j1, l + 1)) {
                        BlockPos blockpos3 = blockpos17.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(new MansionTemplate(this.templateManager, "roof_corner", blockpos3, rotation));
                     } else if (Grid.isHouse(simplegrid, j1 + 1, l + 1)) {
                        BlockPos blockpos19 = blockpos17.relative(rotation.rotate(Direction.SOUTH), 5);
                        list.add(new MansionTemplate(this.templateManager, "roof_inner_corner", blockpos19, rotation));
                     }

                     if (!Grid.isHouse(simplegrid, j1, l - 1)) {
                        list.add(
                           new MansionTemplate(
                              this.templateManager, "roof_corner", blockpos17, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)
                           )
                        );
                     } else if (Grid.isHouse(simplegrid, j1 + 1, l - 1)) {
                        BlockPos blockpos20 = blockpos5.relative(rotation.rotate(Direction.EAST), 9);
                        blockpos20 = blockpos20.relative(rotation.rotate(Direction.NORTH), 2);
                        list.add(
                           new MansionTemplate(this.templateManager, "roof_inner_corner", blockpos20, rotation.getRotated(Rotation.CLOCKWISE_90))
                        );
                     }
                  }

                  if (!Grid.isHouse(simplegrid, j1 - 1, l)) {
                     BlockPos blockpos18 = blockpos5.relative(rotation.rotate(Direction.EAST), 0);
                     blockpos18 = blockpos18.relative(rotation.rotate(Direction.SOUTH), 0);
                     if (!Grid.isHouse(simplegrid, j1, l + 1)) {
                        BlockPos blockpos21 = blockpos18.relative(rotation.rotate(Direction.SOUTH), 6);
                        list.add(
                           new MansionTemplate(this.templateManager, "roof_corner", blockpos21, rotation.getRotated(Rotation.CLOCKWISE_90))
                        );
                     } else if (Grid.isHouse(simplegrid, j1 - 1, l + 1)) {
                        BlockPos blockpos22 = blockpos18.relative(rotation.rotate(Direction.SOUTH), 8);
                        blockpos22 = blockpos22.relative(rotation.rotate(Direction.WEST), 3);
                        list.add(
                           new MansionTemplate(
                              this.templateManager, "roof_inner_corner", blockpos22, rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)
                           )
                        );
                     }

                     if (!Grid.isHouse(simplegrid, j1, l - 1)) {
                        list.add(
                           new MansionTemplate(this.templateManager, "roof_corner", blockpos18, rotation.getRotated(Rotation.CLOCKWISE_180))
                        );
                     } else if (Grid.isHouse(simplegrid, j1 - 1, l - 1)) {
                        BlockPos blockpos23 = blockpos18.relative(rotation.rotate(Direction.SOUTH), 1);
                        list.add(
                           new MansionTemplate(
                              this.templateManager, "roof_inner_corner", blockpos23, rotation.getRotated(Rotation.CLOCKWISE_180)
                           )
                        );
                     }
                  }
               }
            }
         }
      }

      private void entrance(List<MansionTemplate> list, PlacementData placementdata) {
         Direction enumfacing = placementdata.rotation.rotate(Direction.WEST);
         list.add(
            new MansionTemplate(
               this.templateManager,
               "entrance",
               placementdata.position.relative(enumfacing, 9),
               placementdata.rotation
            )
         );
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.SOUTH), 16);
      }

      private void traverseWallPiece(List<MansionTemplate> list, PlacementData placementdata) {
         list.add(
            new MansionTemplate(
               this.templateManager,
               placementdata.wallType,
               placementdata.position.relative(placementdata.rotation.rotate(Direction.EAST), 7),
               placementdata.rotation
            )
         );
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.SOUTH), 8);
      }

      private void traverseTurn(List<MansionTemplate> list, PlacementData placementdata) {
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.SOUTH), -1);
         list.add(
            new MansionTemplate(
               this.templateManager, "wall_corner", placementdata.position, placementdata.rotation
            )
         );
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.SOUTH), -7);
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.WEST), -6);
         placementdata.rotation = placementdata.rotation.getRotated(Rotation.CLOCKWISE_90);
      }

      private void traverseInnerTurn(List<MansionTemplate> list, PlacementData placementdata) {
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.SOUTH), 6);
         placementdata.position = placementdata.position
            .relative(placementdata.rotation.rotate(Direction.EAST), 8);
         placementdata.rotation = placementdata.rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
      }

      private void addRoom1x1(
         List<MansionTemplate> list,
         BlockPos blockpos,
         Rotation rotation,
         Direction enumfacing,
         RoomCollection roomcollection
      ) {
         Rotation rotation1 = Rotation.NONE;
         String s = roomcollection.get1x1(this.random);
         if (enumfacing != Direction.EAST) {
            if (enumfacing == Direction.NORTH) {
               rotation1 = rotation1.getRotated(Rotation.COUNTERCLOCKWISE_90);
            } else if (enumfacing == Direction.WEST) {
               rotation1 = rotation1.getRotated(Rotation.CLOCKWISE_180);
            } else if (enumfacing == Direction.SOUTH) {
               rotation1 = rotation1.getRotated(Rotation.CLOCKWISE_90);
            } else {
               s = roomcollection.get1x1Secret(this.random);
            }
         }

         BlockPos blockpos1 = zeroPositionWithTransform(new BlockPos(1, 0, 0), Mirror.NONE, rotation1, 7, 7);
         rotation1 = rotation1.getRotated(rotation);
         blockpos1 = blockpos1.rotate(rotation);
         BlockPos blockpos2 = blockpos.offset(blockpos1.getX(), 0, blockpos1.getZ());
         list.add(new MansionTemplate(this.templateManager, s, blockpos2, rotation1));
      }

      private void addRoom1x2(
         List<MansionTemplate> list,
         BlockPos blockpos,
         Rotation rotation,
         Direction enumfacing,
         Direction enumfacing1,
         RoomCollection roomcollection,
         boolean flag
      ) {
         if (enumfacing1 == Direction.EAST && enumfacing == Direction.SOUTH) {
            BlockPos blockpos14 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            list.add(
               new MansionTemplate(
                  this.templateManager, roomcollection.get1x2SideEntrance(this.random, flag), blockpos14, rotation
               )
            );
         } else if (enumfacing1 == Direction.EAST && enumfacing == Direction.NORTH) {
            BlockPos blockpos13 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos13 = blockpos13.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(
               new MansionTemplate(
                  this.templateManager, roomcollection.get1x2SideEntrance(this.random, flag), blockpos13, rotation, Mirror.LEFT_RIGHT
               )
            );
         } else if (enumfacing1 == Direction.WEST && enumfacing == Direction.NORTH) {
            BlockPos blockpos12 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            blockpos12 = blockpos12.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2SideEntrance(this.random, flag),
                  blockpos12,
                  rotation.getRotated(Rotation.CLOCKWISE_180)
               )
            );
         } else if (enumfacing1 == Direction.WEST && enumfacing == Direction.SOUTH) {
            BlockPos blockpos11 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            list.add(
               new MansionTemplate(
                  this.templateManager, roomcollection.get1x2SideEntrance(this.random, flag), blockpos11, rotation, Mirror.FRONT_BACK
               )
            );
         } else if (enumfacing1 == Direction.SOUTH && enumfacing == Direction.EAST) {
            BlockPos blockpos10 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2SideEntrance(this.random, flag),
                  blockpos10,
                  rotation.getRotated(Rotation.CLOCKWISE_90),
                  Mirror.LEFT_RIGHT
               )
            );
         } else if (enumfacing1 == Direction.SOUTH && enumfacing == Direction.WEST) {
            BlockPos blockpos9 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2SideEntrance(this.random, flag),
                  blockpos9,
                  rotation.getRotated(Rotation.CLOCKWISE_90)
               )
            );
         } else if (enumfacing1 == Direction.NORTH && enumfacing == Direction.WEST) {
            BlockPos blockpos8 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            blockpos8 = blockpos8.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2SideEntrance(this.random, flag),
                  blockpos8,
                  rotation.getRotated(Rotation.CLOCKWISE_90),
                  Mirror.FRONT_BACK
               )
            );
         } else if (enumfacing1 == Direction.NORTH && enumfacing == Direction.EAST) {
            BlockPos blockpos7 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos7 = blockpos7.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2SideEntrance(this.random, flag),
                  blockpos7,
                  rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)
               )
            );
         } else if (enumfacing1 == Direction.SOUTH && enumfacing == Direction.NORTH) {
            BlockPos blockpos6 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos6 = blockpos6.relative(rotation.rotate(Direction.NORTH), 8);
            list.add(
               new MansionTemplate(
                  this.templateManager, roomcollection.get1x2FrontEntrance(this.random, flag), blockpos6, rotation
               )
            );
         } else if (enumfacing1 == Direction.NORTH && enumfacing == Direction.SOUTH) {
            BlockPos blockpos5 = blockpos.relative(rotation.rotate(Direction.EAST), 7);
            blockpos5 = blockpos5.relative(rotation.rotate(Direction.SOUTH), 14);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2FrontEntrance(this.random, flag),
                  blockpos5,
                  rotation.getRotated(Rotation.CLOCKWISE_180)
               )
            );
         } else if (enumfacing1 == Direction.WEST && enumfacing == Direction.EAST) {
            BlockPos blockpos4 = blockpos.relative(rotation.rotate(Direction.EAST), 15);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2FrontEntrance(this.random, flag),
                  blockpos4,
                  rotation.getRotated(Rotation.CLOCKWISE_90)
               )
            );
         } else if (enumfacing1 == Direction.EAST && enumfacing == Direction.WEST) {
            BlockPos blockpos3 = blockpos.relative(rotation.rotate(Direction.WEST), 7);
            blockpos3 = blockpos3.relative(rotation.rotate(Direction.SOUTH), 6);
            list.add(
               new MansionTemplate(
                  this.templateManager,
                  roomcollection.get1x2FrontEntrance(this.random, flag),
                  blockpos3,
                  rotation.getRotated(Rotation.COUNTERCLOCKWISE_90)
               )
            );
         } else if (enumfacing1 == Direction.UP && enumfacing == Direction.EAST) {
            BlockPos blockpos2 = blockpos.relative(rotation.rotate(Direction.EAST), 15);
            list.add(
               new MansionTemplate(
                  this.templateManager, roomcollection.get1x2Secret(this.random), blockpos2, rotation.getRotated(Rotation.CLOCKWISE_90)
               )
            );
         } else if (enumfacing1 == Direction.UP && enumfacing == Direction.SOUTH) {
            BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
            blockpos1 = blockpos1.relative(rotation.rotate(Direction.NORTH), 0);
            list.add(
               new MansionTemplate(
                  this.templateManager, roomcollection.get1x2Secret(this.random), blockpos1, rotation
               )
            );
         }
      }

      private void addRoom2x2(
         List<MansionTemplate> list,
         BlockPos blockpos,
         Rotation rotation,
         Direction enumfacing,
         Direction enumfacing1,
         RoomCollection roomcollection
      ) {
         int i = 0;
         int j = 0;
         Rotation rotation1 = rotation;
         Mirror mirror = Mirror.NONE;
         if (enumfacing1 == Direction.EAST && enumfacing == Direction.SOUTH) {
            i = -7;
         } else if (enumfacing1 == Direction.EAST && enumfacing == Direction.NORTH) {
            i = -7;
            j = 6;
            mirror = Mirror.LEFT_RIGHT;
         } else if (enumfacing1 == Direction.NORTH && enumfacing == Direction.EAST) {
            i = 1;
            j = 14;
            rotation1 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
         } else if (enumfacing1 == Direction.NORTH && enumfacing == Direction.WEST) {
            i = 7;
            j = 14;
            rotation1 = rotation.getRotated(Rotation.COUNTERCLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (enumfacing1 == Direction.SOUTH && enumfacing == Direction.WEST) {
            i = 7;
            j = -8;
            rotation1 = rotation.getRotated(Rotation.CLOCKWISE_90);
         } else if (enumfacing1 == Direction.SOUTH && enumfacing == Direction.EAST) {
            i = 1;
            j = -8;
            rotation1 = rotation.getRotated(Rotation.CLOCKWISE_90);
            mirror = Mirror.LEFT_RIGHT;
         } else if (enumfacing1 == Direction.WEST && enumfacing == Direction.NORTH) {
            i = 15;
            j = 6;
            rotation1 = rotation.getRotated(Rotation.CLOCKWISE_180);
         } else if (enumfacing1 == Direction.WEST && enumfacing == Direction.SOUTH) {
            i = 15;
            mirror = Mirror.FRONT_BACK;
         }

         BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.EAST), i);
         blockpos1 = blockpos1.relative(rotation.rotate(Direction.SOUTH), j);
         list.add(
            new MansionTemplate(
               this.templateManager, roomcollection.get2x2(this.random), blockpos1, rotation1, mirror
            )
         );
      }

      private void addRoom2x2Secret(
         List<MansionTemplate> list,
         BlockPos blockpos,
         Rotation rotation,
         RoomCollection roomcollection
      ) {
         BlockPos blockpos1 = blockpos.relative(rotation.rotate(Direction.EAST), 1);
         list.add(
            new MansionTemplate(
               this.templateManager, roomcollection.get2x2Secret(this.random), blockpos1, rotation, Mirror.NONE
            )
         );
      }
   }

   abstract static class RoomCollection {
      private RoomCollection() {
      }

      public abstract String get1x1(Random random);

      public abstract String get1x1Secret(Random random);

      public abstract String get1x2SideEntrance(Random random, boolean flag);

      public abstract String get1x2FrontEntrance(Random random, boolean flag);

      public abstract String get1x2Secret(Random random);

      public abstract String get2x2(Random random);

      public abstract String get2x2Secret(Random random);
   }

   static class SecondFloor extends RoomCollection {
      private SecondFloor() {
      }

      @Override
      public String get1x1(Random random) {
         return "1x1_b" + (random.nextInt(4) + 1);
      }

      @Override
      public String get1x1Secret(Random random) {
         return "1x1_as" + (random.nextInt(4) + 1);
      }

      @Override
      public String get1x2SideEntrance(Random random, boolean flag) {
         return flag ? "1x2_c_stairs" : "1x2_c" + (random.nextInt(4) + 1);
      }

      @Override
      public String get1x2FrontEntrance(Random random, boolean flag) {
         return flag ? "1x2_d_stairs" : "1x2_d" + (random.nextInt(5) + 1);
      }

      @Override
      public String get1x2Secret(Random random) {
         return "1x2_se" + (random.nextInt(1) + 1);
      }

      @Override
      public String get2x2(Random random) {
         return "2x2_b" + (random.nextInt(5) + 1);
      }

      @Override
      public String get2x2Secret(Random random) {
         return "2x2_s1";
      }
   }

   static class SimpleGrid {
      private final int[][] grid;
      private final int width;
      private final int height;
      private final int valueIfOutside;

      public SimpleGrid(int i, int j, int k) {
         this.width = i;
         this.height = j;
         this.valueIfOutside = k;
         this.grid = new int[i][j];
      }

      public void set(int i, int j, int k) {
         if (i >= 0 && i < this.width && j >= 0 && j < this.height) {
            this.grid[i][j] = k;
         }
      }

      public void set(int i, int j, int k, int l, int i1) {
         for (int j1 = j; j1 <= l; j1++) {
            for (int k1 = i; k1 <= k; k1++) {
               this.set(k1, j1, i1);
            }
         }
      }

      public int get(int i, int j) {
         return i >= 0 && i < this.width && j >= 0 && j < this.height ? this.grid[i][j] : this.valueIfOutside;
      }

      public void setIf(int i, int j, int k, int l) {
         if (this.get(i, j) == k) {
            this.set(i, j, l);
         }
      }

      public boolean edgesTo(int i, int j, int k) {
         return this.get(i - 1, j) == k || this.get(i + 1, j) == k || this.get(i, j + 1) == k || this.get(i, j - 1) == k;
      }
   }

   static class ThirdFloor extends SecondFloor {
      private ThirdFloor() {
      }
   }
}
