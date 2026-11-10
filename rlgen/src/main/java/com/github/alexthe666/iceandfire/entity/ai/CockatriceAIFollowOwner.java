package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIFollowOwner;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;

/** 1.12 CockatriceAIFollowOwner (EntityAIFollowOwner while the command is "follow"). */
public class CockatriceAIFollowOwner extends EntityAIFollowOwner {
   EntityCockatrice cockatrice;

   public CockatriceAIFollowOwner(EntityCockatrice cockatrice, double speed, float minDist, float maxDist) {
      super(cockatrice, speed, minDist, maxDist);
      this.cockatrice = cockatrice;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      return super.shouldExecute() && this.cockatrice.getCommand() == 2;
   }
}
