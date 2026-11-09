package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 IPhasesThroughBlock */
public interface IPhasesThroughBlock {
   boolean canPhaseThroughBlock(Level level, BlockPos pos);
}
