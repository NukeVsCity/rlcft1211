package com.github.alexthe666.iceandfire.entity.ai;

import javax.annotation.Nullable;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.Vec3;

/** Ice and Fire 1.7.1 SeaSerpentAIGetInWater: searches 30 x 20 blocks for water. */
public class SeaSerpentAIGetInWater extends AquaticAIGetInWater {
   public SeaSerpentAIGetInWater(PathfinderMob creature, double movementSpeedIn) {
      super(creature, movementSpeedIn);
   }

   @Override
   protected boolean isAttackerInWater() {
      return false;
   }

   @Nullable
   @Override
   public Vec3 findPossibleShelter() {
      return this.findPossibleShelter(30, 20);
   }
}
