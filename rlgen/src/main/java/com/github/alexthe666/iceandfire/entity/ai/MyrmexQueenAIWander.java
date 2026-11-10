package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;

/** 1.12 MyrmexQueenAIWander */
public class MyrmexQueenAIWander extends MyrmexAIWander {
   public MyrmexQueenAIWander(EntityMyrmexBase myrmex, double speed) {
      super(myrmex, speed);
   }

   @Override
   public boolean canUse() {
      return (this.myrmex.canSeeSky() || this.myrmex.getHive() == null) && super.canUse();
   }
}
