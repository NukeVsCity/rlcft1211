package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;
import net.minecraft.world.entity.player.Player;

/** 1.12 MyrmexAITradePlayer (mutex 5: move + jump) */
public class MyrmexAITradePlayer extends EntityAIBase {
   private final EntityMyrmexBase myrmex;

   public MyrmexAITradePlayer(EntityMyrmexBase myrmex) {
      this.myrmex = myrmex;
      this.setMutexBits(5);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.myrmex.isAlive()) {
         return false;
      } else if (this.myrmex.isInWater()) {
         return false;
      } else if (!this.myrmex.onGround()) {
         return false;
      } else if (this.myrmex.hurtMarked) {
         return false;
      } else {
         Player entityplayer = this.myrmex.getCustomer();
         if (entityplayer == null) {
            return false;
         } else {
            return this.myrmex.distanceToSqr(entityplayer) > 16.0 ? false : entityplayer.containerMenu != null;
         }
      }
   }

   @Override
   public void updateTask() {
      this.myrmex.getNavigation().stop();
   }

   @Override
   public void resetTask() {
      this.myrmex.setCustomer(null);
   }
}
