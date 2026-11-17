package com.lycanitesmobs.compat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;

public final class LycBlocks {
   private LycBlocks() {
   }

   /** 1.12 IBlockState.isFullBlock / isFullCube: a full, solid cube. */
   public static boolean isFullBlock(BlockState state) {
      return state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
   }

   /** 1.12 Blocks.WOOL with the dye's metadata. */
   public static net.minecraft.world.level.block.Block wool(net.minecraft.world.item.DyeColor color) {
      return switch (color) {
         case WHITE -> net.minecraft.world.level.block.Blocks.WHITE_WOOL;
         case ORANGE -> net.minecraft.world.level.block.Blocks.ORANGE_WOOL;
         case MAGENTA -> net.minecraft.world.level.block.Blocks.MAGENTA_WOOL;
         case LIGHT_BLUE -> net.minecraft.world.level.block.Blocks.LIGHT_BLUE_WOOL;
         case YELLOW -> net.minecraft.world.level.block.Blocks.YELLOW_WOOL;
         case LIME -> net.minecraft.world.level.block.Blocks.LIME_WOOL;
         case PINK -> net.minecraft.world.level.block.Blocks.PINK_WOOL;
         case GRAY -> net.minecraft.world.level.block.Blocks.GRAY_WOOL;
         case LIGHT_GRAY -> net.minecraft.world.level.block.Blocks.LIGHT_GRAY_WOOL;
         case CYAN -> net.minecraft.world.level.block.Blocks.CYAN_WOOL;
         case PURPLE -> net.minecraft.world.level.block.Blocks.PURPLE_WOOL;
         case BLUE -> net.minecraft.world.level.block.Blocks.BLUE_WOOL;
         case BROWN -> net.minecraft.world.level.block.Blocks.BROWN_WOOL;
         case GREEN -> net.minecraft.world.level.block.Blocks.GREEN_WOOL;
         case RED -> net.minecraft.world.level.block.Blocks.RED_WOOL;
         case BLACK -> net.minecraft.world.level.block.Blocks.BLACK_WOOL;
      };
   }
}
