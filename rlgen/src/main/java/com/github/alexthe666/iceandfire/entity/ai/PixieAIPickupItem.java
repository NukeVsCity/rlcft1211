package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.AirWaterRefs;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAITarget;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** Ice and Fire 1.7.1 PixieAIPickupItem: wild pixies are tamed by a thrown cake, tame ones eat sugar to heal. */
public class PixieAIPickupItem extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<ItemEntity> targetEntitySelector;
   protected ItemEntity targetEntity;

   public PixieAIPickupItem(EntityPixie creature, boolean checkSight) {
      super(creature, checkSight, false);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(creature);
      this.targetEntitySelector = item -> !item.getItem().isEmpty()
         && (item.getItem().is(Items.CAKE) && !creature.isTame() || item.getItem().is(Items.SUGAR) && creature.isTame()
            && creature.getHealth() < creature.getMaxHealth());
      this.setMutexBits(3);
   }

   @Override
   public boolean shouldExecute() {
      List<ItemEntity> list = this.taskOwner.level().getEntitiesOfClass(ItemEntity.class, this.getTargetableArea(this.getTargetDistance()),
         this.targetEntitySelector);
      if (list.isEmpty()) {
         return false;
      } else {
         list.sort(this.theNearestAttackableTargetSorter);
         this.targetEntity = list.get(0);
         return true;
      }
   }

   protected AABB getTargetableArea(double targetDistance) {
      return this.taskOwner.getBoundingBox().expandTowards(targetDistance, 4.0, targetDistance);
   }

   @Override
   public void startExecuting() {
      this.taskOwner.getMoveControl().setWantedPosition(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), 0.25);
      if (this.taskOwner.getTarget() == null) {
         this.taskOwner.getLookControl().setLookAt(this.targetEntity.getX(), this.targetEntity.getY(), this.targetEntity.getZ(), 180.0F, 20.0F);
      }
      super.startExecuting();
   }

   @Override
   public void updateTask() {
      super.updateTask();
      if (this.targetEntity == null || !this.targetEntity.isAlive()) {
         this.resetTask();
      }
      if (this.targetEntity != null && this.targetEntity.isAlive() && this.taskOwner.distanceToSqr(this.targetEntity) < 1.0) {
         EntityPixie pixie = (EntityPixie)this.taskOwner;
         if (this.targetEntity.getItem().is(Items.SUGAR)) {
            pixie.heal(5.0F);
         }
         Entity thrower = this.targetEntity.getOwner();
         if (this.targetEntity.getItem().is(Items.CAKE) && !pixie.isTame() && thrower instanceof Player owner) {
            pixie.setTame(true, false);
            pixie.setOwnerUUID(owner.getUUID());
            pixie.setSitting(true);
         }
         this.targetEntity.getItem().shrink(1);
         pixie.playSound(AirWaterRefs.sound("pixie_taunt"), 1.0F, 1.0F);
         this.resetTask();
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return ((EntityPixie)this.taskOwner).getLegacyMoveControl().hasWanted();
   }
}
