package com.nukevscity.rlgen.rc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/** What RC reads (and writes) in the world. */
public interface RcWorldView {
   BlockState get(BlockPos pos);

   default boolean set(BlockPos pos, BlockState state) {
      return false;
   }

   /** The real level while spawning, null while preparing. */
   default WorldGenLevel level() {
      return null;
   }

   /** True if {@link #get} reads the real world here (not the terrain oracle). */
   default boolean isLive(BlockPos pos) {
      return false;
   }
}
