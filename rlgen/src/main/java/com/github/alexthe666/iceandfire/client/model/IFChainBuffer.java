package com.github.alexthe666.iceandfire.client.model;

import com.github.alexthe666.iceandfire.entity.IFlapable;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

// STUB(iaf/dragon-client): the calculate half of Ice and Fire 1.7.1 IFChainBuffer; the apply* methods come with the llibrary models.
public class IFChainBuffer {
   private int yawTimer;
   private float yawVariation;
   private int pitchTimer;
   private float pitchVariation;
   private float prevYawVariation;
   private float prevPitchVariation;

   public void resetRotations() {
      this.yawVariation = 0.0F;
      this.pitchVariation = 0.0F;
      this.prevYawVariation = 0.0F;
      this.prevPitchVariation = 0.0F;
   }

   private boolean compareDouble(double a, double b) {
      double c = a - b;
      return Math.abs(c - 1.0) <= 0.01;
   }

   public void calculateChainSwingBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevYawVariation = this.yawVariation;
      if (!this.compareDouble(entity.yBodyRot, entity.yBodyRotO) && Math.abs(this.yawVariation) < maxAngle) {
         this.yawVariation = this.yawVariation + Mth.clamp((entity.yBodyRotO - entity.yBodyRot) / divisor, -maxAngle, maxAngle);
      }
      this.decay(bufferTime, angleDecrement);
   }

   public void calculateChainWaveBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevPitchVariation = this.pitchVariation;
      this.pitchVariation = entity.getXRot();
   }

   public void calculateChainFlapBuffer(float maxAngle, int bufferTime, float angleDecrement, float divisor, LivingEntity entity) {
      this.prevYawVariation = this.yawVariation;
      if (!this.compareDouble(entity.yBodyRot, entity.yBodyRotO) && Math.abs(this.yawVariation) < maxAngle) {
         this.yawVariation = this.yawVariation + Mth.clamp((entity.yBodyRotO - entity.yBodyRot) / divisor, -maxAngle, maxAngle);
         if (entity instanceof IFlapable flapable && (double)Math.abs(entity.yBodyRotO - entity.yBodyRot) > 15.0) {
            flapable.flapWings();
         }
      }
      this.decay(bufferTime, angleDecrement);
   }

   private void decay(int bufferTime, float angleDecrement) {
      if (this.yawVariation > 1.0F * angleDecrement) {
         if (this.yawTimer > bufferTime) {
            this.yawVariation -= angleDecrement;
            if (Math.abs(this.yawVariation) < angleDecrement) {
               this.yawVariation = 0.0F;
               this.yawTimer = 0;
            }
         } else {
            this.yawTimer++;
         }
      } else if (this.yawVariation < -1.0F * angleDecrement) {
         if (this.yawTimer > bufferTime) {
            this.yawVariation += angleDecrement;
            if (Math.abs(this.yawVariation) < angleDecrement) {
               this.yawVariation = 0.0F;
               this.yawTimer = 0;
            }
         } else {
            this.yawTimer++;
         }
      }
   }

   public void calculateChainSwingBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainSwingBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public void calculateChainWaveBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainWaveBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public void calculateChainFlapBuffer(float maxAngle, int bufferTime, float angleDecrement, LivingEntity entity) {
      this.calculateChainFlapBuffer(maxAngle, bufferTime, angleDecrement, 1.0F, entity);
   }

   public float getYaw(float partialTicks) {
      return this.prevYawVariation + (this.yawVariation - this.prevYawVariation) * partialTicks;
   }

   public float getPitch(float partialTicks) {
      return this.prevPitchVariation + (this.pitchVariation - this.prevPitchVariation) * partialTicks;
   }
}
