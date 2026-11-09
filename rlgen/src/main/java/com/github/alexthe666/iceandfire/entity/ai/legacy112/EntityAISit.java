package com.github.alexthe666.iceandfire.entity.ai.legacy112;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;

/** 1.12.2 EntityAISit. The tameable's 1.12 setSitting is {@link Sittable#setSitting}. */
public class EntityAISit extends EntityAIBase {
   public interface Sittable {
      void setSitting(boolean sitting);
   }

   private final TamableAnimal tameable;
   private boolean isSitting;

   public EntityAISit(TamableAnimal tameable) {
      this.tameable = tameable;
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
      this.sit(true);
   }

   @Override
   public void resetTask() {
      this.sit(false);
   }

   private void sit(boolean sitting) {
      if (this.tameable instanceof Sittable s) {
         s.setSitting(sitting);
      } else {
         this.tameable.setInSittingPose(sitting);
      }
   }

   public void setSitting(boolean sitting) {
      this.isSitting = sitting;
   }
}
