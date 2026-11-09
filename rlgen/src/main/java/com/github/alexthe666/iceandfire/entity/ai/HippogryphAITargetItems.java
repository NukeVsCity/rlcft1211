package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAINearestAttackableTarget;
import com.github.alexthe666.iceandfire.entity.ai.legacy112.EntityAITarget;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;

/** Ice and Fire 1.7.1 HippogryphAITargetItems: eats dropped rabbit feet; one in three tames it to the player who threw it. */
public class HippogryphAITargetItems extends EntityAITarget {
   protected final EntityAINearestAttackableTarget.Sorter theNearestAttackableTargetSorter;
   protected final Predicate<ItemEntity> targetEntitySelector;
   protected ItemEntity targetEntity;

   public HippogryphAITargetItems(PathfinderMob creature, boolean checkSight) {
      super(creature, checkSight, false);
      this.theNearestAttackableTargetSorter = new EntityAINearestAttackableTarget.Sorter(creature);
      this.targetEntitySelector = item -> !item.getItem().isEmpty() && item.getItem().is(Items.RABBIT_FOOT);
   }

   @Override
   public boolean shouldExecute() {
      if (!((EntityHippogryph)this.taskOwner).canMove()) {
         return false;
      } else {
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
   }

   /** AxisAlignedBB.expand (one-sided in 1.12) */
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
         EntityHippogryph hippo = (EntityHippogryph)this.taskOwner;
         this.targetEntity.getItem().shrink(1);
         this.taskOwner.playSound(SoundEvents.GENERIC_EAT, 1.0F, 1.0F);
         hippo.setAnimation(EntityHippogryph.ANIMATION_EAT);
         hippo.heal(4.0F);
         Entity thrower = this.targetEntity.getOwner();
         if (hippo.getRandom().nextInt(3) == 0 && !hippo.isTame() && thrower instanceof Player owner) {
            hippo.setTame(true, false);
            hippo.setOwnerUUID(owner.getUUID());
            hippo.setTarget(null);
            hippo.setCommand(1);
            hippo.setSitting(true);
         }
         this.resetTask();
      }
   }

   @Override
   public boolean shouldContinueExecuting() {
      return !this.taskOwner.getNavigation().isDone();
   }
}
