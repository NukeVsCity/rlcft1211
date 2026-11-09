package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexEgg;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexWorker;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/** 1.12 MyrmexAIPickupBabies: a worker picks up larvae, pupae and eggs that are not in a nursery. */
public class MyrmexAIPickupBabies extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<LivingEntity> targetEntitySelector;
   protected LivingEntity targetEntity;
   public EntityMyrmexWorker myrmex;

   public MyrmexAIPickupBabies(EntityMyrmexWorker myrmex) {
      super(myrmex, false, false);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(myrmex);
      this.targetEntitySelector = m -> m != null
         && (
            m instanceof EntityMyrmexBase && ((EntityMyrmexBase)m).getGrowthStage() < 2 && !((EntityMyrmexBase)m).isInNursery()
               || m instanceof EntityMyrmexEgg && !((EntityMyrmexEgg)m).isInNursery()
         );
      this.myrmex = myrmex;
   }

   @Override
   public boolean canUse() {
      if (this.myrmex.canMove() && this.myrmex.getNavigation().isDone() && !this.myrmex.shouldEnterHive() && this.myrmex.keepSearching && !this.myrmex.holdingBaby()) {
         List<LivingEntity> listBabies = new ArrayList<>(
            this.taskOwner.level().getEntitiesOfClass(LivingEntity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)
         );
         if (listBabies.isEmpty()) {
            return false;
         } else {
            Collections.sort(listBabies, this.theNearestAttackableTargetSorter);
            this.targetEntity = listBabies.get(0);
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
      if (this.targetEntity == null || this.targetEntity != null && !this.targetEntity.isAlive()) {
         this.stop();
      }

      if (this.targetEntity != null && this.targetEntity.isAlive() && this.taskOwner.distanceToSqr(this.targetEntity) < 2.0) {
         this.targetEntity.startRiding(this.myrmex);
         this.stop();
      }
   }

   @Override
   public boolean canContinueToUse() {
      return !this.taskOwner.getNavigation().isDone();
   }
}
