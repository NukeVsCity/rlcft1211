package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.goal112.EntityAITarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

/** 1.12 MyrmexAIForageForItems: a worker picks up any dropped item that is not in water. */
public class MyrmexAIForageForItems extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<ItemEntity> targetEntitySelector;
   protected ItemEntity targetEntity;
   public EntityMyrmexWorker myrmex;

   public MyrmexAIForageForItems(EntityMyrmexWorker myrmex) {
      super(myrmex, false, false);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(myrmex);
      this.targetEntitySelector = item -> item != null && !item.getItem().isEmpty() && !item.isInWater();
      this.myrmex = myrmex;
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.canMove()
         && !this.myrmex.holdingBaby()
         && this.myrmex.getNavigation().isDone()
         && !this.myrmex.shouldEnterHive()
         && this.myrmex.keepSearching
         && this.myrmex.getTarget() == null) {
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
      } else {
         return false;
      }
   }

   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().inflate(targetDistance, 4.0, targetDistance);
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
      if (this.targetEntity == null || this.targetEntity != null && (!this.targetEntity.isAlive() || this.targetEntity.isInWater())) {
         this.stop();
      }

      if (this.targetEntity != null && this.targetEntity.isAlive() && this.taskOwner.distanceToSqr(this.targetEntity) < 1.0) {
         this.myrmex.onPickupItem(this.targetEntity);
         this.myrmex.setItemInHand(InteractionHand.MAIN_HAND, this.targetEntity.getItem());
         this.targetEntity.discard();
         this.stop();
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.taskOwner.getNavigation().isDone() && this.myrmex.getTarget() == null;
   }
}
