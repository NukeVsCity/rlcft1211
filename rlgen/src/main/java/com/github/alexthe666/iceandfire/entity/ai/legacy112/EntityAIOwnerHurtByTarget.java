package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/** 1.12.2 EntityAIOwnerHurtByTarget */
public class EntityAIOwnerHurtByTarget extends EntityAITarget {
   TamableAnimal tameable;
   LivingEntity attacker;
   private int timestamp;

   public EntityAIOwnerHurtByTarget(TamableAnimal tameable) {
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
            this.attacker = owner.getLastHurtByMob();
            int i = owner.getLastHurtByMobTimestamp();
            return i != this.timestamp && this.isSuitableTarget(this.attacker, false) && this.tameable.wantsToAttack(this.attacker, owner);
         }
      }
   }

   @Override
   public void startExecuting() {
      this.taskOwner.setTarget(this.attacker);
      LivingEntity owner = this.tameable.getOwner();
      if (owner != null) {
         this.timestamp = owner.getLastHurtByMobTimestamp();
      }
      super.startExecuting();
   }
}
