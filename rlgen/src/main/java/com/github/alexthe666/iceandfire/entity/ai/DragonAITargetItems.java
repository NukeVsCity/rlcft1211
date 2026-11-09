package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.api.FoodUtils;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityIceDragon;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;

/** Ice and Fire 1.7.1 DragonAITargetItems: hungry dragons walk to dropped meat (and fish for ice dragons) and eat it. */
public class DragonAITargetItems extends EntityAITarget {
   protected final DragonAITargetItems.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<ItemEntity> targetEntitySelector;
   private final int targetChance;
   protected ItemEntity targetEntity;
   private boolean isIce = false;

   public DragonAITargetItems(PathfinderMob creature, boolean checkSight) {
      this(creature, checkSight, false);
   }

   public DragonAITargetItems(PathfinderMob creature, boolean checkSight, boolean onlyNearby) {
      this(creature, 20, checkSight, onlyNearby);
      this.isIce = creature instanceof EntityIceDragon;
   }

   public DragonAITargetItems(PathfinderMob creature, int chance, boolean checkSight, boolean onlyNearby) {
      super(creature, checkSight, onlyNearby);
      this.isIce = creature instanceof EntityIceDragon;
      this.targetChance = chance;
      this.theNearestAttackableTargetSorter = new DragonAITargetItems.Sorter(creature);
      this.setMutexBits(1);
      this.targetEntitySelector = item -> item != null && !item.getItem().isEmpty() && FoodUtils.getFoodPoints(item.getItem(), true, this.isIce) > 0;
   }

   @Override
   public boolean shouldExecute() {
      if (((EntityDragonBase)this.taskOwner).getHunger() >= 100) {
         return false;
      } else if (!((EntityDragonBase)this.taskOwner).canMove()) {
         return false;
      } else if (this.targetChance > 0 && this.taskOwner.getRandom().nextInt(10) != 0) {
         return false;
      } else {
         List<ItemEntity> list = this.taskOwner.level().getEntitiesOfClass(ItemEntity.class, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
         if (list.isEmpty()) {
            return false;
         } else {
            Collections.sort(list, this.theNearestAttackableTargetSorter);
            this.targetEntity = list.get(0);
            return true;
         }
      }
   }

   /** 1.12 AxisAlignedBB.expand: grows towards +x/+y/+z only. */
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
      if (this.targetEntity == null || this.targetEntity != null && this.targetEntity.isRemoved()) {
         this.resetTask();
      }

      if (this.targetEntity != null && !this.targetEntity.isRemoved() && this.taskOwner.distanceToSqr(this.targetEntity) < 1.0) {
         EntityDragonBase dragon = (EntityDragonBase)this.taskOwner;
         this.targetEntity.getItem().shrink(1);
         this.taskOwner.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
         int hunger = FoodUtils.getFoodPoints(this.targetEntity.getItem(), true, this.isIce);
         dragon.setHunger(Math.min(100, dragon.getHunger() + hunger));
         dragon.eatFoodBonus(this.targetEntity.getItem());
         this.taskOwner
            .setHealth(
               Math.min(this.taskOwner.getMaxHealth(), (float)((int)(this.taskOwner.getHealth() + (float)FoodUtils.getFoodPoints(this.targetEntity.getItem(), true, this.isIce))))
            );
         if (EntityDragonBase.ANIMATION_EAT != null) {
            dragon.setAnimation(EntityDragonBase.ANIMATION_EAT);
         }

         for (int i = 0; i < 4; i++) {
            dragon.spawnItemCrackParticles(this.targetEntity.getItem().getItem());
         }

         this.resetTask();
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.taskOwner.getNavigation().isDone();
   }

   public static class Sorter implements Comparator<Entity> {
      private final Entity theEntity;

      public Sorter(Entity theEntityIn) {
         this.theEntity = theEntityIn;
      }

      @Override
      public int compare(Entity a, Entity b) {
         double d0 = this.theEntity.distanceToSqr(a);
         double d1 = this.theEntity.distanceToSqr(b);
         return d0 < d1 ? -1 : (d0 > d1 ? 1 : 0);
      }
   }
}
