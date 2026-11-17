package com.alcatrazescapee.notreepunching.common.tile;

import com.alcatrazescapee.alcatrazcore.tile.TileInventory;
import com.alcatrazescapee.notreepunching.NoTreePunching;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 1.12 TileLargeVessel: 9 slots, item handler on every side (hoppers work). */
public class TileLargeVessel extends TileInventory {
   public TileLargeVessel(BlockPos pos, BlockState state) {
      super(NoTreePunching.LARGE_VESSEL_TILE.get(), pos, state, 9);
   }
}
