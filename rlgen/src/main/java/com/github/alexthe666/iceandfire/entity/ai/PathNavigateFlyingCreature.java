package com.github.alexthe666.iceandfire.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.level.Level;

/** Ice and Fire 1.7.1 PathNavigateFlyingCreature: any position above air is a destination. */
public class PathNavigateFlyingCreature extends FlyingPathNavigation {
   public PathNavigateFlyingCreature(Mob entity, Level world) {
      super(entity, world);
   }

   @Override
   public boolean isStableDestination(BlockPos pos) {
      return this.level.isEmptyBlock(pos.below());
   }
}
