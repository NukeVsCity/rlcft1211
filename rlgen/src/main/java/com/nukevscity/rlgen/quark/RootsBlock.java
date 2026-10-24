package com.nukevscity.rlgen.quark;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Quark BlockRoots: vine-like roots hanging in caves (some with flowers); they don't spread like vines. */
public class RootsBlock extends VineBlock {
   public RootsBlock(Properties p) {
      super(p);
   }

   @Override
   protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
   }
}
