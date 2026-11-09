package com.github.alexthe666.iceandfire.block;

import com.github.alexthe666.iceandfire.core.IafNames;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 BlockFallingGeneric (ash, chared/frozen gravel): falling dust colour -8356741. */
public class BlockFallingGeneric extends FallingBlock {
   public static final MapCodec<BlockFallingGeneric> CODEC = simpleCodec(p -> new BlockFallingGeneric(p, "iceandfire.ash"));

   public BlockFallingGeneric(Properties props, String name) {
      super(props);
      IafNames.block(this, name);
   }

   @Override
   protected MapCodec<? extends FallingBlock> codec() {
      return CODEC;
   }

   @Override
   public int getDustColor(BlockState state, BlockGetter level, BlockPos pos) {
      return -8356741;
   }
}
