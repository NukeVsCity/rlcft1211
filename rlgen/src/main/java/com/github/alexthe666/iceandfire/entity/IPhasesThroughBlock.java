package com.github.alexthe666.iceandfire.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

<<<<<<< HEAD
/** Blocks this entity moves through: 1.12 removed their boxes in GetCollisionBoxesEvent, here mixin.BlockCollisionsMixin does it. */
public interface IPhasesThroughBlock {
   boolean canPhaseThroughBlock(Level world, BlockPos pos);
=======
/** Ice and Fire 1.7.1 IPhasesThroughBlock */
public interface IPhasesThroughBlock {
   boolean canPhaseThroughBlock(Level level, BlockPos pos);
>>>>>>> origin/iaf/mobs-land
}
