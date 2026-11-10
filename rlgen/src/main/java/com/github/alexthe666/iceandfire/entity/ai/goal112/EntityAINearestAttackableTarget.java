package com.github.alexthe666.iceandfire.entity.ai.goal112;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** 1.12.2 EntityAINearestAttackableTarget (targetChance roll, sorted list, the 1.12 getNearestAttackablePlayer). */
public class EntityAINearestAttackableTarget<T extends LivingEntity> extends EntityAITarget {
   protected final Class<T> targetClass;
   private final int targetChance;
   protected final Sorter sorter;
   protected final Predicate<T> targetEntitySelector;
   protected T targetEntity;

   public EntityAINearestAttackableTarget(PathfinderMob owner, Class<T> cls, boolean checkSight) {
      this(owner, cls, checkSight, false);
   }

   public EntityAINearestAttackableTarget(PathfinderMob owner, Class<T> cls, boolean checkSight, boolean onlyNearby) {
      this(owner, cls, 10, checkSight, onlyNearby, null);
   }

   public EntityAINearestAttackableTarget(
      PathfinderMob owner, Class<T> cls, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate<? super T> selector
   ) {
      super(owner, checkSight, onlyNearby);
      this.targetClass = cls;
      this.targetChance = chance;
      this.sorter = new Sorter(owner);
      this.targetEntitySelector = e -> {
         if (e == null) {
            return false;
         } else if (selector != null && !selector.test(e)) {
            return false;
         } else {
            return e.isSpectator() ? false : this.isSuitableTarget(e, false);
         }
      };
   }

   @SuppressWarnings("unchecked")
   @Override
   public boolean canUse() {
      if (this.targetChance > 0 && this.taskOwner.getRandom().nextInt(this.targetChance) != 0) {
         return false;
      } else if (!Player.class.isAssignableFrom(this.targetClass)) {
         List<T> list = new ArrayList<>(
            this.taskOwner.level().getEntitiesOfClass(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector)
         );
         if (list.isEmpty()) {
            return false;
         } else {
            Collections.sort(list, this.sorter);
            this.targetEntity = list.get(0);
            return true;
         }
      } else {
         this.targetEntity = (T)LegacyAi.getNearestAttackablePlayer(
            this.taskOwner.level(),
            this.taskOwner.getX(),
            this.taskOwner.getY() + (double)this.taskOwner.getEyeHeight(),
            this.taskOwner.getZ(),
            this.getTargetDistance(),
            this.getTargetDistance(),
            p -> this.targetClass.isInstance(p) && this.targetEntitySelector.test((T)p)
         );
         return this.targetEntity != null;
      }
   }

   protected AABB getTargetableArea(double dist) {
      return this.taskOwner.getBoundingBox().inflate(dist, 4.0, dist);
   }

   @Override
   public void start() {
      this.taskOwner.setTarget(this.targetEntity);
      super.start();
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
