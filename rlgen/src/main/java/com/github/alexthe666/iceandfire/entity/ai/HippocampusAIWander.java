package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityHippocampus;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWander;
import net.minecraft.world.entity.PathfinderMob;

/** Ice and Fire 1.7.1 HippocampusAIWander: wanders on land only. */
public class HippocampusAIWander extends EntityAIWander {
   public HippocampusAIWander(PathfinderMob creatureIn, double speedIn) {
      super(creatureIn, speedIn);
   }

   private boolean sitting() {
      return this.entity instanceof EntityHippocampus h && h.isSitting();
   }

   @Override
   public boolean shouldExecute() {
      return !this.sitting() && !this.entity.isInWater() && super.shouldExecute();
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.sitting() && !this.entity.isInWater() && super.shouldContinueExecuting();
   }
}
