package com.github.alexthe666.iceandfire.entity.ai.legacy;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/** 1.12.2 net.minecraft.entity.ai.EntityAISit. The mob's 1.12 setSitting goes through {@link Sitter}. */
public class EntityAISit extends EntityAIBase {
   public interface Sitter {
      void setSitting(boolean sitting);
   }

   private final TamableAnimal tameable;
   private final Sitter sitter;
   private boolean isSitting;

   public <T extends TamableAnimal & Sitter> EntityAISit(T tameable) {
      this.tameable = tameable;
      this.sitter = tameable;
      this.setMutexBits(5);
   }

   @Override
   public boolean shouldExecute() {
      if (!this.tameable.isTame()) {
         return false;
      } else if (this.tameable.isInWater()) {
         return false;
      } else if (!this.tameable.onGround()) {
         return false;
      } else {
         LivingEntity owner = this.tameable.getOwner();
         if (owner == null) {
            return true;
         } else {
            return this.tameable.distanceToSqr(owner) < 144.0 && owner.getLastHurtByMob() != null ? false : this.isSitting;
         }
      }
   }

   @Override
   public void startExecuting() {
      this.tameable.getNavigation().stop();
      this.sitter.setSitting(true);
   }

   @Override
   public void resetTask() {
      this.sitter.setSitting(false);
   }

   public void setSitting(boolean sitting) {
      this.isSitting = sitting;
   }
}
