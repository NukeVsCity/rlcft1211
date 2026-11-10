package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.function.Predicate;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** 1.12 CockatriceAITarget */
public class CockatriceAITarget<T extends LivingEntity> extends EntityAINearestAttackableTarget<T> {
   private EntityCockatrice cockatrice;

   public CockatriceAITarget(EntityCockatrice entityIn, Class<T> classTarget, boolean checkSight, Predicate<? super T> targetSelector) {
      super(entityIn, classTarget, 0, checkSight, false, targetSelector);
      this.cockatrice = entityIn;
   }

   @Override
   public boolean shouldExecute() {
      if (this.taskOwner.getRandom().nextInt(20) == 0 && this.cockatrice.level().getDifficulty() != Difficulty.PEACEFUL) {
         if (super.shouldExecute() && this.targetEntity != null && !this.targetEntity.getClass().equals(this.cockatrice.getClass())) {
            if (this.targetEntity instanceof Player && !this.cockatrice.isOwnedBy(this.targetEntity)) {
               return !this.cockatrice.isTame();
            }

            if (!this.cockatrice.isOwnedBy(this.targetEntity) && this.cockatrice.canMove()) {
               return true;
            }
         }

         return false;
      } else {
         return false;
      }
   }
}
