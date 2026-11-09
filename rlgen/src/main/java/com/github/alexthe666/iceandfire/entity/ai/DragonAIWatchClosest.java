package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIWatchClosest;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

/** Ice and Fire 1.7.1 DragonAIWatchClosest. */
public class DragonAIWatchClosest extends EntityAIWatchClosest {
   public DragonAIWatchClosest(Mob entitylivingIn, Class<? extends Entity> watchTargetClass, float maxDistance) {
      super(entitylivingIn, watchTargetClass, maxDistance);
   }

   @Override
   public boolean shouldExecute() {
      return (!(this.entity instanceof EntityDragonBase) || ((EntityDragonBase)this.entity).canMove())
            && ((EntityDragonBase)this.entity).getAnimation() != EntityDragonBase.ANIMATION_SHAKEPREY
         ? super.shouldExecute()
         : false;
   }
}
