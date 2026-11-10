package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCyclops;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIAttackMelee;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;

/** 1.12 CyclopsAIAttackMelee */
public class CyclopsAIAttackMelee extends EntityAIAttackMelee {
   public CyclopsAIAttackMelee(EntityCyclops creature, double speedIn, boolean useLongMemory) {
      super(creature, speedIn, useLongMemory);
   }

   @Override
   protected void checkAndPerformAttack(LivingEntity entity, double distance) {
      double d0 = this.getAttackReachSqr(entity);
      if (this.isCyclopsBlinded() && distance >= 36.0) {
         this.resetTask();
      } else {
         if (distance <= d0 && this.attackTick <= 0) {
            this.attackTick = 20;
            this.attacker.swing(InteractionHand.MAIN_HAND);
            this.attacker.doHurtTarget(entity);
         }
      }
   }

   private boolean isCyclopsBlinded() {
      return this.attacker instanceof EntityCyclops && ((EntityCyclops)this.attacker).isBlinded();
   }
}
