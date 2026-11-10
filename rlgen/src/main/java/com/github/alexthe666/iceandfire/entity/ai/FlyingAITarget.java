package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.phys.AABB;

/** Ice and Fire 1.7.1 FlyingAITarget: a cube-shaped search area; sea serpents only look for prey while in water and not jumping. */
public class FlyingAITarget<T extends LivingEntity> extends EntityAINearestAttackableTarget<T> {
   public FlyingAITarget(PathfinderMob creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby,
         @Nullable Predicate<? super T> targetSelector) {
      super(creature, classTarget, chance, checkSight, onlyNearby, targetSelector);
   }

   @Override
   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().inflate(targetDistance, targetDistance, targetDistance);
   }

   @Override
   public boolean shouldExecute() {
      return !(this.taskOwner instanceof EntitySeaSerpent serpent) || !serpent.isJumpingOutOfWater() && this.taskOwner.isInWater()
         ? super.shouldExecute()
         : false;
   }
}
