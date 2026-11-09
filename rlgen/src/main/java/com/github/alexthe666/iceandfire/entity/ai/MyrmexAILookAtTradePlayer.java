package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;

/** 1.12 MyrmexAILookAtTradePlayer */
public class MyrmexAILookAtTradePlayer extends LookAtPlayerGoal {
   private final EntityMyrmexBase myrmex;

   public MyrmexAILookAtTradePlayer(EntityMyrmexBase myrmex) {
      super(myrmex, Player.class, 8.0F);
      this.myrmex = myrmex;
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.isTrading()) {
         this.lookAt = this.myrmex.getCustomer();
         return true;
      } else {
         return false;
      }
   }
}
