package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

/** 1.12 MyrmexAITradePlayer (mutex 5: move + jump) */
public class MyrmexAITradePlayer extends Goal {
   private final EntityMyrmexBase myrmex;

   public MyrmexAITradePlayer(EntityMyrmexBase myrmex) {
      this.myrmex = myrmex;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
   }

   @Override
   public boolean canUse() {
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
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      this.myrmex.getNavigation().stop();
   }

   @Override
   public void stop() {
      this.myrmex.setCustomer(null);
   }
}
