package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.api.FoodUtils;
import com.github.alexthe666.iceandfire.entity.DragonUtils;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/**
 * Ice and Fire 1.7.1 DragonAITarget: every evaluation (no random chance) the nearest living thing within the follow range
 * (64 in RLCraft, in all three axes) that is not wider than the dragon, is edible (an AgeableMob or a player) and, when the
 * dragon is not hungry (hunger 90+), only players for wild dragons.
 */
public class DragonAITarget<T extends LivingEntity> extends EntityAINearestAttackableTarget<T> {
   private EntityDragonBase dragon;

   public DragonAITarget(EntityDragonBase entityIn, Class<T> classTarget, boolean checkSight, Predicate<? super T> targetSelector) {
      super(entityIn, classTarget, 0, checkSight, false, targetSelector);
      this.dragon = entityIn;
   }

   @Override
   public boolean shouldExecute() {
      if (super.shouldExecute() && this.targetEntity != null && !this.targetEntity.getClass().equals(this.dragon.getClass())) {
         float dragonSize = Math.max(this.dragon.getBbWidth(), this.dragon.getBbWidth() * (this.dragon.getRenderSize() / 3.0F));
         if (dragonSize >= this.targetEntity.getBbWidth()) {
            if (this.targetEntity instanceof EntityDragonBase) {
               EntityDragonBase dragon = (EntityDragonBase)this.targetEntity;
               if (dragon.getOwner() != null && this.dragon.getOwner() != null && this.dragon.isOwner(dragon.getOwner())) {
                  return false;
               }

               return !dragon.isModelDead();
            }

            if (this.targetEntity instanceof Player && this.dragon.isTame()) {
               return false;
            }

            if (!this.dragon.isOwner(this.targetEntity)
               && FoodUtils.getFoodPoints(this.targetEntity) > 0
               && this.dragon.canMove()
               && (this.dragon.getHunger() < 90 || !this.dragon.isTame() && this.targetEntity instanceof Player)) {
               if (this.dragon.isTame()) {
                  return DragonUtils.canTameDragonAttack(this.dragon, this.targetEntity);
               }

               return true;
            }
         }
      }

      return false;
   }

   @Override
   protected AABB getTargetableArea(double targetDistance) {
      return this.dragon.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
   }
}
