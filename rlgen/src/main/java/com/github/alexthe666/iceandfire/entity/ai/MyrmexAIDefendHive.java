package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.MyrmexHive;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAITarget;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 MyrmexAIDefendHive: attack the hive's aggressors, or now and then a player whose reputation is below 25. */
public class MyrmexAIDefendHive extends EntityAITarget {
   EntityMyrmexBase myrmex;
   LivingEntity villageAgressorTarget;

   public MyrmexAIDefendHive(EntityMyrmexBase myrmex) {
      super(myrmex, false, true);
      this.myrmex = myrmex;
   }

   @Override
   public boolean canUse() {
      MyrmexHive village = this.myrmex.getHive();
      if (this.myrmex.canMove() && village != null) {
         this.villageAgressorTarget = village.findNearestVillageAggressor(this.myrmex);
         if (this.isSuitableTarget(this.villageAgressorTarget, false)) {
            return true;
         } else if (this.taskOwner.getRandom().nextInt(20) == 0) {
            this.villageAgressorTarget = village.getNearestTargetPlayer(this.myrmex, this.myrmex.level());
            return this.isSuitableTarget(this.villageAgressorTarget, false);
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public void start() {
      this.myrmex.setTarget(this.villageAgressorTarget);
      super.start();
   }
}
