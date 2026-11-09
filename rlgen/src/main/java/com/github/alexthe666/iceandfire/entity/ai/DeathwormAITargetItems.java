package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityDeathWorm;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

/** 1.12 DeathwormAITargetItems: eats dropped TNT and explodes 3 seconds later. */
public class DeathwormAITargetItems extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<ItemEntity> targetEntitySelector;
   protected ItemEntity targetEntity;

   public DeathwormAITargetItems(EntityDeathWorm creature, boolean checkSight) {
      this(creature, checkSight, false);
   }

   public DeathwormAITargetItems(EntityDeathWorm creature, boolean checkSight, boolean onlyNearby) {
      super(creature, checkSight, onlyNearby);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(creature);
      this.targetEntitySelector = item -> item != null && !item.getItem().isEmpty() && item.getItem().getItem() == Blocks.TNT.asItem();
   }

   @Override
   public boolean canUse() {
      List<ItemEntity> list = new ArrayList<>(
         this.taskOwner.level().getEntitiesOfClass(ItemEntity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)
      );
      if (list.isEmpty()) {
         return false;
      } else {
         Collections.sort(list, this.theNearestAttackableTargetSorter);
         this.targetEntity = list.get(0);
         return true;
      }
   }

   /** 1.12 expand(d, 4, d) */
   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().expandTowards(targetDistance, 4.0, targetDistance);
   }

   @Override
   public void start() {
      this.taskOwner.getNavigation().moveTo(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), 1.0);
      super.start();
   }

   @Override
   public boolean requiresUpdateEveryTick() {
      return true;
   }

   @Override
   public void tick() {
      super.tick();
      if (this.targetEntity == null || this.targetEntity != null && !this.targetEntity.isAlive()) {
         this.stop();
      }

      if (this.targetEntity != null && this.targetEntity.isAlive() && this.taskOwner.distanceToSqr(this.targetEntity) < 1.0) {
         EntityDeathWorm deathWorm = (EntityDeathWorm)this.taskOwner;
         this.targetEntity.getItem().shrink(1);
         this.taskOwner.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
         deathWorm.setAnimation(EntityDeathWorm.ANIMATION_BITE);
         deathWorm.setExplosive(true);
         this.stop();
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.taskOwner.getNavigation().isDone();
   }
}
