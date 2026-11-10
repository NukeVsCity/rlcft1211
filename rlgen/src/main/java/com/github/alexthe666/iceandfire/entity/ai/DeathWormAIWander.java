package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWander;

/** 1.12 DeathWormAIWander (EntityAIWander, chance 120, out of sand only). */
public class DeathWormAIWander extends EntityAIWander {
   private EntityDeathWorm worm;

   public DeathWormAIWander(EntityDeathWorm creatureIn, double speedIn) {
      super(creatureIn, speedIn);
      this.worm = creatureIn;
   }

   @Override
   public boolean shouldExecute() {
      return !this.worm.isInSand() && super.shouldExecute();
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.worm.isInSand() && super.shouldContinueExecuting();
   }
}
