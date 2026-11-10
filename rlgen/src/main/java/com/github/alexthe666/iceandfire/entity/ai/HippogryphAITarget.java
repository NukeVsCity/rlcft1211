package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterUtils;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;

/** Ice and Fire 1.7.1 HippogryphAITarget */
public class HippogryphAITarget<T extends LivingEntity> extends EntityAINearestAttackableTarget<T> {
   private final EntityHippogryph hippogryph;

   public HippogryphAITarget(EntityHippogryph entityIn, Class<T> classTarget, boolean checkSight, Predicate<? super T> targetSelector) {
      super(entityIn, classTarget, 20, checkSight, false, targetSelector);
      this.hippogryph = entityIn;
   }

   @Override
   public boolean shouldExecute() {
      if (this.taskOwner.getRandom().nextInt(20) != 0) {
         return false;
      } else {
         if (super.shouldExecute() && this.targetEntity != null && !this.targetEntity.getClass().equals(this.hippogryph.getClass())
            && this.hippogryph.getBbWidth() >= this.targetEntity.getBbWidth()) {
            if (this.targetEntity instanceof Player) {
               return !this.hippogryph.isTame();
            }
            if (!this.hippogryph.isOwnedBy(this.targetEntity) && this.hippogryph.canMove() && this.targetEntity instanceof Animal) {
               if (this.hippogryph.isTame()) {
                  return AirWaterUtils.canTameDragonAttack(this.hippogryph, this.targetEntity);
               }
               return true;
            }
         }
         return false;
      }
   }
}
