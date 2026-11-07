package com.nukevscity.rlgen.vanilla.monument;

import com.google.common.collect.Lists;
import com.nukevscity.rlgen.deco.DWorld;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.ElderGuardian;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12.2 StructureOceanMonumentPieces, on {@link DWorld} (the 1.12 populate world). */
public final class MonumentPieces {
   public static class DoubleXRoom extends MonumentPieces.Piece {
      public DoubleXRoom() {
      }

      public DoubleXRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 2, 1, 1);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         MonumentPieces.RoomDefinition roomdefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         MonumentPieces.RoomDefinition roomdefinition1 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 8, 0, roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (roomdefinition1.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 4, 1, 7, 4, 6, ROUGH_PRISMARINE);
         }

         if (roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 8, 4, 1, 14, 4, 6, ROUGH_PRISMARINE);
         }

         this.fillWithBlocks(world, structureboundingbox, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 15, 3, 0, 15, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 0, 15, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 7, 14, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 2, 0, 0, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 15, 2, 0, 15, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 2, 0, 15, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 2, 7, 14, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 15, 1, 0, 15, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 0, 15, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 7, 14, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 1, 0, 10, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 2, 0, 9, 2, 3, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 3, 0, 10, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(world, SEA_LANTERN, 6, 2, 3, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 9, 2, 3, structureboundingbox);
         if (roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 0, 4, 2, 0, false);
         }

         if (roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 7, 4, 2, 7, false);
         }

         if (roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 3, 0, 2, 4, false);
         }

         if (roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 11, 1, 0, 12, 2, 0, false);
         }

         if (roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 11, 1, 7, 12, 2, 7, false);
         }

         if (roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 15, 1, 3, 15, 2, 4, false);
         }

         return true;
      }
   }

   public static class DoubleXYRoom extends MonumentPieces.Piece {
      public DoubleXYRoom() {
      }

      public DoubleXYRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 2, 2, 1);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         MonumentPieces.RoomDefinition roomdefinition = this.roomDefinition.connections[Direction.EAST.get3DDataValue()];
         MonumentPieces.RoomDefinition roomdefinition1 = this.roomDefinition;
         MonumentPieces.RoomDefinition roomdefinition2 = roomdefinition1.connections[Direction.UP
            .get3DDataValue()];
         MonumentPieces.RoomDefinition roomdefinition3 = roomdefinition.connections[Direction.UP
            .get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 8, 0, roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (roomdefinition2.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 8, 1, 7, 8, 6, ROUGH_PRISMARINE);
         }

         if (roomdefinition3.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 8, 8, 1, 14, 8, 6, ROUGH_PRISMARINE);
         }

         for (int i = 1; i <= 7; i++) {
            BlockState iblockstate = BRICKS_PRISMARINE;
            if (i == 2 || i == 6) {
               iblockstate = ROUGH_PRISMARINE;
            }

            this.fillWithBlocks(world, structureboundingbox, 0, i, 0, 0, i, 7, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 15, i, 0, 15, i, 7, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 1, i, 0, 15, i, 0, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 1, i, 7, 14, i, 7, iblockstate, iblockstate, false);
         }

         this.fillWithBlocks(world, structureboundingbox, 2, 1, 3, 2, 7, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 2, 4, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 5, 4, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 13, 1, 3, 13, 7, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 11, 1, 2, 12, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 11, 1, 5, 12, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 1, 3, 5, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 1, 3, 10, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 7, 2, 10, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 5, 2, 5, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 5, 2, 10, 7, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 5, 5, 5, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 5, 5, 10, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(world, BRICKS_PRISMARINE, 6, 6, 2, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 9, 6, 2, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 6, 6, 5, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 9, 6, 5, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 5, 4, 3, 6, 4, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 9, 4, 3, 10, 4, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(world, SEA_LANTERN, 5, 4, 2, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 5, 4, 5, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 10, 4, 2, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 10, 4, 5, structureboundingbox);
         if (roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 0, 4, 2, 0, false);
         }

         if (roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 7, 4, 2, 7, false);
         }

         if (roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 3, 0, 2, 4, false);
         }

         if (roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 11, 1, 0, 12, 2, 0, false);
         }

         if (roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 11, 1, 7, 12, 2, 7, false);
         }

         if (roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 15, 1, 3, 15, 2, 4, false);
         }

         if (roomdefinition2.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 5, 0, 4, 6, 0, false);
         }

         if (roomdefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 5, 7, 4, 6, 7, false);
         }

         if (roomdefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 5, 3, 0, 6, 4, false);
         }

         if (roomdefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 11, 5, 0, 12, 6, 0, false);
         }

         if (roomdefinition3.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 11, 5, 7, 12, 6, 7, false);
         }

         if (roomdefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 15, 5, 3, 15, 6, 4, false);
         }

         return true;
      }
   }

   public static class DoubleYRoom extends MonumentPieces.Piece {
      public DoubleYRoom() {
      }

      public DoubleYRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 1, 2, 1);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         MonumentPieces.RoomDefinition roomdefinition = this.roomDefinition.connections[Direction.UP.get3DDataValue()];
         if (roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 8, 1, 6, 8, 6, ROUGH_PRISMARINE);
         }

         this.fillWithBlocks(world, structureboundingbox, 0, 4, 0, 0, 4, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 4, 0, 7, 4, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 4, 0, 6, 4, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 4, 7, 6, 4, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 4, 1, 2, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 4, 2, 1, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 4, 1, 5, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 4, 2, 6, 4, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 4, 5, 2, 4, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 4, 5, 1, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 4, 5, 5, 4, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 4, 5, 6, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         MonumentPieces.RoomDefinition roomdefinition1 = this.roomDefinition;

         for (int i = 1; i <= 5; i += 4) {
            int j = 0;
            if (roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 2, i, j, 2, i + 2, j, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 5, i, j, 5, i + 2, j, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 3, i + 2, j, 4, i + 2, j, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, 0, i, j, 7, i + 2, j, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 0, i + 1, j, 7, i + 1, j, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            byte b0 = 7;
            if (roomdefinition1.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 2, i, b0, 2, i + 2, b0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 5, i, b0, 5, i + 2, b0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 3, i + 2, b0, 4, i + 2, b0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, 0, i, b0, 7, i + 2, b0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 0, i + 1, b0, 7, i + 1, b0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            int k = 0;
            if (roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, k, i, 2, k, i + 2, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, k, i, 5, k, i + 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, k, i + 2, 3, k, i + 2, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, k, i, 0, k, i + 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, k, i + 1, 0, k, i + 1, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            byte b1 = 7;
            if (roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, b1, i, 2, b1, i + 2, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, b1, i, 5, b1, i + 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, b1, i + 2, 3, b1, i + 2, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, b1, i, 0, b1, i + 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, b1, i + 1, 0, b1, i + 1, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            roomdefinition1 = roomdefinition;
         }

         return true;
      }
   }

   public static class DoubleYZRoom extends MonumentPieces.Piece {
      public DoubleYZRoom() {
      }

      public DoubleYZRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 1, 2, 2);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         MonumentPieces.RoomDefinition roomdefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         MonumentPieces.RoomDefinition roomdefinition1 = this.roomDefinition;
         MonumentPieces.RoomDefinition roomdefinition2 = roomdefinition.connections[Direction.UP
            .get3DDataValue()];
         MonumentPieces.RoomDefinition roomdefinition3 = roomdefinition1.connections[Direction.UP
            .get3DDataValue()];
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 0, 8, roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (roomdefinition3.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 8, 1, 6, 8, 7, ROUGH_PRISMARINE);
         }

         if (roomdefinition2.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 8, 8, 6, 8, 14, ROUGH_PRISMARINE);
         }

         for (int i = 1; i <= 7; i++) {
            BlockState iblockstate = BRICKS_PRISMARINE;
            if (i == 2 || i == 6) {
               iblockstate = ROUGH_PRISMARINE;
            }

            this.fillWithBlocks(world, structureboundingbox, 0, i, 0, 0, i, 15, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 7, i, 0, 7, i, 15, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 1, i, 0, 6, i, 0, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 1, i, 15, 6, i, 15, iblockstate, iblockstate, false);
         }

         for (int j = 1; j <= 7; j++) {
            BlockState iblockstate1 = DARK_PRISMARINE;
            if (j == 2 || j == 6) {
               iblockstate1 = SEA_LANTERN;
            }

            this.fillWithBlocks(world, structureboundingbox, 3, j, 7, 4, j, 8, iblockstate1, iblockstate1, false);
         }

         if (roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 0, 4, 2, 0, false);
         }

         if (roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 7, 1, 3, 7, 2, 4, false);
         }

         if (roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 3, 0, 2, 4, false);
         }

         if (roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 15, 4, 2, 15, false);
         }

         if (roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 11, 0, 2, 12, false);
         }

         if (roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 7, 1, 11, 7, 2, 12, false);
         }

         if (roomdefinition3.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 5, 0, 4, 6, 0, false);
         }

         if (roomdefinition3.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 7, 5, 3, 7, 6, 4, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 4, 2, 6, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 1, 2, 6, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 1, 5, 6, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         if (roomdefinition3.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 5, 3, 0, 6, 4, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 4, 2, 2, 4, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 2, 1, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 5, 1, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         if (roomdefinition2.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 5, 15, 4, 6, 15, false);
         }

         if (roomdefinition2.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 5, 11, 0, 6, 12, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 4, 10, 2, 4, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 10, 1, 3, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 13, 1, 3, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         if (roomdefinition2.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 7, 5, 11, 7, 6, 12, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 4, 10, 6, 4, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 1, 10, 6, 3, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 1, 13, 6, 3, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         return true;
      }
   }

   public static class DoubleZRoom extends MonumentPieces.Piece {
      public DoubleZRoom() {
      }

      public DoubleZRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 1, 1, 2);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         MonumentPieces.RoomDefinition roomdefinition = this.roomDefinition.connections[Direction.NORTH.get3DDataValue()];
         MonumentPieces.RoomDefinition roomdefinition1 = this.roomDefinition;
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 0, 8, roomdefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, roomdefinition1.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (roomdefinition1.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 4, 1, 6, 4, 7, ROUGH_PRISMARINE);
         }

         if (roomdefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 4, 8, 6, 4, 14, ROUGH_PRISMARINE);
         }

         this.fillWithBlocks(world, structureboundingbox, 0, 3, 0, 0, 3, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 3, 0, 7, 3, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 0, 7, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 15, 6, 3, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 2, 0, 0, 2, 15, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 2, 0, 7, 2, 15, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 2, 0, 7, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 2, 15, 6, 2, 15, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 0, 1, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 1, 0, 7, 1, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 0, 7, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 15, 6, 1, 15, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 1, 1, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 1, 1, 6, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 1, 1, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 3, 1, 6, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 13, 1, 1, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 1, 13, 6, 1, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 13, 1, 3, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 3, 13, 6, 3, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 1, 6, 2, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 1, 6, 5, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 1, 9, 2, 3, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 1, 9, 5, 3, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 2, 6, 4, 2, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 2, 9, 4, 2, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 2, 7, 2, 2, 8, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 2, 7, 5, 2, 8, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(world, SEA_LANTERN, 2, 2, 5, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 5, 2, 5, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 2, 2, 10, structureboundingbox);
         this.setBlockState(world, SEA_LANTERN, 5, 2, 10, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 2, 3, 5, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 5, 3, 5, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 2, 3, 10, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 5, 3, 10, structureboundingbox);
         if (roomdefinition1.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 0, 4, 2, 0, false);
         }

         if (roomdefinition1.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 7, 1, 3, 7, 2, 4, false);
         }

         if (roomdefinition1.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 3, 0, 2, 4, false);
         }

         if (roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 15, 4, 2, 15, false);
         }

         if (roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 11, 0, 2, 12, false);
         }

         if (roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 7, 1, 11, 7, 2, 12, false);
         }

         return true;
      }
   }

   public static class EntryRoom extends MonumentPieces.Piece {
      public EntryRoom() {
      }

      public EntryRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition) {
         super(1, facing, roomdefinition, 1, 1, 1);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         this.fillWithBlocks(world, structureboundingbox, 0, 3, 0, 2, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 2, 0, 1, 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 2, 0, 7, 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 1, 7, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 0, 2, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 1, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 7, 4, 2, 7, false);
         }

         if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 0, 1, 3, 1, 2, 4, false);
         }

         if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 6, 1, 3, 7, 2, 4, false);
         }

         return true;
      }
   }

   static class FitSimpleRoomHelper implements MonumentPieces.MonumentRoomFitHelper {
      private FitSimpleRoomHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         return true;
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         roomdefinition.claimed = true;
         return new MonumentPieces.SimpleRoom(facing, roomdefinition, random);
      }
   }

   static class FitSimpleRoomTopHelper implements MonumentPieces.MonumentRoomFitHelper {
      private FitSimpleRoomTopHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         return !roomdefinition.hasOpening[Direction.WEST.get3DDataValue()]
            && !roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]
            && !roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]
            && !roomdefinition.hasOpening[Direction.SOUTH.get3DDataValue()]
            && !roomdefinition.hasOpening[Direction.UP.get3DDataValue()];
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         roomdefinition.claimed = true;
         return new MonumentPieces.SimpleTopRoom(facing, roomdefinition, random);
      }
   }

   public static class MonumentBuilding extends MonumentPieces.Piece {
      private MonumentPieces.RoomDefinition sourceRoom;
      private MonumentPieces.RoomDefinition coreRoom;
      private final List<MonumentPieces.Piece> childPieces = Lists.newArrayList();

      public MonumentBuilding() {
      }

      public MonumentBuilding(Random random, int i, int j, Direction facing) {
         super(0);
         this.setCoordBaseMode(facing);
         Direction facing1 = this.getCoordBaseMode();
         if (facing1.getAxis() == Direction.Axis.Z) {
            this.boundingBox = new StructureBoundingBox(i, 39, j, i + 58 - 1, 61, j + 58 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(i, 39, j, i + 58 - 1, 61, j + 58 - 1);
         }

         List<MonumentPieces.RoomDefinition> list = this.generateRoomGraph(random);
         this.sourceRoom.claimed = true;
         this.childPieces.add(new MonumentPieces.EntryRoom(facing1, this.sourceRoom));
         this.childPieces.add(new MonumentPieces.MonumentCoreRoom(facing1, this.coreRoom, random));
         List<MonumentPieces.MonumentRoomFitHelper> list1 = Lists.newArrayList();
         list1.add(new MonumentPieces.XYDoubleRoomFitHelper());
         list1.add(new MonumentPieces.YZDoubleRoomFitHelper());
         list1.add(new MonumentPieces.ZDoubleRoomFitHelper());
         list1.add(new MonumentPieces.XDoubleRoomFitHelper());
         list1.add(new MonumentPieces.YDoubleRoomFitHelper());
         list1.add(new MonumentPieces.FitSimpleRoomTopHelper());
         list1.add(new MonumentPieces.FitSimpleRoomHelper());

         for (MonumentPieces.RoomDefinition roomdefinition : list) {
            if (!roomdefinition.claimed && !roomdefinition.isSpecial()) {
               for (MonumentPieces.MonumentRoomFitHelper monumentroomfithelper : list1) {
                  if (monumentroomfithelper.fits(roomdefinition)) {
                     this.childPieces
                        .add(monumentroomfithelper.create(facing1, roomdefinition, random));
                     break;
                  }
               }
            }
         }

         int l = this.boundingBox.minY;
         int i1 = this.getXWithOffset(9, 22);
         int j1 = this.getZWithOffset(9, 22);

         for (MonumentPieces.Piece piece : this.childPieces) {
            piece.getBoundingBox().offset(i1, l, j1);
         }

         StructureBoundingBox structureboundingbox1 = StructureBoundingBox.createProper(
            this.getXWithOffset(1, 1),
            this.getYWithOffset(1),
            this.getZWithOffset(1, 1),
            this.getXWithOffset(23, 21),
            this.getYWithOffset(8),
            this.getZWithOffset(23, 21)
         );
         StructureBoundingBox structureboundingbox2 = StructureBoundingBox.createProper(
            this.getXWithOffset(34, 1),
            this.getYWithOffset(1),
            this.getZWithOffset(34, 1),
            this.getXWithOffset(56, 21),
            this.getYWithOffset(8),
            this.getZWithOffset(56, 21)
         );
         StructureBoundingBox structureboundingbox = StructureBoundingBox.createProper(
            this.getXWithOffset(22, 22),
            this.getYWithOffset(13),
            this.getZWithOffset(22, 22),
            this.getXWithOffset(35, 35),
            this.getYWithOffset(17),
            this.getZWithOffset(35, 35)
         );
         int k = random.nextInt();
         this.childPieces.add(new MonumentPieces.WingRoom(facing1, structureboundingbox1, k++));
         this.childPieces.add(new MonumentPieces.WingRoom(facing1, structureboundingbox2, k++));
         this.childPieces.add(new MonumentPieces.Penthouse(facing1, structureboundingbox));
      }

      private List<MonumentPieces.RoomDefinition> generateRoomGraph(Random random) {
         MonumentPieces.RoomDefinition[] aroomdefinition = new MonumentPieces.RoomDefinition[75];

         for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 4; j++) {
               int k = 0;
               int l = getRoomIndex(i, 0, j);
               aroomdefinition[l] = new MonumentPieces.RoomDefinition(l);
            }
         }

         for (int i2 = 0; i2 < 5; i2++) {
            for (int l2 = 0; l2 < 4; l2++) {
               int k3 = 1;
               int j4 = getRoomIndex(i2, 1, l2);
               aroomdefinition[j4] = new MonumentPieces.RoomDefinition(j4);
            }
         }

         for (int j2 = 1; j2 < 4; j2++) {
            for (int i3 = 0; i3 < 2; i3++) {
               int l3 = 2;
               int k4 = getRoomIndex(j2, 2, i3);
               aroomdefinition[k4] = new MonumentPieces.RoomDefinition(k4);
            }
         }

         this.sourceRoom = aroomdefinition[GRIDROOM_SOURCE_INDEX];

         for (int k2 = 0; k2 < 5; k2++) {
            for (int j3 = 0; j3 < 5; j3++) {
               for (int i4 = 0; i4 < 3; i4++) {
                  int l4 = getRoomIndex(k2, i4, j3);
                  if (aroomdefinition[l4] != null) {
                     for (Direction facing : Direction.values()) {
                        int i1 = k2 + facing.getStepX();
                        int j1 = i4 + facing.getStepY();
                        int k1 = j3 + facing.getStepZ();
                        if (i1 >= 0 && i1 < 5 && k1 >= 0 && k1 < 5 && j1 >= 0 && j1 < 3) {
                           int l1 = getRoomIndex(i1, j1, k1);
                           if (aroomdefinition[l1] != null) {
                              if (k1 == j3) {
                                 aroomdefinition[l4].setConnection(facing, aroomdefinition[l1]);
                              } else {
                                 aroomdefinition[l4]
                                    .setConnection(facing.getOpposite(), aroomdefinition[l1]);
                              }
                           }
                        }
                     }
                  }
               }
            }
         }

         MonumentPieces.RoomDefinition roomdefinition = new MonumentPieces.RoomDefinition(1003);
         MonumentPieces.RoomDefinition roomdefinition1 = new MonumentPieces.RoomDefinition(1001);
         MonumentPieces.RoomDefinition roomdefinition2 = new MonumentPieces.RoomDefinition(1002);
         aroomdefinition[GRIDROOM_TOP_CONNECT_INDEX].setConnection(Direction.UP, roomdefinition);
         aroomdefinition[GRIDROOM_LEFTWING_CONNECT_INDEX]
            .setConnection(Direction.SOUTH, roomdefinition1);
         aroomdefinition[GRIDROOM_RIGHTWING_CONNECT_INDEX]
            .setConnection(Direction.SOUTH, roomdefinition2);
         roomdefinition.claimed = true;
         roomdefinition1.claimed = true;
         roomdefinition2.claimed = true;
         this.sourceRoom.isSource = true;
         this.coreRoom = aroomdefinition[getRoomIndex(random.nextInt(4), 0, 2)];
         this.coreRoom.claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         this.coreRoom.connections[Direction.EAST.get3DDataValue()].connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         List<MonumentPieces.RoomDefinition> list = Lists.newArrayList();

         for (MonumentPieces.RoomDefinition roomdefinition4 : aroomdefinition) {
            if (roomdefinition4 != null) {
               roomdefinition4.updateOpenings();
               list.add(roomdefinition4);
            }
         }

         roomdefinition.updateOpenings();
         Collections.shuffle(list, random);
         int i5 = 1;

         for (MonumentPieces.RoomDefinition roomdefinition3 : list) {
            int j5 = 0;
            int k5 = 0;

            while (j5 < 2 && k5 < 5) {
               k5++;
               int l5 = random.nextInt(6);
               if (roomdefinition3.hasOpening[l5]) {
                  int i6 = Direction.from3DDataValue(l5).getOpposite().get3DDataValue();
                  roomdefinition3.hasOpening[l5] = false;
                  roomdefinition3.connections[l5].hasOpening[i6] = false;
                  if (roomdefinition3.findSource(i5++)
                     && roomdefinition3.connections[l5].findSource(i5++)) {
                     j5++;
                  } else {
                     roomdefinition3.hasOpening[l5] = true;
                     roomdefinition3.connections[l5].hasOpening[i6] = true;
                  }
               }
            }
         }

         list.add(roomdefinition);
         list.add(roomdefinition1);
         list.add(roomdefinition2);
         return list;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         int i = Math.max(SEA_LEVEL, 64) - this.boundingBox.minY;
         this.generateWaterBox(world, structureboundingbox, 0, 0, 0, 58, i, 58, false);
         this.generateWing(false, 0, world, random, structureboundingbox);
         this.generateWing(true, 33, world, random, structureboundingbox);
         this.generateEntranceArchs(world, random, structureboundingbox);
         this.generateEntranceWall(world, random, structureboundingbox);
         this.generateRoofPiece(world, random, structureboundingbox);
         this.generateLowerWall(world, random, structureboundingbox);
         this.generateMiddleWall(world, random, structureboundingbox);
         this.generateUpperWall(world, random, structureboundingbox);

         for (int j = 0; j < 7; j++) {
            int k = 0;

            while (k < 7) {
               if (k == 0 && j == 3) {
                  k = 6;
               }

               int l = j * 9;
               int i1 = k * 9;

               for (int j1 = 0; j1 < 4; j1++) {
                  for (int k1 = 0; k1 < 4; k1++) {
                     this.setBlockState(world, BRICKS_PRISMARINE, l + j1, 0, i1 + k1, structureboundingbox);
                     this.replaceAirAndLiquidDownwards(world, BRICKS_PRISMARINE, l + j1, -1, i1 + k1, structureboundingbox);
                  }
               }

               if (j != 0 && j != 6) {
                  k += 6;
               } else {
                  k++;
               }
            }
         }

         for (int l1 = 0; l1 < 5; l1++) {
            this.generateWaterBox(world, structureboundingbox, -1 - l1, 0 + l1 * 2, -1 - l1, -1 - l1, 23, 58 + l1, false);
            this.generateWaterBox(world, structureboundingbox, 58 + l1, 0 + l1 * 2, -1 - l1, 58 + l1, 23, 58 + l1, false);
            this.generateWaterBox(world, structureboundingbox, 0 - l1, 0 + l1 * 2, -1 - l1, 57 + l1, 23, -1 - l1, false);
            this.generateWaterBox(world, structureboundingbox, 0 - l1, 0 + l1 * 2, 58 + l1, 57 + l1, 23, 58 + l1, false);
         }

         for (MonumentPieces.Piece piece : this.childPieces) {
            if (piece.getBoundingBox().intersectsWith(structureboundingbox)) {
               piece.addComponentParts(world, random, structureboundingbox);
            }
         }

         return true;
      }

      private void generateWing(boolean flag, int i, DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         int j = 24;
         if (this.doesChunkIntersect(structureboundingbox, i, 0, i + 23, 20)) {
            this.fillWithBlocks(world, structureboundingbox, i + 0, 0, 0, i + 24, 0, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, i + 0, 1, 0, i + 24, 10, 20, false);

            for (int k = 0; k < 4; k++) {
               this.fillWithBlocks(world, structureboundingbox, i + k, k + 1, k, i + k, k + 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, i + k + 7, k + 5, k + 7, i + k + 7, k + 5, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, i + 17 - k, k + 5, k + 7, i + 17 - k, k + 5, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, i + 24 - k, k + 1, k, i + 24 - k, k + 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, i + k + 1, k + 1, k, i + 23 - k, k + 1, k, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, i + k + 8, k + 5, k + 7, i + 16 - k, k + 5, k + 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(world, structureboundingbox, i + 4, 4, 4, i + 6, 4, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 7, 4, 4, i + 17, 4, 6, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 18, 4, 4, i + 20, 4, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 11, 8, 11, i + 13, 8, 20, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(world, DOT_DECO_DATA, i + 12, 9, 12, structureboundingbox);
            this.setBlockState(world, DOT_DECO_DATA, i + 12, 9, 15, structureboundingbox);
            this.setBlockState(world, DOT_DECO_DATA, i + 12, 9, 18, structureboundingbox);
            int k1 = i + (flag ? 19 : 5);
            int l = i + (flag ? 5 : 19);

            for (int i1 = 20; i1 >= 5; i1 -= 3) {
               this.setBlockState(world, DOT_DECO_DATA, k1, 5, i1, structureboundingbox);
            }

            for (int l1 = 19; l1 >= 7; l1 -= 3) {
               this.setBlockState(world, DOT_DECO_DATA, l, 5, l1, structureboundingbox);
            }

            for (int i2 = 0; i2 < 4; i2++) {
               int j1 = flag ? i + (24 - (17 - i2 * 3)) : i + 17 - i2 * 3;
               this.setBlockState(world, DOT_DECO_DATA, j1, 5, 5, structureboundingbox);
            }

            this.setBlockState(world, DOT_DECO_DATA, l, 5, 5, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, i + 11, 1, 12, i + 13, 7, 12, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 12, 1, 11, i + 12, 7, 13, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }
      }

      private void generateEntranceArchs(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.doesChunkIntersect(structureboundingbox, 22, 5, 35, 17)) {
            this.generateWaterBox(world, structureboundingbox, 25, 0, 0, 32, 8, 20, false);

            for (int i = 0; i < 4; i++) {
               this.fillWithBlocks(world, structureboundingbox, 24, 2, 5 + i * 4, 24, 4, 5 + i * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 22, 4, 5 + i * 4, 23, 4, 5 + i * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.setBlockState(world, BRICKS_PRISMARINE, 25, 5, 5 + i * 4, structureboundingbox);
               this.setBlockState(world, BRICKS_PRISMARINE, 26, 6, 5 + i * 4, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, 26, 5, 5 + i * 4, structureboundingbox);
               this.fillWithBlocks(world, structureboundingbox, 33, 2, 5 + i * 4, 33, 4, 5 + i * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 34, 4, 5 + i * 4, 35, 4, 5 + i * 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.setBlockState(world, BRICKS_PRISMARINE, 32, 5, 5 + i * 4, structureboundingbox);
               this.setBlockState(world, BRICKS_PRISMARINE, 31, 6, 5 + i * 4, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, 31, 5, 5 + i * 4, structureboundingbox);
               this.fillWithBlocks(world, structureboundingbox, 27, 6, 5 + i * 4, 30, 6, 5 + i * 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }
         }
      }

      private void generateEntranceWall(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.doesChunkIntersect(structureboundingbox, 15, 20, 42, 21)) {
            this.fillWithBlocks(world, structureboundingbox, 15, 0, 21, 42, 0, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 26, 1, 21, 31, 3, 21, false);
            this.fillWithBlocks(world, structureboundingbox, 21, 12, 21, 36, 12, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 17, 11, 21, 40, 11, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 16, 10, 21, 41, 10, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 15, 7, 21, 42, 9, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 16, 6, 21, 41, 6, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 17, 5, 21, 40, 5, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 21, 4, 21, 36, 4, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 22, 3, 21, 26, 3, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 31, 3, 21, 35, 3, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 23, 2, 21, 25, 2, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 32, 2, 21, 34, 2, 21, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 28, 4, 20, 29, 4, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(world, BRICKS_PRISMARINE, 27, 3, 21, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 30, 3, 21, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 26, 2, 21, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 31, 2, 21, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 25, 1, 21, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 32, 1, 21, structureboundingbox);

            for (int i = 0; i < 7; i++) {
               this.setBlockState(world, DARK_PRISMARINE, 28 - i, 6 + i, 21, structureboundingbox);
               this.setBlockState(world, DARK_PRISMARINE, 29 + i, 6 + i, 21, structureboundingbox);
            }

            for (int j = 0; j < 4; j++) {
               this.setBlockState(world, DARK_PRISMARINE, 28 - j, 9 + j, 21, structureboundingbox);
               this.setBlockState(world, DARK_PRISMARINE, 29 + j, 9 + j, 21, structureboundingbox);
            }

            this.setBlockState(world, DARK_PRISMARINE, 28, 12, 21, structureboundingbox);
            this.setBlockState(world, DARK_PRISMARINE, 29, 12, 21, structureboundingbox);

            for (int k = 0; k < 3; k++) {
               this.setBlockState(world, DARK_PRISMARINE, 22 - k * 2, 8, 21, structureboundingbox);
               this.setBlockState(world, DARK_PRISMARINE, 22 - k * 2, 9, 21, structureboundingbox);
               this.setBlockState(world, DARK_PRISMARINE, 35 + k * 2, 8, 21, structureboundingbox);
               this.setBlockState(world, DARK_PRISMARINE, 35 + k * 2, 9, 21, structureboundingbox);
            }

            this.generateWaterBox(world, structureboundingbox, 15, 13, 21, 42, 15, 21, false);
            this.generateWaterBox(world, structureboundingbox, 15, 1, 21, 15, 6, 21, false);
            this.generateWaterBox(world, structureboundingbox, 16, 1, 21, 16, 5, 21, false);
            this.generateWaterBox(world, structureboundingbox, 17, 1, 21, 20, 4, 21, false);
            this.generateWaterBox(world, structureboundingbox, 21, 1, 21, 21, 3, 21, false);
            this.generateWaterBox(world, structureboundingbox, 22, 1, 21, 22, 2, 21, false);
            this.generateWaterBox(world, structureboundingbox, 23, 1, 21, 24, 1, 21, false);
            this.generateWaterBox(world, structureboundingbox, 42, 1, 21, 42, 6, 21, false);
            this.generateWaterBox(world, structureboundingbox, 41, 1, 21, 41, 5, 21, false);
            this.generateWaterBox(world, structureboundingbox, 37, 1, 21, 40, 4, 21, false);
            this.generateWaterBox(world, structureboundingbox, 36, 1, 21, 36, 3, 21, false);
            this.generateWaterBox(world, structureboundingbox, 33, 1, 21, 34, 1, 21, false);
            this.generateWaterBox(world, structureboundingbox, 35, 1, 21, 35, 2, 21, false);
         }
      }

      private void generateRoofPiece(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.doesChunkIntersect(structureboundingbox, 21, 21, 36, 36)) {
            this.fillWithBlocks(world, structureboundingbox, 21, 0, 22, 36, 0, 36, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 21, 1, 22, 36, 23, 36, false);

            for (int i = 0; i < 4; i++) {
               this.fillWithBlocks(world, structureboundingbox, 21 + i, 13 + i, 21 + i, 36 - i, 13 + i, 21 + i, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 21 + i, 13 + i, 36 - i, 36 - i, 13 + i, 36 - i, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 21 + i, 13 + i, 22 + i, 21 + i, 13 + i, 35 - i, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 36 - i, 13 + i, 22 + i, 36 - i, 13 + i, 35 - i, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(world, structureboundingbox, 25, 16, 25, 32, 16, 32, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 25, 17, 25, 25, 19, 25, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 32, 17, 25, 32, 19, 25, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 25, 17, 32, 25, 19, 32, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 32, 17, 32, 32, 19, 32, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(world, BRICKS_PRISMARINE, 26, 20, 26, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 27, 21, 27, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 27, 20, 27, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 26, 20, 31, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 27, 21, 30, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 27, 20, 30, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 31, 20, 31, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 30, 21, 30, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 30, 20, 30, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 31, 20, 26, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 30, 21, 27, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 30, 20, 27, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 28, 21, 27, 29, 21, 27, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 27, 21, 28, 27, 21, 29, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 28, 21, 30, 29, 21, 30, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 30, 21, 28, 30, 21, 29, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }
      }

      private void generateLowerWall(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.doesChunkIntersect(structureboundingbox, 0, 21, 6, 58)) {
            this.fillWithBlocks(world, structureboundingbox, 0, 0, 21, 6, 0, 57, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 0, 1, 21, 6, 7, 57, false);
            this.fillWithBlocks(world, structureboundingbox, 4, 4, 21, 6, 4, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for (int i = 0; i < 4; i++) {
               this.fillWithBlocks(world, structureboundingbox, i, i + 1, 21, i, i + 1, 57 - i, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int j = 23; j < 53; j += 3) {
               this.setBlockState(world, DOT_DECO_DATA, 5, 5, j, structureboundingbox);
            }

            this.setBlockState(world, DOT_DECO_DATA, 5, 5, 52, structureboundingbox);

            for (int k = 0; k < 4; k++) {
               this.fillWithBlocks(world, structureboundingbox, k, k + 1, 21, k, k + 1, 57 - k, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(world, structureboundingbox, 4, 1, 52, 6, 3, 52, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 1, 51, 5, 3, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

         if (this.doesChunkIntersect(structureboundingbox, 51, 21, 58, 58)) {
            this.fillWithBlocks(world, structureboundingbox, 51, 0, 21, 57, 0, 57, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 51, 1, 21, 57, 7, 57, false);
            this.fillWithBlocks(world, structureboundingbox, 51, 4, 21, 53, 4, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for (int l = 0; l < 4; l++) {
               this.fillWithBlocks(world, structureboundingbox, 57 - l, l + 1, 21, 57 - l, l + 1, 57 - l, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int i1 = 23; i1 < 53; i1 += 3) {
               this.setBlockState(world, DOT_DECO_DATA, 52, 5, i1, structureboundingbox);
            }

            this.setBlockState(world, DOT_DECO_DATA, 52, 5, 52, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 51, 1, 52, 53, 3, 52, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 52, 1, 51, 52, 3, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }

         if (this.doesChunkIntersect(structureboundingbox, 0, 51, 57, 57)) {
            this.fillWithBlocks(world, structureboundingbox, 7, 0, 51, 50, 0, 57, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 7, 1, 51, 50, 10, 57, false);

            for (int j1 = 0; j1 < 4; j1++) {
               this.fillWithBlocks(world, structureboundingbox, j1 + 1, j1 + 1, 57 - j1, 56 - j1, j1 + 1, 57 - j1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }
         }
      }

      private void generateMiddleWall(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.doesChunkIntersect(structureboundingbox, 7, 21, 13, 50)) {
            this.fillWithBlocks(world, structureboundingbox, 7, 0, 21, 13, 0, 50, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 7, 1, 21, 13, 10, 50, false);
            this.fillWithBlocks(world, structureboundingbox, 11, 8, 21, 13, 8, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for (int i = 0; i < 4; i++) {
               this.fillWithBlocks(world, structureboundingbox, i + 7, i + 5, 21, i + 7, i + 5, 54, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int j = 21; j <= 45; j += 3) {
               this.setBlockState(world, DOT_DECO_DATA, 12, 9, j, structureboundingbox);
            }
         }

         if (this.doesChunkIntersect(structureboundingbox, 44, 21, 50, 54)) {
            this.fillWithBlocks(world, structureboundingbox, 44, 0, 21, 50, 0, 50, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 44, 1, 21, 50, 10, 50, false);
            this.fillWithBlocks(world, structureboundingbox, 44, 8, 21, 46, 8, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for (int k = 0; k < 4; k++) {
               this.fillWithBlocks(world, structureboundingbox, 50 - k, k + 5, 21, 50 - k, k + 5, 54, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int l = 21; l <= 45; l += 3) {
               this.setBlockState(world, DOT_DECO_DATA, 45, 9, l, structureboundingbox);
            }
         }

         if (this.doesChunkIntersect(structureboundingbox, 8, 44, 49, 54)) {
            this.fillWithBlocks(world, structureboundingbox, 14, 0, 44, 43, 0, 50, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 14, 1, 44, 43, 10, 50, false);

            for (int i1 = 12; i1 <= 45; i1 += 3) {
               this.setBlockState(world, DOT_DECO_DATA, i1, 9, 45, structureboundingbox);
               this.setBlockState(world, DOT_DECO_DATA, i1, 9, 52, structureboundingbox);
               if (i1 == 12 || i1 == 18 || i1 == 24 || i1 == 33 || i1 == 39 || i1 == 45) {
                  this.setBlockState(world, DOT_DECO_DATA, i1, 9, 47, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 9, 50, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 10, 45, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 10, 46, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 10, 51, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 10, 52, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 11, 47, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 11, 50, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 12, 48, structureboundingbox);
                  this.setBlockState(world, DOT_DECO_DATA, i1, 12, 49, structureboundingbox);
               }
            }

            for (int j1 = 0; j1 < 3; j1++) {
               this.fillWithBlocks(world, structureboundingbox, 8 + j1, 5 + j1, 54, 49 - j1, 5 + j1, 54, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            }

            this.fillWithBlocks(world, structureboundingbox, 11, 8, 54, 46, 8, 54, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 14, 8, 44, 43, 8, 53, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }
      }

      private void generateUpperWall(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.doesChunkIntersect(structureboundingbox, 14, 21, 20, 43)) {
            this.fillWithBlocks(world, structureboundingbox, 14, 0, 21, 20, 0, 43, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 14, 1, 22, 20, 14, 43, false);
            this.fillWithBlocks(world, structureboundingbox, 18, 12, 22, 20, 12, 39, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 18, 12, 21, 20, 12, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);

            for (int i = 0; i < 4; i++) {
               this.fillWithBlocks(world, structureboundingbox, i + 14, i + 9, 21, i + 14, i + 9, 43 - i, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int j = 23; j <= 39; j += 3) {
               this.setBlockState(world, DOT_DECO_DATA, 19, 13, j, structureboundingbox);
            }
         }

         if (this.doesChunkIntersect(structureboundingbox, 37, 21, 43, 43)) {
            this.fillWithBlocks(world, structureboundingbox, 37, 0, 21, 43, 0, 43, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 37, 1, 22, 43, 14, 43, false);
            this.fillWithBlocks(world, structureboundingbox, 37, 12, 22, 39, 12, 39, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 37, 12, 21, 39, 12, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);

            for (int k = 0; k < 4; k++) {
               this.fillWithBlocks(world, structureboundingbox, 43 - k, k + 9, 21, 43 - k, k + 9, 43 - k, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int l = 23; l <= 39; l += 3) {
               this.setBlockState(world, DOT_DECO_DATA, 38, 13, l, structureboundingbox);
            }
         }

         if (this.doesChunkIntersect(structureboundingbox, 15, 37, 42, 43)) {
            this.fillWithBlocks(world, structureboundingbox, 21, 0, 37, 36, 0, 43, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.generateWaterBox(world, structureboundingbox, 21, 1, 37, 36, 14, 43, false);
            this.fillWithBlocks(world, structureboundingbox, 21, 12, 37, 36, 12, 39, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);

            for (int i1 = 0; i1 < 4; i1++) {
               this.fillWithBlocks(world, structureboundingbox, 15 + i1, i1 + 9, 43 - i1, 42 - i1, i1 + 9, 43 - i1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            for (int j1 = 21; j1 <= 36; j1 += 3) {
               this.setBlockState(world, DOT_DECO_DATA, j1, 13, 38, structureboundingbox);
            }
         }
      }
   }

   public static class MonumentCoreRoom extends MonumentPieces.Piece {
      public MonumentCoreRoom() {
      }

      public MonumentCoreRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 2, 2, 2);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         this.generateBoxOnFillOnly(world, structureboundingbox, 1, 8, 0, 14, 8, 14, ROUGH_PRISMARINE);
         int i = 7;
         BlockState iblockstate = BRICKS_PRISMARINE;
         this.fillWithBlocks(world, structureboundingbox, 0, 7, 0, 0, 7, 15, iblockstate, iblockstate, false);
         this.fillWithBlocks(world, structureboundingbox, 15, 7, 0, 15, 7, 15, iblockstate, iblockstate, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 7, 0, 15, 7, 0, iblockstate, iblockstate, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 7, 15, 14, 7, 15, iblockstate, iblockstate, false);

         for (int k = 1; k <= 6; k++) {
            iblockstate = BRICKS_PRISMARINE;
            if (k == 2 || k == 6) {
               iblockstate = ROUGH_PRISMARINE;
            }

            for (int j = 0; j <= 15; j += 15) {
               this.fillWithBlocks(world, structureboundingbox, j, k, 0, j, k, 1, iblockstate, iblockstate, false);
               this.fillWithBlocks(world, structureboundingbox, j, k, 6, j, k, 9, iblockstate, iblockstate, false);
               this.fillWithBlocks(world, structureboundingbox, j, k, 14, j, k, 15, iblockstate, iblockstate, false);
            }

            this.fillWithBlocks(world, structureboundingbox, 1, k, 0, 1, k, 0, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 6, k, 0, 9, k, 0, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 14, k, 0, 14, k, 0, iblockstate, iblockstate, false);
            this.fillWithBlocks(world, structureboundingbox, 1, k, 15, 14, k, 15, iblockstate, iblockstate, false);
         }

         this.fillWithBlocks(world, structureboundingbox, 6, 3, 6, 9, 6, 9, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 4, 7, 8, 5, 8, Blocks.GOLD_BLOCK.defaultBlockState(), Blocks.GOLD_BLOCK.defaultBlockState(), false);

         for (int l = 3; l <= 6; l += 3) {
            for (int i1 = 6; i1 <= 9; i1 += 3) {
               this.setBlockState(world, SEA_LANTERN, i1, l, 6, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, i1, l, 9, structureboundingbox);
            }
         }

         this.fillWithBlocks(world, structureboundingbox, 5, 1, 6, 5, 2, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 1, 9, 5, 2, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 1, 6, 10, 2, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 1, 9, 10, 2, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 1, 5, 6, 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 9, 1, 5, 9, 2, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 1, 10, 6, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 9, 1, 10, 9, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 2, 5, 5, 6, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 2, 10, 5, 6, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 2, 5, 10, 6, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 2, 10, 10, 6, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 7, 1, 5, 7, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 7, 1, 10, 7, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 5, 7, 9, 5, 7, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 7, 9, 10, 7, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 7, 5, 6, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 7, 10, 6, 7, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 9, 7, 5, 14, 7, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 9, 7, 10, 14, 7, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 1, 2, 2, 1, 3, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 2, 3, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 13, 1, 2, 13, 1, 3, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 12, 1, 2, 12, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, 1, 12, 2, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 13, 3, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 13, 1, 12, 13, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 12, 1, 13, 12, 1, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         return true;
      }
   }

   interface MonumentRoomFitHelper {
      boolean fits(MonumentPieces.RoomDefinition roomdefinition);

      MonumentPieces.Piece create(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random);
   }

   public static class Penthouse extends MonumentPieces.Piece {
      public Penthouse() {
      }

      public Penthouse(Direction facing, StructureBoundingBox structureboundingbox) {
         super(facing, structureboundingbox);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         this.fillWithBlocks(world, structureboundingbox, 2, -1, 2, 11, -1, 11, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, -1, 0, 1, -1, 11, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 12, -1, 0, 13, -1, 11, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, -1, 0, 11, -1, 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 2, -1, 12, 11, -1, 13, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 0, 0, 0, 0, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 13, 0, 0, 13, 0, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 0, 0, 12, 0, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 0, 13, 12, 0, 13, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);

         for (int i = 2; i <= 11; i += 3) {
            this.setBlockState(world, SEA_LANTERN, 0, 0, i, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 13, 0, i, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, i, 0, 0, structureboundingbox);
         }

         this.fillWithBlocks(world, structureboundingbox, 2, 0, 3, 4, 0, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 9, 0, 3, 11, 0, 9, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 4, 0, 9, 9, 0, 11, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.setBlockState(world, BRICKS_PRISMARINE, 5, 0, 8, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 8, 0, 8, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 10, 0, 10, structureboundingbox);
         this.setBlockState(world, BRICKS_PRISMARINE, 3, 0, 10, structureboundingbox);
         this.fillWithBlocks(world, structureboundingbox, 3, 0, 3, 3, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 10, 0, 3, 10, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, 0, 10, 7, 0, 10, DARK_PRISMARINE, DARK_PRISMARINE, false);
         int l = 3;

         for (int j = 0; j < 2; j++) {
            for (int k = 2; k <= 8; k += 3) {
               this.fillWithBlocks(world, structureboundingbox, l, 0, k, l, 2, k, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            l = 10;
         }

         this.fillWithBlocks(world, structureboundingbox, 5, 0, 10, 5, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 8, 0, 10, 8, 2, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 6, -1, 7, 7, -1, 8, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.generateWaterBox(world, structureboundingbox, 6, -1, 3, 7, -1, 4, false);
         this.spawnElder(world, structureboundingbox, 6, 1, 6);
         return true;
      }
   }

   public abstract static class Piece extends StructureComponent {
      protected static final BlockState ROUGH_PRISMARINE = Blocks.PRISMARINE.defaultBlockState();
      protected static final BlockState BRICKS_PRISMARINE = Blocks.PRISMARINE_BRICKS.defaultBlockState();
      protected static final BlockState DARK_PRISMARINE = Blocks.DARK_PRISMARINE.defaultBlockState();
      protected static final BlockState DOT_DECO_DATA = BRICKS_PRISMARINE;
      protected static final BlockState SEA_LANTERN = Blocks.SEA_LANTERN.defaultBlockState();
      protected static final BlockState WATER = Blocks.WATER.defaultBlockState();
      protected static final int GRIDROOM_SOURCE_INDEX = getRoomIndex(2, 0, 0);
      protected static final int GRIDROOM_TOP_CONNECT_INDEX = getRoomIndex(2, 2, 0);
      protected static final int GRIDROOM_LEFTWING_CONNECT_INDEX = getRoomIndex(0, 1, 0);
      protected static final int GRIDROOM_RIGHTWING_CONNECT_INDEX = getRoomIndex(4, 1, 0);
      protected MonumentPieces.RoomDefinition roomDefinition;

      protected static final int getRoomIndex(int i, int j, int k) {
         return j * 25 + k * 5 + i;
      }

      public Piece() {
         super(0);
      }

      public Piece(int i) {
         super(i);
      }

      public Piece(Direction facing, StructureBoundingBox structureboundingbox) {
         super(1);
         this.setCoordBaseMode(facing);
         this.boundingBox = structureboundingbox;
      }

      protected Piece(
         int i, Direction facing, MonumentPieces.RoomDefinition roomdefinition, int j, int k, int l
      ) {
         super(i);
         this.setCoordBaseMode(facing);
         this.roomDefinition = roomdefinition;
         int i1 = roomdefinition.index;
         int j1 = i1 % 5;
         int k1 = i1 / 5 % 5;
         int l1 = i1 / 25;
         if (facing != Direction.NORTH && facing != Direction.SOUTH) {
            this.boundingBox = new StructureBoundingBox(0, 0, 0, l * 8 - 1, k * 4 - 1, j * 8 - 1);
         } else {
            this.boundingBox = new StructureBoundingBox(0, 0, 0, j * 8 - 1, k * 4 - 1, l * 8 - 1);
         }

         switch (facing) {
            case NORTH:
               this.boundingBox.offset(j1 * 8, l1 * 4, -(k1 + l) * 8 + 1);
               break;
            case SOUTH:
               this.boundingBox.offset(j1 * 8, l1 * 4, k1 * 8);
               break;
            case WEST:
               this.boundingBox.offset(-(k1 + l) * 8 + 1, l1 * 4, j1 * 8);
               break;
            default:
               this.boundingBox.offset(k1 * 8, l1 * 4, j1 * 8);
         }
      }



      protected void generateWaterBox(DWorld world, StructureBoundingBox structureboundingbox, int i, int j, int k, int l, int i1, int j1, boolean flag) {
         for (int k1 = j; k1 <= i1; k1++) {
            for (int l1 = i; l1 <= l; l1++) {
               for (int i2 = k; i2 <= j1; i2++) {
                  if (!flag || this.getBlockStateFromPos(world, l1, k1, i2, structureboundingbox).isAir() == false) {
                     if (this.getYWithOffset(k1) >= SEA_LEVEL) {
                        this.setBlockState(world, Blocks.AIR.defaultBlockState(), l1, k1, i2, structureboundingbox);
                     } else {
                        this.setBlockState(world, WATER, l1, k1, i2, structureboundingbox);
                     }
                  }
               }
            }
         }
      }

      protected void generateDefaultFloor(DWorld world, StructureBoundingBox structureboundingbox, int i, int j, boolean flag) {
         if (flag) {
            this.fillWithBlocks(world, structureboundingbox, i + 0, 0, j + 0, i + 2, 0, j + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 5, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 3, 0, j + 0, i + 4, 0, j + 2, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 3, 0, j + 5, i + 4, 0, j + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 3, 0, j + 2, i + 4, 0, j + 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 3, 0, j + 5, i + 4, 0, j + 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 2, 0, j + 3, i + 2, 0, j + 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, i + 5, 0, j + 3, i + 5, 0, j + 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         } else {
            this.fillWithBlocks(world, structureboundingbox, i + 0, 0, j + 0, i + 8 - 1, 0, j + 8 - 1, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
         }
      }

      protected void generateBoxOnFillOnly(
         DWorld world, StructureBoundingBox structureboundingbox, int i, int j, int k, int l, int i1, int j1, BlockState iblockstate
      ) {
         for (int k1 = j; k1 <= i1; k1++) {
            for (int l1 = i; l1 <= l; l1++) {
               for (int i2 = k; i2 <= j1; i2++) {
                  if (this.getBlockStateFromPos(world, l1, k1, i2, structureboundingbox) == WATER) {
                     this.setBlockState(world, iblockstate, l1, k1, i2, structureboundingbox);
                  }
               }
            }
         }
      }

      protected boolean doesChunkIntersect(StructureBoundingBox structureboundingbox, int i, int j, int k, int l) {
         int i1 = this.getXWithOffset(i, j);
         int j1 = this.getZWithOffset(i, j);
         int k1 = this.getXWithOffset(k, l);
         int l1 = this.getZWithOffset(k, l);
         return structureboundingbox.intersectsWith(Math.min(i1, k1), Math.min(j1, l1), Math.max(i1, k1), Math.max(j1, l1));
      }

      protected boolean spawnElder(DWorld world, StructureBoundingBox structureboundingbox, int i, int j, int k) {
         int l = this.getXWithOffset(i, k);
         int i1 = this.getYWithOffset(j);
         int j1 = this.getZWithOffset(i, k);
         if (structureboundingbox.isVecInside(new BlockPos(l, i1, j1))) {
            ElderGuardian entityelderguardian = EntityType.ELDER_GUARDIAN.create(world.level.getLevel());
            if (entityelderguardian != null) {
               entityelderguardian.heal(entityelderguardian.getMaxHealth());
               entityelderguardian.moveTo((double)l + 0.5, (double)i1, (double)j1 + 0.5, 0.0F, 0.0F);
               entityelderguardian.finalizeSpawn(world.level, world.level.getCurrentDifficultyAt(entityelderguardian.blockPosition()), MobSpawnType.STRUCTURE, null);
               world.level.addFreshEntityWithPassengers(entityelderguardian);
            }
            return true;
         } else {
            return false;
         }
      }
   }

   static class RoomDefinition {
      int index;
      MonumentPieces.RoomDefinition[] connections = new MonumentPieces.RoomDefinition[6];
      boolean[] hasOpening = new boolean[6];
      boolean claimed;
      boolean isSource;
      int scanIndex;

      public RoomDefinition(int i) {
         this.index = i;
      }

      public void setConnection(Direction facing, MonumentPieces.RoomDefinition roomdefinition1) {
         this.connections[facing.get3DDataValue()] = roomdefinition1;
         roomdefinition1.connections[facing.getOpposite().get3DDataValue()] = this;
      }

      public void updateOpenings() {
         for (int i = 0; i < 6; i++) {
            this.hasOpening[i] = this.connections[i] != null;
         }
      }

      public boolean findSource(int i) {
         if (this.isSource) {
            return true;
         } else {
            this.scanIndex = i;

            for (int j = 0; j < 6; j++) {
               if (this.connections[j] != null && this.hasOpening[j] && this.connections[j].scanIndex != i && this.connections[j].findSource(i)) {
                  return true;
               }
            }

            return false;
         }
      }

      public boolean isSpecial() {
         return this.index >= 75;
      }

      public int countOpenings() {
         int i = 0;

         for (int j = 0; j < 6; j++) {
            if (this.hasOpening[j]) {
               i++;
            }
         }

         return i;
      }
   }

   public static class SimpleRoom extends MonumentPieces.Piece {
      private int mainDesign;

      public SimpleRoom() {
      }

      public SimpleRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 1, 1, 1);
         this.mainDesign = random.nextInt(3);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 4, 1, 6, 4, 6, ROUGH_PRISMARINE);
         }

         boolean flag = this.mainDesign != 0
            && random.nextBoolean()
            && !this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]
            && !this.roomDefinition.hasOpening[Direction.UP.get3DDataValue()]
            && this.roomDefinition.countOpenings() > 1;
         if (this.mainDesign == 0) {
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 2, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 3, 0, 2, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 2, 0, 0, 2, 2, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 2, 0, 2, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(world, SEA_LANTERN, 1, 2, 1, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 5, 1, 0, 7, 1, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 3, 0, 7, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 2, 0, 7, 2, 2, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 2, 0, 6, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(world, SEA_LANTERN, 6, 2, 1, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 5, 2, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 3, 5, 2, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 2, 5, 0, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 2, 7, 2, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(world, SEA_LANTERN, 1, 2, 6, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 5, 1, 5, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 3, 5, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 2, 5, 7, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 2, 7, 6, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.setBlockState(world, SEA_LANTERN, 6, 2, 6, structureboundingbox);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 3, 3, 0, 4, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, 3, 3, 0, 4, 3, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 3, 2, 0, 4, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 3, 1, 0, 4, 1, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 3, 3, 7, 4, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, 3, 3, 6, 4, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 3, 2, 7, 4, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 3, 1, 6, 4, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 0, 3, 3, 0, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, 0, 3, 3, 1, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 0, 2, 3, 0, 2, 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 0, 1, 3, 1, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 7, 3, 3, 7, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            } else {
               this.fillWithBlocks(world, structureboundingbox, 6, 3, 3, 7, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 7, 2, 3, 7, 2, 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 6, 1, 3, 7, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }
         } else if (this.mainDesign == 1) {
            this.fillWithBlocks(world, structureboundingbox, 2, 1, 2, 2, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 2, 1, 5, 2, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 1, 5, 5, 3, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 5, 1, 2, 5, 3, 2, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(world, SEA_LANTERN, 2, 2, 2, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 2, 2, 5, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 5, 2, 5, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 5, 2, 2, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 1, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 1, 0, 3, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 7, 1, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 6, 0, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 1, 7, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 1, 6, 7, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 1, 0, 7, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 1, 1, 7, 3, 1, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.setBlockState(world, ROUGH_PRISMARINE, 1, 2, 0, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 0, 2, 1, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 1, 2, 7, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 0, 2, 6, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 6, 2, 7, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 7, 2, 6, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 6, 2, 0, structureboundingbox);
            this.setBlockState(world, ROUGH_PRISMARINE, 7, 2, 1, structureboundingbox);
            if (!this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 1, 2, 0, 6, 2, 0, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 1, 2, 7, 6, 2, 7, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 0, 3, 1, 0, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 0, 2, 1, 0, 2, 6, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 0, 1, 1, 0, 1, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            if (!this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.fillWithBlocks(world, structureboundingbox, 7, 3, 1, 7, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 7, 2, 1, 7, 2, 6, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, 7, 1, 1, 7, 1, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }
         } else if (this.mainDesign == 2) {
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
               this.generateWaterBox(world, structureboundingbox, 3, 1, 0, 4, 2, 0, false);
            }

            if (this.roomDefinition.hasOpening[Direction.NORTH.get3DDataValue()]) {
               this.generateWaterBox(world, structureboundingbox, 3, 1, 7, 4, 2, 7, false);
            }

            if (this.roomDefinition.hasOpening[Direction.WEST.get3DDataValue()]) {
               this.generateWaterBox(world, structureboundingbox, 0, 1, 3, 0, 2, 4, false);
            }

            if (this.roomDefinition.hasOpening[Direction.EAST.get3DDataValue()]) {
               this.generateWaterBox(world, structureboundingbox, 7, 1, 3, 7, 2, 4, false);
            }
         }

         if (flag) {
            this.fillWithBlocks(world, structureboundingbox, 3, 1, 3, 4, 1, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 3, 2, 3, 4, 2, 4, ROUGH_PRISMARINE, ROUGH_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 3, 3, 3, 4, 3, 4, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         }

         return true;
      }
   }

   public static class SimpleTopRoom extends MonumentPieces.Piece {
      public SimpleTopRoom() {
      }

      public SimpleTopRoom(Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random) {
         super(1, facing, roomdefinition, 1, 1, 1);
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.roomDefinition.index / 25 > 0) {
            this.generateDefaultFloor(world, structureboundingbox, 0, 0, this.roomDefinition.hasOpening[Direction.DOWN.get3DDataValue()]);
         }

         if (this.roomDefinition.connections[Direction.UP.get3DDataValue()] == null) {
            this.generateBoxOnFillOnly(world, structureboundingbox, 1, 4, 1, 6, 4, 6, ROUGH_PRISMARINE);
         }

         for (int i = 1; i <= 6; i++) {
            for (int j = 1; j <= 6; j++) {
               if (random.nextInt(3) != 0) {
                  int k = 2 + (random.nextInt(4) == 0 ? 0 : 1);
                  this.fillWithBlocks(
                     world, structureboundingbox, i, k, j, i, 3, j, Blocks.WET_SPONGE.defaultBlockState(), Blocks.WET_SPONGE.defaultBlockState(), false
                  );
               }
            }
         }

         this.fillWithBlocks(world, structureboundingbox, 0, 1, 0, 0, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 1, 0, 7, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 0, 6, 1, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 1, 7, 6, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 2, 0, 0, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 2, 0, 7, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 2, 0, 6, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 2, 7, 6, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 3, 0, 0, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 3, 0, 7, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 0, 6, 3, 0, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 1, 3, 7, 6, 3, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 0, 1, 3, 0, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 7, 1, 3, 7, 2, 4, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 0, 4, 2, 0, DARK_PRISMARINE, DARK_PRISMARINE, false);
         this.fillWithBlocks(world, structureboundingbox, 3, 1, 7, 4, 2, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
         if (this.roomDefinition.hasOpening[Direction.SOUTH.get3DDataValue()]) {
            this.generateWaterBox(world, structureboundingbox, 3, 1, 0, 4, 2, 0, false);
         }

         return true;
      }
   }

   public static class WingRoom extends MonumentPieces.Piece {
      private int mainDesign;

      public WingRoom() {
      }

      public WingRoom(Direction facing, StructureBoundingBox structureboundingbox, int i) {
         super(facing, structureboundingbox);
         this.mainDesign = i & 1;
      }

      @Override
      public boolean addComponentParts(DWorld world, Random random, StructureBoundingBox structureboundingbox) {
         if (this.mainDesign == 0) {
            for (int i = 0; i < 4; i++) {
               this.fillWithBlocks(world, structureboundingbox, 10 - i, 3 - i, 20 - i, 12 + i, 3 - i, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            }

            this.fillWithBlocks(world, structureboundingbox, 7, 0, 6, 15, 0, 16, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 6, 0, 6, 6, 3, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 16, 0, 6, 16, 3, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 1, 7, 7, 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 15, 1, 7, 15, 1, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 7, 1, 6, 9, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 13, 1, 6, 15, 3, 6, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 8, 1, 7, 9, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 13, 1, 7, 14, 1, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 9, 0, 5, 13, 0, 5, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 10, 0, 7, 12, 0, 7, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 8, 0, 10, 8, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 14, 0, 10, 14, 0, 12, DARK_PRISMARINE, DARK_PRISMARINE, false);

            for (int i1 = 18; i1 >= 7; i1 -= 3) {
               this.setBlockState(world, SEA_LANTERN, 6, 3, i1, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, 16, 3, i1, structureboundingbox);
            }

            this.setBlockState(world, SEA_LANTERN, 10, 0, 10, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 12, 0, 10, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 10, 0, 12, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 12, 0, 12, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 8, 3, 6, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 14, 3, 6, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 4, 2, 4, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 4, 1, 4, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 4, 0, 4, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 18, 2, 4, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 18, 1, 4, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 18, 0, 4, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 4, 2, 18, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 4, 1, 18, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 4, 0, 18, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 18, 2, 18, structureboundingbox);
            this.setBlockState(world, SEA_LANTERN, 18, 1, 18, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 18, 0, 18, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 9, 7, 20, structureboundingbox);
            this.setBlockState(world, BRICKS_PRISMARINE, 13, 7, 20, structureboundingbox);
            this.fillWithBlocks(world, structureboundingbox, 6, 0, 21, 7, 4, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 15, 0, 21, 16, 4, 21, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.spawnElder(world, structureboundingbox, 11, 2, 16);
         } else if (this.mainDesign == 1) {
            this.fillWithBlocks(world, structureboundingbox, 9, 3, 18, 13, 3, 20, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 9, 0, 18, 9, 2, 18, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 13, 0, 18, 13, 2, 18, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            int j1 = 9;
            int j = 20;
            int k = 5;

            for (int l = 0; l < 2; l++) {
               this.setBlockState(world, BRICKS_PRISMARINE, j1, 6, 20, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, j1, 5, 20, structureboundingbox);
               this.setBlockState(world, BRICKS_PRISMARINE, j1, 4, 20, structureboundingbox);
               j1 = 13;
            }

            this.fillWithBlocks(world, structureboundingbox, 7, 3, 7, 15, 3, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
            byte b0 = 10;

            for (int k1 = 0; k1 < 2; k1++) {
               this.fillWithBlocks(world, structureboundingbox, b0, 0, 10, b0, 6, 10, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, b0, 0, 12, b0, 6, 12, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.setBlockState(world, SEA_LANTERN, b0, 0, 10, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, b0, 0, 12, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, b0, 4, 10, structureboundingbox);
               this.setBlockState(world, SEA_LANTERN, b0, 4, 12, structureboundingbox);
               b0 = 12;
            }

            b0 = 8;

            for (int l1 = 0; l1 < 2; l1++) {
               this.fillWithBlocks(world, structureboundingbox, b0, 0, 7, b0, 2, 7, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               this.fillWithBlocks(world, structureboundingbox, b0, 0, 14, b0, 2, 14, BRICKS_PRISMARINE, BRICKS_PRISMARINE, false);
               b0 = 14;
            }

            this.fillWithBlocks(world, structureboundingbox, 8, 3, 8, 8, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.fillWithBlocks(world, structureboundingbox, 14, 3, 8, 14, 3, 13, DARK_PRISMARINE, DARK_PRISMARINE, false);
            this.spawnElder(world, structureboundingbox, 11, 5, 13);
         }

         return true;
      }
   }

   static class XDoubleRoomFitHelper implements MonumentPieces.MonumentRoomFitHelper {
      private XDoubleRoomFitHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         return roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]
            && !roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed;
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         roomdefinition.claimed = true;
         roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
         return new MonumentPieces.DoubleXRoom(facing, roomdefinition, random);
      }
   }

   static class XYDoubleRoomFitHelper implements MonumentPieces.MonumentRoomFitHelper {
      private XYDoubleRoomFitHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         if (roomdefinition.hasOpening[Direction.EAST.get3DDataValue()]
            && !roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed
            && roomdefinition.hasOpening[Direction.UP.get3DDataValue()]
            && !roomdefinition.connections[Direction.UP.get3DDataValue()].claimed) {
            MonumentPieces.RoomDefinition roomdefinition1 = roomdefinition.connections[Direction.EAST
               .get3DDataValue()];
            return roomdefinition1.hasOpening[Direction.UP.get3DDataValue()]
               && !roomdefinition1.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         roomdefinition.claimed = true;
         roomdefinition.connections[Direction.EAST.get3DDataValue()].claimed = true;
         roomdefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
         roomdefinition.connections[Direction.EAST.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new MonumentPieces.DoubleXYRoom(facing, roomdefinition, random);
      }
   }

   static class YDoubleRoomFitHelper implements MonumentPieces.MonumentRoomFitHelper {
      private YDoubleRoomFitHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         return roomdefinition.hasOpening[Direction.UP.get3DDataValue()]
            && !roomdefinition.connections[Direction.UP.get3DDataValue()].claimed;
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         roomdefinition.claimed = true;
         roomdefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
         return new MonumentPieces.DoubleYRoom(facing, roomdefinition, random);
      }
   }

   static class YZDoubleRoomFitHelper implements MonumentPieces.MonumentRoomFitHelper {
      private YZDoubleRoomFitHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         if (roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]
            && !roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed
            && roomdefinition.hasOpening[Direction.UP.get3DDataValue()]
            && !roomdefinition.connections[Direction.UP.get3DDataValue()].claimed) {
            MonumentPieces.RoomDefinition roomdefinition1 = roomdefinition.connections[Direction.NORTH
               .get3DDataValue()];
            return roomdefinition1.hasOpening[Direction.UP.get3DDataValue()]
               && !roomdefinition1.connections[Direction.UP.get3DDataValue()].claimed;
         } else {
            return false;
         }
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         roomdefinition.claimed = true;
         roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         roomdefinition.connections[Direction.UP.get3DDataValue()].claimed = true;
         roomdefinition.connections[Direction.NORTH.get3DDataValue()].connections[Direction.UP.get3DDataValue()].claimed = true;
         return new MonumentPieces.DoubleYZRoom(facing, roomdefinition, random);
      }
   }

   static class ZDoubleRoomFitHelper implements MonumentPieces.MonumentRoomFitHelper {
      private ZDoubleRoomFitHelper() {
      }

      @Override
      public boolean fits(MonumentPieces.RoomDefinition roomdefinition) {
         return roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]
            && !roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed;
      }

      @Override
      public MonumentPieces.Piece create(
         Direction facing, MonumentPieces.RoomDefinition roomdefinition, Random random
      ) {
         MonumentPieces.RoomDefinition roomdefinition1 = roomdefinition;
         if (!roomdefinition.hasOpening[Direction.NORTH.get3DDataValue()]
            || roomdefinition.connections[Direction.NORTH.get3DDataValue()].claimed) {
            roomdefinition1 = roomdefinition.connections[Direction.SOUTH.get3DDataValue()];
         }

         roomdefinition1.claimed = true;
         roomdefinition1.connections[Direction.NORTH.get3DDataValue()].claimed = true;
         return new MonumentPieces.DoubleZRoom(facing, roomdefinition1, random);
      }
   }
}
