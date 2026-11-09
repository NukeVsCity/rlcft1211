package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;

/** 1.12 CockatriceAIFollowOwner (EntityAIFollowOwner while the command is "follow"). */
public class CockatriceAIFollowOwner extends FollowOwnerGoal {
   EntityCockatrice cockatrice;

   public CockatriceAIFollowOwner(EntityCockatrice cockatrice, double speed, float minDist, float maxDist) {
      super(cockatrice, speed, minDist, maxDist);
      this.cockatrice = cockatrice;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
   }

   @Override
   public boolean canUse() {
      return super.canUse() && this.cockatrice.getCommand() == 2;
   }
}
