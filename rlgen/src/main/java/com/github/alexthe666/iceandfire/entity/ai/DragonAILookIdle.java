package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;

/** Ice and Fire 1.7.1 DragonAILookIdle. */
public class DragonAILookIdle extends EntityAIBase {
   private EntityDragonBase dragon;
   private double lookX;
   private double lookZ;
   private int idleTime;

   public DragonAILookIdle(EntityDragonBase prehistoric) {
      this.dragon = prehistoric;
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      return this.dragon.canMove() && this.dragon.getAnimation() != EntityDragonBase.ANIMATION_SHAKEPREY ? this.dragon.getRNG().nextFloat() < 0.02F : false;
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.idleTime >= 0;
   }

   @Override
   public void startExecuting() {
      double d0 = (Math.PI * 2) * this.dragon.getRNG().nextDouble();
      this.lookX = Math.cos(d0);
      this.lookZ = Math.sin(d0);
      this.idleTime = 20 + this.dragon.getRNG().nextInt(20);
   }

   @Override
   public void updateTask() {
      this.idleTime--;
      this.dragon
         .getLookControl()
         .setLookAt(
            this.dragon.getX() + this.lookX,
            this.dragon.getY() + (double)this.dragon.getEyeHeight(),
            this.dragon.getZ() + this.lookZ,
            (float)this.dragon.getHorizontalFaceSpeed(),
            (float)this.dragon.getVerticalFaceSpeed()
         );
   }
}
