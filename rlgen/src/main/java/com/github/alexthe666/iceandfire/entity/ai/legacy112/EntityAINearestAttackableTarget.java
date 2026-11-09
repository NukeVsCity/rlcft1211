package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** 1.12.2 EntityAINearestAttackableTarget */
public class EntityAINearestAttackableTarget<T extends LivingEntity> extends EntityAITarget {
   protected final Class<T> targetClass;
   private final int targetChance;
   protected final Sorter sorter;
   protected final Predicate<T> targetEntitySelector;
   protected T targetEntity;

   public EntityAINearestAttackableTarget(PathfinderMob creature, Class<T> classTarget, boolean checkSight) {
      this(creature, classTarget, checkSight, false);
   }

   public EntityAINearestAttackableTarget(PathfinderMob creature, Class<T> classTarget, boolean checkSight, boolean onlyNearby) {
      this(creature, classTarget, 10, checkSight, onlyNearby, null);
   }

   public EntityAINearestAttackableTarget(PathfinderMob creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby,
         @Nullable Predicate<? super T> targetSelector) {
      super(creature, checkSight, onlyNearby);
      this.targetClass = classTarget;
      this.targetChance = chance;
      this.sorter = new Sorter(creature);
      this.setMutexBits(1);
      this.targetEntitySelector = e -> {
         if (e == null) {
            return false;
         } else if (targetSelector != null && !targetSelector.test(e)) {
            return false;
         } else {
            return !Legacy112.notSpectating(e) ? false : this.isSuitableTarget(e, false);
         }
      };
   }

   @Override
   @SuppressWarnings("unchecked")
   public boolean shouldExecute() {
      if (this.targetChance > 0 && this.taskOwner.getRandom().nextInt(this.targetChance) != 0) {
         return false;
      } else if (this.targetClass != Player.class) {
         List<T> list = this.taskOwner.level().getEntitiesOfClass(this.targetClass, this.getTargetableArea(this.getTargetDistance()), this.targetEntitySelector);
         if (list.isEmpty()) {
            return false;
         } else {
            list.sort(this.sorter);
            this.targetEntity = list.get(0);
            return true;
         }
      } else {
         this.targetEntity = (T)Legacy112.getNearestAttackablePlayer(this.taskOwner.level(), this.taskOwner.getX(), this.taskOwner.getEyeY(),
            this.taskOwner.getZ(), this.getTargetDistance(), this.getTargetDistance(), player -> {
               ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
               boolean skull = head.is(Items.SKELETON_SKULL) && this.taskOwner instanceof Skeleton;
               boolean zombie = head.is(Items.ZOMBIE_HEAD) && this.taskOwner instanceof Zombie;
               boolean creeper = head.is(Items.CREEPER_HEAD) && this.taskOwner instanceof Creeper;
               return skull || zombie || creeper ? 0.5 : 1.0;
            }, p -> this.targetEntitySelector.test((T)p));
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
