package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityCockatrice;
import com.github.alexthe666.iceandfire.entity.LandUtils;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** 1.12 CockatriceAITargetItems: a hurt cockatrice walks to dropped seeds or rotten flesh and eats them. */
public class CockatriceAITargetItems extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<ItemEntity> targetEntitySelector;
   private final int targetChance;
   protected ItemEntity targetEntity;

   public CockatriceAITargetItems(EntityCockatrice creature, boolean checkSight) {
      this(creature, checkSight, false);
   }

   public CockatriceAITargetItems(EntityCockatrice creature, boolean checkSight, boolean onlyNearby) {
      super(creature, checkSight, onlyNearby);
      this.targetChance = 0;
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(creature);
      this.targetEntitySelector = item -> item != null && !item.getItem().isEmpty() && (item.getItem().getItem() == Items.ROTTEN_FLESH || LandUtils.isSeeds(item.getItem()));
   }

   @Override
   public boolean shouldExecute() {
      if (!((EntityCockatrice)this.taskOwner).canMove()) {
         return false;
      } else if (this.taskOwner.getHealth() >= this.taskOwner.getMaxHealth()) {
         return false;
      } else {
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
   }

   /** 1.12 expand(d, 4, d): grows towards the positive axes only. */
   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().expandTowards(targetDistance, 4.0, targetDistance);
   }

   @Override
   public void startExecuting() {
      this.taskOwner.getNavigation().moveTo(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), 1.0);
      super.startExecuting();
   }

   @Override
   public void updateTask() {
      super.updateTask();
      if (this.targetEntity == null || this.targetEntity != null && !this.targetEntity.isAlive()) {
         this.resetTask();
      }

      if (this.targetEntity != null && this.targetEntity.isAlive() && this.taskOwner.distanceToSqr(this.targetEntity) < 1.0) {
         EntityCockatrice cockatrice = (EntityCockatrice)this.taskOwner;
         this.targetEntity.getItem().shrink(1);
         this.taskOwner.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
         cockatrice.heal(8.0F);
         cockatrice.setAnimation(EntityCockatrice.ANIMATION_EAT);
         this.resetTask();
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.taskOwner.getNavigation().isDone();
   }
}
