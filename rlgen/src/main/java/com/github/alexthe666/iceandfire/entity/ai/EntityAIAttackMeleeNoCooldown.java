package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import net.minecraft.world.entity.PathfinderMob;

/** 1.12 EntityAIAttackMeleeNoCooldown */
public class EntityAIAttackMeleeNoCooldown extends EntityAIAttackMelee {
   public EntityAIAttackMeleeNoCooldown(PathfinderMob creature, double speed, boolean memory) {
      super(creature, speed, memory);
   }

   @Override
   public void updateTask() {
      super.updateTask();
      this.attackTick = 0;
   }
}
