package com.github.alexthe666.iceandfire.entity.ai;

import com.github.alexthe666.iceandfire.entity.EntityGorgon;
import net.minecraft.world.entity.LivingEntity;
import com.github.alexthe666.iceandfire.entity.ai.legacy.EntityAIBase;

/** 1.12 GorgonAIStareAttack (shouldExecute is "no attack target", as in 1.7.1). */
public class GorgonAIStareAttack extends EntityAIBase {
   private final EntityGorgon entity;
   private final double moveSpeedAmp;
   private final float maxAttackDistance;
   private int attackCooldown;
   private int attackTime = -1;
   private int seeTime;
   private boolean strafingClockwise;
   private boolean strafingBackwards;
   private int strafingTime = -1;

   public GorgonAIStareAttack(EntityGorgon gorgon, double speedAmplifier, int delay, float maxDistance) {
      this.entity = gorgon;
      this.moveSpeedAmp = speedAmplifier;
      this.attackCooldown = delay;
      this.maxAttackDistance = maxDistance * maxDistance;
      this.setMutexBits(3);
   }

   public void setAttackCooldown(int cooldown) {
      this.attackCooldown = cooldown;
   }

   @Override
   public boolean shouldExecute() {
      return this.entity.getTarget() == null;
   }

   @Override
   public boolean shouldContinueExecuting() {
      return this.shouldExecute() || !this.entity.getNavigation().isDone();
   }

   @Override
   public void resetTask() {
      this.seeTime = 0;
      this.attackTime = -1;
      this.entity.stopUsingItem();
   }

   @Override
   public void updateTask() {
      LivingEntity target = this.entity.getTarget();
      if (target != null) {
         if (EntityGorgon.isStoneMob(target)) {
            this.entity.setTarget(null);
            this.resetTask();
            return;
         }

         this.entity.getLookControl().setLookAt(target.getX(), target.getY() + (double)target.getEyeHeight(), target.getZ(), (float)this.entity.getHeadRotSpeed(), (float)this.entity.getMaxHeadXRot());
         double d0 = this.entity.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());
         boolean flag = this.entity.getSensing().hasLineOfSight(target);
         boolean flag1 = this.seeTime > 0;
         if (flag != flag1) {
            this.seeTime = 0;
         }

         if (flag) {
            this.seeTime++;
         } else {
            this.seeTime--;
         }

         if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20) {
            this.entity.getNavigation().stop();
            this.strafingTime++;
         } else {
            this.entity.getNavigation().moveTo(target, this.moveSpeedAmp);
            this.strafingTime = -1;
         }

         if (this.strafingTime >= 20) {
            if ((double)this.entity.getRandom().nextFloat() < 0.3) {
               this.strafingClockwise = !this.strafingClockwise;
            }

            if ((double)this.entity.getRandom().nextFloat() < 0.3) {
               this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0;
         }

         if (this.strafingTime > -1) {
            if (d0 > (double)(this.maxAttackDistance * 0.75F)) {
               this.strafingBackwards = false;
            } else if (d0 < (double)(this.maxAttackDistance * 0.25F)) {
               this.strafingBackwards = true;
            }

            this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
            this.entity.getLookControl().setLookAt(target.getX(), target.getY() + (double)target.getEyeHeight(), target.getZ(), (float)this.entity.getHeadRotSpeed(), (float)this.entity.getMaxHeadXRot());
         } else {
            this.entity.getLookControl().setLookAt(target, 30.0F, 30.0F);
         }
      }
   }
}
