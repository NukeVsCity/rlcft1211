package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import net.minecraft.world.entity.player.Player;

/** 1.12 MyrmexAILookAtTradePlayer */
public class MyrmexAILookAtTradePlayer extends EntityAIWatchClosest {
   private final EntityMyrmexBase myrmex;

   public MyrmexAILookAtTradePlayer(EntityMyrmexBase myrmex) {
      super(myrmex, Player.class, 8.0F);
      this.myrmex = myrmex;
   }

   @Override
   public boolean shouldExecute() {
      if (this.myrmex.isTrading()) {
         this.closestEntity = this.myrmex.getCustomer();
         return true;
      } else {
         return false;
      }
   }
}
