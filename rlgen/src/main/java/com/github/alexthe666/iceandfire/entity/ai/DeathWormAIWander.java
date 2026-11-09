package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;

/** 1.12 DeathWormAIWander (EntityAIWander, chance 120, out of sand only). */
public class DeathWormAIWander extends RandomStrollGoal {
   private EntityDeathWorm worm;

   public DeathWormAIWander(EntityDeathWorm creatureIn, double speedIn) {
      super(creatureIn, speedIn);
      this.worm = creatureIn;
   }

   @Override
   public boolean canUse() {
      return !this.worm.isInSand() && super.canUse();
   }

   @Override
   public boolean canContinueToUse() {
      return !this.worm.isInSand() && super.canContinueToUse();
   }
}
