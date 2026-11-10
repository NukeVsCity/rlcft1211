package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWanderAvoidWater;

/** 1.12 MyrmexAIWander (EntityAIWanderAvoidWater while the myrmex has no hive) */
public class MyrmexAIWander extends EntityAIWanderAvoidWater {
   protected EntityMyrmexBase myrmex;

   public MyrmexAIWander(EntityMyrmexBase myrmex, double speed) {
      super(myrmex, speed);
      this.myrmex = myrmex;
   }

   @Override
   public boolean shouldExecute() {
      return this.myrmex.canMove() && this.myrmex.shouldWander() && super.shouldExecute();
   }
}
