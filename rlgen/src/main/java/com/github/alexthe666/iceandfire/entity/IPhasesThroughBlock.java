package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** Blocks this entity moves through: 1.12 removed their boxes in GetCollisionBoxesEvent, here mixin.BlockCollisionsMixin does it. */
public interface IPhasesThroughBlock {
   boolean canPhaseThroughBlock(Level world, BlockPos pos);
}
