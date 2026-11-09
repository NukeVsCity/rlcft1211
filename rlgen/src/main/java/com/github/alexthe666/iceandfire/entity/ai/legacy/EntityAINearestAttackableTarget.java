package com.github.alexthe666.iceandfire.entity.ai.legacy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** 1.12.2 net.minecraft.entity.ai.EntityAINearestAttackableTarget (living targets; the player-only branch is not needed). */
public class EntityAINearestAttackableTarget<T extends LivingEntity> extends EntityAITarget {
   protected final Class<T> targetClass;
   private final int targetChance;
   protected final EntityAINearestAttackableTarget.Sorter sorter;
   protected final Predicate<T> targetEntitySelector;
   protected T targetEntity;

   public EntityAINearestAttackableTarget(PathfinderMob creature, Class<T> classTarget, boolean checkSight) {
      this(creature, classTarget, checkSight, false);
   }

   public EntityAINearestAttackableTarget(PathfinderMob creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
      this(creature, classTarget, 10, checkSight, onlyNearby, null);
   }

   public EntityAINearestAttackableTarget(
      PathfinderMob creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate<? super T> targetSelector
   ) {
      super(creature, checkSight, onlyNearby);
      this.targetClass = classTarget;
      this.targetChance = chance;
      this.sorter = new EntityAINearestAttackableTarget.Sorter(creature);
      this.setMutexBits(1);
      this.targetEntitySelector = entity -> {
         if (entity == null) {
            return false;
         } else if (targetSelector != null && !targetSelector.test(entity)) {
            return false;
         } else {
            return !EntitySelector.NO_SPECTATORS.test(entity) ? false : this.isSuitableTarget(entity, false);
         }
      };
   }

   @Override
   public boolean shouldExecute() {
      if (this.targetChance > 0 && this.taskOwner.getRandom().nextInt(this.targetChance) != 0) {
         return false;
      } else if (this.targetClass != Player.class) {
         List<T> list = this.taskOwner.level().getEntitiesOfClass(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
         if (list.isEmpty()) {
            return false;
         } else {
            Collections.sort(list, this.sorter);
            this.targetEntity = list.get(0);
            return true;
         }
      } else {
         double range = this.getTargetDistance();
         Player best = null;
         double bestDist = -1.0;
         for (Player player : this.taskOwner.level().players()) {
            @SuppressWarnings("unchecked")
            T asT = (T)player;
            if (this.targetEntitySelector.test(asT)) {
               double d = player.distanceToSqr(this.taskOwner.getX(), this.taskOwner.getY() + (double)this.taskOwner.getEyeHeight(), this.taskOwner.getZ());
               if (d < range * range && (bestDist == -1.0 || d < bestDist)) {
                  bestDist = d;
                  best = player;
               }
            }
         }
         @SuppressWarnings("unchecked")
         T found = (T)best;
         this.targetEntity = found;
         return this.targetEntity != null;
      }
   }

   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().inflate(targetDistance, 4.0, targetDistance);
   }

   @Override
   public void startExecuting() {
      this.taskOwner.setTarget(this.targetEntity);
      super.startExecuting();
   }

   public static class Sorter implements Comparator<Entity> {
      private final Entity entity;

      public Sorter(Entity entity) {
         this.entity = entity;
      }

      @Override
      public int compare(Entity a, Entity b) {
         double d0 = this.entity.distanceToSqr(a);
         double d1 = this.entity.distanceToSqr(b);
         if (d0 < d1) {
            return -1;
         } else {
            return d0 > d1 ? 1 : 0;
         }
      }
   }
}
