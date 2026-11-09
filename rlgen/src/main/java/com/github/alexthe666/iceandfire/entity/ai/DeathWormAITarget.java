package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** 1.12 DeathWormAITarget (target chance 20, searches a cube of follow range in every direction). */
public class DeathWormAITarget<T extends LivingEntity> extends EntityAINearestAttackableTarget<T> {
   private EntityDeathWorm deathworm;

   public DeathWormAITarget(EntityDeathWorm entityIn, Class<T> classTarget, boolean checkSight, Predicate<? super T> targetSelector) {
      super(entityIn, classTarget, 20, checkSight, false, targetSelector);
      this.deathworm = entityIn;
   }

   @Override
   public boolean canUse() {
      if (super.canUse() && this.targetEntity != null && !this.targetEntity.getClass().equals(this.deathworm.getClass())) {
         if (this.targetEntity instanceof Player && !this.deathworm.isOwnedBy(this.targetEntity)) {
            return !this.deathworm.isTame();
         }

         if (!this.deathworm.isOwnedBy(this.targetEntity)) {
            return true;
         }

         if (this.targetEntity instanceof Monster && this.deathworm.getWormAge() > 2) {
            if (this.targetEntity instanceof Creeper) {
               return this.deathworm.getWormAge() > 3;
            }

            return true;
         }
      }

      return false;
   }

   @Override
   protected AABB getTargetableArea(double targetDistance) {
      return this.deathworm.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
   }
}
