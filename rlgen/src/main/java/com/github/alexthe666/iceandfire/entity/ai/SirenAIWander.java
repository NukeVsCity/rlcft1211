package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntitySiren;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAIWander;
import net.minecraft.world.entity.PathfinderMob;

/** Ice and Fire 1.7.1 SirenAIWander */
public class SirenAIWander extends EntityAIWander {
   public SirenAIWander(PathfinderMob creatureIn, double speedIn) {
      super(creatureIn, speedIn);
   }

   @Override
   public boolean shouldExecute() {
      return !this.entity.isInWater() && !((EntitySiren)this.entity).isSinging() && super.shouldExecute();
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.entity.isInWater() && !((EntitySiren)this.entity).isSinging() && super.shouldContinueExecuting();
   }
}
