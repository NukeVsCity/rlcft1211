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
}
