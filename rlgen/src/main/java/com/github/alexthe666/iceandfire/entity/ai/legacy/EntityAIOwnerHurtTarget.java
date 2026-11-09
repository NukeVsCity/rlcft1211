package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/** 1.12.2 net.minecraft.entity.ai.EntityAIOwnerHurtTarget. */
public class EntityAIOwnerHurtTarget extends EntityAITarget {
   TamableAnimal tameable;
   LivingEntity attacker;
   private int timestamp;

   public EntityAIOwnerHurtTarget(TamableAnimal tameable) {
      super(tameable, false);
      this.tameable = tameable;
      this.setMutexBits(1);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.tameable.isTame()) {
         return false;
      } else {
         LivingEntity owner = this.tameable.getOwner();
         if (owner == null) {
            return false;
         } else {
            this.attacker = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != this.timestamp && this.isSuitableTarget(this.attacker, false) && this.tameable.wantsToAttack(this.attacker, owner);
         }
      }
   }

   @Override
   public void startExecuting() {
      this.taskOwner.setTarget(this.attacker);
      LivingEntity owner = this.tameable.getOwner();
      if (owner != null) {
         this.timestamp = owner.getLastHurtMobTimestamp();
      }

      super.startExecuting();
   }
}
