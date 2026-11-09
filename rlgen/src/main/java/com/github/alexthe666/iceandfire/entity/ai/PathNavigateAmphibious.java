package com.github.alexthe666.iceandfire.entity.ai;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;

/**
 * Ice and Fire 1.7.1 PathNavigateAmphibious: a copy of 1.12 PathNavigateGround that can swim and navigates while in liquid; that is the
 * 1.21 ground navigation with floating on.
 */
public class PathNavigateAmphibious extends GroundPathNavigation {
   public PathNavigateAmphibious(Mob mob, Level level) {
      super(mob, level);
      this.setCanFloat(true);
      this.setCanOpenDoors(false);
   }

   @Override
   protected boolean canUpdatePath() {
      return this.mob.onGround() || this.canFloat() && this.isInLiquid() || this.mob.isPassenger();
   }
}
