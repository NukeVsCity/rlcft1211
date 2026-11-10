package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.IafLand;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.LegacyAi;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** 1.12 CyclopsAITargetSheepPlayers: players in sheep armour or sneaking are noticed from closer. */
public class CyclopsAITargetSheepPlayers<T extends LivingEntity> extends EntityAITarget {
   protected final Class<T> targetClass;
   private final int targetChance;
   protected final EntityAINearestAttackableTarget.Sorter sorter;
   protected final Predicate<T> targetEntitySelector;
   protected T targetEntity;

   public CyclopsAITargetSheepPlayers(
      PathfinderMob creature, Class<T> classTarget, int chance, boolean checkSight, boolean onlyNearby, @Nullable final Predicate<? super T> targetSelector
   ) {
      super(creature, checkSight, onlyNearby);
      this.setMutexBits(1);
      this.targetClass = classTarget;
      this.targetChance = chance;
      this.sorter = new EntityAINearestAttackableTarget.Sorter(creature);
      this.targetEntitySelector = e -> {
         if (e == null) {
            return false;
         } else if (targetSelector != null && !targetSelector.test(e)) {
            return false;
         } else {
            return e.isSpectator() ? false : this.isSuitableTarget(e, false);
         }
      };
   }

   @SuppressWarnings("unchecked")
   @Override
   public boolean shouldExecute() {
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
            player -> {
               double subHelm = IafLand.isItem(player.getItemBySlot(EquipmentSlot.HEAD).getItem(), "sheep_helmet") ? 0.2 : 0.0;
               double subChest = IafLand.isItem(player.getItemBySlot(EquipmentSlot.CHEST).getItem(), "sheep_chestplate") ? 0.2 : 0.0;
               double subLegs = IafLand.isItem(player.getItemBySlot(EquipmentSlot.LEGS).getItem(), "sheep_leggings") ? 0.2 : 0.0;
               double subBoots = IafLand.isItem(player.getItemBySlot(EquipmentSlot.FEET).getItem(), "sheep_boots") ? 0.2 : 0.0;
               double subSneaking = player.isShiftKeyDown() ? 0.2 : 0.0;
               return 1.0 - subHelm - subChest - subLegs - subBoots - subSneaking;
            },
            p -> this.targetClass.isInstance(p) && this.targetEntitySelector.test((T)p)
         );
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
}
